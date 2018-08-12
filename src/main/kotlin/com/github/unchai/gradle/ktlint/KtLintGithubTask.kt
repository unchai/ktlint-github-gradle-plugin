package com.github.unchai.gradle.ktlint

import com.github.shyiko.ktlint.core.KtLint
import com.github.shyiko.ktlint.core.RuleSetProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.util.*

@CacheableTask
open class KtLintGithubTask : DefaultTask() {
    @Option(option = "githubOAuth", description = "")
    private var ghToken: String? = null

    @Option(option = "githubPullRequest", description = "")
    private var ghPullRequest: Int? = null

    private val githubHelper: GithubHelper = GithubHelper()

    @TaskAction
    fun action() {
        val extension = this.project.extensions.getByName("ktlintGithub") as KtLintGithubPluginExtension
        println(extension.ghEndpoint)
        println(extension.ghRepository)
        println(ghToken)
        println(ghPullRequest)

        //githubHelper.connect(extension.ghEndpoint, ghToken!!, extension.ghRepository!!, ghPullRequest!!)
        //githubHelper.changeStatus(GHCommitState.PENDING, null)

        val ruleSetProviders = ServiceLoader.load(RuleSetProvider::class.java)
                .map { it.get().id to it }
                .sortedBy { if (it.first == "standard") "\u0000${it.first}" else it.first }

        KtLint.lint("", ruleSetProviders.map { it.second.get() }) {
            println(it)
        }
    }
}
