package fi.riista.common.domain.groupHunting.ui.huntingDays

import fi.riista.common.model.LocalDate

data class ListHuntingDaysViewModel(
    /**
     * The hunting days if they exist. Should be null in case no hunting days were available
     * but loading succeeded.
     */
    val huntingDays: HuntingDays?,

    /**
     * Can the user create new hunting days?
     */
    val canCreateHuntingDay: Boolean,

    /**
     * A text to be displayed when there are no hunting days.
     */
    val noHuntingDaysText: String?,
) {
    /**
     * Are there any hunting days to be displayed?
     */
    val containsHuntingDaysAfterFiltering: Boolean
        get() {
            return huntingDays != null && huntingDays.filteredHuntingDays.isNotEmpty()
        }
}

data class HuntingDays(
    val filterStartDate: LocalDate,
    val filterEndDate: LocalDate,
    val minFilterDate: LocalDate,
    val maxFilterDate: LocalDate,
    /**
     * The hunting days that have passed the filter i.e. that are available for displaying.
     */
    val filteredHuntingDays: List<HuntingDayViewModel>,

    // should never be empty. Indicate no content by passing null huntingDays to viewModel (see above)
    val allHuntingDays: List<HuntingDayViewModel>,
) {
    init {
        if (allHuntingDays.isEmpty()) {
            throw AssertionError("There must be at least one hunting day!")
        }
    }
}