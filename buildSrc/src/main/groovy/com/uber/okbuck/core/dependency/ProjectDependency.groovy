package com.uber.okbuck.core.dependency

import org.gradle.api.Project

class ProjectDependency extends VersionlessDependency {

    final Project project

    ProjectDependency(Project project) {
        super(getDepIdentifier(project.group,
                project.hasProperty("archiveBaseName") ? project.archiveBaseName : project.name), null)
        this.project = project
    }

    @Override
    String toString() {
        if (classifier) {
            return "${this.group}:${this.name}-${this.classifier} -> ${this.project}"
        }
        return "${this.group}:${this.name} -> ${this.project}"
    }
}
