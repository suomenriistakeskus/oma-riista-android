package fi.riista.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class CredentialsStoreImpl(private val appContext: Context) : CredentialsStore {

    override fun get(): Credentials? {
        val sharedPrefs = getCredentialsPreferences()

        try {
            val username: String? = sharedPrefs.getString(PREFS_LOGIN_USERNAME_KEY, null)
            val password: String? = sharedPrefs.getString(PREFS_LOGIN_PASSWORD_KEY, null)

            if (username != null && password != null) {
                return Credentials(username, password)
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    override fun save(username: String?, password: String?) {
        val editor: SharedPreferences.Editor = getCredentialsPreferences().edit()
        editor.putString(PREFS_LOGIN_USERNAME_KEY, username)
        editor.putString(PREFS_LOGIN_PASSWORD_KEY, password)
        editor.apply()
    }

    override fun clear() {
        if (isCredentialsSaved()) {
            val editor: SharedPreferences.Editor = getCredentialsPreferences().edit()
            editor.remove(PREFS_LOGIN_USERNAME_KEY)
            editor.remove(PREFS_LOGIN_PASSWORD_KEY)
            editor.remove("")
            editor.apply()
        }
    }

    private fun getCredentialsPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    companion object {
        private const val PREFS_LOGIN_PASSWORD_KEY = "password"
        private const val PREFS_LOGIN_USERNAME_KEY = "username"
    }
}
