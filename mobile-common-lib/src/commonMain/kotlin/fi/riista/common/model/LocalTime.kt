package fi.riista.common.model

import kotlinx.serialization.Serializable

/**
 * A data class for wrapping hours, minutes, seconds and nanoseconds into a time entity
 * in order to easily pass time data around where needed.
 *
 * Currently encapsulates the time to the accuracy of seconds. kotlinx-datetime supports
 * better precision but currently we don't intentionally utilize that possibility.
 */
@Serializable
data class LocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int,
) : Comparable<LocalTime> {

    override fun compareTo(other: LocalTime): Int {
        return hour.compareTo(other.hour).takeIf { it != 0 }
                ?: minute.compareTo(other.minute).takeIf { it != 0 }
                ?: second.compareTo(other.second)
    }

    companion object {
        /**
         * Attempts to parse a [LocalTime] from the given [timeString]. It is assumed that the
         * given string would form an ISO-8601 formatted datetime if prefixed with a date.
         *
         * An example of a local times in ISO-8601 format:
         * - `18:43`
         * - `18:43:00`
         * - `18:43:00.500`
         * - `18:43:00.123456789`
         *
         * Uses [kotlinx.datetime.LocalDateTime.parse] under the hood for parsing by prefixing
         * the given [timeString] with a date.
         */
        fun parseLocalTime(timeString: String): LocalTime? {
            // kotlinx-datetime library doesn't expose parsing for just times. Let's not try
            // to reinvent the wheel but just utilize what's already available i.e. prefix
            // the time string with a random date.
            val datetimeString = "2021-01-01T$timeString"

            val localDateTime = try {
                kotlinx.datetime.LocalDateTime.parse(datetimeString)
            } catch (e : IllegalArgumentException) {
                null
            }

            return localDateTime?.let {
                LocalTime(
                        hour = it.hour,
                        minute = it.minute,
                        second = it.second,
                )
            }
        }
    }
}

fun LocalTime.toHoursAndMinutesString(): String {
    // kotlin stdlib does not seem to have String.format yet so format manually
    val hourString = if (hour < 10) {
        "0$hour"
    } else {
        hour.toString()
    }
    val minuteString = if (minute < 10) {
        "0$minute"
    } else {
        minute.toString()
    }

    return "$hourString:$minuteString"
}

/**
 * Returns the whole minutes between `this` and [other] times. Seconds are ignored.
 */
fun LocalTime.minutesUntil(other: LocalTime): Int {
    return (other.hour - hour) * 60 + (other.minute - minute)
}