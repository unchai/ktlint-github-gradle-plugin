plugins {
    kotlin("jvm") version "1.2.60"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.0"
}

group = "com.github.unchai.gradle.ktlint"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compile("org.kohsuke:github-api:1.93")
    compile("com.github.shyiko:ktlint:0.27.0")

    testCompile("junit:junit:4.12")
    testCompile(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("ktlintGithubPlugin") {
            id = "com.github.unchai.ktlint-github"
            implementationClass = "com.github.unchai.gradle.ktlint.KtLintGithubPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/unchai/ktlint-github-gradle-plugin"
    vcsUrl = "https://github.com/unchai/ktlint-github-gradle-plugin"
    tags = listOf("kotlin", "ktlint", "github", "pull-requests")

    (plugins) {
        "ktlintGithubPlugin" {
            displayName = "KtLint github pull request plugin"
            description = "A gradle plugin that leaves the result of a ktlint of a project as a comment on github's pull request."
        }
    }
}
