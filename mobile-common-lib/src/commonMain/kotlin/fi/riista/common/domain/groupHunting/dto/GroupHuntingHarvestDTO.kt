package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.dto.*
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.dto.*
import fi.riista.common.model.BackendId
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingHarvestDTO(
    val id: Long,
    val rev: Int,
    val type: String = "HARVEST",
    val gameSpeciesCode: SpeciesCodeDTO,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<UuidDTO>,
    val specimens: List<HarvestSpecimenDTO> = listOf(),
    val amount: Int? = null,
    val huntingDayId: BackendId? = null,
    val authorInfo: PersonWithHunterNumberDTO,
    val actorInfo: PersonWithHunterNumberDTO,
    val harvestSpecVersion: Int,
    val harvestReportRequired: Boolean,
    val harvestReportState: HarvestReportStateDTO? = null,
    val permitNumber: String? = null,
    val stateAcceptedToHarvestPermit: StateAcceptedToHarvestPermitDTO? = null,
    val deerHuntingType: DeerHuntingTypeDTO? = null,
    val deerHuntingOtherTypeDescription: String? = null,
    val mobileClientRefId: Long? = null,
    val harvestReportDone: Boolean,
)

internal fun GroupHuntingHarvestDTO.toGroupHuntingHarvest() : GroupHuntingHarvest? {
    val harvestPointOfTime = pointOfTime.toLocalDateTime()
            ?: kotlin.run {
                // require valid date in order to create harvest
                return null
            }

    return GroupHuntingHarvest(
            id = id,
            rev = rev,
            gameSpeciesCode = gameSpeciesCode,
            geoLocation = geoLocation.toETRMSGeoLocation(),
            pointOfTime = harvestPointOfTime,
            description = description,
            canEdit = canEdit,
            imageIds = imageIds,
            specimens = specimens.map { it.toHarvestSpecimen() },
            amount = amount,
            huntingDayId = GroupHuntingDayId.remote(huntingDayId),
            authorInfo = authorInfo.toPersonWithHunterNumber(),
            actorInfo = actorInfo.toPersonWithHunterNumber(),
            harvestSpecVersion = harvestSpecVersion,
            harvestReportRequired = harvestReportRequired,
            harvestReportState = harvestReportState.toBackendEnum(),
            permitNumber = permitNumber,
            stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit.toBackendEnum(),
            deerHuntingType = deerHuntingType.toBackendEnum(),
            deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
            mobileClientRefId = mobileClientRefId,
            harvestReportDone = harvestReportDone,
    )
}
