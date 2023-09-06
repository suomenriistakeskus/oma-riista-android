package fi.riista.common.domain.season.provider

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.domain.season.storage.HarvestSeasonsMemoryCache
import fi.riista.common.network.sync.MockSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece

internal class MockBackendHarvestSeasonsProvider: BackendHarvestSeasonsProvider {
    override val synchronizationContext = MockSynchronizationContext(SyncDataPiece.HARVEST_SEASONS)

    val harvestSeasons = HarvestSeasonsMemoryCache()

    override fun hasHarvestSeasons(huntingYear: HuntingYear): Boolean =
        harvestSeasons.hasHarvestSeasons(huntingYear)

    override fun getHarvestSeasons(speciesCode: SpeciesCode, huntingYear: HuntingYear): List<HarvestSeason>? {
        return harvestSeasons.getHarvestSeasons(speciesCode, huntingYear)
    }
}

