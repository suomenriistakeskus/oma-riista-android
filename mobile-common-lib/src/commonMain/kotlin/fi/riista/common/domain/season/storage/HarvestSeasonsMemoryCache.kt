package fi.riista.common.domain.season.storage

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.domain.season.provider.HarvestSeasonProvider

class HarvestSeasonsMemoryCache: HarvestSeasonProvider {
    private val seasonsByHuntingYear = mutableMapOf<HuntingYear, List<HarvestSeason>>()

    fun setHarvestSeasons(huntingYear: HuntingYear, harvestSeasons: List<HarvestSeason>) {
        seasonsByHuntingYear.set(
            key = huntingYear,
            value = harvestSeasons
        )
    }

    override fun hasHarvestSeasons(huntingYear: HuntingYear): Boolean {
        return seasonsByHuntingYear.containsKey(huntingYear)
    }

    override fun getHarvestSeasons(speciesCode: SpeciesCode, huntingYear: HuntingYear): List<HarvestSeason>? {
        val huntingYearSeasons = seasonsByHuntingYear[huntingYear] ?: return null

        return huntingYearSeasons.filter {
            it.speciesCode == speciesCode
        }
    }
}