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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@CacheableTask
open class KtLintGithubTask : DefaultTask() {
    @Option(option = "githubOAuth", description = "Github oauth token")
    private lateinit var ghToken: String

    @Option(option = "githubPullRequest", description = "Github pull request id")
    private lateinit var ghPullRequest: String

    @TaskAction
    fun action() {
        val extension = this.project.extensions.getByName("ktlintGithub") as KtLintGithubPluginExtension
        val projectDir = this.project.projectDir.path

        KtLintGithubExecutor(
                GithubHelper(extension.ghEndpoint, ghToken, extension.ghRepository, ghPullRequest.toInt()),
                projectDir
        ).exec()
    }

    fun setGhToken(ghToken: String) {
        this.ghToken = ghToken
    }

    fun setGhPullRequest(ghPullRequest: String) {
        this.ghPullRequest = ghPullRequest
    }
}
