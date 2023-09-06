package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.model.HuntingControlEvent
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.AbstractSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider


internal class HuntingControlSynchronizationContext(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    private val commonFileProvider: CommonFileProvider,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : AbstractSynchronizationContext(
    preferences = preferences,
    localDateTimeProvider = localDateTimeProvider,
    syncDataPiece = SyncDataPiece.HUNTING_CONTROL,
) {
    private val repository = HuntingControlRepository(database)

    private val rhyFromNetworkProvider = HuntingControlRhyFromNetworkProvider(backendApiProvider)
    private val rhyToDatabaseUpdater = HuntingControlRhyToDatabaseUpdater(database, currentUserContextProvider)
    private val eventsToNetworkUpdater = HuntingControlEventToNetworkUpdater(
        backendApiProvider = backendApiProvider,
        commonFileProvider = commonFileProvider,
        database = database,
    )

    override suspend fun synchronize(config: SynchronizationConfig) {
        val lastSynchronizationTimeStamp = getLastSynchronizationTimeStamp()
            .takeIf { config.forceContentReload.not() }
        val now = localDateTimeProvider.now()

        var success = true
        val username = currentUserContextProvider.userContext.username ?: kotlin.run {
            logger.w { "Unable to sync when no logged in user" }
            return
        }

        // Send modified (or new) events
        val modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        success = success && eventsToNetworkUpdater.update(modifiedEvents)

        // Fetch data from backend
        rhyFromNetworkProvider.syncTimestamp.set(lastSynchronizationTimeStamp)
        rhyFromNetworkProvider.fetch(refresh = true)

        val rhys = rhyFromNetworkProvider.rhys
        if (rhys == null) {
            logger.w { "Unable to load data to sync" }
            return
        }

        val response = rhyToDatabaseUpdater.update(rhys)

        deleteDanglingAttachments()
        fetchThumbnails()

        success == success && response is HuntingControlRhyOperationResponse.Success

        if (success) {
            saveLastSynchronizationTimeStamp(timestamp = now)
        }
    }

    internal suspend fun sendHuntingControlEventToBackend(event: HuntingControlEvent): Boolean {
        return eventsToNetworkUpdater.update(listOf(event))
    }

    private fun deleteDanglingAttachments() {
        val existingAttachments = repository.listAllAttachmentUuids()
        val files = commonFileProvider.getAllFilesIn(directory = CommonFileProvider.Directory.ATTACHMENTS)
        files.forEach { file ->
            val fileUuid = file.fileUuid
            if (!existingAttachments.contains(fileUuid)) {
                logger.i { "Delete dangling attachment $fileUuid" }
                file.delete()
            }
        }
    }

    private suspend fun fetchThumbnails() {
        repository.listAttachmentsMissingThumbnails().forEach { attachmentId ->
            val response = backendApiProvider.backendAPI.fetchHuntingControlAttachmentThumbnail(attachmentId)
            response.onError { statusCode, exception ->
                logger.w { "Failed to fetch thumbnail for attachment $attachmentId, statusCode=$statusCode, message=${exception?.message}" }
            }
            response.onSuccess { _, data ->
                if (data.raw.isNotEmpty()) {
                    repository.setThumbnail(data.raw, attachmentId)
                }
            }
        }
    }
}

