package fi.riista.common.domain.huntingControl.sync

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.network.AbstractSynchronizationContext
import fi.riista.common.network.AbstractSynchronizationContextProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.SyncDataPiece
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

internal class HuntingControlSynchronizationContextProvider(
    private val backendApiProvider: BackendApiProvider,
    private val database: RiistaDatabase,
    private val preferences: Preferences,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val commonFileProvider: CommonFileProvider,
    syncFinishedListener: (suspend () -> Unit)?,
) : AbstractSynchronizationContextProvider(syncFinishedListener = syncFinishedListener) {

    private data class UserSynchronizationContext(
        val username: String,
        val synchronizationContext: HuntingControlSynchronizationContext
    )
    private var userSynchronizationContext: AtomicReference<UserSynchronizationContext?> = AtomicReference(null)

    override val synchronizationContext: HuntingControlSynchronizationContext?
        get() {
            val username = RiistaSDK.currentUserContext.username
            return if (username != null) {
                var synchronizationContext = userSynchronizationContext.value
                if (synchronizationContext == null || synchronizationContext.username != username) {
                    synchronizationContext = UserSynchronizationContext(
                        username = username,
                        synchronizationContext = HuntingControlSynchronizationContext(
                            backendApiProvider = backendApiProvider,
                            database = database,
                            preferences = preferences,
                            localDateTimeProvider = localDateTimeProvider,
                            commonFileProvider = commonFileProvider,
                            username = username,
                        )
                    ).also {
                        userSynchronizationContext.set(it)
                    }
                }
                return synchronizationContext.synchronizationContext
            } else {
                null
            }
        }
}

internal class HuntingControlSynchronizationContext(
    val backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    private val commonFileProvider: CommonFileProvider,
    val username: String,
) : AbstractSynchronizationContext(
    preferences = preferences,
    localDateTimeProvider = localDateTimeProvider,
    syncDataPiece = SyncDataPiece.HUNTING_CONTROL,
) {
    private val repository = HuntingControlRepository(database)

    private val rhyFromNetworkProvider = HuntingControlRhyFromNetworkProvider(backendApiProvider)
    private val rhyToDatabaseUpdater = HuntingControlRhyToDatabaseUpdater(database, username)
    private val eventsToNetworkUpdater = HuntingControlEventToNetworkUpdater(
        backendApiProvider = backendApiProvider,
        commonFileProvider = commonFileProvider,
        database = database,
    )

    override suspend fun doSynchronize() {
        val lastSynchronizationTimeStamp = getLastSynchronizationTimeStamp()
        val now = localDateTimeProvider.now()

        var success = true
        val username = RiistaSDK.currentUserContext.username ?: kotlin.run {
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

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(HuntingControlSynchronizationContext::class)
    }
}

