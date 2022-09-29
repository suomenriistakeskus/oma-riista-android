package fi.riista.common.network

import fi.riista.common.domain.dto.HunterNumberDTO
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.groupHunting.dto.*
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingclub.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.domain.huntingclub.dto.HuntingClubMembershipsDTO
import fi.riista.common.domain.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.io.CommonFile
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.cookies.CookieData


interface BackendAPI {
    fun getAllNetworkCookies(): List<CookieData>
    fun getNetworkCookies(requestUrl: String): List<CookieData>

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

    suspend fun fetchTrainings(): NetworkResponse<TrainingsDTO>

    suspend fun fetchHuntingClubMemberships(): NetworkResponse<HuntingClubMembershipsDTO>
    suspend fun fetchHuntingClubMemberInvitations(): NetworkResponse<HuntingClubMemberInvitationsDTO>
    suspend fun acceptHuntingClubMemberInvitation(invitationId: HuntingClubMemberInvitationId): NetworkResponse<Unit>
    suspend fun rejectHuntingClubMemberInvitation(invitationId: HuntingClubMemberInvitationId): NetworkResponse<Unit>

    suspend fun fetchHuntingControlRhys(modifiedAfter: LocalDateTime?): NetworkResponse<LoadRhysAndHuntingControlEventsDTO>
    suspend fun fetchHuntingControlAttachmentThumbnail(attachmentId: Long): NetworkResponse<ByteArray>
    suspend fun createHuntingControlEvent(rhyId: OrganizationId, event: HuntingControlEventCreateDTO): NetworkResponse<HuntingControlEventDTO>
    suspend fun updateHuntingControlEvent(rhyId: OrganizationId, event: HuntingControlEventDTO): NetworkResponse<HuntingControlEventDTO>

    suspend fun deleteHuntingControlEventAttachment(attachmentId: Long): NetworkResponse<Unit>
    suspend fun uploadHuntingControlEventAttachment(
        eventRemoteId: Long, uuid: String, fileName: String, contentType: String, file: CommonFile
    ): NetworkResponse<Long>

    suspend fun fetchSrvaMetadata(): NetworkResponse<SrvaMetadataDTO>
    suspend fun fetchObservationMetadata(): NetworkResponse<ObservationMetadataDTO>
}
