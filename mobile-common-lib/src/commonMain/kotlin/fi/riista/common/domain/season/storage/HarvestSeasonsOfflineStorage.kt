package fi.riista.common.domain.season.storage

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.domain.season.dto.toHarvestSeason
import fi.riista.common.domain.season.model.HarvestSeason

internal open class HarvestSeasonsOfflineStorage(
    internal val repository: HarvestSeasonsRepository
): HarvestSeasonsStorage {
    internal val memoryStorage = HarvestSeasonsMemoryCache()

    override suspend fun setHarvestSeasons(huntingYear: HuntingYear, harvestSeasonDTOs: List<HarvestSeasonDTO>) {
        saveHarvestSeasonsToMemoryCache(huntingYear, harvestSeasonDTOs)
        repository.saveHarvestSeasons(huntingYear, harvestSeasonDTOs)
    }

    override fun hasHarvestSeasons(huntingYear: HuntingYear): Boolean {
        fetchHarvestSeasonsToMemoryStorageIfNeeded(huntingYear)
        return memoryStorage.hasHarvestSeasons(huntingYear)
    }

    override fun getHarvestSeasons(speciesCode: SpeciesCode, huntingYear: HuntingYear): List<HarvestSeason>? {
        fetchHarvestSeasonsToMemoryStorageIfNeeded(huntingYear)
        return memoryStorage.getHarvestSeasons(speciesCode, huntingYear)
    }

    private fun fetchHarvestSeasonsToMemoryStorageIfNeeded(huntingYear: HuntingYear) {
        if (memoryStorage.hasHarvestSeasons(huntingYear)) {
            return
        }

        repository.getHarvestSeasons(huntingYear)?.let { harvestSeasonDTOS ->
            saveHarvestSeasonsToMemoryCache(huntingYear, harvestSeasonDTOS)
        }
    }

    private fun saveHarvestSeasonsToMemoryCache(huntingYear: HuntingYear, harvestSeasonDTOs: List<HarvestSeasonDTO>) {
        val harvestSeasons = harvestSeasonDTOs.map { it.toHarvestSeason(huntingYear) }
        memoryStorage.setHarvestSeasons(huntingYear, harvestSeasons)
    }
}