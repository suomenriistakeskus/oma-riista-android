package fi.riista.common.domain.userInfo

import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.domain.userInfo.repository.UserInformationRepository
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences

class CurrentUserContextProvider internal constructor(
    val userContext: UserContext,
): UsernameProvider by userContext {

    internal constructor(
        backendApiProvider: BackendApiProvider,
        groupHuntingAvailabilityResolver: GroupHuntingAvailabilityResolver,
        userInformationRepository: UserInformationRepository,
        preferences: Preferences,
    ): this(
        userContext = UserContext(
            backendApiProvider = backendApiProvider,
            groupHuntingAvailabilityResolver = groupHuntingAvailabilityResolver,
            userInformationRepository = userInformationRepository,
            preferences = preferences,
        )
    )

    suspend fun userLoggedIn(userInfo: UserInfoDTO) {
        userContext.userLoggedIn(userInfo)
    }

    suspend fun userLoggedOut() {
        userContext.userLoggedOut()
    }
}
