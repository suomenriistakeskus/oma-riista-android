package fi.riista.common.domain.observation.sync.dto

import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.domain.observation.dto.toObservationSpecimen
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.toObservationSpecimenDTO
import fi.riista.common.dto.ETRMSGeoLocationDTO
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toETRMSGeoLocation
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.model.toETRMSGeoLocationDTO
import kotlinx.serialization.Serializable

@Serializable
data class ObservationDTO(
    val id: Long,
    val rev: Long,
    val type: String,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val gameSpeciesCode: Int? = null,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<String>,
    val observationType: String,
    val totalSpecimenAmount: Int? = null,
    val observationCategory: String? = null,
    val deerHuntingType: String? = null,
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
    val observerName: String? = null,
    val observerPhoneNumber: String? = null,
    val officialAdditionalInfo: String? = null,
    val specimens: List<CommonObservationSpecimenDTO>? = null,
    val pack: Boolean? = null,
    val litter: Boolean? = null,
    val mobileClientRefId: Long? = null,
    val observationSpecVersion: Int,
)

fun ObservationDTO.toCommonObservation(
    localId: Long? = null,
    modified: Boolean = false,
    deleted: Boolean = false,
): CommonObservation? {
    val pointOfTime = pointOfTime.toLocalDateTime() ?: return null

    return CommonObservation(
        localId = localId,
        localUrl = null,
        remoteId = id,
        revision = rev,
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,
        species = when (gameSpeciesCode) {
            null -> Species.Other
            else -> Species.Known(gameSpeciesCode)
        },
        observationCategory = observationCategory.toBackendEnum(),
        observationType = observationType.toBackendEnum(),
        deerHuntingType = deerHuntingType.toBackendEnum(),
        deerHuntingOtherTypeDescription = deerHuntingTypeDescription,
        location = geoLocation.toETRMSGeoLocation(),
        pointOfTime = pointOfTime,
        description = description,
        images = EntityImages(
            remoteImageIds = imageIds,
            localImages = listOf()
        ),
        totalSpecimenAmount = totalSpecimenAmount,
        specimens = specimens?.map { specimen -> specimen.toObservationSpecimen() },
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        mooselikeMaleAmount = mooselikeMaleAmount,
        mooselikeFemaleAmount = mooselikeFemaleAmount,
        mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount,
        mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount,
        mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount,
        mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount,
        mooselikeCalfAmount = mooselikeCalfAmount,
        mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount,
        observerName = observerName,
        observerPhoneNumber = observerPhoneNumber,
        officialAdditionalInfo = officialAdditionalInfo,
        verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority,
        inYardDistanceToResidence = inYardDistanceToResidence,
        litter = litter,
        pack = pack,
    )
}

fun CommonObservation.toObservationDTO(): ObservationDTO? {
    if (remoteId == null || revision == null || observationType.rawBackendEnumValue == null) {
        return null
    }

    return ObservationDTO(
        id = remoteId,
        rev = revision,
        type = "OBSERVATION",
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,
        gameSpeciesCode = species.knownSpeciesCodeOrNull(),
        observationCategory = observationCategory.rawBackendEnumValue,
        observationType = observationType.rawBackendEnumValue,
        deerHuntingType = deerHuntingType.rawBackendEnumValue,
        deerHuntingTypeDescription = deerHuntingOtherTypeDescription,
        geoLocation = location.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        description = description,
        imageIds = images.remoteImageIds,
        totalSpecimenAmount = totalSpecimenAmount,
        specimens = specimens?.map { it.toObservationSpecimenDTO() },
        canEdit = canEdit,
        mooselikeMaleAmount = mooselikeMaleAmount,
        mooselikeFemaleAmount = mooselikeFemaleAmount,
        mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount,
        mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount,
        mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount,
        mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount,
        mooselikeCalfAmount = mooselikeCalfAmount,
        mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount,
        observerName = observerName,
        observerPhoneNumber = observerPhoneNumber,
        officialAdditionalInfo = officialAdditionalInfo,
        verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority,
        inYardDistanceToResidence = inYardDistanceToResidence,
        litter = litter,
        pack = pack,
    )
}
