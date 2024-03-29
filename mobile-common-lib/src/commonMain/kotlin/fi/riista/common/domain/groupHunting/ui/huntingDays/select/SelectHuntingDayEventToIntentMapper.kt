package fi.riista.common.domain.groupHunting.ui.huntingDays.select

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.ui.intent.IntentHandler

internal class SelectHuntingDayEventToIntentMapper(
    val intentHandler: IntentHandler<SelectHuntingDayIntent>
): SelectHuntingDayEventDispatcher {
    override fun dispatchHuntingDaySelected(huntingDayId: GroupHuntingDayId) {
        intentHandler.handleIntent(SelectHuntingDayIntent.ChangeSelectedHuntingDay(huntingDayId))
    }
}

sealed class SelectHuntingDayIntent {
    data class ChangeSelectedHuntingDay(val huntingDayId: GroupHuntingDayId): SelectHuntingDayIntent()
}