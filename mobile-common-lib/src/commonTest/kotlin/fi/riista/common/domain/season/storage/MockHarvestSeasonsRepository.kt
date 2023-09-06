package fi.riista.common.domain.season.storage

import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO

class MockHarvestSeasonsRepository: HarvestSeasonsRepository {
    private val harvestSeasons = mutableMapOf<HuntingYear, List<HarvestSeasonDTO>>()

    override suspend fun saveHarvestSeasons(huntingYear: HuntingYear, harvestSeasonsDTOs: List<HarvestSeasonDTO>) {
        harvestSeasons[huntingYear] = harvestSeasonsDTOs
    }

    override fun getHarvestSeasons(huntingYear: HuntingYear): List<HarvestSeasonDTO>? =
        harvestSeasons[huntingYear]
}

