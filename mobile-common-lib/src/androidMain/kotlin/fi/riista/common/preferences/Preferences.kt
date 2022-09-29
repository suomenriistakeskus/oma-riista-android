package fi.riista.common.preferences

import android.content.Context
import android.content.SharedPreferences
import fi.riista.common.ApplicationContextHolder
import fi.riista.common.logging.getLogger

actual class PlatformPreferences actual constructor(): Preferences {
    private var _sharedPrefs: SharedPreferences? = null
    private val sharedPrefs: SharedPreferences?
        get() {
            _sharedPrefs?.let { return it }

            val preferences = ApplicationContextHolder.applicationContext
                ?.getSharedPreferences("riistasdk_prefs", Context.MODE_PRIVATE)
                ?.also { prefs ->
                    _sharedPrefs = prefs
                }

            if (preferences == null) {
                // Will probably produce a lot of debug log if preferences is used more.
                // This is intentional.
                logger.w { "ApplicationContext missing, cannot access SharedPreferences" }
            }

            return preferences
        }

    actual override fun contains(key: String): Boolean {
        return sharedPrefs?.contains(key) ?: false
    }

    actual override fun putString(key: String, value: String) {
        sharedPrefs?.edit()
            ?.putString(key, value)
            ?.apply()
    }

    actual override fun getString(key: String, defaultValue: String?): String? {
        return safeGet(key, defaultValue) { preferences ->
            // getString already supports returning nullable defaultValues
            // -> no need to check key existence like with getInt or getBoolean
            preferences.getString(key, defaultValue)
        }
    }

    actual override fun putInt(key: String, value: Int) {
        sharedPrefs?.edit()
            ?.putInt(key, value)
            ?.apply()
    }

    actual override fun getInt(key: String, defaultValue: Int?): Int? {
        return safeGet(key, defaultValue) { preferences ->
            return if (preferences.contains(key)) {
                // key exists, given default value will not be used (unless someone just removed value)
                preferences.getInt(key, 0)
            } else {
                defaultValue
            }
        }
    }

    actual override fun putBoolean(key: String, value: Boolean) {
        sharedPrefs?.edit()
            ?.putBoolean(key, value)
            ?.apply()
    }

    actual override fun getBoolean(key: String, defaultValue: Boolean?): Boolean? {
        return safeGet(key, defaultValue) { preferences ->
            return if (preferences.contains(key)) {
                // key exists, given default value will not be used (unless someone just removed value)
                preferences.getBoolean(key, false)
            } else {
                defaultValue
            }
        }
    }

    actual override fun remove(key: String) {
        sharedPrefs?.edit()
            ?.remove(key)
            ?.apply()
    }

    /**
     * SharedPreferences get-functions will throw ClassCastException if the contained data is not
     * of correct type. Catches those, logs the issue and returns defaultValue in those cases.
     */
    private inline fun <reified T> safeGet(key: String, defaultValue: T?, unsafeGet: (SharedPreferences) -> T?): T? {
        try {
            sharedPrefs?.let { preferences ->
                return unsafeGet(preferences)
            }

            return defaultValue
        } catch (e : ClassCastException) {
            logger.w { "Preferences value for $key is not of type ${T::class.simpleName}" }
        }
        return defaultValue
    }

    companion object {
        private val logger by getLogger(Preferences::class)
    }
}
