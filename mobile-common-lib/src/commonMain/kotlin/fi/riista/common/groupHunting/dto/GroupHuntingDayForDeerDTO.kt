package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.LocalDateDTO
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingDayForDeerDTO(
    val id: Long? = null, // Not used
    val rev: Int? = null, // Not used
    val huntingGroupId: Long,
    val date: LocalDateDTO,
)
