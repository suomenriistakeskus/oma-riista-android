package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.ui.dataField.ActionEventDispatcher
import fi.riista.common.ui.dataField.IntEventDispatcher

interface HunterInfoDispatcher {
    fun dispatchHunterNumber(number: String)
    fun dispatchSsn(number: String)
}

interface HunterInfoIntEventDispatcher : IntEventDispatcher<HunterInfoField>
interface HunterInfoActionEventDispatcher : ActionEventDispatcher<HunterInfoField>
