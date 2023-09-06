package fi.riista.common.model.extensions

import fi.riista.common.model.*
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.floor

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

fun LocalDate.formatShort(stringProvider: StringProvider): String {
    return stringProvider.getFormattedDate(RR.stringFormat.date_format_short, this)
}

fun LocalDate.toJulianDay(): Double {
    // according to https://quasar.as.utexas.edu/BillInfo/JulianDatesG.html
    // 1) Express the date as Y M D, where Y is the year, M is the month number (Jan = 1, Feb = 2, etc.), and D is the day in the month.
    //
    // 2) If the month is January or February, subtract 1 from the year to get a new Y, and add 12 to the month to get a new M. (Thus, we are thinking of January and February as being the 13th and 14th month of the previous year).
    //
    // 3) Dropping the fractional part of all results of all multiplications and divisions, let
    // A = Y/100
    // B = A/4
    // C = 2-A+B
    // E = 365.25x(Y+4716)
    // F = 30.6001x(M+1)
    // JD= C+D+E+F-1524.5
    // This is the Julian Day Number for the beginning of the date in question at 0 hours, Greenwich time.
    // Note that this always gives you a half day extra. That is because the Julian Day begins at noon, Greenwich time.
    val y: Double = year.toDouble() - if (monthNumber <= 2) { 1.0 } else { 0.0 } // 1. and 2.
    val m: Double = monthNumber.toDouble() + if (monthNumber <= 2) { 12.0 } else { 0.0 } // 1. and 2.
    val d: Double = dayOfMonth.toDouble()

    val a = floor(y / 100.0)
    val b = floor(a / 4.0)
    val c = 2 - a + b
    val e = floor(365.25 * (y + 4716.0))
    val f = floor(30.6001 * (m + 1))

    return c + d + e + f - 1524.5
}
