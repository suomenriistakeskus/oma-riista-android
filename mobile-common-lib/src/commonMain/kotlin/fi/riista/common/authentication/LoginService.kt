package fi.riista.common.authentication

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.authentication.login.Login
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkResponse

class LoginService internal constructor(
    internal val networkClient: NetworkClient,
    val currentUserContextProvider: CurrentUserContextProvider,
) {
    private val loginCredentialsHolder = AtomicReference<LoginCredentials?>(null)
    private var loginCredentials: LoginCredentials?
        get() = loginCredentialsHolder.value
        set(value) {
            loginCredentialsHolder.value = value
        }

    suspend fun login(username: String, password: String): NetworkResponse<UserInfoDTO> {
        return networkClient.performRequest(Login(username, password)).also { response ->
            response.onSuccess { _, userInfo ->
                setLoginCredentials(username, password)
                currentUserContextProvider.userLoggedIn(userInfo = userInfo.typed)
            }

            response.onError { statusCode, _ ->
                if (statusCode == 401) {
                    // user not authorized any more i.e. same as logging out
                    logout()
                }
            }
        }
    }

    /**
     * Attempts to logging in again using stored credentials.
     *
     * Returns true if login succeeds and false if it fails for any reason (401, network error etc)
     */
    suspend fun relogin(): Boolean {
        val (username, password) = loginCredentials ?: kotlin.run {
            logger.i { "Refusing to relogin, no credentials" }
            return false
        }

        return login(username, password).let { response ->
            response.onSuccess { _, userInfo ->
                currentUserContextProvider.userLoggedIn(userInfo = userInfo.typed)
                return@let true
            }

            // todo: consider more fine grained response than true/false
            // - it is possible that relogin fails because of e.g. a network error. It should
            //   not cause everything to be lost but instead user should have a possibility
            //   to try again.

            false
        }
    }

    fun setLoginCredentials(username: String, password: String) {
        loginCredentials = LoginCredentials(username, password)
    }

    fun logout() {
        loginCredentials = null
        currentUserContextProvider.userLoggedOut()
    }


    private data class LoginCredentials(val username: String, val password: String)

    companion object {
        private val logger by getLogger(LoginService::class)
    }
}
