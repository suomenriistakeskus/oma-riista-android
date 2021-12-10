package fi.riista.common.dto

import fi.riista.common.model.LocalTime

typealias LocalTimeDTO = String

fun LocalTimeDTO.toLocalTime(): LocalTime? {
    return LocalTime.parseLocalTime(this)
}