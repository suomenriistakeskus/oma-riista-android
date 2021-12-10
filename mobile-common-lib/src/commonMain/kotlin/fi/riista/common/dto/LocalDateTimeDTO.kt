package fi.riista.common.dto

import fi.riista.common.model.LocalDateTime

typealias LocalDateTimeDTO = String

fun LocalDateTimeDTO.toLocalDateTime(): LocalDateTime? {
    return LocalDateTime.parseLocalDateTime(this)
}
