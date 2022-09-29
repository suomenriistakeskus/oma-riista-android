package fi.riista.common.domain.srva.metadata.dto

import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A DTO for srva metadata (not for sending category information due to ignored fields)
 */
@Serializable
data class SrvaMetadataDTO(
    val ages: List<String>,
    val genders: List<String>,
    val species: List<SrvaSpeciesDTO>,
    @SerialName("events")
    val categories: List<SrvaCategoryDTO>,
)

internal fun SrvaMetadataDTO.toSrvaMetadata(): SrvaMetadata {
    return SrvaMetadata(
        species = species.map { it.toKnownSpecies() },
        ages = ages.map { it.toBackendEnum() },
        genders = genders.map { it.toBackendEnum() },
        eventCategories = categories.map { it.toSrvaEventCategory() }
    )
}