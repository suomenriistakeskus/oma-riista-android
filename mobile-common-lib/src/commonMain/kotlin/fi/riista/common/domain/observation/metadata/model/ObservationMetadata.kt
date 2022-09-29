package fi.riista.common.domain.observation.metadata.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.domain.observation.ui.CommonObservationField

data class ObservationMetadata(
    val lastModified: String,
    val speciesMetadata: Map<SpeciesCode, SpeciesObservationMetadata>,
    val observationSpecVersion: Int,
) {
    internal fun getObservationFields(
        observation: CommonObservationData
    ): Map<CommonObservationField, ObservationFieldRequirement> {
        val metadata = getSpeciesMetadata(observation)
            ?: return mapOf()

        val contextualObservationFields = metadata.getContextualFields(observation)?.observationFields ?: mapOf()

        return metadata.observationFields + contextualObservationFields
    }

    internal fun getSpecimenFields(
        observation: CommonObservationData
    ): Map<ObservationSpecimenField, ObservationFieldRequirement> {
        val metadata = getSpeciesMetadata(observation)
            ?: return mapOf()

        val contextualObservationFields = metadata.getContextualFields(observation)?.specimenFields ?: mapOf()

        return metadata.specimenFields + contextualObservationFields
    }

    internal fun getSpeciesMetadata(
        observation: CommonObservationData
    ) = getSpeciesMetadata(species = observation.species)

    internal fun getSpeciesMetadata(
        species: Species
    ): SpeciesObservationMetadata? {
        return species.knownSpeciesCodeOrNull()
            ?.let { speciesCode ->
                speciesMetadata[speciesCode]
            }
    }
}
