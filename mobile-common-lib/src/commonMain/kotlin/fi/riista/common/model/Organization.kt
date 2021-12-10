package fi.riista.common.model

typealias OrganizationId = Long

class Organization(
    val id: OrganizationId,
    val name: LocalizedString,
    val officialCode: String,
)