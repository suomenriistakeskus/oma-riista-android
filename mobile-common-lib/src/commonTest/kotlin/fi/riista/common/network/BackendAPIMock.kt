package fi.riista.common.network

import fi.riista.common.domain.dto.HunterNumberDTO
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
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
import fi.riista.common.domain.harvest.MockHarvestData
import fi.riista.common.domain.harvest.MockHarvestPageData
import fi.riista.common.domain.harvest.sync.dto.DeletedHarvestsDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestCreateDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestPageDTO
import fi.riista.common.domain.huntingControl.MockHuntingControlData
import fi.riista.common.domain.huntingControl.dto.HuntingControlHunterInfoDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingControl.ui.hunterInfo.MockHunterInfoData
import fi.riista.common.domain.huntingclub.MockHuntingClubData
import fi.riista.common.domain.huntingclub.dto.HuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.invitations.dto.HuntingClubMemberInvitationsDTO
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitationId
import fi.riista.common.domain.huntingclub.memberships.dto.HuntingClubMembershipsDTO
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.observation.MockObservationData
import fi.riista.common.domain.observation.MockObservationPageData
import fi.riista.common.domain.observation.metadata.MockObservationMetadata
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.sync.dto.DeletedObservationsDTO
import fi.riista.common.domain.observation.sync.dto.ObservationCreateDTO
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.domain.observation.sync.dto.ObservationPageDTO
import fi.riista.common.domain.permit.metsahallitusPermit.dto.CommonMetsahallitusPermitDTO
import fi.riista.common.domain.permit.metsahallitusPermit.sync.MockMetsahallitusPermitsData
import fi.riista.common.domain.poi.MockPoiData
import fi.riista.common.domain.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.domain.season.sync.MockHarvestSeasonsData
import fi.riista.common.domain.shootingTest.MockShootingTestData
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
import fi.riista.common.domain.srva.MockSrvaEventData
import fi.riista.common.domain.srva.MockSrvaEventPageData
import fi.riista.common.domain.srva.metadata.MockSrvaMetadata
import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.sync.dto.DeletedSrvaEventsDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventCreateDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventPageDTO
import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.domain.training.ui.MockTrainingData
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.io.CommonFile
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.Revision
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.network.cookies.CookieData
import fi.riista.common.util.deserializeFromJson
import io.ktor.utils.io.core.*
import kotlin.reflect.KCallable

data class MockResponse(
    val statusCode: Int? = 200,
    val responseData: String? = null
) {
    companion object {
        fun success(responseData: String?) = MockResponse(responseData = responseData)
        fun success(statusCode: Int?, responseData: String?) = MockResponse(statusCode = statusCode, responseData = responseData)
        fun successWithNoData(statusCode: Int?) = MockResponse(statusCode = statusCode)
        fun error(statusCode: Int?) = MockResponse(statusCode = statusCode)
    }
}

