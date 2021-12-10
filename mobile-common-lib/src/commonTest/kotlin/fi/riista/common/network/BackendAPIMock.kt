package fi.riista.common.network

import fi.riista.common.dto.HunterNumberDTO
import fi.riista.common.dto.MockUserInfo
import fi.riista.common.dto.PersonWithHunterNumberDTO
import fi.riista.common.dto.UserInfoDTO
import fi.riista.common.groupHunting.MockGroupHuntingData
import fi.riista.common.groupHunting.dto.*
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.huntingclub.MockHuntingClubData
import fi.riista.common.huntingclub.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.huntingclub.dto.HuntingClubMembershipsDTO
import fi.riista.common.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.network.cookies.CookieData
import fi.riista.common.poi.MockPoiData
import fi.riista.common.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.util.deserializeFromJson

data class MockResponse(
    val statusCode: Int? = 200,
    val responseData: String? = null
) {
    companion object {
        fun success(responseData: String?) = MockResponse(responseData = responseData)
        fun successWithNoData(statusCode: Int?) = MockResponse(statusCode = statusCode)
        fun error(statusCode: Int?) = MockResponse(statusCode = statusCode)
    }
}

@Suppress("MemberVisibilityCanBePrivate")
open class BackendAPIMock(
    var loginResponse: MockResponse = MockResponse.success(MockUserInfo.Pentti),
    var groupHuntingClubsAndGroupsResponse: MockResponse = MockResponse.success(MockGroupHuntingData.OneClub),
    var groupHuntingGroupMembersResponse: MockResponse = MockResponse.success(MockGroupHuntingData.Members),
    var groupHuntingGroupHuntingAreaResponse: MockResponse = MockResponse.success(MockGroupHuntingData.HuntingArea),
    var groupHuntingGroupStatusResponse: MockResponse = MockResponse.success(MockGroupHuntingData.GroupStatus),
    var groupHuntingGroupHuntingDaysResponse: MockResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDays),
    var groupHuntingCreateHuntingDayResponse: MockResponse = MockResponse.success(MockGroupHuntingData.CreatedGroupHuntingDay),
    var groupHuntingUpdateHuntingDayResponse: MockResponse = MockResponse.success(MockGroupHuntingData.UpdatedFirstHuntingDay),
    var groupHuntingHuntingDayForDeerResponse: MockResponse = MockResponse.success(MockGroupHuntingData.DeerHuntingDay),
    var groupHuntingGameDiaryResponse: MockResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDiary),
    var groupHuntingCreateHarvestResponse: MockResponse = MockResponse.success(MockGroupHuntingData.CreatedHarvest),
    var groupHuntingAcceptHarvestResponse: MockResponse = MockResponse.success(MockGroupHuntingData.AcceptedHarvest),
    var groupHuntingAcceptObservationResponse: MockResponse = MockResponse.success(MockGroupHuntingData.AcceptedObservation),
    var groupHuntingRejectDiaryEntryResponse: MockResponse = MockResponse.successWithNoData(204),
    var groupHuntingCreateGroupHuntingObservationResponse: MockResponse = MockResponse.success(MockGroupHuntingData.AcceptedObservation),
    var searchPersonByHunterNumberResponse: MockResponse = MockResponse.success(MockGroupHuntingData.PersonWithHunterNumber88888888),
    var huntingClubMembershipResponse: MockResponse = MockResponse.success(MockHuntingClubData.HuntingClubMemberships),
    var huntingClubMemberInvitationsResponse: MockResponse = MockResponse.success(MockHuntingClubData.HuntingClubMemberInvitations),
    var acceptHuntingClubMemberInvitationResponse: MockResponse = MockResponse.successWithNoData(204),
    var rejectHuntingClubMemberInvitationResponse: MockResponse = MockResponse.successWithNoData(204),
    var poiLocationGroupsResponse: MockResponse = MockResponse.success(MockPoiData.PoiLocationGroups)
) : BackendAPI {
    private val callCounts: MutableMap<String, Int> = mutableMapOf()
    private val callParameters: MutableMap<String, Any> = mutableMapOf()

    override fun getAllNetworkCookies(): List<CookieData> = listOf()

    override suspend fun login(username: String, password: String): NetworkResponse<UserInfoDTO> {
        increaseCallCount(::login.name)
        return respond(loginResponse)
    }

    override suspend fun fetchGroupHuntingClubsAndHuntingGroups(): NetworkResponse<GroupHuntingClubsAndGroupsDTO> {
        increaseCallCount(::fetchGroupHuntingClubsAndHuntingGroups.name)
        return respond(groupHuntingClubsAndGroupsResponse)
    }

    override suspend fun fetchHuntingGroupMembers(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupMembersDTO> {
        increaseCallCount(::fetchHuntingGroupMembers.name)
        return respond(groupHuntingGroupMembersResponse)
    }

    override suspend fun fetchHuntingGroupArea(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupAreaDTO> {
        increaseCallCount(::fetchHuntingGroupArea.name)
        return respond(groupHuntingGroupHuntingAreaResponse)
    }

    override suspend fun fetchHuntingGroupStatus(huntingGroupId: HuntingGroupId): NetworkResponse<HuntingGroupStatusDTO> {
        increaseCallCount(::fetchHuntingGroupStatus.name)
        return respond(groupHuntingGroupStatusResponse)
    }

    override suspend fun fetchHuntingGroupHuntingDays(huntingGroupId: HuntingGroupId): NetworkResponse<GroupHuntingDaysDTO> {
        increaseCallCount(::fetchHuntingGroupHuntingDays.name)
        return respond(groupHuntingGroupHuntingDaysResponse)
    }

    override suspend fun createHuntingGroupHuntingDay(huntingDayDTO: GroupHuntingDayCreateDTO): NetworkResponse<GroupHuntingDayDTO> {
        increaseCallCount(::createHuntingGroupHuntingDay.name)
        callParameters[::createHuntingGroupHuntingDay.name] = huntingDayDTO
        return respond(groupHuntingCreateHuntingDayResponse)
    }

    override suspend fun updateHuntingGroupHuntingDay(huntingDayDTO: GroupHuntingDayUpdateDTO): NetworkResponse<GroupHuntingDayDTO> {
        increaseCallCount(::updateHuntingGroupHuntingDay.name)
        callParameters[::updateHuntingGroupHuntingDay.name] = huntingDayDTO
        return respond(groupHuntingUpdateHuntingDayResponse)
    }

    override suspend fun fetchHuntingGroupHuntingDayForDeer(huntingDayForDeerDTO: GroupHuntingDayForDeerDTO)
            : NetworkResponse<GroupHuntingDayDTO> {
        increaseCallCount(::fetchHuntingGroupHuntingDayForDeer.name)
        callParameters[::fetchGroupHuntingDiary.name] = huntingDayForDeerDTO
        return respond(groupHuntingHuntingDayForDeerResponse)
    }

    override suspend fun fetchGroupHuntingDiary(huntingGroupId: HuntingGroupId): NetworkResponse<GroupHuntingDiaryDTO> {
        increaseCallCount(::fetchGroupHuntingDiary.name)
        return respond(groupHuntingGameDiaryResponse)
    }

    override suspend fun createGroupHuntingHarvest(harvest: GroupHuntingHarvestCreateDTO): NetworkResponse<GroupHuntingHarvestDTO> {
        increaseCallCount(::createGroupHuntingHarvest.name)
        callParameters[::createGroupHuntingHarvest.name] = harvest
        return respond(groupHuntingCreateHarvestResponse)
    }

    override suspend fun createGroupHuntingObservation(observation: GroupHuntingObservationCreateDTO): NetworkResponse<GroupHuntingObservationDTO> {
        increaseCallCount(::createGroupHuntingObservation.name)
        callParameters[::createGroupHuntingObservation.name] = observation
        return respond(groupHuntingCreateGroupHuntingObservationResponse)
    }

    override suspend fun updateGroupHuntingHarvest(harvest: GroupHuntingHarvestDTO): NetworkResponse<GroupHuntingHarvestDTO> {
        increaseCallCount(::updateGroupHuntingHarvest.name)
        callParameters[::updateGroupHuntingHarvest.name] = harvest
        return respond(groupHuntingAcceptHarvestResponse)
    }

    override suspend fun updateGroupHuntingObservation(observation: GroupHuntingObservationUpdateDTO): NetworkResponse<GroupHuntingObservationDTO> {
        increaseCallCount(::updateGroupHuntingObservation.name)
        callParameters[::updateGroupHuntingObservation.name] = observation
        return respond(groupHuntingAcceptObservationResponse)
    }

    override suspend fun rejectGroupHuntingDiaryEntry(rejectDiaryEntryDTO: RejectDiaryEntryDTO): NetworkResponse<Unit> {
        increaseCallCount(::rejectGroupHuntingDiaryEntry.name)
        callParameters[::rejectGroupHuntingDiaryEntry.name] = rejectDiaryEntryDTO
        return respond(groupHuntingRejectDiaryEntryResponse)
    }

    override suspend fun searchPersonByHunterNumber(hunterNumberDTO: HunterNumberDTO): NetworkResponse<PersonWithHunterNumberDTO> {
        increaseCallCount(::searchPersonByHunterNumber.name)
        callParameters[::searchPersonByHunterNumber.name] = hunterNumberDTO
        return if (hunterNumberDTO == "88888888") {
            respond(searchPersonByHunterNumberResponse)
        } else {
            respond(MockResponse.error(404))
        }
    }

    override suspend fun fetchPoiLocationGroups(externalId: String): NetworkResponse<PoiLocationGroupsDTO> {
        increaseCallCount(::fetchPoiLocationGroups.name)
        return respond(poiLocationGroupsResponse)
    }

    override suspend fun fetchHuntingClubMemberships(): NetworkResponse<HuntingClubMembershipsDTO> {
        increaseCallCount(::fetchHuntingClubMemberships.name)
        return respond(huntingClubMembershipResponse)
    }

    override suspend fun fetchHuntingClubMemberInvitations(): NetworkResponse<HuntingClubMemberInvitationsDTO> {
        increaseCallCount(::fetchHuntingClubMemberInvitations.name)
        return respond(huntingClubMemberInvitationsResponse)
    }

    override suspend fun acceptHuntingClubMemberInvitation(invitationId: HuntingClubMemberInvitationId): NetworkResponse<Unit> {
        increaseCallCount(::acceptHuntingClubMemberInvitation.name)
        callParameters[::acceptHuntingClubMemberInvitation.name] = invitationId
        return respond(acceptHuntingClubMemberInvitationResponse)
    }

    override suspend fun rejectHuntingClubMemberInvitation(invitationId: HuntingClubMemberInvitationId): NetworkResponse<Unit> {
        increaseCallCount(::rejectHuntingClubMemberInvitation.name)
        callParameters[::rejectHuntingClubMemberInvitation.name] = invitationId
        return respond(rejectHuntingClubMemberInvitationResponse)
    }

    /**
     * Returns how many times a BackendAPI function of this mock class has been called.
     * Usage:
     *
     * val backendAPIMock = BackendAPIMock()
     * assertEquals(1, backendAPIMock.callCount(backendAPIMock::fetchHuntingGroupHuntingDayForDeer.name))
     */
    fun callCount(methodName: String): Int {
        return (callCounts[methodName] ?: 0)
    }

    /**
     * Returns the last parameter the given method was called with.
     *
     * Only last call is saved.
     */
    fun callParameter(methodName: String): Any? {
        return callParameters[methodName]
    }

    private fun increaseCallCount(methodName: String) {
        callCounts[methodName] = callCount(methodName) + 1
    }

    protected inline fun <reified T> respond(response: MockResponse): NetworkResponse<T> {
        return if (response.statusCode != null) {
            if (response.statusCode in 200..299) {
                if (response.responseData != null) {
                    NetworkResponse.Success(
                            statusCode = response.statusCode,
                            data = NetworkResponseData(
                                    raw = response.responseData,
                                    typed = response.responseData.deserializeFromJson()!!
                            )
                    )
                } else {
                    NetworkResponse.SuccessWithNoData(statusCode = response.statusCode)
                }
            } else {
                NetworkResponse.ResponseError(response.statusCode)
            }
        } else {
            NetworkResponse.NetworkError(exception = null)
        }
    }
}

