package fi.riista.common.domain.userInfo

import fi.riista.common.domain.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.userInfo.repository.UserInformationRepository
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences

object CurrentUserContextProviderFactory {
    internal fun createMocked(
        backendAPI: BackendAPI = BackendAPIMock(),
        groupHuntingEnabledForAll: Boolean = false,
        groupHuntingEnabledForHunters: List<HunterNumber> = listOf(),
        userInformationRepository: UserInformationRepository = MockUserInformationRepository(),
        preferences: Preferences = MockPreferences(),
    ): CurrentUserContextProvider {
        val backendApiProvider: BackendApiProvider = object : BackendApiProvider {
            override val backendAPI: BackendAPI = backendAPI
        }
        val groupHuntingAvailabilityResolver = object : GroupHuntingAvailabilityResolver {
            override fun isGroupHuntingFunctionalityEnabledFor(hunterNumber: HunterNumber?): Boolean {
                return groupHuntingEnabledForAll || groupHuntingEnabledForHunters.contains(hunterNumber)
            }

        }

        return CurrentUserContextProvider(
            backendApiProvider = backendApiProvider,
            groupHuntingAvailabilityResolver = groupHuntingAvailabilityResolver,
            userInformationRepository = userInformationRepository,
            preferences = preferences,
        )
    }
}
