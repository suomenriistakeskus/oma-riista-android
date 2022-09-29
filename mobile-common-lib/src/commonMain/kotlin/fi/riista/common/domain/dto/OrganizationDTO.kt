package fi.riista.common.domain.dto

import fi.riista.common.domain.model.Organization
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalizedString
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