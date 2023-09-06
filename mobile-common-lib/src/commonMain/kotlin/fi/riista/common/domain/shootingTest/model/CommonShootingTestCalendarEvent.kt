package fi.riista.common.domain.shootingTest.model

import fi.riista.common.dto.DateTimeDTO
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

typealias CalendarEventId = Long
typealias ShootingTestEventId = Long

data class CommonShootingTestCalendarEvent(
    val rhyId: Long?,
    val calendarEventId: CalendarEventId?,
    val shootingTestEventId: ShootingTestEventId?,
    val calendarEventType: BackendEnum<CalendarEventType>?,
    val name: String?,
    val description: String?,
    val date: LocalDate?,
    val beginTime: LocalTime?,
    val endTime: LocalTime?,
    // keep lockedTime in DTO format as we don't currently need the actual timestamp value. It is enough to
    // know whether event has been locked or not. Plus we don't currently have model for DateTime.
    val lockedTime: DateTimeDTO?,
    val venue: CommonShootingTestVenue?,
    val officials: List<CommonShootingTestOfficial>?,
    val numberOfAllParticipants: Int,
    val numberOfParticipantsWithNoAttempts: Int,
    val numberOfCompletedParticipants: Int,
    val totalPaidAmount: Double?,
) {
    enum class State(override val resourcesStringId: RR.string): LocalizableEnum {
        WAITING_TO_START(RR.string.shooting_test_state_waiting_to_start),
        ONGOING(RR.string.shooting_test_state_ongoing),
        ONGOING_READY_TO_CLOSE(RR.string.shooting_test_state_ongoing_ready_to_close),
        CLOSED(RR.string.shooting_test_state_closed),
    }

    val state: State
        get() {
            return if (shootingTestEventId == null) {
                State.WAITING_TO_START
            } else {
                if (lockedTime?.trim().isNullOrEmpty().not()) {
                    State.CLOSED
                } else if (numberOfCompletedParticipants >= numberOfAllParticipants) {
                    // using == should be enough but let's be sure
                    State.ONGOING_READY_TO_CLOSE
                } else {
                    State.ONGOING
                }
            }
        }

    val waitingToStart: Boolean = state == State.WAITING_TO_START
    val ongoing: Boolean = state == State.ONGOING || state == State.ONGOING_READY_TO_CLOSE
    val readyToClose: Boolean = state == State.ONGOING_READY_TO_CLOSE
    val closed: Boolean = state == State.CLOSED
}
