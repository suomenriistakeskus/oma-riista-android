package fi.riista.common.network

import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider
import kotlinx.coroutines.*

enum class SyncDataPiece(val timestampKey: String) {
    HUNTING_CONTROL(timestampKey = "HuntingControlKey"),
    SRVA_METADATA(timestampKey = "SyncSrvaMetadataKey"),
    OBSERVATION_METADATA(timestampKey = "SyncObservationMetadataKey"),
}

internal interface SynchronizationContextProvider {
    val synchronizationContext: SynchronizationContext?
    suspend fun syncStarted()
    suspend fun syncFinished()
}

internal abstract class AbstractSynchronizationContextProvider(
    private val syncStartListener: (suspend () -> Unit)? = null,
    private val syncFinishedListener: (suspend () -> Unit)? = null,
) : SynchronizationContextProvider {

    override suspend fun syncStarted() {
        syncStartListener?.invoke()
    }

    override suspend fun syncFinished() {
        syncFinishedListener?.invoke()
    }
}

internal interface SynchronizationContext {
    suspend fun synchronize()
}

internal abstract class AbstractSynchronizationContext(
    private val preferences: Preferences,
    protected val localDateTimeProvider: LocalDateTimeProvider,
    internal val syncDataPiece: SyncDataPiece,
) : SynchronizationContext {

    private var existingSynchronization = AtomicReference<Job?>(null)

    final override suspend fun synchronize(): Unit = coroutineScope {
        // don't allow simultaneous synchronizations
        var activeSynchronization = existingSynchronization.value
        if (activeSynchronization != null) {
            logger()?.v { "An active synchronization operation found." }
        } else {
            logger()?.v { "No active synchronization operation, creating one.." }

            // create an async task for synchronizing the data. Don't utilize suspend
            // function here in order to allow other callees to wait for the same result
            activeSynchronization = launch(start = CoroutineStart.LAZY) {
                doSynchronize()
            }.also {
                existingSynchronization.value = it
            }
        }

        logger()?.v { "Waiting for the synchronization operation to be performed.."}
        try {
            activeSynchronization.join()
            existingSynchronization.value = null
        } catch (e: CancellationException) {
            logger()?.i { "Synchronization operation was cancelled." }

            existingSynchronization.value = null

            // rethrow exception as that will cause the cancellation of other jobs
            throw e
        }

        logger()?.v { "Synchronization operation completed."}
    }

    abstract suspend fun doSynchronize()

    /**
     * Return a logger in order to log stuff from this class..
     */
    protected abstract fun logger(): Logger?

    protected fun getLastSynchronizationTimeStamp(): LocalDateTime? {
        return preferences.getString(syncDataPiece.timestampKey)?.let { prevSyncTime ->
            LocalDateTime.parseLocalDateTime(prevSyncTime)
        }
    }

    protected fun saveLastSynchronizationTimeStamp(timestamp: LocalDateTime) {
        preferences.putString(syncDataPiece.timestampKey, timestamp.toStringISO8601())
    }
}

class SynchronizationService internal constructor() {
    private val syncContextProviders = IsoMutableMap<SyncDataPiece, SynchronizationContextProvider>()

    internal fun registerSyncContextProvider(
        syncDataPiece: SyncDataPiece,
        synchronizationContextProvider: SynchronizationContextProvider
    ) {
        syncContextProviders[syncDataPiece] = synchronizationContextProvider
    }

    suspend fun synchronizeDataPieces(dataPieces: List<SyncDataPiece>) {
        dataPieces.forEach { dataPiece ->
            val syncProvider = syncContextProviders[dataPiece]

            syncProvider?.synchronizationContext?.let { syncContext ->
                syncProvider.syncStarted()
                syncContext.synchronize()
                syncProvider.syncFinished()
            } ?: kotlin.run {
                logger.w { "No SyncContext for $dataPiece" }
            }
        }
    }

    companion object {
        private val logger by getLogger(SynchronizationService::class)
    }
}
