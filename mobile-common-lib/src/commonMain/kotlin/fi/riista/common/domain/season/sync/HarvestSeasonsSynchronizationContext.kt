package fi.riista.common.domain.season.sync

import fi.riista.common.domain.season.storage.HarvestSeasonsStorage
import fi.riista.common.logging.getLogger
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.network.sync.SynchronizationContext
import fi.riista.common.util.LocalDateTimeProvider


/**
 * A synchronization context for the harvest seasons.
 *
 * The actual hunting year is specified by the [harvestSeasonsFetcher] i.e. this synchronization
 * context does not necessarily fetch seasons for the current hunting year.
 */
internal class HarvestSeasonsSynchronizationContext(
    private val harvestSeasonsStorage: HarvestSeasonsStorage,
    private val harvestSeasonsFetcher: HarvestSeasonsFetcher,
    private val localDateTimeProvider: LocalDateTimeProvider,
): SynchronizationContext(SyncDataPiece.HARVEST_SEASONS) {

    override suspend fun synchronize(config: SynchronizationConfig) {
        val now = localDateTimeProvider.now()

        val fetchHuntingYear = harvestSeasonsFetcher.getHuntingYearToFetchFor(now)
        val hasSeasonsForHuntingYear = harvestSeasonsStorage.hasHarvestSeasons(fetchHuntingYear)
        val shouldFetchHarvestSeasons = harvestSeasonsFetcher.shouldFetchHarvestSeasons(
            now = now,
            hasSeasonsForHuntingYear = hasSeasonsForHuntingYear,
            ignoreFetchCooldown = config.forceContentReload,
        )
        if (!shouldFetchHarvestSeasons) {
            logger.v { "Not going to fetch harvest seasons for hunting year $fetchHuntingYear" }
            return
        }

        val harvestSeasons = harvestSeasonsFetcher.fetchHarvestSeasons(now)

        // require valid harvest seasons (i.e. seasons must exist) in order to store them.
        //
        // DO NOT STORE EMPTY SEASONS! We don't want to store those as
        // - seasons may not exist for the previous year at all --> it's better to use hardcoded seasons
        // - seaons for the next hunting year may have not yet been published
        if (harvestSeasons != null && harvestSeasons.isNotEmpty()) {
            logger.v {
                "Got valid harvest seasons for hunting year $fetchHuntingYear at $now. Storing them for offline."
            }
            harvestSeasonsStorage.setHarvestSeasons(fetchHuntingYear, harvestSeasons)
        } else {
            logger.w { "Failed to fetch harvest seasons for $fetchHuntingYear" }
        }
    }

    companion object {
        private val logger by getLogger(HarvestSeasonsSynchronizationContext::class)
    }
}

