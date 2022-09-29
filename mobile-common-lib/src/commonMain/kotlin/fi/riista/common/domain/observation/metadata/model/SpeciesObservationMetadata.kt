package fi.riista.common.domain.observation.metadata.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.model.BackendEnum

/**
 * Observation metadata for one species identified by [speciesCode]
 */
data class SpeciesObservationMetadata(
    val speciesCode: SpeciesCode,
    val observationFields: Map<CommonObservationField, ObservationFieldRequirement>,

    /**
     * The specimen fields to be displayed in all cases (observation category doesn't affect these)
     */
    val specimenFields: Map<ObservationSpecimenField, ObservationFieldRequirement>,

    /**
     * Additional fields depending on context
     */
    val contextSensitiveFieldSets: List<ObservationMetadataContextualFields>,

    val maxLengthOfPawCentimetres: Double?,
    val minLengthOfPawCentimetres: Double?,
    val maxWidthOfPawCentimetres: Double?,
    val minWidthOfPawCentimetres: Double?,
) {
    fun getContextualFields(
        observationCategory: BackendEnum<ObservationCategory>,
        observationType: BackendEnum<ObservationType>
    ): ObservationMetadataContextualFields? {
        return contextSensitiveFieldSets.firstOrNull { fields ->
            fields.observationCategory == observationCategory &&
                    fields.observationType == observationType
        }
    }

    internal fun getContextualFields(observation: CommonObservationData) =
        getContextualFields(
            observationCategory = observation.observationCategory,
            observationType = observation.observationType,
        )

    fun getAvailableObservationCategories(): List<BackendEnum<ObservationCategory>> {
        return contextSensitiveFieldSets
            .mapTo(destination = mutableSetOf()) { fields ->
                fields.observationCategory
            }
            .toList()
    }

    fun getObservationTypes(observationCategory: BackendEnum<ObservationCategory>): List<BackendEnum<ObservationType>> {
        return contextSensitiveFieldSets.mapNotNull { fields ->
            if (fields.observationCategory == observationCategory) {
                fields.observationType
            } else {
                null
            }
        }
    }
}
