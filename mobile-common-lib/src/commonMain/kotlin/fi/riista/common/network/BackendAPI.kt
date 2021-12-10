package fi.riista.common.network

import fi.riista.common.dto.HunterNumberDTO
import fi.riista.common.dto.PersonWithHunterNumberDTO
import fi.riista.common.dto.UserInfoDTO
import fi.riista.common.groupHunting.dto.*
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.huntingclub.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.huntingclub.dto.HuntingClubMembershipsDTO
import fi.riista.common.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.cookies.CookieData
import fi.riista.common.poi.dto.PoiLocationGroupsDTO

interface BackendAPI {
    fun getAllNetworkCookies(): List<CookieData>

    suspend fun login(username: String, password: String) : NetworkResponse<UserInfoDTO>

    suspend fun fetchGroupHuntingClubsAndHuntingGroups(): NetworkResponse<GroupHuntingClubsAndGroupsDTO>

    suspend fun fetchHuntingGroupMembers(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupMembersDTO>

    suspend fun fetchHuntingGroupArea(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupAreaDTO>

    suspend fun fetchHuntingGroupStatus(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupStatusDTO>

    suspend fun fetchHuntingGroupHuntingDays(huntingGroupId: HuntingGroupId): NetworkResponse<GroupHuntingDaysDTO>
    suspend fun createHuntingGroupHuntingDay(huntingDayDTO: GroupHuntingDayCreateDTO): NetworkResponse<GroupHuntingDayDTO>
    suspend fun updateHuntingGroupHuntingDay(huntingDayDTO: GroupHuntingDayUpdateDTO): NetworkResponse<GroupHuntingDayDTO>
    suspend fun fetchHuntingGroupHuntingDayForDeer(huntingDayForDeerDTO: GroupHuntingDayForDeerDTO)
            : NetworkResponse<GroupHuntingDayDTO>

    suspend fun fetchGroupHuntingDiary(huntingGroupId: HuntingGroupId): NetworkResponse<GroupHuntingDiaryDTO>

    suspend fun createGroupHuntingHarvest(harvest: GroupHuntingHarvestCreateDTO): NetworkResponse<GroupHuntingHarvestDTO>
    suspend fun createGroupHuntingObservation(observation: GroupHuntingObservationCreateDTO): NetworkResponse<GroupHuntingObservationDTO>
    suspend fun updateGroupHuntingHarvest(harvest: GroupHuntingHarvestDTO): NetworkResponse<GroupHuntingHarvestDTO>
    suspend fun updateGroupHuntingObservation(observation: GroupHuntingObservationUpdateDTO): NetworkResponse<GroupHuntingObservationDTO>

    suspend fun rejectGroupHuntingDiaryEntry(rejectDiaryEntryDTO: RejectDiaryEntryDTO): NetworkResponse<Unit>

    suspend fun searchPersonByHunterNumber(hunterNumberDTO: HunterNumberDTO): NetworkResponse<PersonWithHunterNumberDTO>

    suspend fun fetchPoiLocationGroups(externalId: String): NetworkResponse<PoiLocationGroupsDTO>

    suspend fun fetchHuntingClubMemberships(): NetworkResponse<HuntingClubMembershipsDTO>
    suspend fun fetchHuntingClubMemberInvitations(): NetworkResponse<HuntingClubMemberInvitationsDTO>
    suspend fun acceptHuntingClubMemberInvitation(invitationId: HuntingClubMemberInvitationId): NetworkResponse<Unit>
    suspend fun rejectHuntingClubMemberInvitation(invitationId: HuntingClubMemberInvitationId): NetworkResponse<Unit>
}

