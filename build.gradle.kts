plugins {
    kotlin("jvm") version ("1.2.60")
    id("java-gradle-plugin")
    id("maven-publish")
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
}

gradlePlugin {
    plugins {
        create("ktlint-github") {
            id = "com.github.unchai.gradle.ktlint"
            implementationClass = "com.github.unchai.gradle.ktlint.KtLintGithubPlugin"
        }
    }
}
