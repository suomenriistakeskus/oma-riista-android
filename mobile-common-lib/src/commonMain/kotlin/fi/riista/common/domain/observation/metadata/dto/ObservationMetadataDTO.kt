package fi.riista.common.domain.observation.metadata.dto

import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import kotlinx.serialization.Serializable

@Serializable
data class ObservationMetadataDTO(
    val lastModified: String,
    val speciesList: List<SpeciesObservationMetadataDTO>,
    val observationSpecVersion: Int,
)

internal fun ObservationMetadataDTO.toObservationMetadata(): ObservationMetadata {
    return ObservationMetadata(
        lastModified = lastModified,
        speciesMetadata = speciesList
            .map { it.toSpeciesMetadata() }
            .associateBy { it.speciesCode },
        observationSpecVersion = observationSpecVersion
    )
}
