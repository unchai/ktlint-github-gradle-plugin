package com.github.unchai.gradle.ktlint

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class KtLintGithubPluginTest {
    private val temporaryFolder = TemporaryFolder()

    @Before
    fun setUp() {
        temporaryFolder.create()
    }

    @Test
    fun test() {
        File(temporaryFolder.root, "build.gradle").run {
            writeText("""
                plugins {
                    id "ktlint-github"
                }

                ktlintGithub {
                    ghEndpoint "https://enterprise.github.url"
                    ghRepository "testOwner/testRepository"
                }
            """.trimIndent())
        }

        val buildResult = GradleRunner
                .create()
                .withProjectDir(temporaryFolder.root)
                .withPluginClasspath()
                .withArguments("ktlintGithub", "--githubOAuth=abc123", "--githubPullRequest=5")
                .build()

        println("====================")
        println(buildResult.output)
    }
}
