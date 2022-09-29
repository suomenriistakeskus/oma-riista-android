package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.dto.*
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.UuidDTO
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingHarvestCreateDTO(
    val type: String = "HARVEST",
    val gameSpeciesCode: SpeciesCodeDTO,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<UuidDTO>,
    val specimens: List<HarvestSpecimenDTO> = listOf(),
    val amount: Int? = null,
    val huntingDayId: Long? = null,
    val authorInfo: PersonWithHunterNumberDTO? = null,
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
