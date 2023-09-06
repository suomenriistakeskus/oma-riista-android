package fi.riista.common.domain.harvest.model

import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GreySealHuntingMethod
import fi.riista.common.domain.model.HarvestReportState
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.StateAcceptedToHarvestPermit
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.asSearchableOrganization
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import kotlinx.serialization.Serializable

typealias CommonHarvestId = Long

@Serializable
data class CommonHarvest(
    val localId: Long?,
    val localUrl: String?, // Used as localId on iOS
    val id: CommonHarvestId?,
    val rev: Int?,
    val species: Species,
    val geoLocation: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val description: String?,
    val canEdit: Boolean,
    val modified: Boolean,
    val deleted: Boolean,
    val images: EntityImages,
    val specimens: List<CommonHarvestSpecimen>,
    val amount: Int,
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
    val rejected: Boolean,
    val feedingPlace: Boolean?,
    val taigaBeanGoose: Boolean?,
    val greySealHuntingMethod: BackendEnum<GreySealHuntingMethod>,
    val actorInfo: GroupHuntingPerson,
    val selectedClub: Organization?, // club for which this harvest has been recorded / logged
) {
    val harvestState: HarvestState? by lazy {
        HarvestState.combinedState(
            harvestReportState = harvestReportState.value,
            stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit.value,
            harvestReportRequired = harvestReportRequired,
        )
    }
}


internal fun CommonHarvest.toCommonHarvestData(): CommonHarvestData {
    return CommonHarvestData(
        localId = localId,
        localUrl = localUrl,
        id = id,
        rev = rev,
        species = species,
        location = geoLocation.asKnownLocation(),
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        images = images,
        specimens = specimens.map { it.toCommonSpecimenData() },
        amount = amount,
        huntingDayId = null,
        authorInfo = null,
        actorInfo = actorInfo,
        selectedClub = selectedClub.asSearchableOrganization(),
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
        feedingPlace = feedingPlace,
        taigaBeanGoose = taigaBeanGoose,
        greySealHuntingMethod = greySealHuntingMethod,
    )
}

internal fun CommonHarvestData.toCommonHarvest(): CommonHarvest? {
    val geoLocation = location.etrsLocationOrNull ?: return null

    // amount is nullable in harvest data (user can enter the value). Ensure proper
    // value is set in the actual harvest.
    val specimenAmount = amount ?: specimens.size
    if (specimenAmount < 1) {
        return null
    }

    return CommonHarvest(
        localId = localId,
        localUrl = localUrl,
        id = id,
        rev = rev,
        species = species,
        geoLocation = geoLocation,
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        images = images,
        specimens = specimens.map { it.toCommonHarvestSpecimen() },
        amount = specimenAmount,
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
        feedingPlace = feedingPlace,
        taigaBeanGoose = taigaBeanGoose,
        greySealHuntingMethod = greySealHuntingMethod,
        actorInfo = actorInfo,
        selectedClub = selectedClub.organization,
    )
}
