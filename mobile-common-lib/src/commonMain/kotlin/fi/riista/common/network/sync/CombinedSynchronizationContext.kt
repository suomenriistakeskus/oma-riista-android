package fi.riista.common.network.sync

internal class CombinedSynchronizationContext(
    private val childSynchronizationContexts: List<SynchronizationContext>,
    syncDataPiece: SyncDataPiece,
): SynchronizationContext(syncDataPiece) {
    init {
        childSynchronizationContexts.forEach {
            require(it.syncDataPiece == syncDataPiece) {
                "Child synchronization contexts need to have same sync data piece ($syncDataPiece)!"
            }
        }
    }

    override suspend fun synchronize(config: SynchronizationConfig) {
        childSynchronizationContexts.forEach { synchronizationContext ->
            delegateSynchronization(target = synchronizationContext, config = config)
        }
    }
}
