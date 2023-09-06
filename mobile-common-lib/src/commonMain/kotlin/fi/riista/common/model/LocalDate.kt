package fi.riista.common.model

import fi.riista.common.dto.LocalDateDTO
import kotlinx.datetime.daysUntil
import kotlinx.serialization.Serializable

/**
 * A local date components wrapped into a _data class_. By wrapping into data classes these can easily
 * be transferred while not exposing kotlinx-datetime library to users of RiistaSdk.
 */
@Serializable
data class LocalDate(
    /**
     * The year of the date in YYYY format (e.g. 2021)
     */
    val year: Int,

    /**
     * The month of the date, 1 based i.e. January being 1, February 2, ..., December 12
     */
    val monthNumber: Int,


    val dayOfMonth: Int,
) : Comparable<LocalDate> {

    override fun compareTo(other: LocalDate): Int {
        return year.compareTo(other.year).takeIf { it != 0 }
                ?: monthNumber.compareTo(other.monthNumber).takeIf { it != 0 }
                ?: dayOfMonth.compareTo(other.dayOfMonth)
    }

    /**
     * Prints the [LocalDate] as ISO-8601 formatted date.
     */
    fun toStringISO8601(): String {
        return toKotlinxLocalDate().toString()
    }

    /**
     * Use toStringISO8601
     */
    override fun toString(): String {
        return toStringISO8601()
    }

    companion object {
        /**
         * Attempts to parse a [LocalDate] from the given ISO-8601 formatted [dateString].
         *
         * An example of a local date in ISO-8601 format: `2021-05-30`
         *
         * Uses [kotlinx.datetime.LocalDate.parse] under the hood for parsing.
         */
        fun parseLocalDate(dateString: String): LocalDate? {
            val localDate = try {
                kotlinx.datetime.LocalDate.parse(dateString)
            } catch (e: IllegalArgumentException) {
                null
            }

            return localDate?.let {
                LocalDate(
                        year = it.year,
                        monthNumber = it.monthNumber,
                        dayOfMonth = it.dayOfMonth,
                )
            }
        }
    }
}

fun LocalDate.toLocalDateDTO(): LocalDateDTO {
    return toStringISO8601()
}

internal fun LocalDate.toKotlinxLocalDate(): kotlinx.datetime.LocalDate {
    return kotlinx.datetime.LocalDate(year, monthNumber, dayOfMonth)
}

/**
 * Returns the number of whole days between `this` and [other] dates.
 *
 * The value returned is:
 * - positive or zero if this date is earlier than the other,
 * - negative or zero if this date is later than the other,
 * - zero if this date is equal to the other.
 *
 * If the result does not fit in [Int], returns [Int.MAX_VALUE] for a positive result or [Int.MIN_VALUE] for a negative result.
 **/
fun LocalDate.daysUntil(other: LocalDate): Int {
    // NOTE: write tests for this if implementation ever changes from library version!
    return toKotlinxLocalDate().daysUntil(other.toKotlinxLocalDate())
}

/**
 * Return earlier date. Both dates can't be null.
 */
fun minDate(val1: LocalDate?, val2: LocalDate?): LocalDate {
    if (val1 == null && val2 == null) {
        throw AssertionError("Both values can't be null")
    }
    if (val1 == null) {
        return val2!!
    }
    if (val2 == null) {
        return val1
    }
    return minOf(val1, val2)
}

/**
 * Return later date. Both dates can't be null.
 */
fun maxDate(val1: LocalDate?, val2: LocalDate?): LocalDate {
    if (val1 == null && val2 == null) {
        throw AssertionError("Both values can't be null")
    }
    if (val1 == null) {
        return val2!!
    }
    if (val2 == null) {
        return val1
    }
    return maxOf(val1, val2)
}

fun LocalDate?.toStringISO8601WithTime(time: LocalTime) =
    this?.let { LocalDateTime(this, time).toStringISO8601() }

internal fun LocalDate.dayStart() = LocalDateTime(this, LocalTime(0, 0, 0))
internal fun LocalDate.dayEnd() = LocalDateTime(this, LocalTime(23, 59, 59))
