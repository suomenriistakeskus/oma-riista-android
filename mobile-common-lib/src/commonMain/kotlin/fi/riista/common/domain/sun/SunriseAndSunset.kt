package fi.riista.common.domain.sun

import fi.riista.common.model.ETRSCoordinate
import fi.riista.common.model.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class SunriseAndSunset(
    // the location for which sunrise and sunset we're calculated
    val location: ETRSCoordinate,

    /**
     * The time when sun rises. Exists if sun rises after 00:00.
     */
    val sunrise: LocalTime?,

    /**
     * The time when sun sets. Exists if sun sets before 23:59:59.
     */
    val sunset: LocalTime?,
)
