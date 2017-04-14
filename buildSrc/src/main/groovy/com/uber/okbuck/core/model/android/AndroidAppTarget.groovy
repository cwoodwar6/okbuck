package com.uber.okbuck.core.model.android

import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.SigningConfig
import com.android.manifmerger.ManifestMerger2
import com.uber.okbuck.OkBuckGradlePlugin
import com.uber.okbuck.core.model.base.Target
import com.uber.okbuck.core.util.FileUtil
import com.uber.okbuck.extension.TestExtension
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * An Android app target
 */
class AndroidAppTarget extends AndroidLibTarget {

    private static final int DEFAULT_LINEARALLOC_LIMIT = 16777216 // 16 MB

    final boolean multidexEnabled
    final Keystore keystore
    final Set<String> cpuFilters

    final int linearAllocHardLimit
    final Set<String> primaryDexPatterns
    final Set<String> exoPackageDependencies

    final boolean minifyEnabled

    final Map<String, Object> placeholders = [:]
    final boolean includesVectorDrawables
    final AndroidInstrumentationTarget instrumentationTarget

    AndroidAppTarget(Project project, String name, boolean isTest = false) {
        super(project, name, isTest)

        minifyEnabled = baseVariant.buildType.minifyEnabled
        keystore = extractKeystore()
        if (baseVariant.ndkCompile.abiFilters != null) {
            cpuFilters = baseVariant.ndkCompile.abiFilters
        } else {
            cpuFilters = [] as Set<String>
        }

        multidexEnabled = baseVariant.mergedFlavor.multiDexEnabled
        primaryDexPatterns = getProp(okbuck.primaryDexPatterns, []) as Set<String>
        linearAllocHardLimit = getProp(okbuck.linearAllocHardLimit, DEFAULT_LINEARALLOC_LIMIT) as Integer

        exoPackageDependencies = getProp(okbuck.appLibDependencies, []) as Set<String>

        if (isTest) {
            placeholders.put('applicationId', applicationId - ".test" + applicationIdSuffix + ".test")
        } else {
            placeholders.put('applicationId', applicationId + applicationIdSuffix)
        }
        placeholders.putAll(baseVariant.buildType.manifestPlaceholders)
        placeholders.putAll(baseVariant.mergedFlavor.manifestPlaceholders)
        includesVectorDrawables = project.android.defaultConfig.vectorDrawables.useSupportLibrary

        TestExtension testExtension = rootProject.okbuck.test
        if (testExtension.espresso && instrumentationTestVariant) {
            instrumentationTarget = new AndroidInstrumentationTarget(project,
                    AndroidInstrumentationTarget.getInstrumentationTargetName(name))
        } else {
            instrumentationTarget = null
        }
    }

    @Override
    protected BaseVariant getBaseVariant() {
        return (BaseVariant) project.android.applicationVariants.find { it.name == name }
    }

    @Override
    ManifestMerger2.MergeType getMergeType() {
        return ManifestMerger2.MergeType.APPLICATION
    }

    @Override
    String processManifestXml(GPathResult manifestXml) {
        if (isTest) {
            manifestXml.@package = applicationId + applicationIdSuffix + ".test"
        } else {
            manifestXml.@package = applicationId + applicationIdSuffix
        }

        manifestXml.@'android:versionCode' = String.valueOf(versionCode)
        manifestXml.@'android:versionName' = versionName
        manifestXml.application.@'android:debuggable' = String.valueOf(debuggable)
        return super.processManifestXml(manifestXml)
    }

    ExoPackageScope getExopackage() {
        if (getProp(okbuck.exopackage, false)) {
            return new ExoPackageScope(project, main, exoPackageDependencies, manifest)
        } else {
            return null
        }
    }

    String getProguardConfig() {
        if (minifyEnabled) {
            File mergedProguardConfig =  getGenPath("proguard.pro")
            mergedProguardConfig.parentFile.mkdirs()
            mergedProguardConfig.createNewFile()

            Set<File> configs = [] as Set<File>

            // project proguard files
            configs.addAll(baseVariant.mergedFlavor.proguardFiles)
            configs.addAll(baseVariant.buildType.proguardFiles)

            // Consumer proguard files of target dependencies
            configs.addAll((main.targetDeps.findAll { Target target ->
                target instanceof AndroidLibTarget
            } as List<AndroidLibTarget>).collect { AndroidLibTarget target ->
                target.baseVariant.mergedFlavor.consumerProguardFiles +
                        target.baseVariant.buildType.consumerProguardFiles
            }.flatten() as Set<File>)

            String mergedConfig = ""
            configs.findAll { File config ->
                config.exists()
            }.each { File config ->
                mergedConfig += "\n##---- ${config} ----##\n"
                mergedConfig += config.text
            }

            // Consumer proguard files of compiled aar dependencies
            main.externalDeps.findAll { String dep ->
                dep.endsWith(".aar")
            }.each { String dep ->
                String config = getPackedProguardConfig(rootProject.file(dep))
                if (!config.empty) {
                    mergedConfig += "\n##---- ${FilenameUtils.getBaseName(dep)} ----##\n"
                    mergedConfig += config
                }
            }

            mergedProguardConfig.text = mergedConfig
            return FileUtil.getRelativePath(project.rootDir, mergedProguardConfig)
        } else {
            return null
        }
    }

    List<Map<String, String>> getTransforms() {
        return (List<Map<String, String>>) getProp(okbuck.transform.transforms, [])
    }

    static String getPackedProguardConfig(File file) {
        ZipFile zipFile = new ZipFile(file)
        ZipEntry proguardEntry = zipFile.entries().find {
            !it.directory && it.name == "proguard.txt"
        } as ZipEntry
        if (proguardEntry != null) {
            return zipFile.getInputStream(proguardEntry).text
        } else {
            return ''
        }
    }

    private Keystore extractKeystore() {
        SigningConfig config = baseVariant.mergedFlavor.signingConfig
        if (config == null) {
            config = baseVariant.buildType.signingConfig
        }

        if (config != null) {
            return new Keystore(config.storeFile,
                    config.keyAlias,
                    config.storePassword,
                    config.keyPassword,
                    getGenPath())
        } else {
            return null
        }
    }

    static class Keystore {

        final File storeFile
        final String alias
        final String storePassword
        final String keyPassword
        final File path

        Keystore(File storeFile, String alias, String storePassword, String keyPassword, File path) {
            this.storeFile = storeFile
            this.alias = alias
            this.storePassword = storePassword
            this.keyPassword = keyPassword
            this.path = path
        }
    }
}
