package com.github.unchai.gradle.ktlint

import org.gradle.api.Plugin
import org.gradle.api.Project

class KtLintGithubPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("ktlintGithub", KtLintGithubPluginExtension::class.java)

        with(project.tasks) {
            create("ktlintGithub", KtLintGithubTask::class.java) {
                it.group = "Lint"
                it.description = "Leaves result of a ktlint of a project as a comment on github's pull request."
            }
        }
    }
}
