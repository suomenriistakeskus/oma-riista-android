package fi.riista.common.domain.srva.metadata.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A DTO for srva method (not for sending category information due to ignored fields)
 */
@Serializable
data class SrvaMethodDTO(
    @SerialName("name")
    val method: String,

    // ignored as metadata should not determine which methods are checked
    // (this probably exists because same DTO was used elsewhere)
    // val isChecked: Boolean,
)