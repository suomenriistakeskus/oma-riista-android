package fi.riista.common.domain.observation.metadata.dto

import fi.riista.common.domain.observation.metadata.model.ObservationFieldRequirement
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.logging.getLogger
import fi.riista.common.model.toBackendEnum

internal fun Map<ObservationFieldNameDTO, FieldRequirementDTO>.toObservationFieldRequirements():
        Map<CommonObservationField, ObservationFieldRequirement> {

    return this.entries.mapNotNull { (fieldName, requirementDTO) ->
        val observationField = CommonObservationField.create(metadataFieldName = fieldName)
            ?: return@mapNotNull null

        requirementDTO.toObservationFieldRequirement()?.let { fieldRequirement ->
            observationField to fieldRequirement
        }
    }.toMap()
}

internal fun Map<CommonObservationField, ObservationFieldRequirement>.toObservationFieldNameAndRequirementDTO():
        Map<ObservationFieldNameDTO, FieldRequirementDTO> {
    return this.entries.mapNotNull { (field, fieldRequirement) ->
        val observationField = field.metadataFieldName
            ?: return@mapNotNull null

        observationField to fieldRequirement.rawBackendEnumValue
    }.toMap()
}

internal fun Map<ObservationSpecimenFieldNameDTO, FieldRequirementDTO>.toObservationSpecimenFieldRequirements():
        Map<ObservationSpecimenField, ObservationFieldRequirement> {

    return this.entries.mapNotNull { (fieldName, requirementDTO) ->
        val observationSpecimenField = ObservationSpecimenField.create(metadataFieldName = fieldName)
            ?: return@mapNotNull null

        requirementDTO.toObservationFieldRequirement()?.let { fieldRequirement ->
            observationSpecimenField to fieldRequirement
        }
    }.toMap()
}

internal fun Map<ObservationSpecimenField, ObservationFieldRequirement>.toObservationSpecimenFieldNameAndRequirementDTO():
        Map<ObservationSpecimenFieldNameDTO, FieldRequirementDTO> {
    return this.entries.associate { (field, fieldRequirement) ->
        field.metadataFieldName to fieldRequirement.rawBackendEnumValue
    }
}

private fun FieldRequirementDTO.toObservationFieldRequirement(): ObservationFieldRequirement? {
    return toBackendEnum<ObservationFieldRequirement>().value.also { requirement ->
        if (requirement == null) {
            logger.w { "Failed to convert $this to ObservationFieldRequirement" }
        }
    }
}

private val logger by getLogger("ObservationMetadata")
