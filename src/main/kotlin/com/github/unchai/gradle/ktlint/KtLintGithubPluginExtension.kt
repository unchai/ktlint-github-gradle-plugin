package com.github.unchai.gradle.ktlint

open class KtLintGithubPluginExtension {
    var ghEndpoint: String = "https://api.github.com"
    var ghRepository: String? = null
}
