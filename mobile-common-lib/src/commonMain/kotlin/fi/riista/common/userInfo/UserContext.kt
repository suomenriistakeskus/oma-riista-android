package fi.riista.common.userInfo

import fi.riista.common.dto.UserInfoDTO
import fi.riista.common.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.groupHunting.GroupHuntingContext
import fi.riista.common.huntingclub.HuntingClubsContext
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.reactive.Observable

sealed class LoginStatus {
    class NotLoggedIn: LoginStatus()
    // class LoggingIn: LoginStatus() // todo: consider adding
    class LoggedIn(val userInfoDTO: UserInfoDTO): LoginStatus()
    // class LoginError: LoginStatus() // todo: consider adding

    val userInfo: UserInfoDTO?
        get() {
            return when (this) {
                is LoggedIn -> userInfoDTO
                else -> null
            }
        }
}

class UserContext internal constructor(
    backendApiProvider: BackendApiProvider,
    private val groupHuntingAvailabilityResolver: GroupHuntingAvailabilityResolver,
): BackendApiProvider by backendApiProvider {
    val loginStatus = Observable<LoginStatus>(LoginStatus.NotLoggedIn())

    val userInfo: UserInfoDTO?
        get() {
            return loginStatus.value.userInfo
        }

    val groupHuntingContext by lazy {
        GroupHuntingContext(backendApiProvider = this,
                            isGroupHuntingAvailable = { isGroupHuntingAvailable() },
        )
    }

    val huntingClubsContext by lazy {
        HuntingClubsContext(backendApiProvider = this)
    }

    private fun isGroupHuntingAvailable(): Boolean {
        return groupHuntingAvailabilityResolver
            .isGroupHuntingFunctionalityEnabledFor(userInfo?.hunterNumber)
    }

    fun userLoggedIn(userInfo: UserInfoDTO) {
        loginStatus.set(LoginStatus.LoggedIn(userInfo))
    }

    fun userLoggedOut() {
        groupHuntingContext.clear()
        huntingClubsContext.clear()
        loginStatus.set(LoginStatus.NotLoggedIn())
    }
}
