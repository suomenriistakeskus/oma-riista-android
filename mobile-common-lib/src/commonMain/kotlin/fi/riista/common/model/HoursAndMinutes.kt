package fi.riista.common.model

import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import kotlinx.serialization.Serializable

@Serializable
data class HoursAndMinutes(
    val hours: Int,
    val minutes: Int,
): Comparable<HoursAndMinutes> {
    constructor(minutes: Int): this(
            hours = minutes.div(60),
            minutes = minutes.rem(60)
    )

    override fun compareTo(other: HoursAndMinutes): Int {
        return hours.compareTo(other.hours).takeIf { it != 0 }
                ?: minutes.compareTo(other.minutes)
    }

    fun toTotalMinutes(): Int {
        return hours * 60 + minutes
    }
}


fun HoursAndMinutes.formatHoursAndMinutesString(
    stringProvider: StringProvider,
    zeroMinutesStringId: RR.string? = null,
): String {
    if (toTotalMinutes() == 0 && zeroMinutesStringId != null) {
        return stringProvider.getString(zeroMinutesStringId)
    }

    val hoursText = stringProvider.getQuantityString(RR.plurals.hours, hours, hours)
    val minutesText = stringProvider.getQuantityString(RR.plurals.minutes, minutes, minutes)

    return when {
        // e.g. "1 hour, 3 hours, -5 hours"
        hours != 0 && minutes == 0 -> hoursText
        // e.g. "1 minute, 30 minutes, -15 minutes"
        hours == 0 && minutes != 0 -> minutesText
        // e.g. "1 hour 30 minutes"
        else -> stringProvider.getFormattedString(
            stringFormatId = RR.stringFormat.generic_hours_and_minutes_format,
            arg1 = hoursText,
            arg2 = minutesText
        )
    }
}