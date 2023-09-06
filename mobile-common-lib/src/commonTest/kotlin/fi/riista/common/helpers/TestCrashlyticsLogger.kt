package fi.riista.common.helpers

import fi.riista.common.logging.CrashlyticsLogger
import fi.riista.common.logging.getLogger

object TestCrashlyticsLogger : CrashlyticsLogger {

    private val logger by getLogger(TestCrashlyticsLogger::class)

    override fun log(exception: Throwable, message: String?) {
        logger.i { "CrashlyticsLogger: $message" }
    }
}
