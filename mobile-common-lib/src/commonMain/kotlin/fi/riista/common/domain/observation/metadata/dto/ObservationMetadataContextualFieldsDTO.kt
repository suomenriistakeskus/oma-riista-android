package fi.riista.common.domain.observation.metadata.dto

import fi.riista.common.domain.dto.GameAgeDTO
import fi.riista.common.domain.dto.ObservationCategoryDTO
import fi.riista.common.domain.dto.ObservationTypeDTO
import fi.riista.common.domain.observation.dto.ObservationSpecimenMarkingDTO
import fi.riista.common.domain.observation.dto.ObservationSpecimenStateDTO
import fi.riista.common.domain.observation.metadata.model.ObservationMetadataContextualFields
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ObservationMetadataContextualFieldsDTO(
    @SerialName("category")
    val observationCategory: ObservationCategoryDTO,
    @SerialName("type")
    val observationType: ObservationTypeDTO,

    @SerialName("baseFields")
    val observationFields: Map<ObservationFieldNameDTO, FieldRequirementDTO> = mapOf(),
    val specimenFields: Map<ObservationSpecimenFieldNameDTO, FieldRequirementDTO> = mapOf(),

    val allowedAges: List<GameAgeDTO> = listOf(),
    val allowedStates: List<ObservationSpecimenStateDTO> = listOf(),
    val allowedMarkings: List<ObservationSpecimenMarkingDTO> = listOf(),
)

internal fun ObservationMetadataContextualFieldsDTO.toContextualFieldRequirements(): ObservationMetadataContextualFields {
    return ObservationMetadataContextualFields(
        observationCategory = observationCategory.toBackendEnum(),
        observationType = observationType.toBackendEnum(),
        observationFields = observationFields.toObservationFieldRequirements(),
        specimenFields = specimenFields.toObservationSpecimenFieldRequirements(),
        allowedAges = allowedAges.map { it.toBackendEnum() },
        allowedStates = allowedStates.map { it.toBackendEnum() },
        allowedMarkings = allowedMarkings.map { it.toBackendEnum() },
    )
}
