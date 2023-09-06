package fi.riista.common.domain.season

import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.domain.season.provider.BackendHarvestSeasonsProvider
import fi.riista.common.domain.season.provider.MockBackendHarvestSeasonsProvider

object TestHarvestSeasons {
    internal fun createMockHarvestSeasons(
        backendHarvestSeasonsProvider: BackendHarvestSeasonsProvider = MockBackendHarvestSeasonsProvider()
    ): HarvestSeasons {
        return HarvestSeasons(
            backendSeasonsProvider = backendHarvestSeasonsProvider
        )
    }

    fun createMockHarvestSeasons(huntingYear: HuntingYear, harvestSeasons: List<HarvestSeason>): HarvestSeasons {
        val mockSeasonsProvider = MockBackendHarvestSeasonsProvider()
        mockSeasonsProvider.harvestSeasons.setHarvestSeasons(huntingYear, harvestSeasons)

        return HarvestSeasons(
            backendSeasonsProvider = mockSeasonsProvider
        )
    }
}
