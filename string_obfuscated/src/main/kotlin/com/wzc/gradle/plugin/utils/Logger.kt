package com.wzc.gradle.plugin.utils

object Logger {

    var openLog = true

    fun d(message: String) {
        if (openLog)
            println("######### $message")
    }
}