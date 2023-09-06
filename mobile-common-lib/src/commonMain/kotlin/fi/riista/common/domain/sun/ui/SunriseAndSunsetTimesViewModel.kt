package fi.riista.common.domain.sun.ui

import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class SunriseAndSunsetTimesViewModel(
    internal val parameters: SunriseAndSunsetCalculationParams,

    override val fields: DataFields<SunriseAndSunsetField> = listOf(),
): DataFieldViewModel<SunriseAndSunsetField>()
