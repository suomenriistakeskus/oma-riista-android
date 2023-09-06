package fi.riista.common.network

import fi.riista.common.authentication.LoginService
import fi.riista.common.authentication.registration.CancelUnregisterAccount
import fi.riista.common.authentication.registration.UnregisterAccount
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
import fi.riista.common.domain.groupHunting.network.CreateGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.network.CreateGroupHuntingObservation
import fi.riista.common.domain.groupHunting.network.CreateHuntingGroupHuntingDay
import fi.riista.common.domain.groupHunting.network.FetchGroupHuntingClubsAndGroups
import fi.riista.common.domain.groupHunting.network.FetchGroupHuntingDiary
import fi.riista.common.domain.groupHunting.network.FetchHuntingDayForDeer
import fi.riista.common.domain.groupHunting.network.FetchHuntingGroupArea
import fi.riista.common.domain.groupHunting.network.FetchHuntingGroupHuntingDays
import fi.riista.common.domain.groupHunting.network.FetchHuntingGroupMembers
import fi.riista.common.domain.groupHunting.network.FetchHuntingGroupStatus
import fi.riista.common.domain.groupHunting.network.RejectGroupHuntingDiaryEntry
import fi.riista.common.domain.groupHunting.network.SearchPersonByHunterNumber
import fi.riista.common.domain.groupHunting.network.UpdateGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.network.UpdateGroupHuntingObservation
import fi.riista.common.domain.groupHunting.network.UpdateHuntingGroupHuntingDay
import fi.riista.common.domain.harvest.sync.dto.DeletedHarvestsDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestCreateDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestPageDTO
import fi.riista.common.domain.harvest.sync.network.CreateHarvest
import fi.riista.common.domain.harvest.sync.network.DeleteHarvest
import fi.riista.common.domain.harvest.sync.network.DeleteHarvestImage
import fi.riista.common.domain.harvest.sync.network.FetchDeletedHarvests
import fi.riista.common.domain.harvest.sync.network.FetchHarvestPage
import fi.riista.common.domain.harvest.sync.network.UpdateHarvest
import fi.riista.common.domain.harvest.sync.network.UploadHarvestImage
import fi.riista.common.domain.huntingControl.dto.HuntingControlHunterInfoDTO
import fi.riista.common.domain.huntingControl.network.FetchHunterInfoByHunterNumber
import fi.riista.common.domain.huntingControl.network.FetchHunterInfoBySsn
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingControl.sync.network.CreateHuntingControlEvent
import fi.riista.common.domain.huntingControl.sync.network.DeleteHuntingControlEventAttachment
import fi.riista.common.domain.huntingControl.sync.network.FetchAttachmentThumbnail
import fi.riista.common.domain.huntingControl.sync.network.FetchRhysAndHuntingControlEvents
import fi.riista.common.domain.huntingControl.sync.network.UpdateHuntingControlEvent
import fi.riista.common.domain.huntingControl.sync.network.UploadHuntingControlEventAttachment
import fi.riista.common.domain.huntingclub.dto.HuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.invitations.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitationId
import fi.riista.common.domain.huntingclub.invitations.network.FetchHuntingClubMemberInvitations
import fi.riista.common.domain.huntingclub.invitations.network.HuntingClubMemberInvitationRequest
import fi.riista.common.domain.huntingclub.memberships.dto.HuntingClubMembershipsDTO
import fi.riista.common.domain.huntingclub.memberships.sync.FetchHuntingClubMemberships
import fi.riista.common.domain.huntingclub.selectableForEntries.network.SearchHuntingClubByOfficialCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.metadata.network.FetchObservationMetadata
import fi.riista.common.domain.observation.sync.dto.DeletedObservationsDTO
import fi.riista.common.domain.observation.sync.dto.ObservationCreateDTO
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.ObservationPageDTO
import fi.riista.common.domain.observation.sync.network.CreateObservation
import fi.riista.common.domain.observation.sync.network.DeleteObservation
import fi.riista.common.domain.observation.sync.network.DeleteObservationImage
import fi.riista.common.domain.observation.sync.network.FetchDeletedObservations
import fi.riista.common.domain.observation.sync.network.FetchObservationPage
import fi.riista.common.domain.observation.sync.network.UpdateObservation
import fi.riista.common.domain.observation.sync.network.UploadObservationImage
import fi.riista.common.domain.permit.metsahallitusPermit.dto.CommonMetsahallitusPermitDTO
import fi.riista.common.domain.permit.metsahallitusPermit.sync.FetchMetsahallitusPermits
import fi.riista.common.domain.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.domain.poi.network.FetchPoiLocationGroups
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.domain.season.network.FetchHarvestSeasons
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
import fi.riista.common.domain.shootingTest.network.AddShootingTestAttempt
import fi.riista.common.domain.shootingTest.network.AddShootingTestParticipant
import fi.riista.common.domain.shootingTest.network.CloseShootingTestEvent
import fi.riista.common.domain.shootingTest.network.CompleteAllPaymentsForParticipants
import fi.riista.common.domain.shootingTest.network.FetchAvailableShootingTestOfficialsForEvent
import fi.riista.common.domain.shootingTest.network.FetchAvailableShootingTestOfficialsForRhy
import fi.riista.common.domain.shootingTest.network.FetchSelectedShootingTestOfficialsForEvent
import fi.riista.common.domain.shootingTest.network.FetchShootingTestAttempt
import fi.riista.common.domain.shootingTest.network.FetchShootingTestCalendarEvent
import fi.riista.common.domain.shootingTest.network.FetchShootingTestCalendarEvents
import fi.riista.common.domain.shootingTest.network.FetchShootingTestParticipant
import fi.riista.common.domain.shootingTest.network.FetchShootingTestParticipantDetailed
import fi.riista.common.domain.shootingTest.network.FetchShootingTestParticipants
import fi.riista.common.domain.shootingTest.network.OpenShootingTestEvent
import fi.riista.common.domain.shootingTest.network.RemoveShootingTestAttempt
import fi.riista.common.domain.shootingTest.network.ReopenShootingTestEvent
import fi.riista.common.domain.shootingTest.network.SearchShootingTestPersonByHunterNumber
import fi.riista.common.domain.shootingTest.network.SearchShootingTestPersonBySsn
import fi.riista.common.domain.shootingTest.network.UpdatePaymentStateForParticipant
import fi.riista.common.domain.shootingTest.network.UpdateShootingTestAttempt
import fi.riista.common.domain.shootingTest.network.UpdateShootingTestOfficials
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.metadata.network.FetchSrvaMetadata
import fi.riista.common.domain.srva.sync.dto.DeletedSrvaEventsDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventCreateDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventPageDTO
import fi.riista.common.domain.srva.sync.network.CreateSrvaEvent
import fi.riista.common.domain.srva.sync.network.DeleteSrvaEvent
import fi.riista.common.domain.srva.sync.network.DeleteSrvaEventImage
import fi.riista.common.domain.srva.sync.network.FetchDeletedSrvaEvents
import fi.riista.common.domain.srva.sync.network.FetchSrvaEvents
import fi.riista.common.domain.srva.sync.network.UpdateSrvaEvent
import fi.riista.common.domain.srva.sync.network.UploadSrvaEventImage
import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.domain.training.network.FetchTrainings
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.io.CommonFile
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.Revision
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.cookies.CookieData


