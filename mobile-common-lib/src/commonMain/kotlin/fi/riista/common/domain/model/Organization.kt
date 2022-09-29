package fi.riista.common.domain.model

import fi.riista.common.model.LocalizedString

typealias OrganizationId = Long

data class Organization(
    val id: OrganizationId,
    val name: LocalizedString,
    val officialCode: String,
)
