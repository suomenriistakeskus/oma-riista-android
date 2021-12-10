package fi.riista.common.domain.season.dto

import fi.riista.common.domain.season.model.HuntingSeason
import fi.riista.common.dto.DatePeriodDTO
import fi.riista.common.dto.toDatePeriod
import fi.riista.common.model.HuntingYear
import kotlinx.serialization.Serializable

@Serializable
data class HuntingSeasonDTO(
    val startYear: HuntingYear?,
    val endYear: HuntingYear?,
    val yearlySeasonPeriods: List<DatePeriodDTO>
)

fun HuntingSeasonDTO.toHuntingSeason() = HuntingSeason(
        startYear = startYear,
        endYear = endYear,
        yearlySeasonPeriods = yearlySeasonPeriods.map { it.toDatePeriod() }
)