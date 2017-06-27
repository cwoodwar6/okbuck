package com.uber.okbuck.composer.java

import com.uber.okbuck.composer.jvm.JvmBuckRuleComposer
import com.uber.okbuck.core.model.base.RuleType
import com.uber.okbuck.core.model.java.JavaAppTarget
import com.uber.okbuck.core.model.java.JavaTarget
import com.uber.okbuck.rule.java.JavaBinaryRule

final class JavaBinaryRuleComposer extends JvmBuckRuleComposer {

    private JavaBinaryRuleComposer() {
        // no instance
    }

    static JavaBinaryRule compose(JavaAppTarget target) {
        return new JavaBinaryRule(bin(target), ["PUBLIC"], [":${src(target)}"], target.mainClass,
                target.excludes, target.getExtraOpts(RuleType.JAVA_BINARY))
    }

    private static String bin(final JavaTarget target) {
        return "bin_" + target.getName()
    }
}
