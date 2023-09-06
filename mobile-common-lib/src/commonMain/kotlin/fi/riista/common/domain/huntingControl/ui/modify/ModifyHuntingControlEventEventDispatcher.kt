package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.ui.dataField.*

interface ModifyHuntingControlEventEventDispatcher {
    val stringWithIdEventDispatcher: StringWithIdEventDispatcher<HuntingControlEventField>
    val stringWithIdClickEventDispatcher: StringWithIdClickEventDispatcher<HuntingControlEventField>
    val locationEventDispatcher: LocationEventDispatcher<HuntingControlEventField>
    val intEventDispatcher: IntEventDispatcher<HuntingControlEventField>
    val stringEventDispatcher: StringEventDispatcher<HuntingControlEventField>
    val booleanEventDispatcher: BooleanEventDispatcher<HuntingControlEventField>
    val localDateEventDispatcher: LocalDateEventDispatcher<HuntingControlEventField>
    val localTimeEventDispatcher: LocalTimeEventDispatcher<HuntingControlEventField>
    val attachmentActionEventDispatcher: ActionEventDispatcher<HuntingControlEventField>
    val attachmentEventDispatcher: AttachmentEventDispatcher<HuntingControlEventField>
}
