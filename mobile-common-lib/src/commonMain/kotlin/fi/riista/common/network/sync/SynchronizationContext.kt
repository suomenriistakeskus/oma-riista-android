package fi.riista.common.network.sync

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin
import fi.riista.common.logging.getLogger
import fi.riista.common.util.coroutines.SequentialJob
import kotlin.coroutines.cancellation.CancellationException

internal abstract class SynchronizationContext(
    val syncDataPiece: SyncDataPiece
) {
    protected val logger by getLogger(this::class)

    private val sequentialSynchronization = SequentialJob(jobName = "$syncDataPiece sync", logger = logger)

    /**
     * Starts the synchronization. Safe to be called from multiple threads/coroutines simultaneously.
     */
    @Throws(SynchronizationException::class, CancellationException::class)
    suspend fun startSynchronization(config: SynchronizationConfig) {
        sequentialSynchronization.join {
            try {
                this.synchronize(config)
            } catch (cause: Throwable) {
                // log and send to crashlytics as that allows us to get notified about issues that occur
                // during synchronization both when developing the app as well as when in production
                logger.w { "An exception occurred during synchronization: $cause" }
                logger.v { cause.stackTraceToString() }
                CrashlyticsKotlin.sendHandledException(cause)

                if (cause is CancellationException) {
                    throw SynchronizationException("Sync cancelled", cause)
                } else {
                    throw SynchronizationException("Sync exception occurred", cause)
                }
            }
        }
    }

    /**
     * Delegates the synchronization to the given [target] [SynchronizationContext].
     *
     * One should not call this function simultaneously from multiple threads/coroutines.
     */
    protected suspend fun delegateSynchronization(target: SynchronizationContext, config: SynchronizationConfig) {
        target.synchronize(config)
    }

    /**
     * Perform the synchronization.
     *
     * When overriding this function one can expect that this function will NOT be called from
     * multiple threads/coroutines simultaneously.
     */
    protected abstract suspend fun synchronize(config: SynchronizationConfig)
}
