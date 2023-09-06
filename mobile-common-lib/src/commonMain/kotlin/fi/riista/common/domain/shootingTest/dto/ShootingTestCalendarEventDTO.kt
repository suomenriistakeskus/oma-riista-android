package fi.riista.common.domain.shootingTest.dto

import fi.riista.common.domain.shootingTest.model.CommonShootingTestCalendarEvent
import fi.riista.common.dto.DateTimeDTO
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalTimeDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalTime
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestCalendarEventDTO(
    val rhyId: Long? = null,
    val calendarEventId: Long? = null,
    val shootingTestEventId: Long? = null,
    val calendarEventType: String? = null,
    val name: String? = null,
    val description: String? = null,
    val date: LocalDateDTO? = null,
    val beginTime: LocalTimeDTO? = null,
    val endTime: LocalTimeDTO? = null,
    val lockedTime: DateTimeDTO? = null,
    val venue: ShootingTestVenueDTO? = null,
    val officials: List<ShootingTestOfficialDTO>? = null,
    val numberOfAllParticipants: Int,
    val numberOfParticipantsWithNoAttempts: Int,
    val numberOfCompletedParticipants: Int,
    val totalPaidAmount: Double?,
)

fun ShootingTestCalendarEventDTO.toCommonShootingTestCalendarEvent() =
    CommonShootingTestCalendarEvent(
        rhyId = rhyId,
        calendarEventId = calendarEventId,
        shootingTestEventId = shootingTestEventId,
        calendarEventType = calendarEventType.toBackendEnum(),
        name = name,
        description = description,
        date = date?.toLocalDate(),
        beginTime = beginTime?.toLocalTime(),
        endTime = endTime?.toLocalTime(),
        lockedTime = lockedTime,
        venue = venue?.toShootingTestVenue(),
        officials = officials?.map { it.toCommonShootingTestOfficial() },
        numberOfAllParticipants = numberOfAllParticipants,
        numberOfParticipantsWithNoAttempts = numberOfParticipantsWithNoAttempts,
        numberOfCompletedParticipants = numberOfCompletedParticipants,
        totalPaidAmount = totalPaidAmount,
    )
