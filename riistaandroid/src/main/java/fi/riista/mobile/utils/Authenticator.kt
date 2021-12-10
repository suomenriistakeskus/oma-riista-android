package fi.riista.mobile.utils

interface Authenticator {

    interface AuthCallback {
        fun onLoginSuccessful(userInfo: String?)
        fun onLoginFailed(httpStatusCode: Int)
    }

    abstract class AuthSuccessCallback : AuthCallback {
        // empty-body implementation
        final override fun onLoginFailed(httpStatusCode: Int) {}
    }

    suspend fun authenticate(username: String, password: String, callback: AuthCallback?)

    suspend fun reauthenticate() {
        reauthenticate(null)
    }

    suspend fun reauthenticate(callback: AuthCallback?)

}
