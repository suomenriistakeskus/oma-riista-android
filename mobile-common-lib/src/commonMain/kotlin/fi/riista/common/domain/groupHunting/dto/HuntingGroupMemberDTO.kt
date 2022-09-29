package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.dto.HunterNumberDTO
import fi.riista.common.domain.dto.OccupationTypeDTO
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class HuntingGroupMemberDTO(
    val id: Long,
    val occupationType: OccupationTypeDTO,
    val personId: Long,
    val firstName: String,
    val lastName: String,
    val hunterNumber: HunterNumberDTO? = null,
    val beginDate: LocalDateDTO? = null,
    val endDate: LocalDateDTO? = null,
)

internal fun HuntingGroupMemberDTO.toMember() : HuntingGroupMember {
    return HuntingGroupMember(
        id = id,
        occupationType = occupationType.toBackendEnum(),
        personId = personId,
        firstName = firstName,
        lastName = lastName,
        hunterNumber = hunterNumber,
        beginDate = beginDate?.toLocalDate(),
        endDate = endDate?.toLocalDate()
    )
}