package fi.riista.common.domain.groupHunting.ui.diary

import fi.riista.common.model.LocalDate

interface DiaryEventDispatcher {
    fun dispatchFilterStartDateChanged(newStartDate: LocalDate)
    fun dispatchFilterEndDateChanged(newEndDate: LocalDate)
    fun dispatchDiaryFilterChanged(newDiaryFilter: DiaryFilter)
}
