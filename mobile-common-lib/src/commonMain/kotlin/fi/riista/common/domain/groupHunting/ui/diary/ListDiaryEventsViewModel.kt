package fi.riista.common.domain.groupHunting.ui.diary

import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.model.LocalDate

data class ListDiaryEventsViewModel(
    val events: DiaryEvent?
)

data class DiaryEvent(
    val filterStartDate: LocalDate,
    val filterEndDate: LocalDate,
    val minFilterDate: LocalDate,
    val maxFilterDate: LocalDate,
    val huntingGroupArea: HuntingGroupArea?,
    val diaryFilter: DiaryFilter,
    /**
     * The hunting events that have passed the filter i.e. that are available for displaying.
     */
    val filteredEvents: DiaryViewModel,

    // should never be empty. Indicate no content by passing null events to viewModel (see above)
    val allEvents: DiaryViewModel,
) {

    init {
        if (allEvents.harvests.isEmpty() && allEvents.observations.isEmpty()) {
            throw AssertionError("There must be at least one observation or harvest!")
        }
    }
}
