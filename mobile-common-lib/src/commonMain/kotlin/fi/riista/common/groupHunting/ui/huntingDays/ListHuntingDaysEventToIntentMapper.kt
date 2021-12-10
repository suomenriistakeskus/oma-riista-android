package fi.riista.common.groupHunting.ui.huntingDays

import fi.riista.common.model.LocalDate
import fi.riista.common.ui.intent.IntentHandler

internal class ListHuntingDaysEventToIntentMapper(
    val intentHandler: IntentHandler<ListHuntingDaysIntent>
): ListHuntingDaysEventDispatcher {
    override fun dispatchFilterStartDateChanged(newStartDate: LocalDate) {
        intentHandler.handleIntent(ListHuntingDaysIntent.ChangeFilterStartDate(newStartDate))
    }

    override fun dispatchFilterEndDateChanged(newEndDate: LocalDate) {
        intentHandler.handleIntent(ListHuntingDaysIntent.ChangeFilterEndDate(newEndDate))
    }
}

sealed class ListHuntingDaysIntent {
    data class ChangeFilterStartDate(val startDate: LocalDate): ListHuntingDaysIntent()
    data class ChangeFilterEndDate(val endDate: LocalDate): ListHuntingDaysIntent()
}