package fi.riista.common.domain.season.model

import fi.riista.common.model.*
import kotlinx.serialization.Serializable


@Serializable
data class HuntingSeason(
    /**
     * The first hunting year season periods are valid. Inclusive.
     *
     * Should not be taken into account if null.
     */
    val startYear: HuntingYear?,

    /**
     * The last hunting year the season periods are valid. Inclusive.
     *
     * Should not be taken into account if null.
     */
    val endYear: HuntingYear?,

    /**
     * The date periods for yearly seasons. Supports multiple seasons for each hunting year
     * e.g. 20.8 - 27.8 and 1.10 - 30.11 for Bean Goose.
     *
     * NOTE: [DatePeriod]s are not required to be defined within hunting year! It is fully
     * allowed to define periods so that they pass the hunting year change date (e.g.
     * 16.4 - 31.12 for grey seal).
     */
    val yearlySeasonPeriods: List<DatePeriod>
) {
    fun hasSeasonPeriodsForHuntingYear(huntingYear: HuntingYear): Boolean {
        val afterStartYear = startYear == null || huntingYear >= startYear
        val beforeEndYear = endYear == null || huntingYear <= endYear

        return afterStartYear && beforeEndYear
    }
}




