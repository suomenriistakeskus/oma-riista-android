package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalTimeDTO
import fi.riista.common.model.BackendId
import fi.riista.common.model.Revision
import kotlinx.serialization.Serializable

/**
 * A DTO for updating [GroupHuntingDay] on the backend. Has the same fields as the received DTO
 * but with different nullability requirements.
 */
@Serializable
data class GroupHuntingDayUpdateDTO(
    val id: BackendId,
    val rev: Revision,
    val huntingGroupId: HuntingGroupId,
    val startDate: LocalDateDTO,
    val endDate: LocalDateDTO,
    val startTime: LocalTimeDTO,
    val endTime: LocalTimeDTO,
    // todo: should we have durationInMinutes
    val breakDurationInMinutes: Int,
    val snowDepth: Int?,
    val huntingMethod: GroupHuntingMethodTypeDTO?,
    val numberOfHunters: Int? = null,
    val numberOfHounds: Int? = null,
    val createdBySystem: Boolean,
)
