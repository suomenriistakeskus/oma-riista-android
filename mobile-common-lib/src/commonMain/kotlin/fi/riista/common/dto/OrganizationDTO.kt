package fi.riista.common.dto

import fi.riista.common.model.Organization
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationDTO(
    val id: Long,
    val name: LocalizedStringDTO,
    val officialCode: String,
)

fun OrganizationDTO.toOrganization(): Organization {
    return Organization(
            id = id,
            name = name.toLocalizedString(),
            officialCode = officialCode
    )
}