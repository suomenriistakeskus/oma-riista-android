package fi.riista.common.logging

import co.touchlab.stately.concurrency.AtomicBoolean
import fi.riista.common.RiistaSDK
import kotlin.reflect.KClass

typealias LogEntryFunc = () -> String

class Logger(val tag: String) {
    inline fun v(block: LogEntryFunc) = log(LogLevel.VERBOSE, block)
    inline fun d(block: LogEntryFunc) = log(LogLevel.DEBUG, block)
    inline fun i(block: LogEntryFunc) = log(LogLevel.INFO, block)
    inline fun w(block: LogEntryFunc) = log(LogLevel.WARN, block)
    inline fun e(block: LogEntryFunc) = log(LogLevel.ERROR, block)

    // use block for generating the log message. It won't be evaluated unless
    // content will actually be logged
    inline fun log(logLevel: LogLevel, block: LogEntryFunc) {
        if (logLevel.ordinal >= RiistaSDK.logLevel.ordinal) {
            if (usePlatformLogger.value) {
                platformLogger.log(tag, logLevel, block())
            } else {
                testLogger.log(tag, logLevel, block())
            }
        }
    }

    companion object {
        val usePlatformLogger = AtomicBoolean(false)
        val platformLogger = PlatformLogger()
        val testLogger by lazy { PrintlnLogger() }
    }
}

fun <T : Any> getLogger(klass: KClass<T>): Lazy<Logger> {
    return getLogger(tag = klass.simpleName ?: "<anon>")
}

fun getLogger(tag: String): Lazy<Logger> {
    return lazy { Logger(tag) }
}

expect class PlatformLogger() {
    inline fun log(tag: String, logLevel: LogLevel, msg: String)
}