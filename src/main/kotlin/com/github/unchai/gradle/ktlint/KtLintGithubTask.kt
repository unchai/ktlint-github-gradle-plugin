/*
 * MIT License
 *
 * Copyright (c) 2018 unchai <unchai@protonmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

@CacheableTask
open class KtLintGithubTask : DefaultTask() {
    @Option(option = "githubOAuth", description = "")
    private var ghToken: String? = null

    @Option(option = "githubPullRequest", description = "")
    private var ghPullRequest: String? = null

    @TaskAction
    fun action() {
        val extension = this.project.extensions.getByName("ktlintGithub") as KtLintGithubPluginExtension
        val projectDir = this.project.projectDir.path

        val githubHelper = GithubHelper(extension.ghEndpoint, ghToken!!, extension.ghRepository!!, ghPullRequest!!.toInt())
        githubHelper.changeStatus(GHCommitState.PENDING, null)

        val ruleSetProviders = ServiceLoader.load(RuleSetProvider::class.java)
                .map { it.get().id to it }
                .sortedBy { if (it.first == "standard") "\u0000${it.first}" else it.first }

        val errors = ArrayList<KtLintError>()

        githubHelper.listChangedFile()
                .filter { it.path.endsWith(".kt") }
                .forEach { changedFile ->
                    val file = File(projectDir, changedFile.path)
                    val path = file.path.replace(projectDir, "").substring(1)

                    KtLint.lint(file.readText(), ruleSetProviders.map { it.second.get() }) {
                        if (changedFile.linePositionMap.containsKey(it.line)) {
                            errors.add(KtLintError(path, changedFile.linePositionMap[it.line]!!, mutableListOf(it.detail)))
                        }
                    }
                }

        val comments = errors
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

    fun setGhToken(ghToken: String) {
        this.ghToken = ghToken
    }

    fun setGhPullRequest(ghPullRequest: String) {
        this.ghPullRequest = ghPullRequest
    }
}
