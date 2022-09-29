package fi.riista.common.network

import fi.riista.common.authentication.LoginService
import fi.riista.common.domain.dto.HunterNumberDTO
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.groupHunting.dto.*
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.network.*
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingControl.sync.network.*
import fi.riista.common.domain.huntingclub.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.domain.huntingclub.dto.HuntingClubMembershipsDTO
import fi.riista.common.domain.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.domain.huntingclub.network.FetchHuntingClubMemberInvitations
import fi.riista.common.domain.huntingclub.network.FetchHuntingClubMemberships
import fi.riista.common.domain.huntingclub.network.HuntingClubMemberInvitationRequest
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.metadata.network.FetchObservationMetadata
import fi.riista.common.domain.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.domain.poi.network.FetchPoiLocationGroups
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.metadata.network.FetchSrvaMetadata
import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.domain.training.network.FetchTrainings
import fi.riista.common.io.CommonFile
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.cookies.CookieData


class AuthenticationAwareBackendAPI internal constructor(
    internal val loginService: LoginService,
    internal val networkClient: NetworkClient,
) : BackendAPI {

    override fun getAllNetworkCookies(): List<CookieData> {
        return networkClient.cookiesStorage.addedCookies
    }

    override fun getNetworkCookies(requestUrl: String): List<CookieData> {
        return networkClient.cookiesStorage.getCookies(requestUrl)
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

    override suspend fun fetchTrainings(): NetworkResponse<TrainingsDTO> {
        return performRequest(FetchTrainings())
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

    override suspend fun fetchHuntingControlRhys(modifiedAfter: LocalDateTime?): NetworkResponse<LoadRhysAndHuntingControlEventsDTO> {
        return performRequest(FetchRhysAndHuntingControlEvents(modifiedAfter))
    }

    override suspend fun fetchHuntingControlAttachmentThumbnail(attachmentId: Long): NetworkResponse<ByteArray> {
        return performRequest(FetchAttachmentThumbnail(attachmentId))
    }

    override suspend fun createHuntingControlEvent(
        rhyId: OrganizationId,
        event: HuntingControlEventCreateDTO
    ): NetworkResponse<HuntingControlEventDTO> {
        return performRequest(CreateHuntingControlEvent(rhyId, event))
    }

    override suspend fun updateHuntingControlEvent(
        rhyId: OrganizationId,
        event: HuntingControlEventDTO
    ): NetworkResponse<HuntingControlEventDTO> {
        return performRequest(UpdateHuntingControlEvent(rhyId, event))
    }

    override suspend fun deleteHuntingControlEventAttachment(attachmentId: Long): NetworkResponse<Unit> {
        return performRequest(DeleteHuntingControlEventAttachment(attachmentId))
    }

    override suspend fun uploadHuntingControlEventAttachment(
        eventRemoteId: Long,
        uuid: String,
        fileName: String,
        contentType: String,
        file: CommonFile,
    ): NetworkResponse<Long> {
        return performRequest(
            UploadHuntingControlEventAttachment(
                eventId = eventRemoteId,
                uuid = uuid,
                fileName = fileName,
                contentType = contentType,
                file = file
            )
        )
    }

    override suspend fun fetchSrvaMetadata(): NetworkResponse<SrvaMetadataDTO> {
        return performRequest(FetchSrvaMetadata())
    }

    override suspend fun fetchObservationMetadata(): NetworkResponse<ObservationMetadataDTO> {
        return performRequest(FetchObservationMetadata())
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
