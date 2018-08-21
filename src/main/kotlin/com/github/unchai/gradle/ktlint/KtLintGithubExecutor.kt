package com.github.unchai.gradle.ktlint

import com.github.shyiko.ktlint.core.KtLint
import com.github.shyiko.ktlint.core.RuleSetProvider
import org.kohsuke.github.GHCommitState
import java.io.File
import java.util.*

class KtLintGithubExecutor(
        private val githubHelper: GithubHelper,
        private val projectDir: String
) {
    fun exec() {
        githubHelper.changeStatus(GHCommitState.PENDING, null)

        val errors = lint(loadRuleSetProviders())
        val comments = buildComment(errors)

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

    private fun loadRuleSetProviders(): List<Pair<String, RuleSetProvider>> {
        return ServiceLoader.load(RuleSetProvider::class.java)
                .map { it.get().id to it }
                .sortedBy { if (it.first == "standard") "\u0000${it.first}" else it.first }
    }

    fun lint(ruleSetProviders: List<Pair<String, RuleSetProvider>>): MutableList<KtLintError> {
        val errors = mutableListOf<KtLintError>()

        githubHelper.listChangedFile()
                .filter { it.path.endsWith(".kt") }
                .forEach { changedFile ->
                    val file = File(projectDir, changedFile.path)
                    val path = file.path.replace(projectDir, "").substring(1)

                    KtLint.lint(file.readText(), ruleSetProviders.map { it.second.get() }) {
                        if (changedFile.linePositionMap.containsKey(it.line)) {
                            errors.add(
                                    KtLintError(path, changedFile.linePositionMap[it.line]!!, mutableListOf(it.detail))
                            )
                        }
                    }
                }

        return errors
    }

    private fun buildComment(errors: MutableList<KtLintError>): List<Comment> {
        return errors
                .groupingBy { it.path + "|" + it.position }
                .aggregate { _, accumulator: KtLintError?, element: KtLintError, _ ->
                    when (accumulator) {
                        null -> element
                        else -> {
                            accumulator.details.addAll(element.details)
                            accumulator
                        }
                    }
                }
                .mapNotNull { it.value }
                .map { Comment(it.path, it.position, it.details.joinToString("\n")) }
                .toList()
    }
}