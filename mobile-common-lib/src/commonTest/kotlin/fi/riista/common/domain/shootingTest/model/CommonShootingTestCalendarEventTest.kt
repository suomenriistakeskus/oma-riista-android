package fi.riista.common.domain.shootingTest.model

import fi.riista.common.dto.DateTimeDTO
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime
import fi.riista.common.model.toBackendEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonShootingTestCalendarEventTest {
    @Test
    fun `should be waiting to start`() {
        with (createEvent(shootingTestEventId = null, null, 1, 0)) {
            assertEquals(CommonShootingTestCalendarEvent.State.WAITING_TO_START, state, "lockedTime: null")
            assertTrue(isWaitingToStart(), "lockedTime: null")
            assertTrue(waitingToStart, "lockedTime: null")
            assertFalse(isOngoing(), "lockedTime: null")
            assertFalse(ongoing, "lockedTime: null")
            assertFalse(isReadyToClose(), "lockedTime: null")
            assertFalse(readyToClose, "lockedTime: null")
            assertFalse(isClosed(), "lockedTime: null")
            assertFalse(closed, "lockedTime: null")
        }
        with (createEvent(shootingTestEventId = null, "2023-04-01T14:38:55.902Z", 1, 0)) {
            assertEquals(CommonShootingTestCalendarEvent.State.WAITING_TO_START, state, "lockedTime: <valid>, num = 0")
            assertTrue(isWaitingToStart(), "lockedTime: <valid>, num = 0")
            assertTrue(waitingToStart, "lockedTime: <valid>, num = 0")
            assertFalse(isOngoing(), "lockedTime: <valid>, num = 0")
            assertFalse(ongoing, "lockedTime: <valid>, num = 0")
            assertFalse(isReadyToClose(), "lockedTime: <valid>, num = 0")
            assertFalse(readyToClose, "lockedTime: <valid>, num = 0")
            assertFalse(isClosed(), "lockedTime: <valid>, num = 0")
            assertFalse(closed, "lockedTime: <valid>, num = 0")
        }
        with (createEvent(shootingTestEventId = null, "2023-04-01T14:38:55.902Z", 1, 1)) {
            assertEquals(CommonShootingTestCalendarEvent.State.WAITING_TO_START, state, "lockedTime: <valid>, num = 1")
            assertTrue(isWaitingToStart(), "lockedTime: <valid>, num = 1")
            assertTrue(waitingToStart, "lockedTime: <valid>, num = 1")
            assertFalse(isOngoing(), "lockedTime: <valid>, num = 1")
            assertFalse(ongoing, "lockedTime: <valid>, num = 1")
            assertFalse(isReadyToClose(), "lockedTime: <valid>, num = 1")
            assertFalse(readyToClose, "lockedTime: <valid>, num = 1")
            assertFalse(isClosed(), "lockedTime: <valid>, num = 1")
            assertFalse(closed, "lockedTime: <valid>, num = 1")
        }
    }

    @Test
    fun `should be ongoing`() {
        with (createEvent(shootingTestEventId = 1, null, 1, 0)) {
            assertEquals(CommonShootingTestCalendarEvent.State.ONGOING, state)
            assertFalse(isWaitingToStart())
            assertFalse(waitingToStart)
            assertTrue(isOngoing())
            assertTrue(ongoing)
            assertFalse(isReadyToClose())
            assertFalse(readyToClose)
            assertFalse(isClosed())
            assertFalse(closed)
        }
    }

    @Test
    fun `should be ready to close`() {
        with (createEvent(shootingTestEventId = 1, null, 1, 1)) {
            assertEquals(CommonShootingTestCalendarEvent.State.ONGOING_READY_TO_CLOSE, state)
            assertFalse(isWaitingToStart())
            assertFalse(waitingToStart)
            assertTrue(isOngoing())
            assertTrue(ongoing)
            assertTrue(isReadyToClose())
            assertTrue(readyToClose)
            assertFalse(isClosed())
            assertFalse(closed)
        }
    }

    @Test
    fun `should be closed`() {
        with (createEvent(shootingTestEventId = 1, "2023-04-01T14:38:55.902Z", 1, 1)) {
            assertEquals(CommonShootingTestCalendarEvent.State.CLOSED, state, "completed: 1")
            assertFalse(isWaitingToStart(), "completed: 1")
            assertFalse(waitingToStart, "completed: 1")
            assertFalse(isOngoing(), "completed: 1")
            assertFalse(ongoing, "completed: 1")
            assertFalse(isReadyToClose(), "completed: 1")
            assertFalse(readyToClose, "completed: 1")
            assertTrue(isClosed(), "completed: 1")
            assertTrue(closed, "completed: 1")
        }

        with (createEvent(shootingTestEventId = 1, "2023-04-01T14:38:55.902Z", 1, 0)) {
            assertEquals(CommonShootingTestCalendarEvent.State.CLOSED, state, "completed: 0")
            assertFalse(isWaitingToStart(), "completed: 0")
            assertFalse(waitingToStart, "completed: 0")
            assertFalse(isOngoing(), "completed: 0")
            assertFalse(ongoing, "completed: 0")
            assertFalse(isReadyToClose(), "completed: 0")
            assertFalse(readyToClose, "completed: 0")
            assertTrue(isClosed(), "completed: 0")
            assertTrue(closed, "completed: 0")
        }
    }

    private fun createEvent(
        shootingTestEventId: Long?,
        lockedTime: DateTimeDTO?,
        numberOfAllParticipants: Int,
        numberOfCompletedParticipants: Int
    ) = CommonShootingTestCalendarEvent(
        rhyId = 1,
        calendarEventId = 2,
        shootingTestEventId = shootingTestEventId,
        calendarEventType = CalendarEventType.AMPUMAKOE.toBackendEnum(),
        name = "shooting test",
        description = "description",
        date = LocalDate(2023, 4, 1),
        beginTime = LocalTime(10, 0, 0),
        endTime = LocalTime(14, 0, 0),
        lockedTime = lockedTime,
        venue = CommonShootingTestVenue(
            id = null,
            rev = 1,
            name = null,
            address = null,
            info = null,
        ),
        officials = listOf(),
        numberOfAllParticipants = numberOfAllParticipants,
        numberOfParticipantsWithNoAttempts = numberOfAllParticipants,
        numberOfCompletedParticipants = numberOfCompletedParticipants,
        totalPaidAmount = 120.0,
    )
}


// these functions are taken from the application side

private fun CommonShootingTestCalendarEvent.isWaitingToStart(): Boolean {
    return shootingTestEventId == null
}

private fun CommonShootingTestCalendarEvent.isOngoing(): Boolean {
    return shootingTestEventId != null && !isClosed()
}

private fun CommonShootingTestCalendarEvent.isClosed(): Boolean {
    return shootingTestEventId != null && lockedTime?.trim().isNullOrEmpty().not()
}

private fun CommonShootingTestCalendarEvent.isReadyToClose(): Boolean {
    return isOngoing() && numberOfAllParticipants == numberOfCompletedParticipants
}