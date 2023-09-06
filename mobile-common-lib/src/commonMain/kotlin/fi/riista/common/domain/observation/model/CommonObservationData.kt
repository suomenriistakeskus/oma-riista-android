package fi.riista.common.domain.observation.model

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
internal data class CommonObservationData(
    val localId: Long?,
    val localUrl: String?, // Used as localId on iOS
    val remoteId: Long?,
    val revision: Long?,
    val mobileClientRefId: Long?,

    val observationSpecVersion: Int,
    val species: Species,
    val observationCategory: BackendEnum<ObservationCategory>,
    val observationType: BackendEnum<ObservationType>,

    val deerHuntingType: BackendEnum<DeerHuntingType>,
    val deerHuntingOtherTypeDescription: String?,

    val location: CommonLocation,
    val pointOfTime: LocalDateTime,
    val description: String?,
    val images: EntityImages,

    // the amount of specimens as inputted by the user. Allowed to be null (user has cleared the amount field)
    val totalSpecimenAmount: Int?,

    // the current specimen data. The size of the list most likely doesn't match totalSpecimenAmount.
    // The final, saved specimens should be determined upon saving based on these and totalSpecimenAmount
    val specimens: List<CommonSpecimenData>?,

    val canEdit: Boolean,
    val modified: Boolean,
    val deleted: Boolean,

    val mooselikeMaleAmount: Int?,
    val mooselikeFemaleAmount: Int?,
    val mooselikeFemale1CalfAmount: Int?,
    val mooselikeFemale2CalfsAmount: Int?,
    val mooselikeFemale3CalfsAmount: Int?,
    val mooselikeFemale4CalfsAmount: Int?,
    val mooselikeCalfAmount: Int?,
    val mooselikeUnknownSpecimenAmount: Int?,

    val observerName: String?,
    val observerPhoneNumber: String?,
    val officialAdditionalInfo: String?,
    val verifiedByCarnivoreAuthority: Boolean?,

    val inYardDistanceToResidence: Int?,
    val litter: Boolean?,
    val pack: Boolean?,
) {
    val mooselikeSpecimenCount: Int by lazy {
        val commonAmount = (mooselikeMaleAmount ?: 0) +
                (mooselikeFemaleAmount ?: 0) +
                (mooselikeFemale1CalfAmount ?: 0) * (1 + 1) +
                (mooselikeFemale2CalfsAmount ?: 0) * (1 + 2) +
                (mooselikeFemale3CalfsAmount ?: 0) * (1 + 3) +
                (mooselikeCalfAmount ?: 0) +
                (mooselikeUnknownSpecimenAmount ?: 0)

        // for white-tailed deer include also the female + 4 calves
        if (species.knownSpeciesCodeOrNull() == SpeciesCodes.WHITE_TAILED_DEER_ID) {
            commonAmount + (mooselikeFemale4CalfsAmount ?: 0) * (1 + 4)
        } else {
            commonAmount
        }
    }

    val specimensOrEmptyList: List<CommonSpecimenData> by lazy {
        specimens ?: listOf()
    }
}

internal fun CommonObservationData.toCommonObservation(): CommonObservation? {
    val knownLocation = (location as? CommonLocation.Known)?.etrsLocation
        ?: return null

    return CommonObservation(
        localId = localId,
        localUrl = localUrl,
        remoteId = remoteId,
        revision = revision,
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,
        species = species,
        observationCategory = observationCategory,
        observationType = observationType,
        deerHuntingType = deerHuntingType,
        deerHuntingOtherTypeDescription = deerHuntingOtherTypeDescription,
        location = knownLocation,
        pointOfTime = pointOfTime,
        description = description,
        images = images,
        specimens = specimens?.map { it.toObservationSpecimen() },
        canEdit = canEdit,
        modified = modified,
        deleted = deleted,
        totalSpecimenAmount = totalSpecimenAmount,
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

internal fun CommonSpecimenData.toObservationSpecimen(): CommonObservationSpecimen {
    return CommonObservationSpecimen(
        remoteId = remoteId,
        revision = revision,
        gender = gender ?: BackendEnum.create(null),
        age = age ?: BackendEnum.create(null),
        stateOfHealth = stateOfHealth ?: BackendEnum.create(null),
        marking = marking ?: BackendEnum.create(null),
        widthOfPaw = widthOfPaw,
        lengthOfPaw = lengthOfPaw,
    )
}
