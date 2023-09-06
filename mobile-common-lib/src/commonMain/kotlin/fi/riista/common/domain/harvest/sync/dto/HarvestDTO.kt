package fi.riista.common.domain.harvest.sync.dto

import fi.riista.common.domain.dto.HarvestSpecimenDTO
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.dto.toHarvestSpecimen
import fi.riista.common.domain.dto.toPersonWithHunterNumber
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.toHarvestSpecimenDTO
import fi.riista.common.domain.huntingclub.dto.HuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.dto.toHuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.dto.toOrganization
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.HarvestReportState
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.toPersonWithHunterNumberDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toETRMSGeoLocation
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

@Serializable
internal data class HarvestDTO(
    val id: Long,
    val rev: Int,
    val type: String,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val gameSpeciesCode: Int? = null,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<String>,

    val harvestReportRequired: Boolean,
    val harvestReportState: String? = null,
    val permitNumber: String? = null,
    val permitType: String? = null,
    val stateAcceptedToHarvestPermit: String? = null,
    val specimens: List<HarvestSpecimenDTO>? = null,
    val amount: Int,

    val deerHuntingType: String? = null,
    val deerHuntingOtherTypeDescription: String? = null,
    val harvestReportDone: Boolean,
    val feedingPlace: Boolean? = null,
    val taigaBeanGoose: Boolean? = null,
    val huntingMethod: String? = null,

    val actorInfo: PersonWithHunterNumberDTO? = null,
    val selectedHuntingClub: HuntingClubNameAndCodeDTO? = null,

    val apiVersion: Int,
    val mobileClientRefId: Long? = null,
    val harvestSpecVersion: Int,
)

internal fun HarvestDTO.toCommonHarvest(
    localId: Long? = null,
    modified: Boolean = false,
    deleted: Boolean = false,
): CommonHarvest? {

    val pointOfTime = pointOfTime.toLocalDateTime() ?: return null
    val harvestReportState: BackendEnum<HarvestReportState> = harvestReportState.toBackendEnum()

    return CommonHarvest(
        localId = localId,
        localUrl = null,
        id = id,
        rev = rev,
        species = when (gameSpeciesCode) {
            null -> Species.Other
            else -> Species.Known(gameSpeciesCode)
        },
        geoLocation = geoLocation.toETRMSGeoLocation(),
        pointOfTime = pointOfTime,
        description = description,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        images = EntityImages(
            remoteImageIds = imageIds,
            localImages = emptyList()
        ),
        specimens = specimens?.map { it.toHarvestSpecimen() } ?: emptyList(),
        amount = amount,
        harvestSpecVersion = harvestSpecVersion,
        harvestReportRequired = harvestReportRequired,
        harvestReportState = harvestReportState,
        permitNumber = permitNumber,
        permitType = permitType,
        stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit.toBackendEnum(),
        deerHuntingType = deerHuntingType.toBackendEnum(),
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        mobileClientRefId = mobileClientRefId,
        harvestReportDone = harvestReportDone,
        rejected = harvestReportState == HarvestReportState.REJECTED.toBackendEnum(),
        feedingPlace = feedingPlace,
        taigaBeanGoose = taigaBeanGoose,
        greySealHuntingMethod = huntingMethod.toBackendEnum(),
        actorInfo = actorInfo?.let {
            GroupHuntingPerson.Guest(actorInfo.toPersonWithHunterNumber())
        } ?: GroupHuntingPerson.Unknown,
        selectedClub = selectedHuntingClub?.toOrganization(),
    )
}

internal fun CommonHarvest.toHarvestDTO(): HarvestDTO? {
    if (id == null || rev == null) {
        return null
    }

    return HarvestDTO(
        id = id,
        rev = rev,
        type = "HARVEST",
        geoLocation = geoLocation.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        gameSpeciesCode = species.knownSpeciesCodeOrNull(),
        description = description,
        canEdit = canEdit,
        imageIds = images.remoteImageIds,
        harvestReportRequired = harvestReportRequired,
        harvestReportState = harvestReportState.rawBackendEnumValue,
        permitNumber = permitNumber,
        permitType = permitType,
        stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit.rawBackendEnumValue,
        specimens = specimens.map { it.toHarvestSpecimenDTO() },
        amount = amount,
        deerHuntingType = deerHuntingType.rawBackendEnumValue,
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        harvestReportDone = harvestReportDone,
        feedingPlace = feedingPlace,
        taigaBeanGoose = taigaBeanGoose,
        huntingMethod = greySealHuntingMethod.rawBackendEnumValue,
        apiVersion = 2, // Depricated
        mobileClientRefId = mobileClientRefId,
        harvestSpecVersion = harvestSpecVersion,
        actorInfo = when (actorInfo) {
            is GroupHuntingPerson.Guest -> actorInfo.personInformation.toPersonWithHunterNumberDTO()
            else -> null
        },
        selectedHuntingClub = selectedClub?.toHuntingClubNameAndCodeDTO(),
    )
}
