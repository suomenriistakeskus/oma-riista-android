package fi.riista.common.model

import kotlinx.serialization.Serializable

/**
 * Encapsulates the date information without year.
 */
@Serializable
data class Date(
    /**
     * The month of the date, 1 based i.e. January being 1, February 2, ..., December 12
     */
    val monthNumber: Int,
    val dayOfMonth: Int,
): Comparable<Date> {

    override fun compareTo(other: Date): Int {
        return monthNumber.compareTo(other.monthNumber).takeIf { it != 0 }
                ?: dayOfMonth.compareTo(other.dayOfMonth)
    }

    fun toLocalDate(year: Int) = LocalDate(year, monthNumber, dayOfMonth)
}

fun LocalDate.getDateWithoutYear(): Date = Date(monthNumber, dayOfMonth)

fun Date.toLocalDateWithinHuntingYear(huntingYear: HuntingYear): LocalDate {
    val calendarYear = if (this >= Constants.FIRST_DATE_OF_HUNTING_YEAR) {
        huntingYear // august to december, same calendar year
    } else {
        huntingYear + 1 // january to july, next calendar year
    }

    return toLocalDate(calendarYear)
}