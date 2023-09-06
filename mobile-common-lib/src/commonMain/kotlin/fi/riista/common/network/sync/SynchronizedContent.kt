package fi.riista.common.network.sync

sealed class SynchronizedContent {
    class SelectedLevel(val synchronizationLevel: SynchronizationLevel): SynchronizedContent()
    class SelectedData(val syncDataPieces: List<SyncDataPiece>): SynchronizedContent()

    val synchronizationDataPieces: List<SyncDataPiece>
        get() {
            return when (this) {
                is SelectedData -> syncDataPieces
                is SelectedLevel -> SyncDataPiece.getPiecesIncludedInSynchronizationLevel(synchronizationLevel)
            }
        }
}
