package fi.riista.common.model

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
