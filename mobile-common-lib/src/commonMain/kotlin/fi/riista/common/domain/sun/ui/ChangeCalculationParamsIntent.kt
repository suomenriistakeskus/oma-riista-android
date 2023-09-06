package fi.riista.common.domain.sun.ui

import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate

sealed class ChangeCalculationParamsIntent {
    class ChangeLocation(
        val location: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ): ChangeCalculationParamsIntent()
    class ChangeDate(val localDate: LocalDate): ChangeCalculationParamsIntent()
}
