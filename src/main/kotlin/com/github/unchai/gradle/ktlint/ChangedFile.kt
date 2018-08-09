package com.github.unchai.gradle.ktlint

import org.apache.commons.lang3.builder.ToStringBuilder

class ChangedFile {
    var path: String? = null
    var linePositionMap: Map<Int, Int>? = null

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
