package fi.riista.common.groupHunting.model

import fi.riista.common.groupHunting.dto.GroupHuntingObservationDTO
import fi.riista.common.groupHunting.dto.GroupHuntingObservationUpdateDTO
import fi.riista.common.model.*

typealias GroupHuntingObservationId = Long

data class GroupHuntingObservation(
    val id: GroupHuntingObservationId,
    val rev: Int,
    val gameSpeciesCode: SpeciesCode,
    val geoLocation: ETRMSGeoLocation,
    val pointOfTime: LocalDateTime,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<Uuid>,
    val specimens: List<ObservationSpecimen>,
    val amount: Int? = null,
    val huntingDayId: GroupHuntingDayId?,
    val authorInfo: PersonWithHunterNumber,
    val actorInfo: PersonWithHunterNumber,
    val observerName: String? = null,
    val observerPhoneNumber: String? =  null,
    val linkedToGroupHuntingDay: Boolean,
    val observationType: BackendEnum<ObservationType>,
    val observationCategory: BackendEnum<ObservationCategory>,
    val totalSpecimenAmount: Int?,
    val mobileClientRefId: Long? = null,
    val observationSpecVersion: Int,
    val litter: Boolean? = null,
    val pack: Boolean? = null,
    val deerHuntingType: BackendEnum<DeerHuntingType>,
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
    val rejected: Boolean = false,
) {
    val acceptStatus: AcceptStatus
        get() {
            return if (rejected) {
                AcceptStatus.REJECTED
            } else {
                if (huntingDayId != null) {
                    AcceptStatus.ACCEPTED
                } else {
                    AcceptStatus.PROPOSED
                }
            }
        }
}

internal fun GroupHuntingObservation.toGroupHuntingObservationDTO(): GroupHuntingObservationDTO {
    return GroupHuntingObservationDTO(
        id = id,
        rev = rev,
        gameSpeciesCode = gameSpeciesCode,
        geoLocation = geoLocation.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        description = description,
        canEdit = canEdit,
        imageIds = imageIds,
        specimens = specimens.map { it.toObservationSpecimenDTO() },
        amount = amount,
        huntingDayId = huntingDayId?.remoteId,
        authorInfo = authorInfo.toPersonWithHunterNumberDTO(),
        actorInfo = actorInfo.toPersonWithHunterNumberDTO(),
        observerName = observerName,
        observerPhoneNumber = observerPhoneNumber,
        linkedToGroupHuntingDay = linkedToGroupHuntingDay,
        observationType = observationType.rawBackendEnumValue!!,
        observationCategory = observationCategory.rawBackendEnumValue,
        totalSpecimenAmount = totalSpecimenAmount,
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,
        litter = litter,
        pack = pack,
        deerHuntingType = deerHuntingType.rawBackendEnumValue,
        deerHuntingTypeDescription = deerHuntingTypeDescription,
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
        officialAdditionalInfo = officialAdditionalInfo,
    )
}

internal fun GroupHuntingObservation.toGroupHuntingObservationUpdateDTO(): GroupHuntingObservationUpdateDTO {
    val specimens = specimens.map { it.toObservationSpecimenDTO() }
    return GroupHuntingObservationUpdateDTO(
        id = id,
        rev = rev,
        gameSpeciesCode = gameSpeciesCode,
        geoLocation = geoLocation.toETRMSGeoLocationDTO(),
        pointOfTime = pointOfTime.toStringISO8601(),
        description = description,
        canEdit = canEdit,
        imageIds = imageIds,
        specimens = if (specimens.isEmpty()) { null } else { specimens },
        amount = amount,
        huntingDayId = huntingDayId?.remoteId,
        authorInfo = authorInfo.toPersonWithHunterNumberDTO(),
        actorInfo = actorInfo.toPersonWithHunterNumberDTO(),
        observerName = observerName,
        observerPhoneNumber = observerPhoneNumber,
        linkedToGroupHuntingDay = linkedToGroupHuntingDay,
        observationType = observationType.rawBackendEnumValue!!,
        observationCategory = observationCategory.rawBackendEnumValue,
        mobileClientRefId = mobileClientRefId,
        observationSpecVersion = observationSpecVersion,
        litter = litter,
        pack = pack,
        deerHuntingType = deerHuntingType.rawBackendEnumValue,
        deerHuntingTypeDescription = deerHuntingTypeDescription,
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
        officialAdditionalInfo = officialAdditionalInfo,
    )
}
