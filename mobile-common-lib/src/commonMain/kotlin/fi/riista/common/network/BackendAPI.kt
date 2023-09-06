package fi.riista.common.network

import fi.riista.common.domain.dto.HunterNumberDTO
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingClubsAndGroupsDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayCreateDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayForDeerDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayUpdateDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDaysDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDiaryDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestCreateDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingObservationCreateDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingObservationDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingObservationUpdateDTO
import fi.riista.common.domain.groupHunting.dto.HuntingGroupAreaDTO
import fi.riista.common.domain.groupHunting.dto.HuntingGroupMembersDTO
import fi.riista.common.domain.groupHunting.dto.HuntingGroupStatusDTO
import fi.riista.common.domain.groupHunting.dto.RejectDiaryEntryDTO
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.harvest.sync.dto.DeletedHarvestsDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestCreateDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestPageDTO
import fi.riista.common.domain.huntingControl.dto.HuntingControlHunterInfoDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingclub.dto.HuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.invitations.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitationId
import fi.riista.common.domain.huntingclub.memberships.dto.HuntingClubMembershipsDTO
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.sync.dto.DeletedObservationsDTO
import fi.riista.common.domain.observation.sync.dto.ObservationCreateDTO
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.ObservationPageDTO
import fi.riista.common.domain.permit.metsahallitusPermit.dto.CommonMetsahallitusPermitDTO
import fi.riista.common.domain.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.domain.shootingTest.dto.OpenShootingTestEventDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestAttemptCreateDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestAttemptDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestCalendarEventDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestCalendarEventsDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestOfficialsDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantCreateDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantDetailedDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantsDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestPaymentUpdateDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestPersonDTO
import fi.riista.common.domain.shootingTest.dto.UpdateShootingTestOfficialsDTO
import fi.riista.common.domain.shootingTest.model.CalendarEventId
import fi.riista.common.domain.shootingTest.model.ShootingTestAttemptId
import fi.riista.common.domain.shootingTest.model.ShootingTestEventId
import fi.riista.common.domain.shootingTest.model.ShootingTestParticipantId
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.sync.dto.DeletedSrvaEventsDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventCreateDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventPageDTO
import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.io.CommonFile
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.Revision
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.cookies.CookieData


internal interface BackendAPI {
    fun getAllNetworkCookies(): List<CookieData>
    fun getNetworkCookies(requestUrl: String): List<CookieData>

    suspend fun unregisterAccount(): NetworkResponse<LocalDateTimeDTO>
    suspend fun cancelUnregisterAccount(): NetworkResponse<Unit>

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

    suspend fun searchHuntingClubByOfficialCode(officialCode: String): NetworkResponse<HuntingClubNameAndCodeDTO>

    suspend fun fetchHuntingControlRhys(modifiedAfter: LocalDateTime?): NetworkResponse<LoadRhysAndHuntingControlEventsDTO>
    suspend fun fetchHuntingControlAttachmentThumbnail(attachmentId: Long): NetworkResponse<ByteArray>
    suspend fun createHuntingControlEvent(
        rhyId: OrganizationId,
        event: HuntingControlEventCreateDTO
    ): NetworkResponse<HuntingControlEventDTO>

    suspend fun updateHuntingControlEvent(
        rhyId: OrganizationId,
        event: HuntingControlEventDTO
    ): NetworkResponse<HuntingControlEventDTO>

    suspend fun deleteHuntingControlEventAttachment(attachmentId: Long): NetworkResponse<Unit>
    suspend fun uploadHuntingControlEventAttachment(
        eventRemoteId: Long, uuid: String, fileName: String, contentType: String, file: CommonFile
    ): NetworkResponse<Long>
    suspend fun fetchHuntingControlHunterInfoByHunterNumber(hunterNumber: String): NetworkResponse<HuntingControlHunterInfoDTO>
    suspend fun fetchHuntingControlHunterInfoBySsn(ssn: String): NetworkResponse<HuntingControlHunterInfoDTO>

    suspend fun fetchSrvaEvents(modifiedAfter: LocalDateTime?): NetworkResponse<SrvaEventPageDTO>
    suspend fun createSrvaEvent(event: SrvaEventCreateDTO): NetworkResponse<SrvaEventDTO>
    suspend fun updateSrvaEvent(event: SrvaEventDTO): NetworkResponse<SrvaEventDTO>
    suspend fun deleteSrvaEvent(eventRemoteId: Long): NetworkResponse<Unit>
    suspend fun fetchDeletedSrvaEvents(deletedAfter: LocalDateTime?): NetworkResponse<DeletedSrvaEventsDTO>
    suspend fun uploadSrvaEventImage(
        eventRemoteId: Long, uuid: String, contentType: String, file: CommonFile
    ): NetworkResponse<Unit>
    suspend fun deleteSrvaEventImage(imageUuid: String): NetworkResponse<Unit>

