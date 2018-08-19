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

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import kotlin.test.assertEquals

object GithubHelperSpec : Spek({
    lateinit var github: GitHub
    lateinit var ghRepository: GHRepository
    lateinit var ghPullrequest: GHPullRequest
    lateinit var githubHelper: GithubHelper

    beforeEachTest {
        github = mockk()
        ghRepository = mockk()
        ghPullrequest = mockk()

        every { github.myself.login } returns "username"
        every { github.getRepository(any()) } returns ghRepository
        every { ghRepository.getPullRequest(any()) } returns ghPullrequest

        mockkConstructor(GitHubBuilder::class)
        every {
            anyConstructed<GitHubBuilder>()
                    .withEndpoint(any())
                    .withOAuthToken(any()).build()
        } returns github

        githubHelper = GithubHelper(
                "endpoint",
                "token",
                "owner/repo",
                1
        )
    }

    describe("parse diff") {
        on("modify") {
            val diff = """
                @@ -24,14 +24,16 @@ function ManifestReplacePlugin(options) {
                 ManifestReplacePlugin.prototype.apply = function (compiler) {
                   var pluginOptions = this.pluginOptions;
                   // (empty line)
                -  compiler.plugin('done', function () {
                +  compiler.plugin('after-emit', function (compilation, callback) {
                     var manifest = require(path.join(this.options.output.path, pluginOptions.manifestFilename));
                     // (empty line)
                     glob(path.join(pluginOptions.basedir, pluginOptions.src), function (err, files) {
                       files.forEach(function (file) {
                         replaceString(manifest, file);
                       });
                     });
                +
                +    callback();
                   });
                 };
            """.trimIndent()

            val result = githubHelper.parsePatch(diff)

            it("should return 3 difference") {
                assertEquals(result.size, 3)
                assertEquals(result[27], 5)
                assertEquals(result[35], 13)
                assertEquals(result[36], 14)
            }
        }

        on("add") {
            val diff = """
                @@ -0,0 +1,3 @@
                +/node_modules
                +/dist
                +
            """.trimIndent()

            val result = githubHelper.parsePatch(diff)

            it("should return 3 difference") {
                assertEquals(result.size, 3)
                assertEquals(result[1], 1)
                assertEquals(result[2], 2)
                assertEquals(result[3], 3)
            }
        }

        on("delete") {
            val diff = """
                @@ -1,6 +0,0 @@
                -{
                -  "plugins": {
                -    "node": {}
                -  }
                -}
                -
            """.trimIndent()

            val result = githubHelper.parsePatch(diff)

            it("should return 0 difference") {
                assertEquals(result.size, 0)
            }
        }
    }
})
