package fi.riista.common.model


@kotlinx.serialization.Serializable
data class YearMonth(
    /**
     * The year of the date in YYYY format (e.g. 2021)
     */
    val year: Int,

    /**
     * The month of the date, 1 based i.e. January being 1, February 2, ..., December 12
     */
    val monthNumber: Int,
): Comparable<YearMonth> {
    override fun compareTo(other: YearMonth): Int {
        return year.compareTo(other.year).takeIf { it != 0 }
            ?: monthNumber.compareTo(other.monthNumber)
    }
}

internal fun LocalDateTime.yearMonth(): YearMonth =
    date.yearMonth()

internal fun LocalDate.yearMonth(): YearMonth =
    YearMonth(year = year, monthNumber = monthNumber)
