package fi.riista.common.model.extensions

import fi.riista.common.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val conversionTimeZone = TimeZone.UTC

fun LocalDate.secondsFromEpoch(): Long {
    return LocalDateTime(this, LocalTime(0, 0, 0))
        .toKotlinxLocalDateTime()
        .toInstant(conversionTimeZone)
        .epochSeconds
}

fun LocalDate.Companion.fromEpochSeconds(value: Long): LocalDate {
    return Instant.fromEpochSeconds(value)
        .toLocalDateTime(conversionTimeZone)
        .toRiistaCommonLocalDateTime()
        .date
}