package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.dto.*
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservation
import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.domain.observation.dto.toObservationSpecimen
import fi.riista.common.dto.*
import fi.riista.common.model.BackendId
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingObservationDTO(
    val id: Long,
    val rev: Int,
    val type: String = "OBSERVATION",
    val gameSpeciesCode: SpeciesCodeDTO,
    val geoLocation: ETRMSGeoLocationDTO,
    val pointOfTime: LocalDateTimeDTO,
    val description: String? = null,
    val canEdit: Boolean,
    val imageIds: List<UuidDTO>,
    val specimens: List<CommonObservationSpecimenDTO>? = null,
    val amount: Int? = null,
    val huntingDayId: BackendId? = null,
    val authorInfo: PersonWithHunterNumberDTO,
    val actorInfo: PersonWithHunterNumberDTO,
    val observerName: String? = null,
    val observerPhoneNumber: String? =  null,
    val linkedToGroupHuntingDay: Boolean,
    val observationType: ObservationTypeDTO,
    val observationCategory: ObservationCategoryDTO? = null,
    val totalSpecimenAmount: Int?,
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

internal fun GroupHuntingObservationDTO.toGroupHuntingObservation() : GroupHuntingObservation? {
    val observationPointOfTime = pointOfTime.toLocalDateTime()
            ?: kotlin.run {
                // require valid point of time for observation
                return null
            }

    return GroupHuntingObservation(
            id = id,
            rev = rev,
            gameSpeciesCode = gameSpeciesCode,
            geoLocation = geoLocation.toETRMSGeoLocation(),
            pointOfTime = observationPointOfTime,
            description = description,
            canEdit = canEdit,
            imageIds = imageIds,
            specimens = specimens?.map { it.toObservationSpecimen() } ?: emptyList(),
            amount = amount,
            huntingDayId = GroupHuntingDayId.remote(huntingDayId),
            authorInfo = authorInfo.toPersonWithHunterNumber(),
            actorInfo = actorInfo.toPersonWithHunterNumber(),
            observerName = observerName,
            observerPhoneNumber = observerPhoneNumber,
            linkedToGroupHuntingDay = linkedToGroupHuntingDay,
            observationType = observationType.toBackendEnum(),
            observationCategory = observationCategory.toBackendEnum(),
            totalSpecimenAmount = totalSpecimenAmount,
            mobileClientRefId = mobileClientRefId,
            observationSpecVersion = observationSpecVersion,
            litter = litter,
            pack = pack,
            deerHuntingType = deerHuntingType.toBackendEnum(),
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
