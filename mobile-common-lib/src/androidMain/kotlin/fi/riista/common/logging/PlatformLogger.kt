package fi.riista.common.logging

import android.util.Log

actual class PlatformLogger {
    @Suppress("NOTHING_TO_INLINE") // we may want to have information about call hierarchy
    actual inline fun log(
        tag: String,
        logLevel: LogLevel,
        msg: String
    ) {
        Log.println(logLevel.toLogPriority(), tag, msg)
    }
}

fun LogLevel.toLogPriority(): Int {
    return when(this) {
        LogLevel.VERBOSE -> Log.VERBOSE
        LogLevel.DEBUG -> Log.DEBUG
        LogLevel.INFO -> Log.INFO
        LogLevel.WARN -> Log.WARN
        LogLevel.ERROR -> Log.ERROR
    }
}