    suspend fun fetchObservations(modifiedAfter: LocalDateTime?): NetworkResponse<ObservationPageDTO>
    suspend fun createObservation(observation: ObservationCreateDTO): NetworkResponse<ObservationDTO>
    suspend fun updateObservation(observation: ObservationDTO): NetworkResponse<ObservationDTO>
    suspend fun deleteObservation(observationRemoteId: Long): NetworkResponse<Unit>
    suspend fun fetchDeletedObservations(deletedAfter: LocalDateTime?): NetworkResponse<DeletedObservationsDTO>
    suspend fun uploadObservationImage(
        observationRemoteId: Long, uuid: String, contentType: String, file: CommonFile
    ): NetworkResponse<Unit>
    suspend fun deleteObservationImage(imageUuid: String): NetworkResponse<Unit>

    suspend fun fetchHarvests(modifiedAfter: LocalDateTime?): NetworkResponse<HarvestPageDTO>
    suspend fun createHarvest(harvest: HarvestCreateDTO): NetworkResponse<HarvestDTO>
    suspend fun updateHarvest(harvest: HarvestDTO): NetworkResponse<HarvestDTO>
    suspend fun deleteHarvest(harvestRemoteId: Long): NetworkResponse<Unit>
    suspend fun fetchDeletedHarvests(deletedAfter: LocalDateTime?): NetworkResponse<DeletedHarvestsDTO>
    suspend fun uploadHarvestImage(
        harvestRemoteId: Long, uuid: String, contentType: String, file: CommonFile
    ): NetworkResponse<Unit>
    suspend fun deleteHarvestImage(imageUuid: String): NetworkResponse<Unit>

    suspend fun fetchSrvaMetadata(): NetworkResponse<SrvaMetadataDTO>
    suspend fun fetchObservationMetadata(): NetworkResponse<ObservationMetadataDTO>

    suspend fun fetchShootingTestCalendarEvents(): NetworkResponse<ShootingTestCalendarEventsDTO>
    suspend fun fetchShootingTestCalendarEvent(calendarEventId: CalendarEventId): NetworkResponse<ShootingTestCalendarEventDTO>
    suspend fun openShootingTestEvent(openShootingTestEventDTO: OpenShootingTestEventDTO): NetworkResponse<Unit>
    suspend fun closeShootingTestEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<Unit>
    suspend fun reopenShootingTestEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<Unit>
    suspend fun fetchAvailableShootingTestOfficialsForEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<ShootingTestOfficialsDTO>
    suspend fun fetchSelectedShootingTestOfficialsForEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<ShootingTestOfficialsDTO>
    suspend fun fetchAvailableShootingTestOfficialsForRhy(rhyId: Long): NetworkResponse<ShootingTestOfficialsDTO>
    suspend fun updateShootingTestOfficials(updateShootingTestOfficialsDTO: UpdateShootingTestOfficialsDTO): NetworkResponse<Unit>
    suspend fun searchShootingTestPersonWithSsn(
        shootingTestEventId: ShootingTestEventId,
        ssn: String,
    ): NetworkResponse<ShootingTestPersonDTO>
    suspend fun searchShootingTestPersonWithHunterNumber(
        shootingTestEventId: ShootingTestEventId,
        hunterNumber: String,
    ): NetworkResponse<ShootingTestPersonDTO>
    suspend fun fetchShootingTestParticipants(shootingTestEventId: ShootingTestEventId): NetworkResponse<ShootingTestParticipantsDTO>
    suspend fun fetchShootingTestParticipant(participantId: ShootingTestParticipantId): NetworkResponse<ShootingTestParticipantDTO>
    suspend fun addShootingTestParticipant(
        shootingTestEventId: ShootingTestEventId,
        participant: ShootingTestParticipantCreateDTO,
    ): NetworkResponse<Unit>
    suspend fun fetchShootingTestParticipantDetailed(participantId: ShootingTestParticipantId): NetworkResponse<ShootingTestParticipantDetailedDTO>
    suspend fun fetchShootingTestAttempt(shootingTestAttemptId: ShootingTestAttemptId): NetworkResponse<ShootingTestAttemptDTO>
    suspend fun addShootingTestAttempt(attempt: ShootingTestAttemptCreateDTO): NetworkResponse<Unit>
    suspend fun updateShootingTestAttempt(attempt: ShootingTestAttemptDTO): NetworkResponse<Unit>
    suspend fun removeShootingTestAttempt(shootingTestAttemptId: ShootingTestAttemptId): NetworkResponse<Unit>
    suspend fun updateShootingTestPaymentForParticipant(
        participantId: ShootingTestParticipantId,
        paymentUpdateDTO: ShootingTestPaymentUpdateDTO,
    ): NetworkResponse<Unit>
    suspend fun completeAllPaymentsForParticipant(participantId: ShootingTestParticipantId, participantRev: Revision): NetworkResponse<Unit>

    suspend fun fetchHarvestSeasons(huntingYear: HuntingYear): NetworkResponse<List<HarvestSeasonDTO>>

    suspend fun fetchMetsahallitusPermits(): NetworkResponse<List<CommonMetsahallitusPermitDTO>>
}
