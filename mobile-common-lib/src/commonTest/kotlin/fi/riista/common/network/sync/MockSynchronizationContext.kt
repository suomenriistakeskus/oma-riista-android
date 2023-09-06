package fi.riista.common.network.sync

internal class MockSynchronizationContext(syncDataPiece: SyncDataPiece): SynchronizationContext(syncDataPiece) {
    override suspend fun synchronize(config: SynchronizationConfig) {
        // nop
    }
}