internal class AuthenticationAwareBackendAPI(
    private val loginService: LoginService,
    private val networkClient: NetworkClient,
) : BackendAPI {

    override fun getAllNetworkCookies(): List<CookieData> {
        return networkClient.cookiesStorage.addedCookies
    }

    override fun getNetworkCookies(requestUrl: String): List<CookieData> {
        return networkClient.cookiesStorage.getCookies(requestUrl)
    }

    override suspend fun unregisterAccount(): NetworkResponse<LocalDateTimeDTO> {
        return performRequest(UnregisterAccount())
    }

    override suspend fun cancelUnregisterAccount(): NetworkResponse<Unit> {
        return performRequest(CancelUnregisterAccount())
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

    override suspend fun searchHuntingClubByOfficialCode(
        officialCode: String
    ): NetworkResponse<HuntingClubNameAndCodeDTO> {
        return performRequest(SearchHuntingClubByOfficialCode(officialCode))
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

    override suspend fun fetchHuntingControlHunterInfoByHunterNumber(
        hunterNumber: String,
    ): NetworkResponse<HuntingControlHunterInfoDTO> {
        return performRequest(FetchHunterInfoByHunterNumber(hunterNumber))
    }

    override suspend fun fetchHuntingControlHunterInfoBySsn(ssn: String): NetworkResponse<HuntingControlHunterInfoDTO> {
        return performRequest(FetchHunterInfoBySsn(ssn))
    }

    override suspend fun fetchSrvaEvents(modifiedAfter: LocalDateTime?): NetworkResponse<SrvaEventPageDTO> {
        return performRequest(FetchSrvaEvents(modifiedAfter))
    }

    override suspend fun createSrvaEvent(event: SrvaEventCreateDTO): NetworkResponse<SrvaEventDTO> {
        return performRequest(CreateSrvaEvent(event))
    }

    override suspend fun updateSrvaEvent(event: SrvaEventDTO): NetworkResponse<SrvaEventDTO> {
        return performRequest(UpdateSrvaEvent(event))
    }

    override suspend fun deleteSrvaEvent(eventRemoteId: Long): NetworkResponse<Unit> {
        return performRequest(DeleteSrvaEvent(eventRemoteId))
    }

    override suspend fun fetchDeletedSrvaEvents(deletedAfter: LocalDateTime?): NetworkResponse<DeletedSrvaEventsDTO> {
        return performRequest(FetchDeletedSrvaEvents(deletedAfter))
    }

    override suspend fun uploadSrvaEventImage(
        eventRemoteId: Long,
        uuid: String,
        contentType: String,
        file: CommonFile
    ): NetworkResponse<Unit> {
        return performRequest(
            UploadSrvaEventImage(
                eventRemoteId = eventRemoteId,
                uuid = uuid,
                contentType = contentType,
                file = file,
            )
        )
    }

    override suspend fun deleteSrvaEventImage(imageUuid: String): NetworkResponse<Unit> {
        return performRequest(
            DeleteSrvaEventImage(
                imageUuid = imageUuid,
            )
        )
    }

    override suspend fun fetchObservations(modifiedAfter: LocalDateTime?): NetworkResponse<ObservationPageDTO> {
        return performRequest(FetchObservationPage(modifiedAfter))
    }

    override suspend fun createObservation(observation: ObservationCreateDTO): NetworkResponse<ObservationDTO> {
        return performRequest(CreateObservation(observation))
    }

    override suspend fun updateObservation(observation: ObservationDTO): NetworkResponse<ObservationDTO> {
        return performRequest(UpdateObservation(observation))
    }

    override suspend fun deleteObservation(observationRemoteId: Long): NetworkResponse<Unit> {
        return performRequest(DeleteObservation(observationRemoteId))
    }

    override suspend fun fetchDeletedObservations(deletedAfter: LocalDateTime?): NetworkResponse<DeletedObservationsDTO> {
        return performRequest(FetchDeletedObservations(deletedAfter))
    }

    override suspend fun uploadObservationImage(
        observationRemoteId: Long,
        uuid: String,
        contentType: String,
        file: CommonFile
    ): NetworkResponse<Unit> {
        return performRequest(
            UploadObservationImage(
                observationRemoteId = observationRemoteId,
                uuid = uuid,
                contentType = contentType,
                file = file,
            )
        )
    }

    override suspend fun deleteObservationImage(imageUuid: String): NetworkResponse<Unit> {
        return performRequest(
            DeleteObservationImage(
                imageUuid = imageUuid,
            )
        )
    }

    override suspend fun fetchHarvests(modifiedAfter: LocalDateTime?): NetworkResponse<HarvestPageDTO> {
        return performRequest(FetchHarvestPage(modifiedAfter = modifiedAfter))
    }

    override suspend fun createHarvest(harvest: HarvestCreateDTO): NetworkResponse<HarvestDTO> {
        return performRequest(CreateHarvest(harvest))
    }

    override suspend fun updateHarvest(harvest: HarvestDTO): NetworkResponse<HarvestDTO> {
        return performRequest(UpdateHarvest(harvest))
    }

    override suspend fun deleteHarvest(harvestRemoteId: Long): NetworkResponse<Unit> {
        return performRequest(DeleteHarvest(harvestRemoteId))
    }

    override suspend fun fetchDeletedHarvests(deletedAfter: LocalDateTime?): NetworkResponse<DeletedHarvestsDTO> {
        return performRequest(FetchDeletedHarvests(deletedAfter))
    }

    override suspend fun uploadHarvestImage(
        harvestRemoteId: Long,
        uuid: String,
        contentType: String,
        file: CommonFile
    ): NetworkResponse<Unit> {
        return performRequest(
            UploadHarvestImage(
                harvestRemoteId = harvestRemoteId,
                uuid = uuid,
                contentType = contentType,
                file = file,
            )
        )
    }

    override suspend fun deleteHarvestImage(imageUuid: String): NetworkResponse<Unit> {
        return performRequest(
            DeleteHarvestImage(
                imageUuid = imageUuid,
            )
        )
    }

    override suspend fun fetchSrvaMetadata(): NetworkResponse<SrvaMetadataDTO> {
        return performRequest(FetchSrvaMetadata())
    }

    override suspend fun fetchObservationMetadata(): NetworkResponse<ObservationMetadataDTO> {
        return performRequest(FetchObservationMetadata())
    }

    override suspend fun fetchShootingTestCalendarEvents(): NetworkResponse<ShootingTestCalendarEventsDTO> {
        return performRequest(FetchShootingTestCalendarEvents())
    }

    override suspend fun fetchShootingTestCalendarEvent(
        calendarEventId: CalendarEventId,
    ): NetworkResponse<ShootingTestCalendarEventDTO> {
        return performRequest(FetchShootingTestCalendarEvent(calendarEventId))
    }

    override suspend fun openShootingTestEvent(
        openShootingTestEventDTO: OpenShootingTestEventDTO,
    ): NetworkResponse<Unit> {
        return performRequest(OpenShootingTestEvent(openShootingTestEventDTO))
    }

    override suspend fun closeShootingTestEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<Unit> {
        return performRequest(CloseShootingTestEvent(shootingTestEventId))
    }

    override suspend fun reopenShootingTestEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<Unit> {
        return performRequest(ReopenShootingTestEvent(shootingTestEventId))
    }

    override suspend fun fetchAvailableShootingTestOfficialsForEvent(
        shootingTestEventId: ShootingTestEventId,
    ): NetworkResponse<ShootingTestOfficialsDTO> {
        return performRequest(FetchAvailableShootingTestOfficialsForEvent(shootingTestEventId))
    }

    override suspend fun fetchSelectedShootingTestOfficialsForEvent(
        shootingTestEventId: ShootingTestEventId,
    ): NetworkResponse<ShootingTestOfficialsDTO> {
        return performRequest(FetchSelectedShootingTestOfficialsForEvent(shootingTestEventId))
    }

    override suspend fun fetchAvailableShootingTestOfficialsForRhy(
        rhyId: Long,
    ): NetworkResponse<ShootingTestOfficialsDTO> {
        return performRequest(FetchAvailableShootingTestOfficialsForRhy(rhyId))
    }

    override suspend fun updateShootingTestOfficials(
        updateShootingTestOfficialsDTO: UpdateShootingTestOfficialsDTO
    ): NetworkResponse<Unit> {
        return performRequest(UpdateShootingTestOfficials(updateShootingTestOfficialsDTO))
    }

    override suspend fun searchShootingTestPersonWithSsn(
        shootingTestEventId: ShootingTestEventId,
        ssn: String,
    ): NetworkResponse<ShootingTestPersonDTO> {
        return performRequest(SearchShootingTestPersonBySsn(shootingTestEventId, ssn))
    }

    override suspend fun searchShootingTestPersonWithHunterNumber(
        shootingTestEventId: ShootingTestEventId,
        hunterNumber: String,
    ): NetworkResponse<ShootingTestPersonDTO> {
        return performRequest(SearchShootingTestPersonByHunterNumber(shootingTestEventId, hunterNumber))
    }

    override suspend fun fetchShootingTestParticipants(
        shootingTestEventId: ShootingTestEventId,
    ): NetworkResponse<ShootingTestParticipantsDTO> {
        return performRequest(FetchShootingTestParticipants(shootingTestEventId))
    }

    override suspend fun fetchShootingTestParticipant(
        participantId: ShootingTestParticipantId,
    ): NetworkResponse<ShootingTestParticipantDTO> {
        return performRequest(FetchShootingTestParticipant(participantId))
    }

    override suspend fun addShootingTestParticipant(
        shootingTestEventId: ShootingTestEventId,
        participant: ShootingTestParticipantCreateDTO,
    ): NetworkResponse<Unit> {
        return performRequest(AddShootingTestParticipant(shootingTestEventId, participant))
    }

    override suspend fun fetchShootingTestParticipantDetailed(
        participantId: ShootingTestParticipantId,
    ): NetworkResponse<ShootingTestParticipantDetailedDTO> {
        return performRequest(FetchShootingTestParticipantDetailed(participantId))
    }

    override suspend fun fetchShootingTestAttempt(
        shootingTestAttemptId: ShootingTestAttemptId,
    ): NetworkResponse<ShootingTestAttemptDTO> {
        return performRequest(FetchShootingTestAttempt(shootingTestAttemptId))
    }

    override suspend fun addShootingTestAttempt(attempt: ShootingTestAttemptCreateDTO): NetworkResponse<Unit> {
        return performRequest(AddShootingTestAttempt(attempt))
    }

    override suspend fun updateShootingTestAttempt(attempt: ShootingTestAttemptDTO): NetworkResponse<Unit> {
        return performRequest(UpdateShootingTestAttempt(attempt))
    }

    override suspend fun removeShootingTestAttempt(shootingTestAttemptId: ShootingTestAttemptId): NetworkResponse<Unit> {
        return performRequest(RemoveShootingTestAttempt(shootingTestAttemptId))
    }

    override suspend fun updateShootingTestPaymentForParticipant(
        participantId: ShootingTestParticipantId,
        paymentUpdateDTO: ShootingTestPaymentUpdateDTO
    ): NetworkResponse<Unit> {
        return performRequest(UpdatePaymentStateForParticipant(participantId, paymentUpdateDTO))
    }

    override suspend fun completeAllPaymentsForParticipant(
        participantId: ShootingTestParticipantId,
        participantRev: Revision,
    ): NetworkResponse<Unit> {
        return performRequest(CompleteAllPaymentsForParticipants(participantId = participantId, rev = participantRev))
    }

    override suspend fun fetchHarvestSeasons(huntingYear: HuntingYear): NetworkResponse<List<HarvestSeasonDTO>> {
        return performRequest(FetchHarvestSeasons(huntingYear))
    }

    override suspend fun fetchMetsahallitusPermits(): NetworkResponse<List<CommonMetsahallitusPermitDTO>> {
        return performRequest(FetchMetsahallitusPermits())
    }

    /**
     * Performs the given [request]. Will attempt to relogin if [request] response
     * status code is 401.
     */
    private suspend fun <DataType> performRequest(
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
