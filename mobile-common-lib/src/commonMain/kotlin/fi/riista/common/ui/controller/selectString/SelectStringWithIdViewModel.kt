package fi.riista.common.ui.controller.selectString

import fi.riista.common.model.StringWithId

data class SelectStringWithIdViewModel(
    val allValues: List<SelectableStringWithId>,
    val filter: String,
    val filteredValues: List<SelectableStringWithId>,
    val selectedValue: StringWithId?,
)
