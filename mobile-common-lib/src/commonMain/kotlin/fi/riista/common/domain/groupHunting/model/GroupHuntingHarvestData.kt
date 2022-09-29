package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestCreateDTO
import fi.riista.common.domain.model.*
import fi.riista.common.model.*
import kotlinx.serialization.Serializable

/**
 * Data needed when creating/editing new group hunting harvests.
 */
@Serializable
data class GroupHuntingHarvestData(
    val id: GroupHuntingHarvestId? = null,
    val rev: Int? = null,
    val gameSpeciesCode: SpeciesCode,
    val geoLocation: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<Uuid>,
    val specimens: List<HarvestSpecimen>,
    val amount: Int? = null,
    val huntingDayId: GroupHuntingDayId? = null,
    val authorInfo: PersonWithHunterNumber? = null,
    val actorInfo: GroupHuntingPerson,
    val harvestSpecVersion: Int,
    val harvestReportRequired: Boolean,
    val harvestReportState: BackendEnum<HarvestReportState>,
    val permitNumber: String? = null,
    val stateAcceptedToHarvestPermit: BackendEnum<StateAcceptedToHarvestPermit>,
    val deerHuntingType: BackendEnum<DeerHuntingType>,
    val deerHuntingOtherTypeDescription: String?,
    val mobileClientRefId: Long?,
    val harvestReportDone: Boolean,
    val rejected: Boolean,
) {
    val acceptStatus: AcceptStatus
        get() {
            return if (rejected) {
                AcceptStatus.REJECTED
            } else {
                if (huntingDayId != null) {
                    AcceptStatus.ACCEPTED
                } else {
                    AcceptStatus.PROPOSED
                }
            }
        }
}

fun GroupHuntingHarvest.toGroupHuntingHarvestData(groupMembers: List<HuntingGroupMember>): GroupHuntingHarvestData {
    return GroupHuntingHarvestData(
        id = id,
        rev = rev,
        gameSpeciesCode = gameSpeciesCode,
        geoLocation = geoLocation,
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        imageIds = imageIds,
        specimens = specimens,
        amount = amount,
        huntingDayId = huntingDayId,
        authorInfo = authorInfo,
        actorInfo = actorInfo.let { person ->
            if (groupMembers.isMember(person)) {
                person.asGroupMember()
            } else {
                person.asGuest()
            }
        },
        harvestSpecVersion = harvestSpecVersion,
        harvestReportRequired = harvestReportRequired,
        harvestReportState = harvestReportState,
        permitNumber = permitNumber,
        stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit,
        deerHuntingType = deerHuntingType,
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        mobileClientRefId = mobileClientRefId,
        harvestReportDone = harvestReportDone,
        rejected = rejected,
    )
}

fun GroupHuntingHarvestData.toGroupHuntingHarvest(): GroupHuntingHarvest? {
    val actor = actorInfo.personWithHunterNumber
    if (id == null || rev == null || authorInfo == null || actor == null) {
        return null
    }

    return GroupHuntingHarvest(
        id = id,
        rev = rev,
        gameSpeciesCode = gameSpeciesCode,
        geoLocation = geoLocation,
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        imageIds = imageIds,
        specimens = specimens,
        amount = amount,
        huntingDayId = huntingDayId,
        authorInfo = authorInfo,
        actorInfo = actor,
        harvestSpecVersion = harvestSpecVersion,
        harvestReportRequired = harvestReportRequired,
        harvestReportState = harvestReportState,
        permitNumber = permitNumber,
        stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit,
        deerHuntingType = deerHuntingType,
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        mobileClientRefId = mobileClientRefId,
        harvestReportDone = harvestReportDone,
        rejected = rejected,
    )
}

internal fun GroupHuntingHarvestData.toGroupHuntingHarvestCreateDTO(): GroupHuntingHarvestCreateDTO? {
    val actor = actorInfo.personWithHunterNumber ?: return null

    return GroupHuntingHarvestCreateDTO(
            gameSpeciesCode = gameSpeciesCode,
            geoLocation = geoLocation.toETRMSGeoLocationDTO(),
            pointOfTime = pointOfTime.toStringISO8601(),
            description = description,
            canEdit = canEdit,
            imageIds = imageIds,
            specimens = specimens.map { it.toHarvestSpecimenDTO() },
            amount = specimens.size,
            huntingDayId = huntingDayId?.remoteId,
            authorInfo = authorInfo?.toPersonWithHunterNumberDTO(),
            actorInfo = actor.toPersonWithHunterNumberDTO(),
            harvestSpecVersion = harvestSpecVersion,
            harvestReportRequired = harvestReportRequired,
            harvestReportState = harvestReportState.rawBackendEnumValue,
            permitNumber = permitNumber,
            stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit.rawBackendEnumValue,
            deerHuntingType = deerHuntingType.rawBackendEnumValue,
            deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
            mobileClientRefId = mobileClientRefId,
            harvestReportDone = harvestReportDone,
    )
}
