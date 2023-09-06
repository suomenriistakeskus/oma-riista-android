package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.srva.SrvaEventOperationResponse
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.sync.dto.toSrvaEventPage
import fi.riista.common.domain.srva.sync.model.SrvaEventPage
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.AbstractSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider


internal class SrvaSynchronizationContext(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    val commonFileProvider: CommonFileProvider,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : AbstractSynchronizationContext(
    preferences = preferences,
    localDateTimeProvider = localDateTimeProvider,
    syncDataPiece = SyncDataPiece.SRVA_EVENTS,
) {

    private val repository = SrvaEventRepository(database)
    private val srvaEventToDatabaseUpdater = SrvaEventToDatabaseUpdater(database)
    private val srvaEventToNetworkUpdater = SrvaEventToNetworkUpdater(
        backendApiProvider = backendApiProvider,
        database = database,
    )
    private val deletedSrvaEventsUpdater = DeletedSrvaEventsUpdater(
        backendApiProvider = backendApiProvider,
        database = database,
    )
    private val srvaImageUpdater = SrvaImageUpdater(
        backendApiProvider = backendApiProvider,
        database = database,
        commonFileProvider = commonFileProvider,
    )

    override suspend fun synchronize(config: SynchronizationConfig) {
        val lastSynchronizationTimeStamp = getLastSynchronizationTimeStamp(suffix = EVENT_FETCH_SUFFIX)
            .takeIf { config.forceContentReload.not() }
        var timestamp = lastSynchronizationTimeStamp

        val username = currentUserContextProvider.userContext.username ?: kotlin.run {
            logger.w { "Unable to sync when no logged in user" }
            return
        }

        val lastDeleteTimestamp = getLastSynchronizationTimeStamp(suffix = EVENT_DELETE_SUFFIX)
            .takeIf { config.forceContentReload.not() }
        val deleteTimestamp = deletedSrvaEventsUpdater.fetchFromBackend(username, lastDeleteTimestamp)
        if (deleteTimestamp != null) {
            saveLastSynchronizationTimeStamp(timestamp = deleteTimestamp, suffix = EVENT_DELETE_SUFFIX)
        }

        // Send locally deleted events
        deletedSrvaEventsUpdater.updateToBackend(username)

        // Fetch data from backend
        do {
            val page = fetchSrvaEventPage(modifiedAfter = timestamp)
            if (page == null) {
                logger.w { "Unable to fetch events from backend" }
                return
            }
            timestamp = page.latestEntry

            srvaEventToDatabaseUpdater.update(
                username = username,
                srvaEvents = page.content,
                overwriteNonModified = config.forceContentReload,
            )
        } while (page?.hasMore == true)

        sendModifiedEvents(username)

        // go through srva events having local images (which may already be uploaded). Send unsent images.
        val imagesWithLocalImages = repository.getEventsWithLocalImages(username)
        srvaImageUpdater.updateImagesToBackend(username, imagesWithLocalImages)

        if (timestamp != null) {
            saveLastSynchronizationTimeStamp(timestamp = timestamp, suffix = EVENT_FETCH_SUFFIX)
        }
    }

    internal suspend fun sendSrvaEventToBackend(srvaEvent: CommonSrvaEvent): SrvaEventOperationResponse {
        val username = currentUserContextProvider.userContext.username ?: kotlin.run {
            logger.w { "Unable to sync when no logged in user" }
            return SrvaEventOperationResponse.Error("Unable to sync when no logged in user")
        }

        val sendResponse = srvaEventToNetworkUpdater.sendSrvaEventToBackend(username, srvaEvent)

        // todo: consider sending images during background sync (assuming this is called when srva is saved)
        // - should probably be parameterized
        if (sendResponse is SrvaEventOperationResponse.Success) {
            // disregard possible image upload failures as those can possibly be corrected next time
            // the synchronization is executed
            srvaImageUpdater.updateImagesToBackend(username = username, srvaEvent = sendResponse.srvaEvent)
        }

        return sendResponse
    }

    /**
     * Deletes the specified srva event in the backend.
     *
     * Requires the srva event to be locally deleted already.
     */
    internal suspend fun deleteSrvaEventInBackend(srvaEvent: CommonSrvaEvent) {
        if (!srvaEvent.deleted) {
            logger.w { "Refusing to delete not-locally-deleted srva event in backend." }
            return
        }

        deletedSrvaEventsUpdater.updateToBackend(srvaEvent)
    }

    private suspend fun fetchSrvaEventPage(modifiedAfter: LocalDateTime?): SrvaEventPage? {
        val response = backendApiProvider.backendAPI.fetchSrvaEvents(modifiedAfter = modifiedAfter)
        return response.transformSuccessData { _, data ->
            data.typed.toSrvaEventPage()
        }
    }

    private suspend fun sendModifiedEvents(username: String): List<CommonSrvaEvent> {
        val events = repository.getModifiedEvents(username = username)
        return srvaEventToNetworkUpdater.update(username, events)
    }

    companion object {
        private const val EVENT_FETCH_SUFFIX = "fetch"
        private const val EVENT_DELETE_SUFFIX = "delete"
    }
}
