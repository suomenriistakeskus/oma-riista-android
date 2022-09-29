package fi.riista.common.domain.groupHunting.ui.huntingDays

import fi.riista.common.model.LocalDate

interface ListHuntingDaysEventDispatcher {
    fun dispatchFilterStartDateChanged(newStartDate: LocalDate)
    fun dispatchFilterEndDateChanged(newEndDate: LocalDate)
}