package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.ui.dataField.HoursAndMinutesEventDispatcher
import fi.riista.common.ui.dataField.IntEventDispatcher
import fi.riista.common.ui.dataField.LocalDateTimeEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher

interface ModifyGroupHuntingDayEventDispatcher {
    val intEventDispatcher: IntEventDispatcher<GroupHuntingDayField>
    val dateTimeEventDispatcher: LocalDateTimeEventDispatcher<GroupHuntingDayField>
    val stringWithIdDispatcher: StringWithIdEventDispatcher<GroupHuntingDayField>
    val durationEventDispatcher: HoursAndMinutesEventDispatcher<GroupHuntingDayField>
}