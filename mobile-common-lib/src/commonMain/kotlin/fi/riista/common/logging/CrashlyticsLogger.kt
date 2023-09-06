package fi.riista.common.logging

interface CrashlyticsLogger {
    fun log(exception: Throwable, message: String?)
}
