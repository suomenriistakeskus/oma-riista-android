package fi.riista.common.domain.huntingControl.sync.model

import fi.riista.common.domain.huntingControl.model.HuntingControlEventInspector
import fi.riista.common.model.LocalDate

data class GameWarden(
    val inspector: HuntingControlEventInspector,
    val beginDate: LocalDate?,
    val endDate: LocalDate?,
)
