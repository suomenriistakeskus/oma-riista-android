package fi.riista.common.dto

import fi.riista.common.model.DatePeriod
import kotlinx.serialization.Serializable

@Serializable
data class DatePeriodDTO(
    val beginDate: DateDTO,
    val endDate: DateDTO
)

fun DatePeriodDTO.toDatePeriod() = DatePeriod(beginDate.toDate(), endDate.toDate())