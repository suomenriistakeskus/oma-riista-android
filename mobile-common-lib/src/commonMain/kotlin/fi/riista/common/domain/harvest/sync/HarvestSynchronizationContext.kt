package fi.riista.common.domain.harvest.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.HarvestRepository
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.sync.dto.toHarvestPage
import fi.riista.common.domain.harvest.sync.model.HarvestPage
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.AbstractSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider


internal class HarvestSynchronizationContext(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    commonFileProvider: CommonFileProvider,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : AbstractSynchronizationContext(
    preferences = preferences,
    localDateTimeProvider = localDateTimeProvider,
    syncDataPiece = SyncDataPiece.HARVESTS,
) {
    private val repository = HarvestRepository(database)
    private val harvestToDatabaseUpdater = HarvestToDatabaseUpdater(database)
    private val harvestToNetworkUpdater = HarvestToNetworkUpdater(
        backendApiProvider = backendApiProvider,
        database = database,
    )
    private val deletedHarvestsUpdater = DeletedHarvestsUpdater(
        backendApiProvider = backendApiProvider,
        database = database,
    )
    private val harvestImageUpdater = HarvestImageUpdater(
        backendApiProvider = backendApiProvider,
        database = database,
        commonFileProvider = commonFileProvider,
    )

    override suspend fun synchronize(config: SynchronizationConfig) {
        val lastSynchronizationTimeStamp = getLastSynchronizationTimeStamp(suffix = HARVEST_FETCH_SUFFIX)
            .takeIf { config.forceContentReload.not() && hasHarvestSpecVersionChangedSinceLastSync().not() }
        var timestamp: LocalDateTime? = lastSynchronizationTimeStamp

        val username = currentUserContextProvider.userContext.username ?: kotlin.run {
            logger.w { "Unable to sync when no logged in user" }
            return
        }

        // Fetch deleted harvests from backend and delete those from local DB
        val lastDeleteTimestamp = getLastSynchronizationTimeStamp(suffix = HARVEST_DELETE_SUFFIX)
            .takeIf { config.forceContentReload.not() }
        val deleteTimestamp = deletedHarvestsUpdater.fetchFromBackend(username, lastDeleteTimestamp)
        if (deleteTimestamp != null) {
            saveLastSynchronizationTimeStamp(timestamp = deleteTimestamp, suffix = HARVEST_DELETE_SUFFIX)
        }

        // Send locally deleted harvests
        deletedHarvestsUpdater.updateToBackend(username)

        // Fetch data from backend
        do {
            val page = fetchHarvestPage(modifiedAfter = timestamp)
            if (page == null) {
                logger.w { "Unable to fetch harvests from backend" }
                return
            }
            timestamp = page.latestEntry

            harvestToDatabaseUpdater.update(
                username = username,
                harvests = page.content,
                overwriteNonModified = config.forceContentReload,
            )
        } while (page?.hasMore == true)

        sendModifiedHarvests(username)

        // Send unsent images
        val imagesWithLocalImages = repository.getHarvestsWithImagesNeedingUploading(username)
        harvestImageUpdater.updateImagesToBackend(username, imagesWithLocalImages)

        if (timestamp != null) {
            saveLastSynchronizationTimeStamp(timestamp = timestamp, suffix = HARVEST_FETCH_SUFFIX)
            saveHarvestSpecVersionUsedInLastSync()
        }
    }

    /**
     * Deletes the specified harvest in the backend.
     *
     * Requires the harvest to be locally deleted already.
     */
    internal suspend fun deleteHarvestInBackend(harvest: CommonHarvest) {
        if (!harvest.deleted) {
            logger.w { "Refusing to delete not-locally-deleted harvest in backend." }
            return
        }

        deletedHarvestsUpdater.updateToBackend(harvest)
    }

    internal suspend fun sendHarvestToBackend(harvest: CommonHarvest): HarvestOperationResponse {
        val username = currentUserContextProvider.userContext.username ?: kotlin.run {
            logger.w { "Unable to sync when no logged in user" }
            return HarvestOperationResponse.Error("Unable to sync when no logged in user")
        }

        val sendResponse = harvestToNetworkUpdater.sendHarvestToBackend(username, harvest)

        // todo: consider sending images during background sync (assuming this is called when harvest is saved)
        // - should probably be parameterized
        if (sendResponse is HarvestOperationResponse.Success) {
            // disregard possible image upload failures as those can possibly be corrected next time
            // the synchronization is executed
            harvestImageUpdater.updateImagesToBackend(username = username, harvest = sendResponse.harvest)
        }

        return sendResponse
    }

    private suspend fun fetchHarvestPage(modifiedAfter: LocalDateTime?): HarvestPage? {
        val response = backendApiProvider.backendAPI.fetchHarvests(modifiedAfter = modifiedAfter)
        return response.transformSuccessData { _, data ->
            data.typed.toHarvestPage()
        }
    }

    private suspend fun sendModifiedHarvests(username: String): List<CommonHarvest> {
        val events = repository.getModifiedHarvests(username = username)
        return harvestToNetworkUpdater.update(username, events)
    }

    private fun saveHarvestSpecVersionUsedInLastSync() {
        preferences.putInt(KEY_HARVEST_SYNC_SPEC_VERSION, Constants.HARVEST_SPEC_VERSION)
    }

    private fun hasHarvestSpecVersionChangedSinceLastSync(): Boolean {
        return getHarvestSpecVersionUsedInLastSync() != Constants.HARVEST_SPEC_VERSION
    }

    private fun getHarvestSpecVersionUsedInLastSync(): Int? {
        return preferences.getInt(KEY_HARVEST_SYNC_SPEC_VERSION, defaultValue = null)
    }

    companion object {
        private const val HARVEST_FETCH_SUFFIX = "fetch"
        private const val HARVEST_DELETE_SUFFIX = "delete"

        internal const val KEY_HARVEST_SYNC_SPEC_VERSION = "KEY_HARVEST_SYNC_SPEC_VERSION"
    }
}
