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

import org.gradle.testkit.runner.GradleRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import kotlin.test.assertTrue

object KtLintGithubPluginSpec : Spek({
    var tmpdir: File? = null

    beforeGroup {
        tmpdir = createTempDir("junit5", null, null)
    }

    afterGroup {
        tmpdir!!.delete()
    }

    describe("Gradle plugin test") {
        on("initialize gradle with plugin") {
            File(tmpdir, "build.gradle").run {
                writeText("""
                plugins {
                    id "com.github.unchai.ktlint-github"
                }
            """.trimIndent())
            }

            val buildResult = GradleRunner
                    .create()
                    .withProjectDir(tmpdir)
                    .withPluginClasspath()
                    .build()

            it("gradle exit without error") {
                assertTrue { buildResult.output.contains("BUILD SUCCESSFUL") }
            }
        }
    }
})
