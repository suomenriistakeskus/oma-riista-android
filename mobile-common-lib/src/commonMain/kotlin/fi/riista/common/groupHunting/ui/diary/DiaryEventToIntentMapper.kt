package fi.riista.common.groupHunting.ui.diary

import fi.riista.common.model.LocalDate
import fi.riista.common.ui.intent.IntentHandler

internal class DiaryEventToIntentMapper(
    val intentHandler: IntentHandler<DiaryEventIntent>
) : DiaryEventDispatcher {

    override fun dispatchFilterStartDateChanged(newStartDate: LocalDate) {
        intentHandler.handleIntent(DiaryEventIntent.ChangeFilterStartDate(newStartDate))
    }

    override fun dispatchFilterEndDateChanged(newEndDate: LocalDate) {
        intentHandler.handleIntent(DiaryEventIntent.ChangeFilterEndDate(newEndDate))
    }

    override fun dispatchDiaryFilterChanged(newDiaryFilter: DiaryFilter) {
        intentHandler.handleIntent(DiaryEventIntent.ChangeDiaryFilter(newDiaryFilter))
    }
}

sealed class DiaryEventIntent {
    data class ChangeFilterStartDate(val startDate: LocalDate): DiaryEventIntent()
    data class ChangeFilterEndDate(val endDate: LocalDate): DiaryEventIntent()
    data class ChangeDiaryFilter(val diaryFilter: DiaryFilter): DiaryEventIntent()
}
