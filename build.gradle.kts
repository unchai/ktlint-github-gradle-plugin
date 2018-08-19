import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

val kotlinVersion = "1.2.60"
val junitPlatformVersion = "1.2.0"
val spekVersion = "1.2.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compile("org.kohsuke:github-api:1.93")
    compile("com.github.shyiko:ktlint:0.27.0")

    testImplementation(kotlin("reflect", kotlinVersion))
    testImplementation(kotlin("test", kotlinVersion))

    testImplementation("org.jetbrains.spek:spek-api:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }

    testRuntimeOnly("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.junit.platform")
    }

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion") {
        because("Needed to run tests IDEs that bundle an older version")
    }

    testImplementation("io.mockk:mockk:1.8.6")

    testCompile(gradleTestKit())
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek")
    }
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
            description = "A gradle plugin that leaves comment of the result of a \"KtLint\" on github's pull request."
        }
    }
}
