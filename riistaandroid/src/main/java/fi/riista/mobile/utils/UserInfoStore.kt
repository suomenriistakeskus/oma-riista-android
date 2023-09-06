package fi.riista.mobile.utils

import fi.riista.mobile.models.user.UserInfo

interface UserInfoStore {

    /**
     * Get stored user information.
     *
     * @return Latest user info or null
     */
    fun getUserInfo(): UserInfo?

    /**
     * Get stored user information (if any) in json format.
     *
     * @return Latest user info or null
     */
    fun getUserInfoJson(): String?

    fun getUsernameOrEmpty(): String = getUserInfo()?.username.orEmpty()

    /**
     * Store user information from login reply.
     *
     * @param jsonData Login response string or null
     */
    fun setUserInfo(jsonData: String?)

}
