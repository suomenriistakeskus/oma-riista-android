package fi.riista.common.domain.observation.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.observation.metadata.network.ObservationMetadataFetcher
import fi.riista.common.domain.observation.metadata.network.ObservationMetadataNetworkFetcher
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.model.minutesUntil
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.AbstractSynchronizationContext
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
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
) {

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

    override suspend fun synchronize(config: SynchronizationConfig) {
        // nothing to do if user is not logged in
        if (!currentUserContextProvider.userContext.isLoggedIn) {
            logger.v { "Not synchronizing observation metadata, user not logged in." }
            return
        }

        val lastSynchronizationTime = getLastSynchronizationTimeStamp().takeIf { config.forceContentReload.not() }
        val now = localDateTimeProvider.now()
        if (lastSynchronizationTime == null ||
            lastSynchronizationTime.minutesUntil(now) > Constants.METADATA_UPDATE_COOLDOWN_MINUTES) {

            metadataFetcher.fetch(refresh = true)
            val receivedMetadata = metadataFetcher.metadata

            if (receivedMetadata != null) {
                metadataCache.storeMetadata(receivedMetadata)
                saveLastSynchronizationTimeStamp(timestamp = localDateTimeProvider.now())
            }
        } else {
            logger.v { "Not synchronizing observation metadata (last synced $lastSynchronizationTime)" }
        }
    }
}
