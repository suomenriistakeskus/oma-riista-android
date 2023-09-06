package fi.riista.common.domain.sun.ui

import fi.riista.common.ui.dataField.LocalDateEventDispatcher
import fi.riista.common.ui.dataField.LocationEventDispatcher

interface ChangeCalculationParamsEventDispatcher {
    val localDateEventDispatcher: LocalDateEventDispatcher<SunriseAndSunsetField>
    val locationEventDispatcher: LocationEventDispatcher<SunriseAndSunsetField>
}
