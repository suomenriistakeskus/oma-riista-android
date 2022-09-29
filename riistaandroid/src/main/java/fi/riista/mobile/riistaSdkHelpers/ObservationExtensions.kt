package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.withNumberOfElements
import fi.riista.mobile.models.DeerHuntingType
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.LocalImage
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.models.observation.ObservationCategory
import fi.riista.mobile.models.observation.ObservationSpecimen
import fi.riista.mobile.models.observation.ObservationType


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


fun CommonObservation.toAppObservation(
    observationDeleted: Boolean = false,
    observationModified: Boolean = false,
): GameObservation? {
    val speciesCode: Int = when (val species = species) {
        is Species.Known -> species.speciesCode
        Species.Other,
        Species.Unknown -> return null
    }

    return GameObservation().also { observation ->
        observation.remoteId = remoteId
        observation.rev = revision
        observation.type = GameLog.TYPE_OBSERVATION
        observation.geoLocation = location.toGeoLocation()
        observation.pointOfTime = pointOfTime.toStringISO8601()
        observation.gameSpeciesCode = speciesCode
        observation.description = description
        observation.canEdit = canEdit
        observation.imageIds = images.remoteImageIds
        observation.totalSpecimenAmount = totalSpecimenAmount
        observation.specimens = specimens?.map { it.toAppObservationSpecimen() }
        observation.mobileClientRefId = mobileClientRefId
        observation.observationSpecVersion = observationSpecVersion

        observation.observationCategory = ObservationCategory.fromString(observationCategory.rawBackendEnumValue)
        observation.observationType = ObservationType.fromString(observationType.rawBackendEnumValue)
        observation.deerHuntingType = DeerHuntingType.fromString(deerHuntingType.rawBackendEnumValue)
        observation.deerHuntingTypeDescription = deerHuntingOtherTypeDescription

        observation.mooselikeMaleAmount = mooselikeMaleAmount
        observation.mooselikeFemaleAmount = mooselikeFemaleAmount
        observation.mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount
        observation.mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount
        observation.mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount
        observation.mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount
        observation.mooselikeCalfAmount = mooselikeCalfAmount
        observation.mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount

        observation.officialAdditionalInfo = officialAdditionalInfo
        observation.verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority
        observation.observerName = observerName
        observation.observerPhoneNumber = observerPhoneNumber

        observation.inYardDistanceToResidence = inYardDistanceToResidence
        observation.litter = litter
        observation.pack = pack

        // ignored fields in SrvaEvent
        observation.localId = localId
        observation.deleted = observationDeleted
        observation.modified = observationModified
        observation.localImages = images.localImages.mapNotNull { entityImage ->
            if (entityImage.localUrl == null) {
                // local images on android are using files i.e. urls and not identifiers
                return@mapNotNull null
            }

            when (entityImage.status) {
                EntityImage.Status.LOCAL,
                EntityImage.Status.UPLOADED ->
                    LocalImage().also { localImage ->
                        localImage.serverId = entityImage.serverId
                        localImage.localPath = entityImage.localUrl
                    }
                EntityImage.Status.LOCAL_TO_BE_REMOVED ->
                    // ignore local images that are marked for removal
                    // -> they will get removed later
                    null
            }
        }
        // username should be applied when/if saving the observation
    }
}

fun CommonObservationSpecimen.toAppObservationSpecimen() =
    ObservationSpecimen().also { specimen ->
        specimen.id = remoteId
        specimen.rev = revision?.toLong()
        specimen.gender = gender.rawBackendEnumValue
        specimen.age = age.rawBackendEnumValue
        specimen.state = stateOfHealth.rawBackendEnumValue
        specimen.marking = marking.rawBackendEnumValue
        specimen.lengthOfPaw = lengthOfPaw?.toString()
        specimen.widthOfPaw = widthOfPaw?.toString()
    }
