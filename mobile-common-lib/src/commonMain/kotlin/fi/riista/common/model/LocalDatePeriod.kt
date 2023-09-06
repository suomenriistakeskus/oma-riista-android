package fi.riista.common.model

import kotlinx.serialization.Serializable

private val MIN_DATE = LocalDate(1, 1, 1)
private val MAX_DATE = LocalDate(Int.MAX_VALUE, 12, 31)

@Serializable
sealed class PeriodDate {
    class OpenDate: PeriodDate() {
        override fun equals(other: Any?): Boolean {
            return other is PeriodDate
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
    data class DefinedDate(val date: LocalDate): PeriodDate()
}

@Serializable
data class LocalDatePeriod(
    /**
     * The begin date of the period. Inclusive.
     */
    val beginDate: PeriodDate,

    /**
     * The end date of the period. Inclusive.
     */
    val endDate: PeriodDate,
) {
    constructor(beginDate: LocalDate, endDate: LocalDate) : this(
        beginDate = PeriodDate.DefinedDate(beginDate),
        endDate = PeriodDate.DefinedDate(endDate),
    )

    // empty companion object so that it can be extended
    companion object
}

fun LocalDate.isWithinPeriods(periods: List<LocalDatePeriod>): Boolean {
    return periods.find { isWithinPeriod(it) } != null
}

fun LocalDate.isWithinPeriod(period: LocalDatePeriod): Boolean {
    val (beginDate, endDate) = period.toLocalDates()
    return this in beginDate..endDate
}

fun LocalDate.coerceIn(period: LocalDatePeriod): LocalDate {
    val (beginDate, endDate) = period.toLocalDates()
    return this.coerceIn(beginDate, endDate)
}

fun LocalDatePeriod.isWithinPeriod(period: LocalDatePeriod): Boolean {
    val (beginDate, endDate) = this.toLocalDates()
    return beginDate.isWithinPeriod(period) &&
            beginDate <= endDate &&
            endDate.isWithinPeriod(period)
}

data class PeriodLocalDates(val beginDate: LocalDate, val endDate: LocalDate)

fun LocalDatePeriod.toLocalDates(): PeriodLocalDates {
    val beginDate = when (this.beginDate) {
        is PeriodDate.DefinedDate -> this.beginDate.date
        is PeriodDate.OpenDate -> MIN_DATE
    }
    val endDate = when (this.endDate) {
        is PeriodDate.DefinedDate -> this.endDate.date
        is PeriodDate.OpenDate -> MAX_DATE
    }
    return PeriodLocalDates(beginDate, endDate)
}

fun LocalDate?.toPeriodDate(): PeriodDate {
    return if (this == null) {
        PeriodDate.OpenDate()
    } else {
        PeriodDate.DefinedDate(this)
    }
}
