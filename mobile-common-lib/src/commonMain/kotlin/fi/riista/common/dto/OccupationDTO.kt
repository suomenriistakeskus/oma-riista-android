package fi.riista.common.dto

import fi.riista.common.model.Occupation
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class OccupationDTO(
    val id: Long,
    val occupationType: OccupationTypeDTO,
    val name: LocalizedStringDTO,

    val beginDate: LocalDateDTO? = null,
    val endDate: LocalDateDTO? = null,
    val organisation: OrganizationDTO
)

fun OccupationDTO.toOccupation(): Occupation {
    return Occupation(
        id = id,
        occupationType = occupationType.toBackendEnum(),
        name = name.toLocalizedString(),
        beginDate = beginDate?.toLocalDate(),
        endDate = endDate?.toLocalDate(),
        organisation = organisation.toOrganization(),
    )
}
