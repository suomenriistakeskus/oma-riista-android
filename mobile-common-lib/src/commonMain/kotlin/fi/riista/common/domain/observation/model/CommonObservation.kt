package fi.riista.common.domain.observation.model

import fi.riista.common.domain.model.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CommonObservation(
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

    val location: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val description: String?,
    val images: EntityImages,

    val totalSpecimenAmount: Int?,
    val specimens: List<CommonObservationSpecimen>?,

    val canEdit: Boolean,

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
)

internal fun CommonObservation.toObservationData() =
    CommonObservationData(
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
        location = location.asKnownLocation(),
        pointOfTime = pointOfTime,
        description = description,
        images = images,
        totalSpecimenAmount = totalSpecimenAmount,
        specimens = specimens?.map { it.toCommonSpecimenData() },
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

