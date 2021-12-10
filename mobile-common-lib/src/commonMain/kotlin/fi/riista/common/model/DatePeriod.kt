package fi.riista.common.model

import kotlinx.serialization.Serializable

/**
 * Defines a period between two dates that don't have year information.
 *
 * Note that year is not fixed and thus it is possible that [beginDate] > [endDate]. This
 * is the case e.g. when [beginDate] defines a date in December and [endDate] defines a
 * date in January.
 */
@Serializable
data class DatePeriod(
    /**
     * The begin date of the period. Inclusive.
     */
    val beginDate: Date,

    /**
     * The end date of the period. Inclusive.
     */
    val endDate: Date
) {

    fun toLocalDatePeriod(beginDateYear: Int, endDateYear: Int): LocalDatePeriod {
        return LocalDatePeriod(
                beginDate = beginDate.toLocalDate(beginDateYear),
                endDate = endDate.toLocalDate(endDateYear)
        )
    }
}

/**
 * Transforms this [DatePeriod] to [LocalDatePeriod]s that are within specified [huntingYear].
 *
 * It is possible that multiple [LocalDatePeriod]s are created. This is the case when the [DatePeriod]
 * starts before hunting year change and extends past the change date e.g. 7.7 - 6.6. The resulting
 * [LocalDatePeriod]s for [huntingYear] 2021 are 1.8.2021 - 6.6.2022 and 7.7.2022 - 31.7.2022.
 */
fun DatePeriod.toLocalDatePeriodsWithinHuntingYear(huntingYear: HuntingYear): List<LocalDatePeriod> {
    // check if the period starts before hunting year change and extends past the change date.
    // If it does we have to split this period.
    val beginsBeforeHuntingYearChange = beginDate < Constants.FIRST_DATE_OF_HUNTING_YEAR
    val endsAfterHuntingYearChange = endDate >= Constants.FIRST_DATE_OF_HUNTING_YEAR || endDate < beginDate

    return if (beginsBeforeHuntingYearChange && endsAfterHuntingYearChange) {
        listOf(
                LocalDatePeriod(
                        beginDate = Constants.FIRST_DATE_OF_HUNTING_YEAR.toLocalDateWithinHuntingYear(huntingYear),
                        endDate = endDate.toLocalDateWithinHuntingYear(huntingYear)
                ),
                LocalDatePeriod(
                        beginDate = beginDate.toLocalDateWithinHuntingYear(huntingYear),
                        endDate = Constants.LAST_DATE_OF_HUNTING_YEAR.toLocalDateWithinHuntingYear(huntingYear)
                ),
        )
    } else {
        listOf(LocalDatePeriod(
                beginDate = beginDate.toLocalDateWithinHuntingYear(huntingYear),
                endDate = endDate.toLocalDateWithinHuntingYear(huntingYear)
        ))
    }
}