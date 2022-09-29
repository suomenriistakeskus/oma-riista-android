package fi.riista.common.domain.groupHunting.ui.huntingDays.select

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.model.LocalDate

data class SelectHuntingDayViewModel(
    val huntingDays: List<SelectableHuntingDayViewModel>,
    val selectedHuntingDayId: GroupHuntingDayId?,
    val canCreateHuntingDay: Boolean,

    /**
     * The date of the suggested hunting day (if there's one). If exists, the UI should
     * indicate that a hunting day can be created for this date.
     */
    val suggestedHuntingDayDate: LocalDate?,

    /**
     * A text to be displayed when there are no hunting days.
     */
    val noHuntingDaysText: String?,
) {
    val isHuntingDaySelected: Boolean
        get() = selectedHuntingDayId != null
}