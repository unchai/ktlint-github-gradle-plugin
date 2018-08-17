package com.github.unchai.gradle.ktlint

class KtLintError(val path: String, val position: Int, val details: MutableList<String>)

class ChangedFile(val path: String, val linePositionMap: Map<Int, Int>)

class Comment(val path: String, val position: Int, val body: String)
