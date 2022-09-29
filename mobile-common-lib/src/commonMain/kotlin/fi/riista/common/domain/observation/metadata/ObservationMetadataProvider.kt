package fi.riista.common.domain.observation.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.observation.metadata.network.ObservationMetadataFetcher
import fi.riista.common.domain.observation.metadata.network.ObservationMetadataNetworkFetcher
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.model.minutesUntil
import fi.riista.common.network.AbstractSynchronizationContext
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.SyncDataPiece
import fi.riista.common.network.SynchronizationContext
import fi.riista.common.network.SynchronizationContextProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

internal class ObservationMetadataProvider internal constructor(
    private val metadataRepository: MetadataRepository,
    private val metadataFetcher: ObservationMetadataFetcher,
    private val currentUserContextProvider: CurrentUserContextProvider,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
): AbstractSynchronizationContext(
    preferences = preferences,
    localDateTimeProvider = localDateTimeProvider,
    syncDataPiece = SyncDataPiece.OBSERVATION_METADATA
), SynchronizationContextProvider {

    internal constructor(
        metadataRepository: MetadataRepository,
        backendApiProvider: BackendApiProvider,
        currentUserContextProvider: CurrentUserContextProvider,
        preferences: Preferences,
        localDateTimeProvider: LocalDateTimeProvider,
    ): this(
        metadataRepository = metadataRepository,
        metadataFetcher = ObservationMetadataNetworkFetcher(backendApiProvider),
        currentUserContextProvider = currentUserContextProvider,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
    )
    /**
     * The Observation metadata for the current spec version.
     */
    val metadata: ObservationMetadata
        get() {
            return metadataCache.cachedMetadata ?: fallbackMetadata
        }

    private val metadataCache: ObservationMetadataCache by lazy {
        ObservationMetadataCache(
            metadataRepository = metadataRepository,
        )
    }

    private val fallbackMetadata: ObservationMetadata by lazy {
        HardcodedObservationMetadataProvider.metadata
    }

    override val synchronizationContext: SynchronizationContext
        get() = this

    suspend fun updateMetadata() {
        // nothing to do if user is not logged in
        val loggedIn = currentUserContextProvider.userContext.userInfo != null
        if (!loggedIn) {
            logger.v { "Not synchronizing observation metadata, user not logged in." }
            return
        }

        metadataFetcher.fetch(refresh = true)
        val receivedMetadata = metadataFetcher.metadata

        if (receivedMetadata != null) {
            metadataCache.storeMetadata(receivedMetadata)
            saveLastSynchronizationTimeStamp(timestamp = localDateTimeProvider.now())
        }
    }

    override suspend fun doSynchronize() {
        val lastSynchronizationTime = getLastSynchronizationTimeStamp()
        val now = localDateTimeProvider.now()
        if (lastSynchronizationTime == null ||
            lastSynchronizationTime.minutesUntil(now) > Constants.METADATA_UPDATE_COOLDOWN_MINUTES) {
            updateMetadata()
        } else {
            logger.v { "Not synchronizing observation metadata (last synced $lastSynchronizationTime)" }
        }
    }

    override suspend fun syncStarted() {
        // nop
    }

    override suspend fun syncFinished() {
        // nop
    }

    override fun logger(): Logger {
        return logger
    }

    companion object {
        private val logger by getLogger(ObservationMetadataProvider::class)
    }
}