package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.dto.*
import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.UuidDTO
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingObservationCreateDTO(
    val type: String = "OBSERVATION",
    val gameSpeciesCode: SpeciesCodeDTO,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<UuidDTO>,
    val specimens: List<CommonObservationSpecimenDTO>? = null,
    val amount: Int? = null,
    val huntingDayId: Long? = null,
    val authorInfo: PersonWithHunterNumberDTO?,
    val actorInfo: PersonWithHunterNumberDTO?,
    val observerName: String? = null,
    val observerPhoneNumber: String? =  null,
    val linkedToGroupHuntingDay: Boolean,
    val observationType: ObservationTypeDTO,
    val observationCategory: ObservationCategoryDTO? = null,
    val mobileClientRefId: Long? = null,
    val observationSpecVersion: Int,
    val litter: Boolean? = null,
    val pack: Boolean? = null,
    val deerHuntingType: DeerHuntingTypeDTO? = null,
    val deerHuntingTypeDescription: String? = null,
    val mooselikeMaleAmount: Int? = null,
    val mooselikeFemaleAmount: Int? = null,
    val mooselikeCalfAmount: Int? = null,
    val mooselikeFemale1CalfAmount: Int? = null,
    val mooselikeFemale2CalfsAmount: Int? = null,
    val mooselikeFemale3CalfsAmount: Int? = null,
    val mooselikeFemale4CalfsAmount: Int? = null,
    val mooselikeUnknownSpecimenAmount: Int? = null,
    val inYardDistanceToResidence: Int? = null,
    val verifiedByCarnivoreAuthority: Boolean? = null,
    val officialAdditionalInfo: String? = null,
)
