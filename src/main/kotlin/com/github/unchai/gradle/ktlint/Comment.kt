package com.github.unchai.gradle.ktlint

import org.apache.commons.lang3.builder.ToStringBuilder

class Comment(var path: String, var position: Int) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
