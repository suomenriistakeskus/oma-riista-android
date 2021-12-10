package fi.riista.common.groupHunting.model

import fi.riista.common.groupHunting.dto.GroupHuntingDayCreateDTO
import fi.riista.common.groupHunting.dto.GroupHuntingDayDTO
import fi.riista.common.groupHunting.dto.GroupHuntingDayUpdateDTO
import fi.riista.common.model.*

data class GroupHuntingDay(
    val id: GroupHuntingDayId,
    override val type: Entity.Type,
    val rev: Revision?,
    val huntingGroupId: HuntingGroupId,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    /**
     * The total duration of breaks in minutes. Allowed to be null for deer hunting.
     */
    val breakDurationInMinutes: Int?,
    val snowDepth: Int?,
    val huntingMethod: BackendEnum<GroupHuntingMethodType>,
    val numberOfHunters: Int?,
    val numberOfHounds: Int?,
    val createdBySystem: Boolean,
): Entity {
    /**
     * The hunting day duration in minutes including breaks.
     */
    val durationInMinutes: Int by lazy {
        startDateTime.minutesUntil(endDateTime)
    }

    /**
     * The duration of active hunting in minutes. Active hunting does not include breaks.
     */
    val activeHuntingDurationInMinutes: Int
        get() = durationInMinutes - (breakDurationInMinutes ?: 0)
}

internal fun GroupHuntingDay.toHuntingDayDTO(): GroupHuntingDayDTO? {
    if (id.remoteId == null || rev == null) {
        return null
    }

    return GroupHuntingDayDTO(
            id = id.remoteId,
            rev = rev,
            huntingGroupId = huntingGroupId,
            startDate = startDateTime.date.toLocalDateDTO(),
            startTime = startDateTime.time.toHoursAndMinutesString(),
            endDate = endDateTime.date.toLocalDateDTO(),
            endTime = endDateTime.time.toHoursAndMinutesString(),
            breakDurationInMinutes = breakDurationInMinutes,
            snowDepth = snowDepth,
            huntingMethod = huntingMethod.rawBackendEnumValue,
            numberOfHunters = numberOfHunters,
            numberOfHounds = numberOfHounds,
            createdBySystem = createdBySystem,
    )
}

internal fun GroupHuntingDay.toHuntingDayCreateDTO(): GroupHuntingDayCreateDTO {
    return GroupHuntingDayCreateDTO(
            huntingGroupId = huntingGroupId,
            startDate = startDateTime.date.toLocalDateDTO(),
            startTime = startDateTime.time.toHoursAndMinutesString(),
            endDate = endDateTime.date.toLocalDateDTO(),
            endTime = endDateTime.time.toHoursAndMinutesString(),
            breakDurationInMinutes = breakDurationInMinutes ?: 0,
            snowDepth = snowDepth,
            huntingMethod = huntingMethod.rawBackendEnumValue,
            numberOfHunters = numberOfHunters,
            numberOfHounds = numberOfHounds,
            createdBySystem = createdBySystem,
    )
}

internal fun GroupHuntingDay.toHuntingDayUpdateDTO(): GroupHuntingDayUpdateDTO? {
    if (id.remoteId == null || rev == null) {
        return null
    }

    return GroupHuntingDayUpdateDTO(
            id = id.remoteId,
            rev = rev,
            huntingGroupId = huntingGroupId,
            startDate = startDateTime.date.toLocalDateDTO(),
            startTime = startDateTime.time.toHoursAndMinutesString(),
            endDate = endDateTime.date.toLocalDateDTO(),
            endTime = endDateTime.time.toHoursAndMinutesString(),
            breakDurationInMinutes = breakDurationInMinutes ?: 0,
            snowDepth = snowDepth,
            huntingMethod = huntingMethod.rawBackendEnumValue,
            numberOfHunters = numberOfHunters,
            numberOfHounds = numberOfHounds,
            createdBySystem = createdBySystem,
    )
}