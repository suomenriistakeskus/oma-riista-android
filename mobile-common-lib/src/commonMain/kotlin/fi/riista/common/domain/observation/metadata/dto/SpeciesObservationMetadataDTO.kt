package fi.riista.common.domain.observation.metadata.dto

import fi.riista.common.domain.dto.SpeciesCodeDTO
import fi.riista.common.domain.observation.metadata.model.SpeciesObservationMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Observation metadata for one species identified by [speciesCode]
 */
@Serializable
data class SpeciesObservationMetadataDTO(
    @SerialName("gameSpeciesCode")
    val speciesCode: SpeciesCodeDTO,

    /**
     * The observation fields to be displayed in all cases (observation category doesn't affect these)
     */
    @SerialName("baseFields")
    val observationFields: Map<ObservationFieldNameDTO, FieldRequirementDTO> = mapOf(),

    /**
     * The specimen fields to be displayed in all cases (observation category doesn't affect these)
     */
    val specimenFields: Map<ObservationSpecimenFieldNameDTO, FieldRequirementDTO> = mapOf(),

    /**
     * Additional fields depending on context
     */
    val contextSensitiveFieldSets: List<ObservationMetadataContextualFieldsDTO> = listOf(),

    @SerialName("maxLengthOfPaw")
    val maxLengthOfPawCentimetres: Double? = null,
    @SerialName("minLengthOfPaw")
    val minLengthOfPawCentimetres: Double? = null,
    @SerialName("maxWidthOfPaw")
    val maxWidthOfPawCentimetres: Double? = null,
    @SerialName("minWidthOfPaw")
    val minWidthOfPawCentimetres: Double? = null
)

internal fun SpeciesObservationMetadataDTO.toSpeciesMetadata(): SpeciesObservationMetadata {
    return SpeciesObservationMetadata(
        speciesCode = speciesCode,
        observationFields = observationFields.toObservationFieldRequirements(),
        specimenFields = specimenFields.toObservationSpecimenFieldRequirements(),
        contextSensitiveFieldSets = contextSensitiveFieldSets.map { it.toContextualFieldRequirements() },
        maxLengthOfPawCentimetres = maxLengthOfPawCentimetres,
        minLengthOfPawCentimetres = minLengthOfPawCentimetres,
        maxWidthOfPawCentimetres = maxWidthOfPawCentimetres,
        minWidthOfPawCentimetres = minWidthOfPawCentimetres,
    )
}
