package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalTimeDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalTime
import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.groupHunting.model.HuntingGroupId
import fi.riista.common.model.*
import fi.riista.common.util.letWith
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingDayDTO(
    val id: BackendId,
    val rev: Revision,
    val huntingGroupId: HuntingGroupId,
    val startDate: LocalDateDTO,
    val endDate: LocalDateDTO,
    val startTime: LocalTimeDTO,
    val endTime: LocalTimeDTO,
    val breakDurationInMinutes: Int? = null,
    val snowDepth: Int? = null,
    val huntingMethod: GroupHuntingMethodTypeDTO? = null,
    val numberOfHunters: Int? = null,
    val numberOfHounds: Int? = null,
    val createdBySystem: Boolean,
)

fun GroupHuntingDayDTO.toHuntingDay(): GroupHuntingDay? {
    val startDateTime = startDate.toLocalDate()?.letWith(startTime.toLocalTime()) { date, time ->
        LocalDateTime(date, time)
    }
    val endDateTime = endDate.toLocalDate()?.letWith(endTime.toLocalTime()) { date, time ->
        LocalDateTime(date, time)
    }

    if (startDateTime == null || endDateTime == null) {
        // require valid timestamps
        return null
    }

    return GroupHuntingDay(
            id = GroupHuntingDayId.remote(remoteId = id),
            type = Entity.Type.REMOTE,
            rev = rev,
            huntingGroupId = huntingGroupId,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            breakDurationInMinutes = breakDurationInMinutes,
            snowDepth = snowDepth,
            huntingMethod = huntingMethod.toBackendEnum(),
            numberOfHunters = numberOfHunters,
            numberOfHounds = numberOfHounds,
            createdBySystem = createdBySystem
    )
}