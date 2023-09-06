package fi.riista.common.network.sync

import fi.riista.common.logging.getLogger
import kotlin.coroutines.cancellation.CancellationException

enum class SyncDataPiece(
    val synchronizationLevel: SynchronizationLevel,
    val timestampKey: String,
) {
    HUNTING_CONTROL(synchronizationLevel = SynchronizationLevel.USER_CONTENT, timestampKey = "HuntingControlKey"),
    SRVA_METADATA(synchronizationLevel = SynchronizationLevel.METADATA, timestampKey = "SyncSrvaMetadataKey"),
    OBSERVATION_METADATA(synchronizationLevel = SynchronizationLevel.METADATA, timestampKey = "SyncObservationMetadataKey"),
    HARVEST_SEASONS(synchronizationLevel = SynchronizationLevel.METADATA, timestampKey = "HarvestSeasonsKey"),
    METSAHALLITUS_PERMITS(synchronizationLevel = SynchronizationLevel.METADATA, timestampKey = "MetsahallitusPermitsKey"),
    HUNTING_CLUB_OCCUPATIONS(synchronizationLevel = SynchronizationLevel.METADATA, timestampKey = "HuntingClubOccupationsKey"),

    SRVA_EVENTS(synchronizationLevel = SynchronizationLevel.USER_CONTENT, timestampKey = "SrvaEventsKey"),
    OBSERVATIONS(synchronizationLevel = SynchronizationLevel.USER_CONTENT, timestampKey = "ObservationsKey"),
    HARVESTS(synchronizationLevel = SynchronizationLevel.USER_CONTENT, timestampKey = "HarvestsKey"),
    ;

    fun isIncludedIn(synchronizationLevel: SynchronizationLevel): Boolean {
        return this.synchronizationLevel.isIncludedIn(synchronizationLevel)
    }

    companion object {
        fun getPiecesIncludedInSynchronizationLevel(synchronizationLevel: SynchronizationLevel): List<SyncDataPiece> {
            return values().filter { it.isIncludedIn(synchronizationLevel) }
        }
    }
}

class SynchronizationService internal constructor() {
    private val synchronizationContexts = mutableMapOf<SyncDataPiece, SynchronizationContext>()

    internal fun registerSynchronizationContext(synchronizationContext: SynchronizationContext) {
        logger.v { "Registering synchronization context for ${synchronizationContext.syncDataPiece}" }
        synchronizationContexts[synchronizationContext.syncDataPiece] = synchronizationContext
    }

    @Throws(SynchronizationException::class, CancellationException::class)
    suspend fun synchronizeDataPieces(synchronizedContent: SynchronizedContent, config: SynchronizationConfig) {
        val dataPieces = synchronizedContent.synchronizationDataPieces

        dataPieces.forEach { dataPiece ->
            synchronizationContexts[dataPiece]?.let { synchronizationContext ->
                logger.d { "Synchronizing $dataPiece.." }
                synchronizationContext.startSynchronization(config)
            } ?: kotlin.run {
                logger.w { "Cannot synchronize $dataPiece. No SynchronizationContext." }
            }
        }
    }

    companion object {
        private val logger by getLogger(SynchronizationService::class)
    }
}


