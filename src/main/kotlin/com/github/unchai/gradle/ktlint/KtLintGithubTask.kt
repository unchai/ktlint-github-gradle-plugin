package com.github.unchai.gradle.ktlint

import com.github.shyiko.ktlint.core.KtLint
import com.github.shyiko.ktlint.core.RuleSetProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import java.util.*

@CacheableTask
open class KtLintGithubTask : DefaultTask() {
    @TaskAction
    fun action() {
        val ruleSetProviders = ServiceLoader.load(RuleSetProvider::class.java)
                .map { it.get().id to it }
                .sortedBy { if (it.first == "standard") "\u0000${it.first}" else it.first }

        KtLint.lint("", ruleSetProviders.map { it.second.get() }) {
            println(it)
        }

        println("Hello world!")
    }
}
