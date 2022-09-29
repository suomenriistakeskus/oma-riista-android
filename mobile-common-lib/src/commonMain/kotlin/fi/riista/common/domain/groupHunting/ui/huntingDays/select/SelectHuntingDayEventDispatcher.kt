package fi.riista.common.domain.groupHunting.ui.huntingDays.select

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId

interface SelectHuntingDayEventDispatcher {
    fun dispatchHuntingDaySelected(huntingDayId: GroupHuntingDayId)
}