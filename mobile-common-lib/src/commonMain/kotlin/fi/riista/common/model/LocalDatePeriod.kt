package fi.riista.common.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalDatePeriod(
    /**
     * The begin date of the period. Inclusive.
     */
    val beginDate: LocalDate,

    /**
     * The end date of the period. Inclusive.
     */
    val endDate: LocalDate,
)

fun LocalDate.isWithinPeriods(periods: List<LocalDatePeriod>): Boolean {
    return periods.find { isWithinPeriod(it) } != null
}

fun LocalDate.isWithinPeriod(period: LocalDatePeriod): Boolean {
    return this in period.beginDate..period.endDate
}


fun LocalDate.coerceIn(period: LocalDatePeriod): LocalDate {
    return this.coerceIn(period.beginDate, period.endDate)
}

fun LocalDatePeriod.isWithinPeriod(period: LocalDatePeriod): Boolean {
    return beginDate.isWithinPeriod(period) &&
            beginDate <= endDate &&
            endDate.isWithinPeriod(period)
}

