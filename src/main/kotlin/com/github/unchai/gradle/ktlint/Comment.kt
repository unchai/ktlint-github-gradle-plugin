package com.github.unchai.gradle.ktlint

import org.apache.commons.lang3.builder.ToStringBuilder

class Comment {
    var path: String? = null
    var position: Int = 0

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
