package fi.riista.common.ui.controller.selectString

import fi.riista.common.model.StringWithId

interface SelectStringWithIdEventDispatcher {
    fun dispatchFilterChanged(filter: String)
    fun dispatchSelectedValueChanged(value: StringWithId)
}
