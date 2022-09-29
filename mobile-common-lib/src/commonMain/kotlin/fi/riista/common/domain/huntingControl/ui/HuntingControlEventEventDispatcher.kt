package fi.riista.common.domain.huntingControl.ui

import fi.riista.common.ui.dataField.*

interface HuntingControlEventStringWithIdEventDispatcher
    : StringWithIdEventDispatcher<HuntingControlEventField>

interface HuntingControlEventStringWithIdClickEventDispatcher
    : StringWithIdClickEventDispatcher<HuntingControlEventField>

interface HuntingControlEventLocationEventDispatcher
    : LocationEventDispatcher<HuntingControlEventField>

interface HuntingControlEventIntEventDispatcher
    : IntEventDispatcher<HuntingControlEventField>

interface HuntingControlEventStringEventDispatcher
    : StringEventDispatcher<HuntingControlEventField>

interface HuntingControlEventBooleanEventDispatcher
    : BooleanEventDispatcher<HuntingControlEventField>

interface HuntingControlEventLocalDateEventDispatcher
    : LocalDateEventDispatcher<HuntingControlEventField>

interface HuntingControlEventLocalTimeEventDispatcher
    : LocalTimeEventDispatcher<HuntingControlEventField>

interface HuntingControlAttachmentActionEventDispatcher
    : ActionEventDispatcher<HuntingControlEventField>

interface HuntingControlAttachmentEventDispatcher
    : AttachmentEventDispatcher<HuntingControlEventField>
