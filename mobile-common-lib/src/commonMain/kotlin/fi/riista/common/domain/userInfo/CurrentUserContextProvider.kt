package fi.riista.common.domain.userInfo

import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

class CurrentUserContextProvider internal constructor(
    backendApiProvider: BackendApiProvider,
    groupHuntingAvailabilityResolver: GroupHuntingAvailabilityResolver,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    commonFileProvider: CommonFileProvider,
) {
    val userContext: UserContext = UserContext(
            backendApiProvider, groupHuntingAvailabilityResolver, preferences, localDateTimeProvider, commonFileProvider)

    fun userLoggedIn(userInfo: UserInfoDTO) {
        userContext.userLoggedIn(userInfo)
    }

    fun userLoggedOut() {
        userContext.userLoggedOut()
    }
}
