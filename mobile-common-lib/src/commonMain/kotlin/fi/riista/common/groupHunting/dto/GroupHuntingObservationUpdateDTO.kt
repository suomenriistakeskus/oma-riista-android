package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.*
import kotlinx.serialization.Serializable

/**
 Same as GroupHuntingObservationDTO except without totalSpecimenAmount (backend doesn't allow it in update request)
 */
@Serializable
data class GroupHuntingObservationUpdateDTO(
    val id: Long,
    val rev: Int,
    val type: String = "OBSERVATION",
    val gameSpeciesCode: SpeciesCodeDTO,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<UuidDTO>,
    val specimens: List<ObservationSpecimenDTO>? = null,
    val amount: Int? = null,
    val huntingDayId: Long? = null,
    val authorInfo: PersonWithHunterNumberDTO,
    val actorInfo: PersonWithHunterNumberDTO,
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
