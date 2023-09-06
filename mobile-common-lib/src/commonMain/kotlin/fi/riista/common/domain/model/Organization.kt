package fi.riista.common.domain.model

import fi.riista.common.model.LocalizedString
import kotlinx.serialization.Serializable

typealias OrganizationId = Long

@Serializable
data class Organization(
    val id: OrganizationId,
    val name: LocalizedString,
    val officialCode: String,
)
