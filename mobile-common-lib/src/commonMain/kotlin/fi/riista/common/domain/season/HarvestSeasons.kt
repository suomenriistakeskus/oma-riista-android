package fi.riista.common.domain.season

import fi.riista.common.domain.season.model.HuntingSeason
import fi.riista.common.model.*

class HarvestSeasons {
    private val fallbackProvider: HarvestSeasonProvider = HardCodedHarvestSeasonProvider()

    /**
     * Overrides for hard coded harvest seasons. These will be used if there are
     * _any_ hunting seasons for the given species.
     */
    val overridesProvider = HarvestSeasonOverridesProvider()

    fun isDuringHuntingSeason(speciesCode: SpeciesCode, date: LocalDate): Boolean {
        val huntingYear = date.getHuntingYear()

        val seasonPeriods: List<LocalDatePeriod> = getHuntingSeasons(speciesCode)
            .filter { season ->
                season.hasSeasonPeriodsForHuntingYear(huntingYear)
            }
            .flatMap { season ->
                season.yearlySeasonPeriods.flatMap { datePeriod ->
                    datePeriod.toLocalDatePeriodsWithinHuntingYear(huntingYear)
                }
            }

        return date.isWithinPeriods(seasonPeriods)
    }

    private fun getHuntingSeasons(speciesCode: SpeciesCode): List<HuntingSeason> {
        return overridesProvider.getHuntingSeasons(speciesCode)
            .takeIf {
                // only fallback to hardcoded if no seasons at all in overridesProvider
                it.isNotEmpty()
            } ?: fallbackProvider.getHuntingSeasons(speciesCode)
    }
}