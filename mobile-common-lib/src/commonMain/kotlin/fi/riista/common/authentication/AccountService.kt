package fi.riista.common.authentication

import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.LoginStatus
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences

class AccountService internal constructor(
    private val backendApiProvider: BackendApiProvider,
    private val currentUserContextProvider: CurrentUserContextProvider,
    private val preferences: Preferences,
) {
    /**
     * The timestamp (if any) when user has requested account unregistration.
     */
    val userAccountUnregistrationRequestDatetime: LocalDateTime?
        get() {
            val unregisterRequestedDateTime = preferences.getString(USER_UNREGISTRATION_REQUEST_TIME)
            return unregisterRequestedDateTime?.toLocalDateTime()
        }

    internal fun initialize() {
        // don't notify immediately, only when login status changes. This allows keeping
        // current flag
        currentUserContextProvider.userContext.loginStatus.bind { loginStatus ->
            if (loginStatus is LoginStatus.LoggedIn) {
                saveUnregistrationRequestTime(
                    unregistrationRequestTime = loginStatus.userInformation.unregisterRequestedTime?.toStringISO8601()
                )
            }
        }
    }

    suspend fun unregisterAccount(): LocalDateTime? {
        return backendApiProvider.backendAPI.unregisterAccount()
            .transformSuccessData { _, data ->
                val unregistrationRequestTime = data.typed
                saveUnregistrationRequestTime(unregistrationRequestTime = unregistrationRequestTime)

                unregistrationRequestTime.toLocalDateTime()
            }
    }

    suspend fun cancelUnregisterAccount(): Boolean {
        val response = backendApiProvider.backendAPI.cancelUnregisterAccount()
        response.onSuccessWithoutData {
            clearUnregistrationRequestTime()
            return true
        }

        return false
    }

    private fun saveUnregistrationRequestTime(unregistrationRequestTime: LocalDateTimeDTO?) {
        if (unregistrationRequestTime != null) {
            logger.v { "user has requested unregistration at $unregistrationRequestTime" }
            preferences.putString(USER_UNREGISTRATION_REQUEST_TIME, unregistrationRequestTime)
        } else {

            clearUnregistrationRequestTime()
        }
    }

    private fun clearUnregistrationRequestTime() {
        if (preferences.contains(USER_UNREGISTRATION_REQUEST_TIME)) {
            logger.v { "clearing user unregistration request" }
        } else {
            logger.v { "user has NOT requested unregistration" }
        }
        preferences.remove(USER_UNREGISTRATION_REQUEST_TIME)
    }

    companion object {
        private val logger by getLogger(AccountService::class)

        internal const val USER_UNREGISTRATION_REQUEST_TIME = "as_unregistration_request_time"
    }
}
