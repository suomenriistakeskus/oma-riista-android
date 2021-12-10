package fi.riista.mobile.sync

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.preference.PreferenceManager
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_CONTEXT_NAME
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SyncConfig @Inject constructor(@Named(APPLICATION_CONTEXT_NAME) private val context: Context) {

    internal var syncMode: SyncMode
        get() {
            val prefs = getSharedPreferences()

            if (prefs.contains(SYNC_MODE_PREF_KEY)) {
                val syncMode = prefs.getInt(SYNC_MODE_PREF_KEY, 0)

                if (SyncMode.values().size > syncMode) {
                    return SyncMode.values()[syncMode]
                }
            }
            return SyncMode.SYNC_AUTOMATIC
        }
        set(mode) {
            val editor: Editor = getSharedPreferences().edit()
            editor.putInt(SYNC_MODE_PREF_KEY, mode.ordinal)
            editor.apply()
        }

    fun isAutomatic() = syncMode === SyncMode.SYNC_AUTOMATIC

    private fun getSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val SYNC_MODE_PREF_KEY = "SyncMode"
    }
}
