package fi.riista.common.domain.groupHunting.ui.huntingDays

import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.model.Entity

data class HuntingDayViewModel(
    val huntingDay: GroupHuntingDay,
    val harvests: List<HuntingDayHarvestViewModel>,
    val observations: List<HuntingDayObservationViewModel>,
    val hasProposedEntries: Boolean,
    val canEditHuntingDay: Boolean,

    /**
     * Can a hunting day be created based on [huntingDay] values? Applies most likely
     * only suggested hunting days (see [huntingDayType]).
     */
    val canCreateHuntingDay: Boolean,
    val showHuntingDayDetails: Boolean
) {
    enum class HuntingDayType {
        // Hunting day is being suggested i.e. it doesn't really exist yet.
        SUGGESTED,

        // Hunting day exists already
        EXISTING,
    }

    val huntingDayType: HuntingDayType
        get() {
            return when (huntingDay.type) {
                Entity.Type.LOCAL -> HuntingDayType.SUGGESTED
                Entity.Type.REMOTE -> HuntingDayType.EXISTING
            }
        }

    val harvestCount: Int by lazy {
        calculateTotalAmount(harvests)
    }

    val observationCount: Int by lazy {
        calculateTotalAmount(observations)
    }

    companion object {
        private fun calculateTotalAmount(entries: List<HuntingDayDiaryEntryViewModel>): Int {
            return entries.fold(0) { acc, entry ->
                acc + entry.amount
            }
        }
    }
}