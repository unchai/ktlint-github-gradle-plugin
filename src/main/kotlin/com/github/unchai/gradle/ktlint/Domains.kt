package com.github.unchai.gradle.ktlint

class KtLintError(val path: String, val line: Int, val error: String)

class ChangedFile(var path: String, var linePositionMap: Map<Int, Int>)

class Comment(val path: String, val position: Int, val errors: MutableList<KtLintError>)
