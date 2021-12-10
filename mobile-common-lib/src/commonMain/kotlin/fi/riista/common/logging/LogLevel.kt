package fi.riista.common.logging

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

fun LogLevel.indicator(): Char {
    return when(this) {
        LogLevel.VERBOSE -> 'V'
        LogLevel.DEBUG -> 'D'
        LogLevel.INFO -> 'I'
        LogLevel.WARN -> 'W'
        LogLevel.ERROR -> 'E'
    }
}