package com.uber.okbuck.core.util;

import com.uber.okbuck.OkBuckGradlePlugin;
import com.uber.okbuck.core.dependency.DependencyCache;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class RobolectricUtil {

    private static final String JSON = "org.json:json:20080701";
    private static final String TAGSOUP = "org.ccil.cowan.tagsoup:tagsoup:1.2";
    private static final String ROBOLECTRIC_RUNTIME = "robolectricRuntime";
    public static final String ROBOLECTRIC_CACHE = OkBuckGradlePlugin.DEFAULT_CACHE_PATH + "/robolectric";

    private RobolectricUtil() {}

    public static void download(Project project) {
        List<Configuration> runtimeDeps = new ArrayList<>();

        Configuration runtimeCommon = project.getConfigurations().maybeCreate(ROBOLECTRIC_RUNTIME + "_common");
        project.getDependencies().add(runtimeCommon.getName(), JSON);
        project.getDependencies().add(runtimeCommon.getName(), TAGSOUP);
        runtimeDeps.add(runtimeCommon);

        for (API api : EnumSet.allOf(API.class)) {
            Configuration runtimeApi = project.getConfigurations().maybeCreate(
                    ROBOLECTRIC_RUNTIME + "_" + api.name());
            for (String jar : api.dependencies) {
                project.getDependencies().add(runtimeApi.getName(), jar);
            }
            runtimeDeps.add(runtimeApi);
        }

        for (Configuration configuration : runtimeDeps) {
            new DependencyCache("robolectric" + configuration.getName().toUpperCase(),
                    project,
                    ROBOLECTRIC_CACHE,
                    Collections.singleton(configuration),
                    null,
                    false);
        }
    }

    @SuppressWarnings("unused")
    enum API {
        API_16("org.robolectric:android-all:4.1.2_r1-robolectric-0", "org.robolectric:shadows-core:3.0:16"),
        API_17("org.robolectric:android-all:4.2.2_r1.2-robolectric-0", "org.robolectric:shadows-core:3.0:17"),
        API_18("org.robolectric:android-all:4.3_r2-robolectric-0", "org.robolectric:shadows-core:3.0:18"),
        API_19("org.robolectric:android-all:4.4_r1-robolectric-1", "org.robolectric:shadows-core:3.0:19"),
        API_21("org.robolectric:android-all:5.0.0_r2-robolectric-1", "org.robolectric:shadows-core:3.0:21"),
        API_23("org.robolectric:android-all:6.0.1_r3-robolectric-0", "org.robolectric:shadows-core:3.3.2"),
        API_24("org.robolectric:android-all:7.0.0_r1-robolectric-0", "org.robolectric:shadows-core:3.3.2"),
        API_25("org.robolectric:android-all:7.1.0_r7-robolectric-0", "org.robolectric:shadows-core:3.3.2");

        private final ArrayList<String> dependencies = new ArrayList<>();
        private final String[] COMMON_DEPENDENCIES = {
                "backport-util-concurrent:backport-util-concurrent:3.1", 
                "classworlds:classworlds:1.1-alpha-2",
                "nekohtml:nekohtml:1.9.6.2",
                "org.codehaus.plexus:plexus-container-default:1.0-alpha-9-stable-1",
                "org.codehaus.plexus:plexus-interpolation:1.11",
                "org.codehaus.plexus:plexus-utils:1.5.15",
                "org.apache.maven.wagon:wagon-file:1.0-beta-6",
                "org.apache.maven.wagon:wagon-http-lightweight:1.0-beta-6",
                "org.apache.maven.wagon:wagon-http-shared:1.0-beta-6",
                "org.apache.maven.wagon:wagon-provider-api:1.0-beta-6",
                "nekohtml:xercesMinimal:1.9.6.2",
                "xmlpull:xmlpull:1.1.3.1",
                "xpp3:xpp3_min:1.1.4c",
                "com.thoughtworks.xstream:xstream:1.4.8"
            };

        API(String... deps) {
            for (String dependency : COMMON_DEPENDENCIES) {
                dependencies.add(dependency);
            }
            for (String dep : deps) {
                dependencies.add(dep);
            }
        }
    }
}
