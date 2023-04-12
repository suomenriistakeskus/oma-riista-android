package fi.riista.mobile.utils

import fi.riista.common.authentication.LoginService

interface Authenticator {

    interface AuthCallback {
        fun onLoginSuccessful(userInfo: String?)
        fun onLoginFailed(httpStatusCode: Int)
    }

    abstract class AuthSuccessCallback : AuthCallback {
        // empty-body implementation
        final override fun onLoginFailed(httpStatusCode: Int) {}
    }

    suspend fun authenticate(
        username: String,
        password: String,
        timeoutSeconds: Int,
        callback: AuthCallback?,
    )

    suspend fun reauthenticate() {
        reauthenticate(callback = null, timeoutSeconds = DEFAULT_AUTHENTICATION_TIMEOUT_SECONDS)
    }

    suspend fun reauthenticate(callback: AuthCallback?, timeoutSeconds: Int)

    companion object {
        const val DEFAULT_AUTHENTICATION_TIMEOUT_SECONDS = LoginService.DEFAULT_LOGIN_TIMEOUT_SECONDS
    }
}
