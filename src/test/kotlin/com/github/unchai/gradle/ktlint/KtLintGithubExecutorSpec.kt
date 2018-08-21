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

import io.mockk.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object KtLintGithubExecutorSpec : Spek({
    lateinit var githubHelper: GithubHelper
    lateinit var ktLintGithubExecutor: KtLintGithubExecutor

    beforeEachTest {
        githubHelper = mockk()
        ktLintGithubExecutor = spyk(KtLintGithubExecutor(githubHelper, "/tmp/project/root"))
    }

    describe("KtLint result merge test") {
        on("when ktlint result like that") {
            val fakeErrors = mutableListOf(
                    KtLintError("/src/kotlin/a.kt", 1, mutableListOf("error1")),
                    KtLintError("/src/kotlin/a.kt", 1, mutableListOf("error2")),
                    KtLintError("/src/kotlin/a.kt", 1, mutableListOf("error3")),
                    KtLintError("/src/kotlin/b.kt", 2, mutableListOf("error1")),
                    KtLintError("/src/kotlin/b.kt", 2, mutableListOf("error2")),
                    KtLintError("/src/kotlin/c.kt", 5, mutableListOf("error1")),
                    KtLintError("/src/kotlin/c.kt", 7, mutableListOf("error1")),
                    KtLintError("/src/kotlin/d.kt", 7, mutableListOf("error1"))
            )

            every { ktLintGithubExecutor.lint(any()) } returns fakeErrors

            val actualComments = mutableListOf<Comment>()

            every { githubHelper.changeStatus(any(), any()) } just Runs
            every { githubHelper.removeAllComment() } just Runs
            every { githubHelper.createComment(capture(actualComments)) } just Runs

            ktLintGithubExecutor.exec()

            it("should returns 5 comments") {
                assertEquals(actualComments.size, 5)
                assertTrue(actualComments.contains(Comment("/src/kotlin/a.kt", 1, "error1\nerror2\nerror3")))
                assertTrue(actualComments.contains(Comment("/src/kotlin/b.kt", 2, "error1\nerror2")))
                assertTrue(actualComments.contains(Comment("/src/kotlin/c.kt", 5, "error1")))
                assertTrue(actualComments.contains(Comment("/src/kotlin/c.kt", 7, "error1")))
                assertTrue(actualComments.contains(Comment("/src/kotlin/d.kt", 7, "error1")))
            }
        }
    }
})
