package fi.riista.common.domain.groupHunting

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.dto.toOrganization
import fi.riista.common.domain.groupHunting.dto.GroupHuntingClubsAndGroupsDTO
import fi.riista.common.domain.groupHunting.dto.createPermit
import fi.riista.common.domain.groupHunting.model.HuntingGroup
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.dto.toLocalizedString
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface GroupHuntingClubContextsProvider: DataFetcher {
    val clubContexts: List<GroupHuntingClubContext>
}

internal class GroupHuntingClubContextsFromNetworkProvider(
    private val backendApiProvider: BackendApiProvider,
) : GroupHuntingClubContextsProvider,
    NetworkDataFetcher<GroupHuntingClubsAndGroupsDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _clubContexts = AtomicReference<List<GroupHuntingClubContext>?>(null)
    override val clubContexts: List<GroupHuntingClubContext>
        get() = _clubContexts.value ?: listOf()

    internal fun clear() {
        _clubContexts.set(null)
        loadStatus.set(LoadStatus.NotLoaded())
    }

    override suspend fun fetchFromNetwork(): NetworkResponse<GroupHuntingClubsAndGroupsDTO> =
        backendAPI.fetchGroupHuntingClubsAndHuntingGroups()

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out GroupHuntingClubsAndGroupsDTO>
    ) {
        val createdClubContexts = parseClubsAndGroups(responseData.typed)
            .takeIf { it.isNotEmpty() }
            ?.entries?.map { (club, huntingGroups) ->
                GroupHuntingClubContext(
                        backendApiProvider = backendApiProvider,
                        club = club,
                        huntingGroups = huntingGroups
                )
            }
            ?.takeIf { it.isNotEmpty() }

        _clubContexts.set(createdClubContexts)
    }

    override fun handleError401() {
        _clubContexts.set(null)
    }

    private fun parseClubsAndGroups(
        clubsAndGroups: GroupHuntingClubsAndGroupsDTO
    ): Map<Organization, List<HuntingGroup>> {
        val clubsById: Map<OrganizationId, Organization> =
            clubsAndGroups.clubs.associate { organizationDto ->
                organizationDto.id to organizationDto.toOrganization()
            }

        val groupsByClub = mutableMapOf<Organization, MutableList<HuntingGroup>>()
        clubsAndGroups.groups.forEach { huntingGroupDTO ->
            val club = clubsById[huntingGroupDTO.clubId]
            if (club == null) {
                logger.w { "Couldn't find a club for hunting group ${huntingGroupDTO.id} " +
                        "(clubId = ${huntingGroupDTO.clubId})" }
                return@forEach
            }

            val clubGroups = groupsByClub.getOrPut(key = club) { mutableListOf() }
            clubGroups.add(
                    HuntingGroup(
                            id = huntingGroupDTO.id,
                            club = club,
                            speciesCode = huntingGroupDTO.speciesCode,
                            permit = huntingGroupDTO.createPermit(),
                            huntingYear = huntingGroupDTO.huntingYear,
                            name = huntingGroupDTO.name.toLocalizedString()
                    )
            )
        }

        return groupsByClub
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(GroupHuntingClubContextsFromNetworkProvider::class)
    }
}