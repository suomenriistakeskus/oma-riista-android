package fi.riista.common.domain.sun.ui

import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.sun.SunriseAndSunset
import fi.riista.common.model.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class SunriseAndSunsetCalculationParams(
    val localDate: LocalDate,
    val location: CommonLocation,
    val lastResult: SunriseAndSunset?,
)
