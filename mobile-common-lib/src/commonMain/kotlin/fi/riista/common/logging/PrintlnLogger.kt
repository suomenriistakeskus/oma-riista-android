package fi.riista.common.logging

class PrintlnLogger {
    fun log(
        tag: String,
        logLevel: LogLevel,
        msg: String
    ) {
        println("${logLevel.indicator()}/$tag: $msg")
    }
}
