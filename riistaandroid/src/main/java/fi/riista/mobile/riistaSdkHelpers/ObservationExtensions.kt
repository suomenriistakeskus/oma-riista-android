package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.withNumberOfElements
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.models.observation.ObservationSpecimen


fun GameObservation.toCommonObservation(): CommonObservation? {
    val localDateTime = pointOfTime?.let {
        LocalDateTime.parseLocalDateTime(it)
    } ?: kotlin.run {
        return null
    }

    val sanitizedSpecimenAmount = totalSpecimenAmount ?: 0
    val commonSpecimens =
        (specimens?.map { it.toCommonObservationSpecimen() } ?: listOf())
            .withNumberOfElements(sanitizedSpecimenAmount) {
                CommonObservationSpecimen(
                    remoteId = null,
                    revision = null,
                    gender = BackendEnum.create(null),
                    age = BackendEnum.create(null),
                    stateOfHealth = BackendEnum.create(null),
                    marking = BackendEnum.create(null),
                    widthOfPaw = null,
                    lengthOfPaw = null,
                )
            }

    return CommonObservation(
        localId = localId,
        localUrl = null,
        remoteId = remoteId,
        revision = rev,
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,

        species = Species.Known(gameSpeciesCode),
        observationCategory = observationCategory?.name.toBackendEnum(),
        observationType = observationType?.name.toBackendEnum(),
        deerHuntingType = deerHuntingType?.name.toBackendEnum(),
        deerHuntingOtherTypeDescription = deerHuntingTypeDescription,
        location = geoLocation.toETRMSGeoLocation(),
        pointOfTime = localDateTime,
        description = description,
        specimens = commonSpecimens,
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        totalSpecimenAmount = sanitizedSpecimenAmount,
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
        images = EntityImages(
            remoteImageIds = imageIds,
            localImages = localImages.map { localImage ->
                localImage.toLocalEntityImage(
                    uploaded = imageIds.contains(localImage.serverId)
                )
            }
        ),
    )
}

fun ObservationSpecimen.toCommonObservationSpecimen() =
    CommonObservationSpecimen(
        remoteId = id,
        revision = rev?.toInt(),
        gender = gender.toBackendEnum(),
        age = age.toBackendEnum(),
        stateOfHealth = state.toBackendEnum(),
        marking = marking.toBackendEnum(),
        lengthOfPaw = lengthOfPaw?.toDoubleOrNull(),
        widthOfPaw = widthOfPaw?.toDoubleOrNull(),
    )
