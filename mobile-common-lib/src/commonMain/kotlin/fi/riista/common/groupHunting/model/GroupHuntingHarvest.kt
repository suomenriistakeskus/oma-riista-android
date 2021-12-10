package fi.riista.common.groupHunting.model

import fi.riista.common.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.model.*

typealias GroupHuntingHarvestId = Long

data class GroupHuntingHarvest(
    val id: GroupHuntingHarvestId,
    val rev: Int,
    val gameSpeciesCode: SpeciesCode,
    val geoLocation: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<Uuid>,
    val specimens: List<HarvestSpecimen>,
    val amount: Int? = null,
    val huntingDayId: GroupHuntingDayId? = null,
    val authorInfo: PersonWithHunterNumber,
    val actorInfo: PersonWithHunterNumber,
    val harvestSpecVersion: Int,
    val harvestReportRequired: Boolean,
    val harvestReportState: BackendEnum<HarvestReportState>,
    val permitNumber: String? = null,
    val stateAcceptedToHarvestPermit: BackendEnum<StateAcceptedToHarvestPermit>,
    val deerHuntingType: BackendEnum<DeerHuntingType>,
    val deerHuntingOtherTypeDescription: String?,
    val mobileClientRefId: Long?,
    val harvestReportDone: Boolean,
    val rejected: Boolean = false,
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

internal fun GroupHuntingHarvest.toGroupHuntingHarvestDTO(): GroupHuntingHarvestDTO {
    return GroupHuntingHarvestDTO(
            id = id,
            rev = rev,
            gameSpeciesCode = gameSpeciesCode,
            geoLocation = geoLocation.toETRMSGeoLocationDTO(),
            pointOfTime = pointOfTime.toStringISO8601(),
            description = description,
            canEdit = canEdit,
            imageIds = imageIds,
            specimens = specimens.map { it.toHarvestSpecimenDTO() },
            amount = amount,
            huntingDayId = huntingDayId?.remoteId,
            authorInfo = authorInfo.toPersonWithHunterNumberDTO(),
            actorInfo = actorInfo.toPersonWithHunterNumberDTO(),
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
