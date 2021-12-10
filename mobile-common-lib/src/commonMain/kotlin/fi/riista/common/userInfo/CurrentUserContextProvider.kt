package fi.riista.common.userInfo

import fi.riista.common.dto.UserInfoDTO
import fi.riista.common.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.network.BackendApiProvider

class CurrentUserContextProvider internal constructor(
    backendApiProvider: BackendApiProvider,
    groupHuntingAvailabilityResolver: GroupHuntingAvailabilityResolver,
) {
    val userContext: UserContext = UserContext(
            backendApiProvider, groupHuntingAvailabilityResolver)

    fun userLoggedIn(userInfo: UserInfoDTO) {
        userContext.userLoggedIn(userInfo)
    }

    fun userLoggedOut() {
        userContext.userLoggedOut()
    }
}