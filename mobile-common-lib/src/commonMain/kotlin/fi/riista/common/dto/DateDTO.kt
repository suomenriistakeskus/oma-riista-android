package fi.riista.common.dto

import fi.riista.common.model.Date
import kotlinx.serialization.Serializable

/**
 * Encapsulates the date information without year.
 */
@Serializable
data class DateDTO(
    val month: Int,
    val day: Int,
)

fun DateDTO.toDate() = Date(monthNumber = month, dayOfMonth = day)