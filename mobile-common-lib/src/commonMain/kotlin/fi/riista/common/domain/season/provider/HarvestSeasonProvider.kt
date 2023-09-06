package fi.riista.common.domain.season.provider

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.model.HardcodedHarvestSeason
import fi.riista.common.domain.season.model.HarvestSeason

interface HarvestSeasonProvider {
    /**
     * Gets whether there are hunting seasons for the given [huntingYear].
     */
    fun hasHarvestSeasons(huntingYear: HuntingYear): Boolean

    /**
     * Attempts to get hunting seasons for the specified [speciesCode] and [huntingYear].
     *
     * @return `null` if there are no seasons for the given [huntingYear], otherwise a list of seasons
     * for the species specified by [speciesCode].
     */
    fun getHarvestSeasons(speciesCode: SpeciesCode, huntingYear: HuntingYear): List<HarvestSeason>?
}