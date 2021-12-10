package fi.riista.common.dto

import fi.riista.common.model.LocalDate

typealias LocalDateDTO = String

fun LocalDateDTO.toLocalDate(): LocalDate? {
    return LocalDate.parseLocalDate(this)
}