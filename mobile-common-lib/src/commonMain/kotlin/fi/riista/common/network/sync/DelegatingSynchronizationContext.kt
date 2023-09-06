package fi.riista.common.network.sync

import fi.riista.common.util.OnAction

/**
 * A [SynchronizationContext] that delegates synchronization to the specified [childContext] and
 * provides a way to get notified when synchronization occurs
 */
internal class DelegatingSynchronizationContext<T : SynchronizationContext>(
    internal val childContext: T,
    syncDataPiece: SyncDataPiece,
    private val onSyncStarted: OnAction? = null,
    private val onSyncFinished: OnAction? = null,
): SynchronizationContext(syncDataPiece) {

    override suspend fun synchronize(config: SynchronizationConfig) {
        onSyncStarted?.let { it() }
        delegateSynchronization(target = childContext, config = config)
        onSyncFinished?.let { it() }
    }
}

internal fun <T : SynchronizationContext> T.delegated(
    onSyncStarted: OnAction? = null,
    onSyncFinished: OnAction? = null,
) = DelegatingSynchronizationContext(
    childContext = this,
    syncDataPiece = this.syncDataPiece,
    onSyncStarted = onSyncStarted,
    onSyncFinished = onSyncFinished,
)
