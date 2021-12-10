package fi.riista.common.userInfo

import fi.riista.common.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.model.HunterNumber
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider

object CurrentUserContextProviderFactory {
    fun createMocked(
        backendAPI: BackendAPI = BackendAPIMock(),
        groupHuntingEnabledForAll: Boolean = false,
        groupHuntingEnabledForHunters: List<HunterNumber> = listOf()
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
        )
    }
}