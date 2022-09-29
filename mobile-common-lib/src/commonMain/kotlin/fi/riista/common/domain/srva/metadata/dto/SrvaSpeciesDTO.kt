package fi.riista.common.domain.srva.metadata.dto

import fi.riista.common.domain.model.Species
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A DTO for srva species (not for sending category information due to ignored fields)
 */
@Serializable
data class SrvaSpeciesDTO(
    @SerialName("code")
    val speciesCode: Int,
    // ignored
    //val categoryId: Int,
    // ignored
    //val name: LocalizedStringDTO,
)

internal fun SrvaSpeciesDTO.toKnownSpecies() = Species.Known(speciesCode = speciesCode)