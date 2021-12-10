package fi.riista.common.logging

actual class PlatformLogger {
    actual inline fun log(
        tag: String,
        logLevel: LogLevel,
        msg: String
    ) {
        println("${logLevel.indicator()}/$tag: $msg")
    }
}