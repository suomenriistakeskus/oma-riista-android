package fi.riista.common.network

import fi.riista.common.dto.HunterNumberDTO
import fi.riista.common.dto.PersonWithHunterNumberDTO
import fi.riista.common.dto.UserInfoDTO
import fi.riista.common.groupHunting.dto.*
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.groupHunting.network.*
import fi.riista.common.huntingclub.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.huntingclub.dto.HuntingClubMembershipsDTO
import fi.riista.common.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.huntingclub.network.FetchHuntingClubMemberInvitations
import fi.riista.common.huntingclub.network.FetchHuntingClubMemberships
import fi.riista.common.huntingclub.network.HuntingClubMemberInvitationRequest
import fi.riista.common.logging.getLogger
import fi.riista.common.network.calls.FetchGroupHuntingClubsAndGroups
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.SearchPersonByHunterNumber
import fi.riista.common.network.cookies.CookieData
import fi.riista.common.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.poi.network.FetchPoiLocationGroups

class AuthenticationAwareBackendAPI internal constructor(
    internal val loginService: LoginService,
    internal val networkClient: NetworkClient,
) : BackendAPI {

    override fun getAllNetworkCookies(): List<CookieData> {
        return networkClient.cookiesStorage.addedCookies
    }

    override suspend fun login(username: String, password: String) : NetworkResponse<UserInfoDTO> {
        return loginService.login(username, password)
    }

    override suspend fun fetchGroupHuntingClubsAndHuntingGroups(): NetworkResponse<GroupHuntingClubsAndGroupsDTO> {
        return performRequest(FetchGroupHuntingClubsAndGroups())
    }

    override suspend fun fetchHuntingGroupMembers(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupMembersDTO> {
        return performRequest(FetchHuntingGroupMembers(huntingGroupId))
    }

    override suspend fun fetchHuntingGroupArea(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupAreaDTO> {
        return performRequest(FetchHuntingGroupArea(huntingGroupId))
    }

    override suspend fun fetchHuntingGroupStatus(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupStatusDTO> {
        return performRequest(FetchHuntingGroupStatus(huntingGroupId))
    }

    override suspend fun fetchHuntingGroupHuntingDays(huntingGroupId: HuntingGroupId): NetworkResponse<GroupHuntingDaysDTO> {
        return performRequest(FetchHuntingGroupHuntingDays(huntingGroupId))
    }

    override suspend fun createHuntingGroupHuntingDay(huntingDayDTO: GroupHuntingDayCreateDTO): NetworkResponse<GroupHuntingDayDTO> {
        return performRequest(CreateHuntingGroupHuntingDay(huntingDayDTO))
    }

    override suspend fun updateHuntingGroupHuntingDay(huntingDayDTO: GroupHuntingDayUpdateDTO): NetworkResponse<GroupHuntingDayDTO> {
        return performRequest(UpdateHuntingGroupHuntingDay(huntingDayDTO))
    }

    override suspend fun fetchHuntingGroupHuntingDayForDeer(huntingDayForDeerDTO: GroupHuntingDayForDeerDTO)
            : NetworkResponse<GroupHuntingDayDTO> {
        return performRequest(FetchHuntingDayForDeer(huntingDayForDeerDTO))
    }

    override suspend fun fetchGroupHuntingDiary(huntingGroupId: HuntingGroupId): NetworkResponse<GroupHuntingDiaryDTO> {
        return performRequest(FetchGroupHuntingDiary(huntingGroupId))
    }

    override suspend fun createGroupHuntingHarvest(harvest: GroupHuntingHarvestCreateDTO): NetworkResponse<GroupHuntingHarvestDTO> {
        return performRequest(CreateGroupHuntingHarvest(harvest))
    }

    override suspend fun createGroupHuntingObservation(observation: GroupHuntingObservationCreateDTO)
            : NetworkResponse<GroupHuntingObservationDTO> {
        return performRequest(CreateGroupHuntingObservation(observation))
    }

    override suspend fun updateGroupHuntingHarvest(harvest: GroupHuntingHarvestDTO): NetworkResponse<GroupHuntingHarvestDTO> {
        return performRequest(UpdateGroupHuntingHarvest(harvest))
    }

    override suspend fun updateGroupHuntingObservation(
        observation: GroupHuntingObservationUpdateDTO
    ): NetworkResponse<GroupHuntingObservationDTO> {
        return performRequest(UpdateGroupHuntingObservation(observation))
    }

    override suspend fun rejectGroupHuntingDiaryEntry(
        rejectDiaryEntryDTO: RejectDiaryEntryDTO
    ): NetworkResponse<Unit> {
        return performRequest(RejectGroupHuntingDiaryEntry(rejectDiaryEntryDTO))
    }

    override suspend fun searchPersonByHunterNumber(
        hunterNumberDTO: HunterNumberDTO
    ): NetworkResponse<PersonWithHunterNumberDTO> {
        return performRequest(SearchPersonByHunterNumber(hunterNumberDTO))
    }

    override suspend fun fetchPoiLocationGroups(externalId: String): NetworkResponse<PoiLocationGroupsDTO> {
        return performRequest(FetchPoiLocationGroups(externalId))
    }

    override suspend fun fetchHuntingClubMemberships(): NetworkResponse<HuntingClubMembershipsDTO> {
        return performRequest(FetchHuntingClubMemberships())
    }

    override suspend fun fetchHuntingClubMemberInvitations(): NetworkResponse<HuntingClubMemberInvitationsDTO> {
        return performRequest(FetchHuntingClubMemberInvitations())
    }

    override suspend fun acceptHuntingClubMemberInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): NetworkResponse<Unit> {
        return performRequest(HuntingClubMemberInvitationRequest.Accept(invitationId))
    }

    override suspend fun rejectHuntingClubMemberInvitation(
        invitationId: HuntingClubMemberInvitationId
    ): NetworkResponse<Unit> {
        return performRequest(HuntingClubMemberInvitationRequest.Reject(invitationId))
    }

    /**
     * Performs the given [request]. Will attempt to relogin if [request] response
     * status code is 401.
     */
    internal suspend fun <DataType> performRequest(
        request: NetworkRequest<DataType>
    ): NetworkResponse<DataType> {
        var response = networkClient.performRequest(request)
        if (response is NetworkResponse.ResponseError && response.statusCode == 401) {
            logger.d { "Got 401. Attempting relogin.." }

            if (loginService.relogin()) {
                logger.d { "Relogin succeeded, performing request again.." }
                response = networkClient.performRequest(request)
            }
        }

        return response
    }

    companion object {
        private val logger by getLogger(AuthenticationAwareBackendAPI::class)
    }

}
