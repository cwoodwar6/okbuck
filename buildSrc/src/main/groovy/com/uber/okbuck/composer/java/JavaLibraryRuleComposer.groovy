package com.uber.okbuck.composer.java

import com.uber.okbuck.composer.jvm.JvmBuckRuleComposer
import com.uber.okbuck.core.model.base.RuleType
import com.uber.okbuck.core.model.java.JavaLibTarget
import com.uber.okbuck.core.util.RetrolambdaUtil
import com.uber.okbuck.rule.java.JavaLibraryRule

final class JavaLibraryRuleComposer extends JvmBuckRuleComposer {

    private JavaLibraryRuleComposer() {
        // no instance
    }

    static JavaLibraryRule compose(JavaLibTarget target,
                                   RuleType ruleType = RuleType.JAVA_LIBRARY) {
        List<String> deps = []
        deps.addAll(external(target.main.externalDeps))
        deps.addAll(targets(target.main.targetDeps))

        Set<String> aptDeps = [] as Set
        aptDeps.addAll(externalApt(target.apt.externalDeps))
        aptDeps.addAll(targetsApt(target.apt.targetDeps))

        Set<String> providedDeps = []
        providedDeps.addAll(external(target.provided.externalDeps))
        providedDeps.addAll(targets(target.provided.targetDeps))
        providedDeps.removeAll(deps)

        if (target.retrolambda) {
            providedDeps.add(RetrolambdaUtil.getRtStubJarRule())
        }

        List<String> testTargets = []
        if (target.test.sources) {
            testTargets.add(":${test(target)}")
        }

        new JavaLibraryRule(
                ruleType,
                src(target),
                ["PUBLIC"],
                deps,
                target.main.sources,
                target.annotationProcessors,
                aptDeps,
                providedDeps,
                target.main.resourcesDir,
                target.sourceCompatibility,
                target.targetCompatibility,
                target.postprocessClassesCommands,
                target.main.jvmArgs,
                testTargets,
                target.getExtraOpts(ruleType))
    }
}
