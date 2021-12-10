package fi.riista.common.dto

import fi.riista.common.model.LocalizedString
import kotlinx.serialization.Serializable

@Serializable
data class LocalizedStringDTO(
    val fi: String? = null,
    val sv: String? = null,
    val en: String? = null,
)

internal fun LocalizedStringDTO.toLocalizedString() = LocalizedString(fi, sv, en)