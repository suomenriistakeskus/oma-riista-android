package fi.riista.common.domain.shootingTest

import fi.riista.common.domain.OperationResult
import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.domain.shootingTest.model.CalendarEventType
import fi.riista.common.domain.shootingTest.model.ShootingTestRegistrationStatus
import fi.riista.common.domain.shootingTest.model.ShootingTestResult
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShootingTestContextTest {

    @Test
    fun testFetchShootingTestCalendarEvents() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchShootingTestCalendarEvents()
        assertTrue(response is OperationResultWithData.Success)
        val events = response.data
        assertEquals(1, events.size)

        val event = events[0]
        assertEquals(180, event.rhyId)
        assertEquals(130, event.calendarEventId)
        assertEquals(CalendarEventType.AMPUMAKOE.toBackendEnum(), event.calendarEventType)
        assertEquals("koe 1", event.name)
        assertEquals("Eka koe", event.description)
        assertEquals(LocalDate(year = 2023, monthNumber = 2, dayOfMonth = 21), event.date)
        assertEquals(LocalTime(hour = 12, minute = 5, second = 0), event.beginTime)
        assertEquals(LocalTime(hour = 14, minute = 15, second = 0), event.endTime)
        assertEquals(3, event.numberOfAllParticipants)
        assertEquals(2, event.numberOfParticipantsWithNoAttempts)
        assertEquals(1, event.numberOfCompletedParticipants)
        assertEquals(3.21, event.totalPaidAmount)

        val venue = event.venue
        assertNotNull(venue)
        assertEquals(104, venue.id)
        assertEquals(1, venue.rev)
        assertEquals("test", venue.name)

        val venueAddress = venue.address
        assertNotNull(venueAddress)
        assertEquals(141, venueAddress.id)
        assertEquals(1, venueAddress.rev)
        assertEquals("Kisapaikankatu 1", venueAddress.streetAddress)
        assertEquals("12345", venueAddress.postalCode)
        assertEquals("Pellonpieli", venueAddress.city)

        val officials = event.officials
        assertNotNull(officials)
        assertEquals(2, officials.size)

        val official1 = officials[0]
        assertEquals(450, official1.id)
        assertEquals(89, official1.shootingTestEventId)
        assertEquals(247, official1.occupationId)
        assertEquals(4, official1.personId)
        assertEquals("Pena", official1.firstName)
        assertEquals("Mujunen", official1.lastName)
        assertTrue(official1.shootingTestResponsible)

        val official2 = officials[1]
        assertEquals(451, official2.id)
        assertEquals(89, official2.shootingTestEventId)
        assertEquals(246, official2.occupationId)
        assertEquals(8, official2.personId)
        assertEquals("Veikko", official2.firstName)
        assertEquals("Vartio", official2.lastName)
        assertFalse(official2.shootingTestResponsible)
    }

    @Test
    fun testFetchShootingTestCalendarEvent() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchShootingTestCalendarEvent(123)
        assertTrue(response is OperationResultWithData.Success)
        val event = response.data
        assertEquals(184, event.rhyId)
        assertEquals(90, event.calendarEventId)
        assertEquals(61, event.shootingTestEventId)
        assertEquals(CalendarEventType.AMPUMAKOE, event.calendarEventType?.value)
        assertEquals("Nimi", event.name)
        assertEquals("Kuvaus", event.description)
        assertEquals(LocalDate(2020, 5, 21), event.date)
        assertEquals(LocalTime(18, 0, 0), event.beginTime)
        assertEquals(LocalTime(19, 20, 0), event.endTime)
        assertNull(event.lockedTime)
        assertEquals(5, event.numberOfAllParticipants)
        assertEquals(3, event.numberOfParticipantsWithNoAttempts)
        assertEquals(2, event.numberOfCompletedParticipants)
        assertEquals(15.50, event.totalPaidAmount)

        val venue = event.venue
        assertNotNull(venue)
        assertEquals(2, venue.id)
        assertEquals(0, venue.rev)
        assertEquals("Takamestän rata0", venue.name)
        val venueAddress = venue.address
        assertNotNull(venueAddress)
        assertEquals(33, venueAddress.id)
        assertEquals(0, venueAddress.rev)
        assertEquals("Takamettätie 222", venueAddress.streetAddress)
        assertEquals("33270", venueAddress.postalCode)
        assertEquals("Takametsä", venueAddress.city)

        val officials = event.officials
        assertNotNull(officials)
        assertEquals(2, officials.size)
        assertEquals(61, officials[0].shootingTestEventId)
        assertEquals(79, officials[0].occupationId)
        assertEquals(11, officials[0].personId)
        assertEquals("Kimmo", officials[0].firstName)
        assertEquals("Alakoski", officials[0].lastName)
        assertTrue(officials[0].shootingTestResponsible)
    }

    @Test
    fun testOpenShootingTestEvent() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.openShootingTestEvent(
            calendarEventId = 12,
            shootingTestEventId = 34,
            occupationIds = listOf(4),
            responsibleOccupationId = null,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::openShootingTestEvent))
    }

    @Test
    fun testReopenShootingTestEvent() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.reopenShootingTestEvent(12)
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::reopenShootingTestEvent))
    }

    @Test
    fun testCloseShootingTestEvent() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.closeShootingTestEvent(12)
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::closeShootingTestEvent))
    }

    @Test
    fun testFetchAvailableShootingTestOfficialsForEvent() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchAvailableShootingTestOfficialsForEvent(123)
        assertTrue(response is OperationResultWithData.Success)
        val officials = response.data
        assertEquals(3, officials.size)

        val official1 = officials[0]
        assertEquals(79, official1.occupationId)
        assertEquals(11, official1.personId)
        assertEquals("Kimmo", official1.firstName)
        assertEquals("Alakoski", official1.lastName)
        assertTrue(official1.shootingTestResponsible)

        val official2 = officials[1]
        assertEquals(122, official2.occupationId)
        assertEquals(92, official2.personId)
        assertEquals("Jenna", official2.firstName)
        assertEquals("Aromaa", official2.lastName)
        assertFalse(official2.shootingTestResponsible)
    }

    @Test
    fun testFetchSelectedShootingTestOfficialsForEvent() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchSelectedShootingTestOfficialsForEvent(123)
        assertTrue(response is OperationResultWithData.Success)
        val officials = response.data
        assertEquals(3, officials.size)

        val official1 = officials[0]
        assertEquals(79, official1.occupationId)
        assertEquals(11, official1.personId)
        assertEquals("Kimmo", official1.firstName)
        assertEquals("Alakoski", official1.lastName)
        assertTrue(official1.shootingTestResponsible)

        val official2 = officials[1]
        assertEquals(122, official2.occupationId)
        assertEquals(92, official2.personId)
        assertEquals("Jenna", official2.firstName)
        assertEquals("Aromaa", official2.lastName)
        assertFalse(official2.shootingTestResponsible)
    }

    @Test
    fun testFetchAvailableShootingTestOfficialsForRhy() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchAvailableShootingTestOfficialsForRhy(123)
        assertTrue(response is OperationResultWithData.Success)
        val officials = response.data
        assertEquals(3, officials.size)

        val official1 = officials[0]
        assertEquals(79, official1.occupationId)
        assertEquals(11, official1.personId)
        assertEquals("Kimmo", official1.firstName)
        assertEquals("Alakoski", official1.lastName)
        assertTrue(official1.shootingTestResponsible)

        val official2 = officials[1]
        assertEquals(122, official2.occupationId)
        assertEquals(92, official2.personId)
        assertEquals("Jenna", official2.firstName)
        assertEquals("Aromaa", official2.lastName)
        assertFalse(official2.shootingTestResponsible)
    }

    @Test
    fun testUpdateShootingTestOfficials() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.updateShootingTestOfficials(
            calendarEventId = 12,
            shootingTestEventId = 34,
            officialOccupationIds = listOf(1, 2),
            responsibleOccupationId = null,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::updateShootingTestOfficials))
    }

    @Test
    fun testSearchPersonBySsn() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.searchPersonBySsn(1, "010105A987K")
        assertTrue(response is OperationResultWithData.Success)
        val person = response.data
        assertEquals(3, person.id)
        assertEquals("Asko", person.firstName)
        assertEquals("Partanen", person.lastName)
        assertEquals("11111111", person.hunterNumber)
        assertEquals(LocalDate(1911, 11, 11), person.dateOfBirth)
        assertEquals(ShootingTestRegistrationStatus.IN_PROGRESS, person.registrationStatus.value)
        assertFalse(person.selectedShootingTestTypes.mooseTestIntended)
        assertTrue(person.selectedShootingTestTypes.bearTestIntended)
        assertTrue(person.selectedShootingTestTypes.roeDeerTestIntended)
        assertFalse(person.selectedShootingTestTypes.bowTestIntended)
    }

    @Test
    fun testSearchPersonByHunterNumber() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.searchPersonByHunterNumber(1, "11111111")
        assertTrue(response is OperationResultWithData.Success)
        val person = response.data
        assertEquals(3, person.id)
        assertEquals("Asko", person.firstName)
        assertEquals("Partanen", person.lastName)
        assertEquals("11111111", person.hunterNumber)
        assertEquals(LocalDate(1911, 11, 11), person.dateOfBirth)
        assertEquals(ShootingTestRegistrationStatus.IN_PROGRESS, person.registrationStatus.value)
        assertFalse(person.selectedShootingTestTypes.mooseTestIntended)
        assertTrue(person.selectedShootingTestTypes.bearTestIntended)
        assertTrue(person.selectedShootingTestTypes.roeDeerTestIntended)
        assertFalse(person.selectedShootingTestTypes.bowTestIntended)
    }

    @Test
    fun testFetchShootingTestParticipants() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchShootingTestParticipants(22)
        assertTrue(response is OperationResultWithData.Success)
        assertEquals(1, response.data.size)
        val participant = response.data[0]
        assertEquals(124, participant.id)
        assertEquals(3, participant.rev)
        assertEquals("Asko", participant.firstName)
        assertEquals("Partanen", participant.lastName)
        assertEquals("11111111", participant.hunterNumber)
        assertFalse(participant.mooseTestIntended)
        assertTrue(participant.bearTestIntended)
        assertTrue(participant.deerTestIntended)
        assertFalse(participant.bowTestIntended)
        assertEquals("2023-03-06T10:13:50.480Z", participant.registrationTime)
        assertFalse(participant.completed)

        assertEquals(1, participant.attempts.size)
        val attempt = participant.attempts[0]
        assertEquals(ShootingTestType.BEAR, attempt.type.value)
        assertEquals(2, attempt.attemptCount)
        assertTrue(attempt.qualified)
    }

    @Test
    fun testFetchShootingTestParticipant() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchShootingTestParticipant(124)
        assertTrue(response is OperationResultWithData.Success)
        val participant = response.data
        assertEquals(124, participant.id)
        assertEquals(1, participant.rev)
        assertEquals("Asko", participant.firstName)
        assertEquals("Partanen", participant.lastName)
        assertEquals("11111111", participant.hunterNumber)
        assertFalse(participant.mooseTestIntended)
        assertTrue(participant.bearTestIntended)
        assertTrue(participant.deerTestIntended)
        assertFalse(participant.bowTestIntended)
        assertEquals(20.0, participant.totalDueAmount)
        assertEquals(0.0, participant.paidAmount)
        assertEquals(20.0, participant.remainingAmount)
        assertEquals("2023-03-06T10:13:50.480Z", participant.registrationTime)
        assertFalse(participant.completed)

        assertEquals(1, participant.attempts.size)
        val attempt = participant.attempts[0]
        assertEquals(ShootingTestType.BEAR, attempt.type.value)
        assertEquals(1, attempt.attemptCount)
        assertTrue(attempt.qualified)
    }

    @Test
    fun testAddShootingTestParticipant() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.addShootingTestParticipant(
            shootingTestEventId = 12,
            hunterNumber = "11111111",
            mooseTestIntended = true,
            bearTestIntended = false,
            roeDeerTestIntended = true,
            bowTestIntended = false,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::addShootingTestParticipant))
    }

    @Test
    fun testFetchShootingTestParticipantDetailed() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchShootingTestParticipantDetailed(124)
        assertTrue(response is OperationResultWithData.Success)
        val participant = response.data
        assertEquals(124, participant.id)
        assertEquals(1, participant.rev)
        assertEquals("Asko", participant.firstName)
        assertEquals("Partanen", participant.lastName)
        assertEquals("11111111", participant.hunterNumber)
        assertFalse(participant.mooseTestIntended)
        assertFalse(participant.bearTestIntended)
        assertFalse(participant.deerTestIntended)
        assertTrue(participant.bowTestIntended)
        assertEquals("2023-03-06T10:13:50.480Z", participant.registrationTime)
        assertFalse(participant.completed)

        assertEquals(1, participant.attempts.size)
        val attempt = participant.attempts[0]
        assertEquals(179, attempt.id)
        assertEquals(1, attempt.rev)
        assertEquals(ShootingTestType.BEAR, attempt.type.value)
        assertEquals(ShootingTestResult.QUALIFIED, attempt.result.value)
        assertEquals(4, attempt.hits)
        assertEquals("Hyvin osui", attempt.note)
    }

    @Test
    fun testFetchShootingTestAttempt() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.fetchShootingTestAttempt(123)
        assertTrue(response is OperationResultWithData.Success)
        val attempt = response.data
        assertEquals(179, attempt.id)
        assertEquals(1, attempt.rev)
        assertEquals(124, attempt.participantId)
        assertEquals(3, attempt.participantRev)
        assertEquals(ShootingTestType.BEAR, attempt.type.value)
        assertEquals(ShootingTestResult.QUALIFIED, attempt.result.value)
        assertEquals(4, attempt.hits)
        assertEquals("Osumia tuli", attempt.note)
    }

    @Test
    fun testAddShootingTestAttempt() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.addShootingTestAttempt(
            participantId = 1,
            participantRev = 2,
            type = ShootingTestType.BEAR,
            result = ShootingTestResult.QUALIFIED,
            hits = 2,
            note = null,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::addShootingTestAttempt))
    }

    @Test
    fun updateShootingTestAttempt() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.updateShootingTestAttempt(
            id = 88,
            rev = 1,
            participantId = 1,
            participantRev = 2,
            type = ShootingTestType.BEAR,
            result = ShootingTestResult.QUALIFIED,
            hits = 2,
            note = null,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::updateShootingTestAttempt))
    }

    @Test
    fun testRemoveShootingTestAttempt() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.removeShootingTestAttempt(41)
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::removeShootingTestAttempt))
    }

    @Test
    fun testUpdatePaymentStateForParticipant() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.updatePaymentStateForParticipant(
            participantId = 41,
            participantRev = 1,
            paidAttempts = 3,
            completed = false,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::updateShootingTestPaymentForParticipant))
    }

    @Test
    fun testCompleteAllPaymentsForParticipant() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val context = getShootingTestContext(backendAPIMock)
        val response = context.completeAllPaymentsForParticipant(
            participantId = 41,
            participantRev = 1,
        )
        assertTrue(response is OperationResult.Success)
        assertEquals(1, backendAPIMock.callCount(BackendAPI::completeAllPaymentsForParticipant))
    }

    private fun getShootingTestContext(backendApi: BackendAPIMock = BackendAPIMock()) =
        ShootingTestContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
        )
}
