package fi.riista.common.util

import fi.riista.common.model.LocalDateTime

class MockDateTimeProvider(
    var now: LocalDateTime = LocalDateTime(2015, 9, 4, 12, 30, 0),
): LocalDateTimeProvider {
    override fun now(): LocalDateTime {
        return now
    }
}