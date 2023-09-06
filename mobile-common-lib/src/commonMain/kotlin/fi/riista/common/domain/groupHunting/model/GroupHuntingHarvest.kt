package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestCreateDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.harvest.model.toCommonHarvestSpecimen
import fi.riista.common.domain.harvest.model.toCommonSpecimenData
import fi.riista.common.domain.harvest.model.toHarvestSpecimenDTO
import fi.riista.common.domain.model.*
import fi.riista.common.model.*

typealias GroupHuntingHarvestId = Long

data class GroupHuntingHarvest(
    val id: GroupHuntingHarvestId,
    val rev: Int,
    val gameSpeciesCode: SpeciesCode,
    val geoLocation: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val description: String?,
    val canEdit: Boolean,
    val imageIds: List<Uuid>,
    val specimens: List<CommonHarvestSpecimen>,
    val amount: Int?,
    val huntingDayId: GroupHuntingDayId?,
    val authorInfo: PersonWithHunterNumber,
    val actorInfo: PersonWithHunterNumber,
    val harvestSpecVersion: Int,
    val harvestReportRequired: Boolean,
    val harvestReportState: BackendEnum<HarvestReportState>,
    val permitNumber: String?,
    val permitType: String?,
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

internal fun GroupHuntingHarvest.toCommonHarvestData(groupMembers: List<HuntingGroupMember>): CommonHarvestData {
    return CommonHarvestData(
        localId = null,
        localUrl = null,
        id = id,
        rev = rev,
        species = Species.Known(speciesCode = gameSpeciesCode),
        location = geoLocation.asKnownLocation(),
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        modified = false,
        deleted = false,
        images = EntityImages(
            remoteImageIds = imageIds,
            localImages = emptyList(),
        ),
        specimens = specimens.map { it.toCommonSpecimenData() },
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
        selectedClub = SearchableOrganization.Unknown,
        harvestSpecVersion = harvestSpecVersion,
        harvestReportRequired = harvestReportRequired,
        harvestReportState = harvestReportState,
        permitNumber = permitNumber,
        permitType = permitType,
        stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit,
        deerHuntingType = deerHuntingType,
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        mobileClientRefId = mobileClientRefId,
        harvestReportDone = harvestReportDone,
        rejected = rejected,
        feedingPlace = null,
        taigaBeanGoose = null,
        greySealHuntingMethod = BackendEnum.create(null),
    )
}

internal fun CommonHarvestData.toGroupHuntingHarvest(): GroupHuntingHarvest? {
    val actor = actorInfo.personWithHunterNumber
    val speciesCode = species.knownSpeciesCodeOrNull()
    if (id == null || rev == null || speciesCode == null || authorInfo == null || actor == null) {
        return null
    }

    val geoLocation = (location as? CommonLocation.Known)?.etrsLocation ?: return null

    return GroupHuntingHarvest(
        id = id,
        rev = rev,
        gameSpeciesCode = speciesCode,
        geoLocation = geoLocation,
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        imageIds = images.remoteImageIds,
        specimens = specimens.map { it.toCommonHarvestSpecimen() },
        amount = amount,
        huntingDayId = huntingDayId,
        authorInfo = authorInfo,
        actorInfo = actor,
        harvestSpecVersion = harvestSpecVersion,
        harvestReportRequired = harvestReportRequired,
        harvestReportState = harvestReportState,
        permitNumber = permitNumber,
        permitType = permitType,
        stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit,
        deerHuntingType = deerHuntingType,
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        mobileClientRefId = mobileClientRefId,
        harvestReportDone = harvestReportDone,
        rejected = rejected,
    )
}

internal fun CommonHarvestData.toGroupHuntingHarvestCreateDTO(): GroupHuntingHarvestCreateDTO? {
    val actor = actorInfo.personWithHunterNumber ?: return null
    val speciesCode = species.knownSpeciesCodeOrNull() ?: return null
    val geoLocation = (location as? CommonLocation.Known)?.etrsLocation ?: return null

    return GroupHuntingHarvestCreateDTO(
        gameSpeciesCode = speciesCode,
        geoLocation = geoLocation.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        description = description,
        canEdit = canEdit,
        imageIds = images.remoteImageIds,
        specimens = specimens.map { it.toCommonHarvestSpecimen().toHarvestSpecimenDTO() },
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
