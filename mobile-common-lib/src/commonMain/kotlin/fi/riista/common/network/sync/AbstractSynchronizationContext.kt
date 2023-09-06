package fi.riista.common.network.sync

import fi.riista.common.model.LocalDateTime
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

internal abstract class AbstractSynchronizationContext(
    protected val preferences: Preferences,
    protected val localDateTimeProvider: LocalDateTimeProvider,
    syncDataPiece: SyncDataPiece,
) : SynchronizationContext(syncDataPiece) {
    protected fun getLastSynchronizationTimeStamp(suffix: String? = null): LocalDateTime? {
        return preferences.getString(syncDataPiece.timestampKey.addSuffix(suffix))?.let { prevSyncTime ->
            LocalDateTime.parseLocalDateTime(prevSyncTime)
        }
    }

    protected fun saveLastSynchronizationTimeStamp(timestamp: LocalDateTime, suffix: String? = null) {
        preferences.putString(syncDataPiece.timestampKey.addSuffix(suffix), timestamp.toStringISO8601())
    }
}

private fun String.addSuffix(suffix: String?): String {
    if (suffix != null) {
        return "$this-$suffix"
    }
    return this
}
