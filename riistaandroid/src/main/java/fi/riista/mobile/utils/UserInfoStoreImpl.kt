package fi.riista.mobile.utils

import android.content.Context
import android.preference.PreferenceManager
import fi.riista.mobile.models.user.UserInfo

// This class abstracts away the implementation detail that UserInfo is stored in shared preferences.
class UserInfoStoreImpl(private val appContext: Context, private val userInfoConverter: UserInfoConverter)
    : UserInfoStore {

    /**
     * Get stored user information.
     *
     * @return Latest user info or null
     */
    override fun getUserInfo(): UserInfo? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val jsonData: String = prefs?.getString(USERINFO_KEY, USERINFO_DEFAULT) ?: USERINFO_DEFAULT

        return userInfoConverter.fromJson(jsonData)
    }

    /**
     * Store user information from login reply.
     * If reply data is null then remove stored key and value.
     *
     * @param jsonData Login response string or null
     */
    override fun setUserInfo(jsonData: String?) {
        if (jsonData != null && jsonData.isNotEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(appContext)?.let { prefs ->
                val editor = prefs.edit()
                editor.putString(USERINFO_KEY, jsonData)
                editor.apply()
            }
        } else {
            // Clear stored data.
            PreferenceManager.getDefaultSharedPreferences(appContext)?.let { prefs ->
                val editor = prefs.edit()
                editor.remove(USERINFO_KEY)
                editor.apply()
            }
        }
    }

    companion object {
        private const val USERINFO_KEY = "userInfo"
        private const val USERINFO_DEFAULT = ""
    }
}
