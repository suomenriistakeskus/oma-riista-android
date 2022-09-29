package fi.riista.common.domain.huntingControl.ui.eventSelection

import fi.riista.common.domain.huntingControl.model.HuntingControlEventId
import fi.riista.common.model.LocalDate
import fi.riista.common.model.StringWithId

data class SelectHuntingControlEvent(
    val id: HuntingControlEventId,
    val date: LocalDate,
    val title: String,
    val modified: Boolean,
)

data class SelectHuntingControlEventViewModel(
    val showRhy: Boolean,
    val rhys: List<StringWithId>,
    val selectedRhy: StringWithId?,
    val events: List<SelectHuntingControlEvent>?,
)
