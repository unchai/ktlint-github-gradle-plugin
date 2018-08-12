package com.github.unchai.gradle.ktlint

import org.apache.commons.lang3.builder.ToStringBuilder

class ChangedFile(var path: String, var linePositionMap: Map<Int, Int>) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
