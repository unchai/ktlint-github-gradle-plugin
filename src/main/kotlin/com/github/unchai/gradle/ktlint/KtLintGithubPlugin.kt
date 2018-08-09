package com.github.unchai.gradle.ktlint

import org.gradle.api.Plugin
import org.gradle.api.Project

class KtLintGithubPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project.tasks) {
            create("ktlint-github", KtLintGithubTask::class.java) {
                it.group = "Development"
                it.description = "ggggg"
            }
        }
    }
}
