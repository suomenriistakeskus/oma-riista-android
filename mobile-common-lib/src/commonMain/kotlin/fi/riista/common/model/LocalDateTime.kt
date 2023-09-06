package fi.riista.common.model

import fi.riista.common.model.extensions.toJulianDay
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

/**
 * A local datetime components wrapped into a _data class_. By wrapping into data classes these can
 * easily be transferred while not exposing kotlinx-datetime library to users of RiistaSdk.
 */
@Serializable
data class LocalDateTime constructor(
    val date: LocalDate,
    val time: LocalTime,
) : Comparable<LocalDateTime> {

    // convenience accessors to components
    val year: Int
        get() = date.year
    val monthNumber: Int
        get() = date.monthNumber
    val dayOfMonth: Int
        get() = date.dayOfMonth
    val hour: Int
        get() = time.hour
    val minute: Int
        get() = time.minute
    val second: Int
        get() = time.second

    constructor(year: Int, monthNumber: Int, dayOfMonth: Int,
                hour: Int, minute: Int, second: Int): this(
            date = LocalDate(year, monthNumber, dayOfMonth),
            time = LocalTime(hour, minute, second),
    )

    override fun compareTo(other: LocalDateTime): Int {
        return date.compareTo(other.date).takeIf { it != 0 }
                ?: time.compareTo(other.time)
    }

    /**
     * Prints the [LocalDateTime] as ISO-8601 formatted string.
     */
    fun toStringISO8601(): String {
        if (second == 0) {
            // toKotlinxLocalDateTime doesn't return seconds if seconds are 0
            return "${toKotlinxLocalDateTime()}:00"
        }
        return toKotlinxLocalDateTime().toString()
    }

    /**
     * Use toStringISO8601
     */
    override fun toString(): String {
        return toStringISO8601()
    }


    companion object {
        /**
         * Attempts to parse a [LocalDateTime] from the given ISO-8601 formatted [dateTimeString].
         *
         * An example of a local datetimes in ISO-8601 format:
         * - `2021-05-30T18:43`
         * - `2021-05-30T18:43:00`
         * - `2021-05-30T18:43:00.500`
         * - `2021-05-30T18:43:00.123456789`
         *
         * Uses [kotlinx.datetime.LocalDateTime.parse] under the hood for parsing.
         */
        fun parseLocalDateTime(dateTimeString: String): LocalDateTime? {
            val localDateTime = try {
                kotlinx.datetime.LocalDateTime.parse(dateTimeString)
            } catch (e : IllegalArgumentException) {
                null
            }

            return localDateTime?.let {
                LocalDateTime(
                        year = it.year,
                        monthNumber = it.monthNumber,
                        dayOfMonth = it.dayOfMonth,
                        hour = it.hour,
                        minute = it.minute,
                        second = it.second,
                )
            }
        }
    }
}

internal fun LocalDateTime.toKotlinxLocalDateTime(): kotlinx.datetime.LocalDateTime {
    return kotlinx.datetime.LocalDateTime(
            year, monthNumber, dayOfMonth, hour, minute, second
    )
}

internal fun kotlinx.datetime.LocalDateTime.toRiistaCommonLocalDateTime(): LocalDateTime {
    return LocalDateTime(
            year, monthNumber, dayOfMonth, hour, minute, second
    )
}

fun LocalDateTime.minutesUntil(other: LocalDateTime): Int {
    return date.daysUntil(other.date) * 24 * 60 + time.minutesUntil(other.time)
}

fun LocalDateTime.changeDate(newDate: LocalDate): LocalDateTime {
    return LocalDateTime(
            date = newDate,
            time = this.time
    )
}

fun LocalDateTime.changeTime(newTime: LocalTime): LocalDateTime {
    return LocalDateTime(
            date = this.date,
            time = newTime
    )
}

fun LocalDateTime.changeTime(hour: Int = this.hour,
                             minute: Int = this.minute,
                             second: Int = this.second): LocalDateTime {
    return LocalDateTime(
            date = this.date,
            time = LocalTime(hour, minute, second)
    )
}

fun LocalDateTime.minus(minutes: Int): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    return toKotlinxLocalDateTime().toInstant(timeZone)
        .minus(DateTimePeriod(minutes = minutes), timeZone)
        .toLocalDateTime(timeZone)
        .toRiistaCommonLocalDateTime()
}

fun LocalDateTime.plus(
    days: Int = 0,
    minutes: Int = 0,
): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    return toKotlinxLocalDateTime().toInstant(timeZone)
        .plus(
            period = DateTimePeriod(
                days = days,
                minutes = minutes,
            ),
            timeZone = timeZone
        )
        .toLocalDateTime(timeZone)
        .toRiistaCommonLocalDateTime()
}

fun LocalDateTime.toJulianDay(): Double {
    val timeZone = TimeZone.currentSystemDefault()
    val dayStartInstant = date.dayStart().toKotlinxLocalDateTime().toInstant(timeZone)
    val dayEndInstant = dayStartInstant.plus(1, DateTimeUnit.DAY, timeZone)

    val secondsFromDayStart = dayStartInstant.until(
        other = toKotlinxLocalDateTime().toInstant(timeZone),
        unit = DateTimeUnit.SECOND
    )

    val totalDaySeconds = dayStartInstant.until(
        other = dayEndInstant,
        unit = DateTimeUnit.SECOND
    )

    val timeFraction: Double = secondsFromDayStart.toDouble() / totalDaySeconds.toDouble()

    return date.toJulianDay() + timeFraction
}

