package fi.riista.common.domain.harvest.sync.dto

import fi.riista.common.domain.dto.HarvestSpecimenDTO
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.toHarvestSpecimenDTO
import fi.riista.common.domain.huntingclub.dto.HuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.dto.toHuntingClubNameAndCodeDTO
import fi.riista.common.domain.model.toPersonWithHunterNumberDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.model.toETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

@Serializable
internal data class HarvestCreateDTO(
    val type: String,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val gameSpeciesCode: Int?,
    val description: String?,
    val canEdit: Boolean,
    val imageIds: List<String>,

    val harvestReportRequired: Boolean,
    val harvestReportState: String?,
    val permitNumber: String?,
    val permitType: String?,
    val stateAcceptedToHarvestPermit: String?,
    val specimens: List<HarvestSpecimenDTO>?,
    val amount: Int,

    val deerHuntingType: String?,
    val deerHuntingOtherTypeDescription: String?,
    val harvestReportDone: Boolean,
    val feedingPlace: Boolean?,
    val taigaBeanGoose: Boolean?,
    val huntingMethod: String?,

    val actorInfo: PersonWithHunterNumberDTO?,
    val selectedHuntingClub: HuntingClubNameAndCodeDTO?,

    val apiVersion: Int,
    val mobileClientRefId: Long?,
    val harvestSpecVersion: Int,
)

internal fun CommonHarvest.toHarvestCreateDTO(): HarvestCreateDTO {
    return HarvestCreateDTO(
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
        actorInfo = when (actorInfo) {
            is GroupHuntingPerson.Guest -> actorInfo.personInformation.toPersonWithHunterNumberDTO()
            else -> null
        },
        selectedHuntingClub = selectedClub?.toHuntingClubNameAndCodeDTO(),
        apiVersion = 2, // Depricated
        mobileClientRefId = mobileClientRefId,
        harvestSpecVersion = harvestSpecVersion,
    )
}
