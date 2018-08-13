package com.github.unchai.gradle.ktlint

import com.github.shyiko.ktlint.core.KtLint
import com.github.shyiko.ktlint.core.RuleSetProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kohsuke.github.GHCommitState
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@CacheableTask
open class KtLintGithubTask : DefaultTask() {
    @Option(option = "githubOAuth", description = "")
    private var ghToken: String? = null

    @Option(option = "githubPullRequest", description = "")
    private var ghPullRequest: String? = null

    private val githubHelper: GithubHelper = GithubHelper()

    @TaskAction
    fun action() {
        val extension = this.project.extensions.getByName("ktlintGithub") as KtLintGithubPluginExtension
        val projectDir = this.project.projectDir.path

        githubHelper.connect(extension.ghEndpoint, ghToken!!, extension.ghRepository!!, ghPullRequest!!.toInt())
        githubHelper.changeStatus(GHCommitState.PENDING, null)

        val ruleSetProviders = ServiceLoader.load(RuleSetProvider::class.java)
                .map { it.get().id to it }
                .sortedBy { if (it.first == "standard") "\u0000${it.first}" else it.first }

        val changedFileMap = githubHelper.listChangedFile()
                .filter { changedFile -> changedFile.path.endsWith(".kt") }
                .associateBy({ it.path }, { it })

        val errors = ArrayList<KtLintError>()

        changedFileMap.keys
                .map { path -> File("/Users/JaehyeonNam/Devel/workspace/private/ktlint-test", path) }
                .toList()
                .forEach { file ->
                    KtLint.lint(file.readText(), ruleSetProviders.map { it.second.get() }) {
                        val path = file.path.replace(projectDir, "").substring(1)
                        errors.add(KtLintError(path, it.line, it.detail))
                    }
                }

        val comments = buildComments(changedFileMap, errors)

        githubHelper.removeAllComment()

        for (comment in comments) {
            githubHelper.createComment(comment)
        }

        if (comments.isEmpty()) {
            githubHelper.changeStatus(GHCommitState.SUCCESS, "Good job! You kept all the rules.")
        } else {
            githubHelper.changeStatus(GHCommitState.FAILURE, "reported %d errors.".format(errors.size))
        }
    }

    private fun buildComments(changedFileMap: Map<String, ChangedFile>, errors: List<KtLintError>): List<Comment> {
        val commentMap = HashMap<String, Comment>()

        for (error in errors) {
            val key = error.path + "|" + error.line

            if (commentMap.containsKey(key)) {
                commentMap[key]!!.errors.add(error)
            } else {
                commentMap[error.path] =
                        Comment(
                                error.path,
                                changedFileMap[error.path]!!.linePositionMap[error.line]!!,
                                mutableListOf(error)
                        )
            }
        }

        return commentMap.values.toList()
    }

    fun setGhToken(ghToken: String) {
        this.ghToken = ghToken
    }

    fun setGhPullRequest(ghPullRequest: String) {
        this.ghPullRequest = ghPullRequest
    }
}
