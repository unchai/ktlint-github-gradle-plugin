package com.github.unchai.gradle.ktlint

import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHubBuilder
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

internal class GithubHelper {
    companion object {
        private const val CONTEXT = "coding-convention/ktlint"
        private const val PREFIX = "#### :rotating_light: ktlint defects"
    }

    private var repo: GHRepository? = null
    private var pr: GHPullRequest? = null
    private var username: String? = null

    fun parsePatch(patch: String): Map<Int, Int> {
        var lineNo = 0
        var pathNo = 0

        val map = HashMap<Int, Int>()
        for (line in patch.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (line.startsWith("@@")) {
                val matcher = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@.*").matcher(line)

                if (matcher.matches()) {
                    lineNo = Integer.parseInt(matcher.group(3))
                }
            } else if (line.startsWith(" ")) {
                lineNo++
            } else if (line.startsWith("+")) {
                map[lineNo++] = pathNo
            }

            pathNo++
        }

        return map
    }

    @Throws(IOException::class)
    fun connect(endpoint: String, token: String, repository: String, pullRequest: Int) {
        val github = GitHubBuilder()
                .withEndpoint(endpoint)
                .withOAuthToken(token)
                .build()

        this.username = github.myself.login
        this.repo = github.getRepository(repository)
        this.pr = this.repo!!.getPullRequest(pullRequest)
    }

    fun listChangedFile(): List<ChangedFile> {
        val linePositionMap = ArrayList<ChangedFile>()

        for (fileDetail in this.pr!!.listFiles()) {
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
        this.repo!!.createCommitStatus(
                this.pr!!.head.sha,
                state, null,
                description,
                CONTEXT
        )
    }

    @Throws(IOException::class)
    fun removeAllComment() {
        for (comment in this.pr!!.listReviewComments()) {
            if (comment.user.login == this.username && comment.body.startsWith(PREFIX)) {
                comment.delete()
            }
        }
    }

    @Throws(IOException::class)
    fun createComment(comment: Comment) {
        this.pr!!.createReviewComment(
                buildCommentBody(comment),
                this.pr!!.head.sha,
                comment.path,
                comment.position
        )
    }

    private fun buildCommentBody(comment: Comment): String {
        val builder = StringBuilder()
        builder.append(String.format("%s%n", PREFIX))

        for (error in comment.errors) {
            builder.append(String.format("%s%n", error.error))
        }

        return builder.toString()
    }
}