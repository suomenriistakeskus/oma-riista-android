package fi.riista.common.domain.season.storage

import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.domain.season.provider.HarvestSeasonProvider

internal interface HarvestSeasonsStorage: HarvestSeasonProvider {
    /**
     * Sets the harvest seasons for the given [huntingYear].
     */
    suspend fun setHarvestSeasons(huntingYear: HuntingYear, harvestSeasonDTOs: List<HarvestSeasonDTO>)
}