@Suppress("MemberVisibilityCanBePrivate")
internal open class BackendAPIMock(
    var unregisterAccountResponse: MockResponse = MockResponse.success("\"2023-03-21T15:13:55.320\""),
    var cancelUnregisterAccountResponse: MockResponse = MockResponse.successWithNoData(204),
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
    var searchHuntingClubByOfficialCodeResponse: MockResponse = MockResponse.success(MockHuntingClubData.HuntingClubSearchResult),
    var poiLocationGroupsResponse: MockResponse = MockResponse.success(MockPoiData.PoiLocationGroups),
    var huntingControlRhysResponse: MockResponse = MockResponse.success(MockHuntingControlData.HuntingControlRhys),
    var huntingControlAttachmentThumbnailResponse: MockResponse = MockResponse.success(MockHuntingControlData.AttachmentThumbnail),
    var createHuntingControlEventResponse: MockResponse = MockResponse.success(MockHuntingControlData.CreatedHuntingControlEvent),
    var updateHuntingControlEventReponse: MockResponse = MockResponse.success(MockHuntingControlData.UpdatedHuntingControlEvent),
    var deleteHuntingControlEventAttachmentResponse: MockResponse = MockResponse.successWithNoData(204),
    var uploadHuntingControlEventAttachmentResponse: MockResponse = MockResponse.success(204, "${MockHuntingControlData.UploadedAttachmentRemoteId}"),
    var fetchHuntingControlHunterInfoResponse: MockResponse = MockResponse.success(MockHunterInfoData.HunterInfo),
    var fetchTrainingsResponse: MockResponse = MockResponse.success(MockTrainingData.Trainings),
    var fetchSrvaMetadataResponse: MockResponse = MockResponse.success(MockSrvaMetadata.METADATA_SPEC_VERSION_2),
    var fetchObservationMetadataResponse: MockResponse = MockResponse.success(MockObservationMetadata.METADATA_SPEC_VERSION_4),
    var fetchSrvaEventsResponse: MockResponse = MockResponse.success(MockSrvaEventPageData.srvaPageWithOneEvent),
    var createSrvaEventResponse: MockResponse = MockResponse.success(MockSrvaEventData.srvaEvent),
    var updateSrvaEventResponse: MockResponse = MockResponse.success(MockSrvaEventData.srvaEvent),
    var deleteSrvaEventResponse: MockResponse = MockResponse.successWithNoData(204),
    var fetchDeletedSrvaEventsResponse: MockResponse = MockResponse.success(MockSrvaEventPageData.deletedSrvaEvents),
    var uploadSrvaEventImageResponse: MockResponse = MockResponse.successWithNoData(200),
    var deleteSrvaEventImageResponse: MockResponse = MockResponse.successWithNoData(200),
    var fetchObservationPageResponse: MockResponse = MockResponse.success(MockObservationPageData.observationPage),
    var createObservationResponse: MockResponse = MockResponse.success(MockObservationData.observation),
    var updateObservationResponse: MockResponse = MockResponse.success(MockObservationData.observation),
    var deleteObservationResponse: MockResponse = MockResponse.successWithNoData(204),
    var fetchDeletedObservationsResponse: MockResponse = MockResponse.success(MockObservationPageData.deletedObservations),
    var uploadObservationImageResponse: MockResponse = MockResponse.successWithNoData(200),
    var deleteObservationImageResponse: MockResponse = MockResponse.successWithNoData(200),
    var fetchHarvestPageResponse: MockResponse = MockResponse.success(MockHarvestPageData.harvestPageWithOneHarvest),
    var createHarvestResponse: MockResponse = MockResponse.success(MockHarvestData.harvest),
    var updateHarvestResponse: MockResponse = MockResponse.success(MockHarvestData.harvest),
    var deleteHarvestResponse: MockResponse = MockResponse.successWithNoData(204),
    var fetchDeletedHarvestsResponse: MockResponse = MockResponse.success(MockHarvestPageData.deletedHarvests),
    var uploadHarvestImageResponse: MockResponse = MockResponse.successWithNoData(200),
    var deleteHarvestImageResponse: MockResponse = MockResponse.successWithNoData(200),
    var fetchShootingTestCalendarEventsResponse: MockResponse = MockResponse.success(MockShootingTestData.events),
    var fetchShootingTestCalendarEventResponse: MockResponse = MockResponse.success(MockShootingTestData.firstEvent),
    var openShootingTestEventResponse: MockResponse = MockResponse.successWithNoData(200),
    var closeShootingTestEventResponse: MockResponse = MockResponse.successWithNoData(200),
    var reopenShootingTestEventResponse: MockResponse = MockResponse.successWithNoData(200),
    var fetchAvailableShootingTestOfficialsForEventResponse: MockResponse = MockResponse.success(MockShootingTestData.officials),
    var fetchSelectedShootingTestOfficialsForEventResponse: MockResponse = MockResponse.success(MockShootingTestData.officials),
    var fetchAvailableShootingTestOfficialsForRhyResponse: MockResponse = MockResponse.success(MockShootingTestData.officials),
    var updateShootingTestOfficialsResponse: MockResponse = MockResponse.successWithNoData(200),
    var searchShootingTestPersonWithSsnResponse: MockResponse = MockResponse.success(MockShootingTestData.person),
    var searchShootingTestPersonWithHunterNumberResponse: MockResponse = MockResponse.success(MockShootingTestData.person),
    var fetchShootingTestParticipantsResponse: MockResponse = MockResponse.success(MockShootingTestData.participants),
    var fetchShootingTestParticipantResponse: MockResponse = MockResponse.success(MockShootingTestData.participant),
    var addShootingTestParticipantResponse: MockResponse = MockResponse.successWithNoData(204),
    var fetchShootingTestParticipantDetailedResponse: MockResponse = MockResponse.success(MockShootingTestData.participantDetailed),
    var fetchShootingTestAttemptResponse: MockResponse = MockResponse.success(MockShootingTestData.attempt),
    var addShootingTestAttemptResponse: MockResponse = MockResponse.successWithNoData(204),
    var updateShootingTestAttemptResponse: MockResponse = MockResponse.successWithNoData(204),
    var removeShootingTestAttemptResponse: MockResponse = MockResponse.successWithNoData(204),
    var updateShootingTestPaymentForParticipantResponse: MockResponse = MockResponse.successWithNoData(204),
    var completeAllPaymentsForParticipantResponse: MockResponse = MockResponse.successWithNoData(204),
    var fetchHarvestSeasonsResponse: MockResponse = MockResponse.success(MockHarvestSeasonsData.harvestSeasons),
    var fetchMetsahallitusPermitsResponse: MockResponse = MockResponse.success(MockMetsahallitusPermitsData.permits),
) : BackendAPI {
    private val callCounts: MutableMap<String, Int> = mutableMapOf()
    private val callParameters: MutableMap<String, Any?> = mutableMapOf()

    override fun getAllNetworkCookies(): List<CookieData> = listOf()

    override fun getNetworkCookies(requestUrl: String): List<CookieData> = listOf()

    override suspend fun unregisterAccount(): NetworkResponse<LocalDateTimeDTO> {
        increaseCallCount(::unregisterAccount.name)
        return respond(unregisterAccountResponse)
    }

    override suspend fun cancelUnregisterAccount(): NetworkResponse<Unit> {
        increaseCallCount(::cancelUnregisterAccount.name)
        return respond(cancelUnregisterAccountResponse)
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

    override suspend fun fetchTrainings(): NetworkResponse<TrainingsDTO> {
        increaseCallCount(::fetchTrainings.name)
        return respond(fetchTrainingsResponse)
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

    override suspend fun searchHuntingClubByOfficialCode(officialCode: String): NetworkResponse<HuntingClubNameAndCodeDTO> {
        increaseCallCount(::searchHuntingClubByOfficialCode.name)
        callParameters[::searchHuntingClubByOfficialCode.name] = officialCode
        return respond(searchHuntingClubByOfficialCodeResponse)
    }

    override suspend fun fetchHuntingControlRhys(modifiedAfter: LocalDateTime?): NetworkResponse<LoadRhysAndHuntingControlEventsDTO> {
        increaseCallCount(::fetchHuntingControlRhys.name)
        callParameters[::fetchHuntingControlRhys.name] = modifiedAfter
        return respond(huntingControlRhysResponse)
    }

    override suspend fun fetchHuntingControlAttachmentThumbnail(attachmentId: Long): NetworkResponse<ByteArray> {
        increaseCallCount(::fetchHuntingControlAttachmentThumbnail.name)
        callParameters[::fetchHuntingControlAttachmentThumbnail.name] = attachmentId
        val response = huntingControlAttachmentThumbnailResponse
        return if (response.statusCode != null) {
            if (response.statusCode in 200..299) {
                if (response.responseData != null) {
                        return NetworkResponse.Success(
                            statusCode = response.statusCode,
                            data = NetworkResponseData(
                                raw = response.responseData,
                                typed = response.responseData.toByteArray()
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

    override suspend fun createHuntingControlEvent(
        rhyId: OrganizationId,
        event: HuntingControlEventCreateDTO
    ): NetworkResponse<HuntingControlEventDTO> {
        increaseCallCount(::createHuntingControlEvent.name)
        callParameters[::createHuntingControlEvent.name] = Pair(rhyId, event)
        return respond(createHuntingControlEventResponse)
    }

    override suspend fun updateHuntingControlEvent(
        rhyId: OrganizationId,
        event: HuntingControlEventDTO
    ): NetworkResponse<HuntingControlEventDTO> {
        increaseCallCount(::updateHuntingControlEvent.name)
        callParameters[::updateHuntingControlEvent.name] = Pair(rhyId, event)
        return respond(updateHuntingControlEventReponse)
    }

    override suspend fun deleteHuntingControlEventAttachment(attachmentId: Long): NetworkResponse<Unit> {
        increaseCallCount(::deleteHuntingControlEventAttachment.name)
        callParameters[::deleteHuntingControlEventAttachment.name] = attachmentId
        return respond(deleteHuntingControlEventAttachmentResponse)
    }

    override suspend fun uploadHuntingControlEventAttachment(
        eventRemoteId: Long,
        uuid: String,
        fileName: String,
        contentType: String,
        file: CommonFile,
    ): NetworkResponse<Long> {
        increaseCallCount(::uploadHuntingControlEventAttachment.name)
        callParameters[::uploadHuntingControlEventAttachment.name] = UploadHuntingControlEventAttachmentCallParameters(
            eventRemoteId = eventRemoteId,
            uuid = uuid,
            fileName = fileName,
            contentType = contentType,
            file = file,
        )
        return respond(uploadHuntingControlEventAttachmentResponse)
    }

    override suspend fun fetchHuntingControlHunterInfoByHunterNumber(
        hunterNumber: String,
    ): NetworkResponse<HuntingControlHunterInfoDTO> {
        increaseCallCount(::fetchHuntingControlHunterInfoByHunterNumber.name)
        callParameters[::fetchHuntingControlHunterInfoByHunterNumber.name] = hunterNumber
        return respond(fetchHuntingControlHunterInfoResponse)
    }

    override suspend fun fetchHuntingControlHunterInfoBySsn(ssn: String): NetworkResponse<HuntingControlHunterInfoDTO> {
        increaseCallCount(::fetchHuntingControlHunterInfoBySsn.name)
        callParameters[::fetchHuntingControlHunterInfoBySsn.name] = ssn
        return respond(fetchHuntingControlHunterInfoResponse)
    }

    override suspend fun fetchSrvaEvents(modifiedAfter: LocalDateTime?): NetworkResponse<SrvaEventPageDTO> {
        increaseCallCount(::fetchSrvaEvents.name)
        return respond(fetchSrvaEventsResponse)
    }

    override suspend fun createSrvaEvent(event: SrvaEventCreateDTO): NetworkResponse<SrvaEventDTO> {
        increaseCallCount(::createSrvaEvent.name)
        callParameters[::createSrvaEvent.name] = event
        return respond(createSrvaEventResponse)
    }

    override suspend fun updateSrvaEvent(event: SrvaEventDTO): NetworkResponse<SrvaEventDTO> {
        increaseCallCount(::updateSrvaEvent.name)
        callParameters[::updateSrvaEvent.name] = event
        return respond(updateSrvaEventResponse)
    }

    override suspend fun deleteSrvaEvent(eventRemoteId: Long): NetworkResponse<Unit> {
        increaseCallCount(::deleteSrvaEvent.name)
        callParameters[::deleteSrvaEvent.name] = eventRemoteId
        return respond(deleteSrvaEventResponse)
    }

    override suspend fun fetchDeletedSrvaEvents(deletedAfter: LocalDateTime?): NetworkResponse<DeletedSrvaEventsDTO> {
        increaseCallCount(::fetchDeletedSrvaEventsResponse.name)
        callParameters[::fetchDeletedSrvaEvents.name] = deletedAfter
        return respond(fetchDeletedSrvaEventsResponse)
    }

    override suspend fun uploadSrvaEventImage(
        eventRemoteId: Long,
        uuid: String,
        contentType: String,
        file: CommonFile
    ): NetworkResponse<Unit> {
        increaseCallCount(::uploadSrvaEventImage.name)
        callParameters[::uploadSrvaEventImage.name] = UploadImageCallParameters(
            eventRemoteId = eventRemoteId,
            uuid = uuid,
            contentType = contentType,
            file = file,
        )
        return respond(uploadSrvaEventImageResponse)
    }

    override suspend fun deleteSrvaEventImage(imageUuid: String): NetworkResponse<Unit> {
        increaseCallCount(::deleteSrvaEventImage.name)
        callParameters[::deleteSrvaEventImage.name] = imageUuid
        return respond(deleteSrvaEventImageResponse)
    }

    override suspend fun fetchObservations(modifiedAfter: LocalDateTime?): NetworkResponse<ObservationPageDTO> {
        increaseCallCount(::fetchObservations.name)
        callParameters[::fetchObservations.name] = modifiedAfter
        return respond(fetchObservationPageResponse)
    }

    override suspend fun createObservation(observation: ObservationCreateDTO): NetworkResponse<ObservationDTO> {
        increaseCallCount(::createObservation.name)
        callParameters[::createObservation.name] = observation
        return respond(createObservationResponse)
    }

    override suspend fun updateObservation(observation: ObservationDTO): NetworkResponse<ObservationDTO> {
        increaseCallCount(::updateObservation.name)
        callParameters[::updateObservation.name] = observation
        return respond(updateObservationResponse)
    }

    override suspend fun deleteObservation(observationRemoteId: Long): NetworkResponse<Unit> {
        increaseCallCount(::deleteObservation.name)
        callParameters[::deleteObservation.name] = observationRemoteId
        return respond(deleteObservationResponse)
    }

    override suspend fun fetchDeletedObservations(deletedAfter: LocalDateTime?): NetworkResponse<DeletedObservationsDTO> {
        increaseCallCount(::fetchDeletedObservations.name)
        callParameters[::fetchDeletedObservations.name] = deletedAfter
        return respond(fetchDeletedObservationsResponse)
    }

    override suspend fun uploadObservationImage(
        observationRemoteId: Long,
        uuid: String,
        contentType: String,
        file: CommonFile
    ): NetworkResponse<Unit> {
        increaseCallCount(::uploadObservationImage.name)
        callParameters[::uploadObservationImage.name] = UploadImageCallParameters(
            eventRemoteId = observationRemoteId,
            uuid = uuid,
            contentType = contentType,
            file = file,
        )
        return respond(uploadObservationImageResponse)
    }

    override suspend fun deleteObservationImage(imageUuid: String): NetworkResponse<Unit> {
        increaseCallCount(::deleteObservationImage.name)
        callParameters[::deleteObservationImage.name] = imageUuid
        return respond(deleteObservationImageResponse)
    }

    override suspend fun fetchHarvests(modifiedAfter: LocalDateTime?): NetworkResponse<HarvestPageDTO> {
        increaseCallCount(::fetchHarvests.name)
        callParameters[::fetchHarvests.name] = modifiedAfter
        return respond(fetchHarvestPageResponse)
    }

    override suspend fun createHarvest(harvest: HarvestCreateDTO): NetworkResponse<HarvestDTO> {
        increaseCallCount(::createHarvest.name)
        callParameters[::createHarvest.name] = harvest
        return respond(createHarvestResponse)
    }

    override suspend fun updateHarvest(harvest: HarvestDTO): NetworkResponse<HarvestDTO> {
        increaseCallCount(::updateHarvest.name)
        callParameters[::updateHarvest.name] = harvest
        return respond(updateHarvestResponse)
    }

    override suspend fun deleteHarvest(harvestRemoteId: Long): NetworkResponse<Unit> {
        increaseCallCount(::deleteHarvest.name)
        callParameters[::deleteHarvest.name] = harvestRemoteId
        return respond(deleteHarvestResponse)
    }

    override suspend fun fetchDeletedHarvests(deletedAfter: LocalDateTime?): NetworkResponse<DeletedHarvestsDTO> {
        increaseCallCount(::fetchDeletedHarvests.name)
        callParameters[::fetchDeletedHarvests.name] = deletedAfter
        return respond(fetchDeletedHarvestsResponse)
    }

    override suspend fun uploadHarvestImage(
        harvestRemoteId: Long,
        uuid: String,
        contentType: String,
        file: CommonFile
    ): NetworkResponse<Unit> {
        increaseCallCount(::uploadHarvestImage.name)
        callParameters[::uploadHarvestImage.name] = UploadImageCallParameters(
            eventRemoteId = harvestRemoteId,
            uuid = uuid,
            contentType = contentType,
            file = file,
        )
        return respond(uploadHarvestImageResponse)
    }

    override suspend fun deleteHarvestImage(imageUuid: String): NetworkResponse<Unit> {
        increaseCallCount(::deleteHarvestImage.name)
        callParameters[::deleteHarvestImage.name] = imageUuid
        return respond(deleteHarvestImageResponse)
    }

    override suspend fun fetchSrvaMetadata(): NetworkResponse<SrvaMetadataDTO> {
        increaseCallCount(::fetchSrvaMetadata.name)
        return respond(fetchSrvaMetadataResponse)
    }

    override suspend fun fetchObservationMetadata(): NetworkResponse<ObservationMetadataDTO> {
        increaseCallCount(::fetchObservationMetadata.name)
        return respond(fetchObservationMetadataResponse)
    }

    override suspend fun fetchShootingTestCalendarEvents(): NetworkResponse<ShootingTestCalendarEventsDTO> {
        increaseCallCount(::fetchShootingTestCalendarEvents.name)
        return respond(fetchShootingTestCalendarEventsResponse)
    }

    override suspend fun fetchShootingTestCalendarEvent(
        calendarEventId: CalendarEventId,
    ): NetworkResponse<ShootingTestCalendarEventDTO> {
        increaseCallCount(::fetchShootingTestCalendarEvent.name)
        callParameters[::fetchShootingTestCalendarEvent.name] = calendarEventId
        return respond(fetchShootingTestCalendarEventResponse)
    }

    override suspend fun openShootingTestEvent(openShootingTestEventDTO: OpenShootingTestEventDTO): NetworkResponse<Unit> {
        increaseCallCount(::openShootingTestEvent.name)
        callParameters[::openShootingTestEvent.name] = openShootingTestEventDTO
        return respond(openShootingTestEventResponse)
    }

    override suspend fun closeShootingTestEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<Unit> {
        increaseCallCount(::closeShootingTestEvent.name)
        callParameters[::closeShootingTestEvent.name] = shootingTestEventId
        return respond(closeShootingTestEventResponse)
    }

    override suspend fun reopenShootingTestEvent(shootingTestEventId: ShootingTestEventId): NetworkResponse<Unit> {
        increaseCallCount(::reopenShootingTestEvent.name)
        callParameters[::reopenShootingTestEvent.name] = shootingTestEventId
        return respond(reopenShootingTestEventResponse)
    }

    override suspend fun fetchAvailableShootingTestOfficialsForEvent(
        shootingTestEventId: ShootingTestEventId,
    ): NetworkResponse<ShootingTestOfficialsDTO> {
        increaseCallCount(::fetchAvailableShootingTestOfficialsForEvent.name)
        callParameters[::fetchAvailableShootingTestOfficialsForEvent.name] = shootingTestEventId
        return respond(fetchAvailableShootingTestOfficialsForEventResponse)
    }

    override suspend fun fetchSelectedShootingTestOfficialsForEvent(
        shootingTestEventId: ShootingTestEventId,
    ): NetworkResponse<ShootingTestOfficialsDTO> {
        increaseCallCount(::fetchSelectedShootingTestOfficialsForEvent.name)
        callParameters[::fetchSelectedShootingTestOfficialsForEvent.name] = shootingTestEventId
        return respond(fetchSelectedShootingTestOfficialsForEventResponse)
    }

    override suspend fun fetchAvailableShootingTestOfficialsForRhy(
        rhyId: Long,
    ): NetworkResponse<ShootingTestOfficialsDTO> {
        increaseCallCount(::fetchAvailableShootingTestOfficialsForRhy.name)
        callParameters[::fetchAvailableShootingTestOfficialsForRhy.name] = rhyId
        return respond(fetchAvailableShootingTestOfficialsForRhyResponse)
    }

    override suspend fun updateShootingTestOfficials(
        updateShootingTestOfficialsDTO: UpdateShootingTestOfficialsDTO,
    ): NetworkResponse<Unit> {
        increaseCallCount(::updateShootingTestOfficials.name)
        callParameters[::updateShootingTestOfficials.name] = updateShootingTestOfficialsDTO
        return respond(updateShootingTestOfficialsResponse)
    }

    override suspend fun searchShootingTestPersonWithSsn(
        shootingTestEventId: ShootingTestEventId,
        ssn: String,
    ): NetworkResponse<ShootingTestPersonDTO> {
        increaseCallCount(::searchShootingTestPersonWithSsn.name)
        callParameters[::searchShootingTestPersonWithSsn.name] = Pair(shootingTestEventId, ssn)
        return respond(searchShootingTestPersonWithSsnResponse)
    }

    override suspend fun searchShootingTestPersonWithHunterNumber(
        shootingTestEventId: ShootingTestEventId,
        hunterNumber: String,
    ): NetworkResponse<ShootingTestPersonDTO> {
        increaseCallCount(::searchShootingTestPersonWithHunterNumber.name)
        callParameters[::searchShootingTestPersonWithHunterNumber.name] = Pair(shootingTestEventId, hunterNumber)
        return respond(searchShootingTestPersonWithHunterNumberResponse)
    }

    override suspend fun fetchShootingTestParticipants(
        shootingTestEventId: ShootingTestEventId,
    ): NetworkResponse<ShootingTestParticipantsDTO> {
        increaseCallCount(::fetchShootingTestParticipants.name)
        callParameters[::fetchShootingTestParticipants.name] = shootingTestEventId
        return respond(fetchShootingTestParticipantsResponse)
    }

    override suspend fun fetchShootingTestParticipant(
        participantId: ShootingTestParticipantId,
    ): NetworkResponse<ShootingTestParticipantDTO> {
        increaseCallCount(::fetchShootingTestParticipant.name)
        callParameters[::fetchShootingTestParticipant.name] = participantId
        return respond(fetchShootingTestParticipantResponse)
    }

    override suspend fun addShootingTestParticipant(
        shootingTestEventId: ShootingTestEventId,
        participant: ShootingTestParticipantCreateDTO
    ): NetworkResponse<Unit> {
        increaseCallCount(::addShootingTestParticipant.name)
        callParameters[::addShootingTestParticipant.name] = Pair(shootingTestEventId, participant)
        return respond(addShootingTestParticipantResponse)
    }

    override suspend fun fetchShootingTestParticipantDetailed(
        participantId: ShootingTestParticipantId,
    ): NetworkResponse<ShootingTestParticipantDetailedDTO> {
        increaseCallCount(::fetchShootingTestParticipantDetailed.name)
        callParameters[::fetchShootingTestParticipantDetailed.name] = participantId
        return respond(fetchShootingTestParticipantDetailedResponse)
    }

    override suspend fun fetchShootingTestAttempt(
        shootingTestAttemptId: ShootingTestAttemptId,
    ): NetworkResponse<ShootingTestAttemptDTO> {
        increaseCallCount(::fetchShootingTestAttempt.name)
        callParameters[::fetchShootingTestAttempt.name] = shootingTestAttemptId
        return respond(fetchShootingTestAttemptResponse)
    }

    override suspend fun addShootingTestAttempt(attempt: ShootingTestAttemptCreateDTO): NetworkResponse<Unit> {
        increaseCallCount(::addShootingTestAttempt.name)
        callParameters[::addShootingTestAttempt.name] = attempt
        return respond(addShootingTestAttemptResponse)
    }

    override suspend fun updateShootingTestAttempt(attempt: ShootingTestAttemptDTO): NetworkResponse<Unit> {
        increaseCallCount(::updateShootingTestAttempt.name)
        callParameters[::updateShootingTestAttempt.name] = attempt
        return respond(updateShootingTestAttemptResponse)
    }

    override suspend fun removeShootingTestAttempt(shootingTestAttemptId: ShootingTestAttemptId): NetworkResponse<Unit> {
        increaseCallCount(::removeShootingTestAttempt.name)
        callParameters[::removeShootingTestAttempt.name] = shootingTestAttemptId
        return respond(removeShootingTestAttemptResponse)
    }

    override suspend fun updateShootingTestPaymentForParticipant(
        participantId: ShootingTestParticipantId,
        paymentUpdateDTO: ShootingTestPaymentUpdateDTO
    ): NetworkResponse<Unit> {
        increaseCallCount(::updateShootingTestPaymentForParticipant.name)
        callParameters[::updateShootingTestPaymentForParticipant.name] = Pair(participantId, paymentUpdateDTO)
        return respond(updateShootingTestPaymentForParticipantResponse)
    }

    override suspend fun completeAllPaymentsForParticipant(
        participantId: ShootingTestParticipantId,
        participantRev: Revision,
    ): NetworkResponse<Unit> {
        increaseCallCount(::completeAllPaymentsForParticipant.name)
        callParameters[::completeAllPaymentsForParticipant.name] = Pair(participantId, participantRev)
        return respond(completeAllPaymentsForParticipantResponse)
    }

    override suspend fun fetchHarvestSeasons(huntingYear: HuntingYear): NetworkResponse<List<HarvestSeasonDTO>> {
        increaseCallCount(::fetchHarvestSeasons.name)
        callParameters[::fetchHarvestSeasons.name] = huntingYear
        return respond(fetchHarvestSeasonsResponse)
    }

    override suspend fun fetchMetsahallitusPermits(): NetworkResponse<List<CommonMetsahallitusPermitDTO>> {
        increaseCallCount(::fetchMetsahallitusPermits.name)
        return respond(fetchMetsahallitusPermitsResponse)
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

    fun <R> callCount(method: KCallable<R>): Int {
        return callCount(methodName = method.name)
    }

    /**
     * Returns how many times all BackendAPI functions all called combined.
     */
    fun totalCallCount(): Int {
        return callCounts.values.sum()
    }

    /**
     * Returns the last parameter the given method was called with.
     *
     * Only last call is saved.
     */
    fun callParameter(methodName: String): Any? {
        return callParameters[methodName]
    }

    fun <R> callParameter(method: KCallable<R>): Any? {
        return callParameter(methodName = method.name)
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

    data class UploadHuntingControlEventAttachmentCallParameters(
        val eventRemoteId: Long,
        val uuid: String,
        val fileName: String,
        val contentType: String,
        val file: CommonFile,
    )

    data class UploadImageCallParameters(
        val eventRemoteId: Long,
        val uuid: String,
        val contentType: String,
        val file: CommonFile,
    )
}
