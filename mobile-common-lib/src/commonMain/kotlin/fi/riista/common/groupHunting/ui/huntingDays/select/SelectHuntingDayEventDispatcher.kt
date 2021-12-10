package fi.riista.common.groupHunting.ui.huntingDays.select

import fi.riista.common.groupHunting.model.GroupHuntingDayId

interface SelectHuntingDayEventDispatcher {
    fun dispatchHuntingDaySelected(huntingDayId: GroupHuntingDayId)
}