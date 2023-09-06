package fi.riista.common.domain.season.provider

import fi.riista.common.domain.season.storage.HarvestSeasonsOfflineStorage
import fi.riista.common.domain.season.storage.HarvestSeasonsRepository
import fi.riista.common.domain.season.storage.HarvestSeasonsStorage
import fi.riista.common.domain.season.sync.HarvestSeasonsFetcher
import fi.riista.common.domain.season.sync.HarvestSeasonsSynchronizationContext
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.CombinedSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationContext
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

/**
 * An interface for the provider that fetches harvest seasons from the backend.
 *
 * Using interface allows mocking the provider in tests.
 */
internal interface BackendHarvestSeasonsProvider: HarvestSeasonProvider {
    val synchronizationContext: SynchronizationContext
}

/**
 * The actual harvests seasons provider that fetchers seasons from the backend.
 */
internal class BackendHarvestSeasonsProviderImpl internal constructor(
    private val harvestSeasonsStorage: HarvestSeasonsStorage,
    private val backendApiProvider: BackendApiProvider,
    private val preferences: Preferences,
    private val localDateTimeProvider: LocalDateTimeProvider,
): BackendHarvestSeasonsProvider, HarvestSeasonProvider by harvestSeasonsStorage {

    internal constructor(
        harvestSeasonsRepository: HarvestSeasonsRepository,
        backendApiProvider: BackendApiProvider,
        preferences: Preferences,
        localDateTimeProvider: LocalDateTimeProvider,
    ): this(
        harvestSeasonsStorage = HarvestSeasonsOfflineStorage(
            repository = harvestSeasonsRepository,
        ),
        backendApiProvider = backendApiProvider,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
    )

    override val synchronizationContext = CombinedSynchronizationContext(
        childSynchronizationContexts = listOf(
            createHarvestSeasonsSynchronizationContext(HarvestSeasonsFetcher.Season.PREVIOUS),
            createHarvestSeasonsSynchronizationContext(HarvestSeasonsFetcher.Season.CURRENT),
            createHarvestSeasonsSynchronizationContext(HarvestSeasonsFetcher.Season.NEXT),
        ),
        syncDataPiece = SyncDataPiece.HARVEST_SEASONS
    )

    private fun createHarvestSeasonsSynchronizationContext(
        season: HarvestSeasonsFetcher.Season,
    ): HarvestSeasonsSynchronizationContext {
        return HarvestSeasonsSynchronizationContext(
            harvestSeasonsStorage = harvestSeasonsStorage,
            harvestSeasonsFetcher = HarvestSeasonsFetcher(
                harvestSeason = season,
                backendApiProvider = backendApiProvider,
                preferences = preferences,
            ),
            localDateTimeProvider = localDateTimeProvider
        )
    }
}
