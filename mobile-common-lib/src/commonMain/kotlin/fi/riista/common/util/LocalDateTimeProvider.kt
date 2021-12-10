package fi.riista.common.util

import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toRiistaCommonLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface LocalDateTimeProvider {
    fun now(): LocalDateTime
}

internal class SystemDateTimeProvider: LocalDateTimeProvider {
    override fun now(): LocalDateTime {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toRiistaCommonLocalDateTime()
    }
}