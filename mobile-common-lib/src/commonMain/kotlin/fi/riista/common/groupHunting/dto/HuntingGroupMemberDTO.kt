package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.HunterNumberDTO
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.OccupationTypeDTO
import fi.riista.common.groupHunting.model.HuntingGroupMember
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
    return HuntingGroupMember(id, occupationType, personId, firstName,
                              lastName, hunterNumber, beginDate, endDate)
}