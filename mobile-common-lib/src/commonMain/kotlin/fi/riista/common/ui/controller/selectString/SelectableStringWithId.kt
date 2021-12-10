package fi.riista.common.ui.controller.selectString

import fi.riista.common.model.StringWithId

data class SelectableStringWithId(
    val value: StringWithId,
    val selected: Boolean,
)
