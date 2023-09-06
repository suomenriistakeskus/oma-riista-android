package fi.riista.common.domain.observation.sync.dto

import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.toObservationSpecimenDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.model.toETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

@Serializable
data class ObservationCreateDTO(
    val type: String,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val gameSpeciesCode: Int?,
    val description: String?,
    val canEdit: Boolean,
    val imageIds: List<String>,
    val observationType: String,
    val totalSpecimenAmount: Int?,
    val observationCategory: String?,
    val deerHuntingType: String?,
    val deerHuntingTypeDescription: String?,
    val mooselikeMaleAmount: Int?,
    val mooselikeFemaleAmount: Int?,
    val mooselikeCalfAmount: Int?,
    val mooselikeFemale1CalfAmount: Int?,
    val mooselikeFemale2CalfsAmount: Int?,
    val mooselikeFemale3CalfsAmount: Int?,
    val mooselikeFemale4CalfsAmount: Int?,
    val mooselikeUnknownSpecimenAmount: Int?,
    val inYardDistanceToResidence: Int?,
    val verifiedByCarnivoreAuthority: Boolean?,
    val observerName: String?,
    val observerPhoneNumber: String?,
    val officialAdditionalInfo: String?,
    val specimens: List<CommonObservationSpecimenDTO>?,
    val pack: Boolean?,
    val litter: Boolean?,
    val mobileClientRefId: Long?,
    val observationSpecVersion: Int,
)

fun CommonObservation.toObservationCreateDTO(): ObservationCreateDTO? {
    val observationType = observationType.rawBackendEnumValue ?: return null

    return ObservationCreateDTO(
        type = "OBSERVATION",
        geoLocation = location.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        gameSpeciesCode = species.knownSpeciesCodeOrNull(),
        description = description,
        canEdit = canEdit,
        imageIds = images.remoteImageIds,
        observationType = observationType,
        totalSpecimenAmount = totalSpecimenAmount,
        observationCategory = observationCategory.rawBackendEnumValue,
        deerHuntingType = deerHuntingType.rawBackendEnumValue,
        deerHuntingTypeDescription = deerHuntingOtherTypeDescription,
        mooselikeMaleAmount = mooselikeMaleAmount,
        mooselikeFemaleAmount = mooselikeFemaleAmount,
        mooselikeCalfAmount = mooselikeCalfAmount,
        mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount,
        mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount,
        mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount,
        mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount,
        mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount,
        inYardDistanceToResidence = inYardDistanceToResidence,
        verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority,
        observerName = observerName,
        observerPhoneNumber = observerPhoneNumber,
        officialAdditionalInfo = officialAdditionalInfo,
        specimens = specimens?.map { it.toObservationSpecimenDTO() },
        pack = pack,
        litter = litter,
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,
    )
}
