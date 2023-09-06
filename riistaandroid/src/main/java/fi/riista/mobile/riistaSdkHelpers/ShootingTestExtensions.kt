package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.shootingTest.model.CommonShootingTestAttempt
import fi.riista.common.domain.shootingTest.model.CommonShootingTestCalendarEvent
import fi.riista.common.domain.shootingTest.model.CommonShootingTestOfficial
import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipant
import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipantAttempt
import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipantDetailed
import fi.riista.common.domain.shootingTest.model.CommonShootingTestVenue
import fi.riista.common.domain.shootingTest.model.CommonShootingTestVenueAddress
import fi.riista.common.model.toHoursAndMinutesString
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptSummary
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.models.shootingTest.ShootingTestParticipantDetailed
import fi.riista.mobile.models.shootingTest.ShootingTestVenue
import fi.riista.mobile.models.shootingTest.ShootingTestVenueAddress
import java.math.BigDecimal

fun CommonShootingTestCalendarEvent.toShootingTestCalendarEvent(): ShootingTestCalendarEvent {
    val event =  ShootingTestCalendarEvent()
    event.rhyId = rhyId
    event.calendarEventId = calendarEventId
    event.shootingTestEventId = shootingTestEventId
    event.calendarEventType = calendarEventType?.rawBackendEnumValue
    event.name = name
    event.description = description
    event.date = date?.toStringISO8601()
    event.beginTime = beginTime?.toHoursAndMinutesString()
    event.endTime = endTime?.toHoursAndMinutesString()
    event.lockedTime = lockedTime
    event.venue = venue?.toShootingTestVenue()
    event.officials = officials?.map { it.toShootingTestOfficial() }
    event.numberOfAllParticipants = numberOfAllParticipants
    event.numberOfCompletedParticipants = numberOfCompletedParticipants
    event.numberOfParticipantsWithNoAttempts = numberOfParticipantsWithNoAttempts
    event.totalPaidAmount = BigDecimal.valueOf(totalPaidAmount ?: 0.0)
    return event
}

fun CommonShootingTestVenue.toShootingTestVenue(): ShootingTestVenue {
    val venue = ShootingTestVenue()
    venue.id = id
    venue.rev = rev
    venue.name = name
    venue.address = address?.toShootingTestVenueAddress()
    venue.info = info
    return venue
}

fun CommonShootingTestVenueAddress.toShootingTestVenueAddress(): ShootingTestVenueAddress {
    val address = ShootingTestVenueAddress()
    address.id = id
    address.rev = rev
    address.streetAddress = streetAddress
    address.postalCode = postalCode
    address.city = city
    return address
}

fun CommonShootingTestOfficial.toShootingTestOfficial(): ShootingTestOfficial {
    val official = ShootingTestOfficial()
    official.id = id
    official.shootingTestEventId = shootingTestEventId
    official.occupationId = occupationId
    official.personId = personId
    official.firstName = firstName
    official.lastName = lastName
    official.shootingTestResponsible = shootingTestResponsible
    return official
}

fun CommonShootingTestAttempt.toShootingTestAttemptDetailed(): ShootingTestAttemptDetailed {
    val attempt = ShootingTestAttemptDetailed()
    attempt.id = id
    attempt.rev = rev
    attempt.type = type.value
    attempt.result = result.value
    attempt.hits = hits
    attempt.note = note
    return attempt
}

fun CommonShootingTestParticipantDetailed.toShootingTestParticipantDetailed(): ShootingTestParticipantDetailed {
    val participant = ShootingTestParticipantDetailed()
    participant.id = id
    participant.rev = rev
    participant.firstName = firstName
    participant.lastName = lastName
    participant.hunterNumber = hunterNumber
    participant.dateOfBirth = dateOfBirth?.toStringISO8601()
    participant.mooseTestIntended = mooseTestIntended
    participant.bearTestIntended = bearTestIntended
    participant.deerTestIntended = deerTestIntended
    participant.bowTestIntended = bowTestIntended
    participant.attempts = attempts.map { it.toShootingTestAttemptDetailed() }
    return participant
}

fun CommonShootingTestParticipant.toShootingTestParticipant(): ShootingTestParticipant {
    val participant = ShootingTestParticipant()
    participant.id = id
    participant.rev = rev
    participant.firstName = firstName
    participant.lastName = lastName
    participant.hunterNumber = hunterNumber
    participant.mooseTestIntended = mooseTestIntended
    participant.bearTestIntended = bearTestIntended
    participant.deerTestIntended = deerTestIntended
    participant.bowTestIntended = bowTestIntended
    participant.attempts = attempts.map { it.toShootingTestAttemptSummary() }
    participant.totalDueAmount = totalDueAmount.toInt()
    participant.paidAmount = paidAmount.toInt()
    participant.remainingAmount = remainingAmount.toInt()
    participant.registrationTime = registrationTime
    participant.completed = completed
    return participant
}

fun CommonShootingTestParticipantAttempt.toShootingTestAttemptSummary(): ShootingTestAttemptSummary {
    val attemptSummary = ShootingTestAttemptSummary()
    attemptSummary.type = type.value
    attemptSummary.attemptCount = attemptCount
    attemptSummary.qualified = qualified
    return attemptSummary
}
