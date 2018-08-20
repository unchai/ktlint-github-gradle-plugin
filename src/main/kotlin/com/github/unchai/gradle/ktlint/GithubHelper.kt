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

import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHubBuilder
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class GithubHelper(endpoint: String, token: String, repository: String, pullRequest: Int) {
    companion object {
        private const val CONTEXT = "coding-convention/ktlint"
        private const val PREFIX = "#### :rotating_light: ktlint defects"
    }

    private var repo: GHRepository
    private var pr: GHPullRequest
    private var username: String

    init {
        val github = GitHubBuilder()
                .withEndpoint(endpoint)
                .withOAuthToken(token)
                .build()

        this.username = github.myself.login
        this.repo = github.getRepository(repository)
        this.pr = this.repo.getPullRequest(pullRequest)
    }

    fun parsePatch(patch: String): Map<Int, Int> {
        val diffHeaderPattern = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@.*")
        var lineNo = 0

        val map = HashMap<Int, Int>()
        for ((position, line) in patch.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().withIndex()) {
            if (line.startsWith("@@")) {
                val matcher = diffHeaderPattern.matcher(line)

                if (matcher.matches()) {
                    lineNo = Integer.parseInt(matcher.group(3))
                }
            } else if (line.startsWith(" ")) {
                lineNo++
            } else if (line.startsWith("+")) {
                map[lineNo++] = position
            }
        }

        return map
    }

    fun listChangedFile(): List<ChangedFile> {
        val linePositionMap = mutableListOf<ChangedFile>()

        for (fileDetail in this.pr.listFiles()) {
            if (fileDetail.patch == null) {
                continue
            }

            val diffMap = parsePatch(fileDetail.patch)

            if (!diffMap.isEmpty()) {
                linePositionMap.add(ChangedFile(fileDetail.filename, diffMap))
            }
        }

        return linePositionMap
    }

    @Throws(IOException::class)
    fun changeStatus(state: GHCommitState, description: String?) {
        this.repo.createCommitStatus(
                this.pr.head.sha,
                state, null,
                description,
                CONTEXT
        )
    }

    @Throws(IOException::class)
    fun removeAllComment() {
        for (comment in this.pr.listReviewComments()) {
            if (comment.user.login == this.username && comment.body.startsWith(PREFIX)) {
                comment.delete()
            }
        }
    }

    @Throws(IOException::class)
    fun createComment(comment: Comment) {
        this.pr.createReviewComment(
                buildCommentBody(comment),
                this.pr.head.sha,
                comment.path,
                comment.position
        )
    }

    private fun buildCommentBody(comment: Comment): String {
        val builder = StringBuilder()
        builder.append("%s%n".format(PREFIX))
        builder.append(comment.body)

        return builder.toString()
    }
}
