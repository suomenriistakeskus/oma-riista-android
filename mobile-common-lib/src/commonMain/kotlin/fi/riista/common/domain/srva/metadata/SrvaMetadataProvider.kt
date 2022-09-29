package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.srva.metadata.network.SrvaMetadataFetcher
import fi.riista.common.domain.srva.metadata.network.SrvaMetadataNetworkFetcher
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.model.minutesUntil
import fi.riista.common.network.*
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

internal class SrvaMetadataProvider internal constructor(
    private val metadataRepository: MetadataRepository,
    private val metadataFetcher: SrvaMetadataFetcher,
    private val currentUserContextProvider: CurrentUserContextProvider,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
): AbstractSynchronizationContext(
    preferences = preferences,
    localDateTimeProvider = localDateTimeProvider,
    syncDataPiece = SyncDataPiece.SRVA_METADATA
), SynchronizationContextProvider {

    internal constructor(
        metadataRepository: MetadataRepository,
        backendApiProvider: BackendApiProvider,
        currentUserContextProvider: CurrentUserContextProvider,
        preferences: Preferences,
        localDateTimeProvider: LocalDateTimeProvider,
    ): this(
        metadataRepository = metadataRepository,
        metadataFetcher = SrvaMetadataNetworkFetcher(backendApiProvider),
        currentUserContextProvider = currentUserContextProvider,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
    )
    /**
     * The SRVA metadata for the current spec version.
     */
    val metadata: SrvaMetadata
        get() {
            return metadataCache.cachedMetadata ?: fallbackMetadata
        }

    private val metadataCache: SrvaMetadataCache by lazy {
        SrvaMetadataCache(
            metadataRepository = metadataRepository,
        )
    }

    private val fallbackMetadata: SrvaMetadata = HardcodedSrvaMetadataProvider.metadata

    override val synchronizationContext: SynchronizationContext
        get() = this

    suspend fun updateMetadata() {
        metadataFetcher.fetch(refresh = true)
        val receivedMetadata = metadataFetcher.metadata

        if (receivedMetadata != null) {
            metadataCache.storeMetadata(receivedMetadata)
            saveLastSynchronizationTimeStamp(timestamp = localDateTimeProvider.now())
        }
    }

    override suspend fun doSynchronize() {
        // nothing to do if user does not have SRVA enabled
        val srvaEnabled = currentUserContextProvider.userContext.userInfo?.enableSrva ?: false
        if (!srvaEnabled) {
            logger.v { "Not synchronizing SRVA metadata, srva not enabled for user." }
            return
        }

        val lastSynchronizationTime = getLastSynchronizationTimeStamp()
        val now = localDateTimeProvider.now()
        if (lastSynchronizationTime == null ||
            lastSynchronizationTime.minutesUntil(now) > Constants.METADATA_UPDATE_COOLDOWN_MINUTES) {
            updateMetadata()
        } else {
            logger.v { "Not synchronizing SRVA metadata (last synced $lastSynchronizationTime)" }
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
        private val logger by getLogger(SrvaMetadataProvider::class)
    }
}