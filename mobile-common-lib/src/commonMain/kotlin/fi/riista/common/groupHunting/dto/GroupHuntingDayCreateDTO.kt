package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalTimeDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.model.Revision
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

/**
 * A DTO for creating a [GroupHuntingDay] on the backend.
 */
@Serializable
data class GroupHuntingDayCreateDTO(
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
