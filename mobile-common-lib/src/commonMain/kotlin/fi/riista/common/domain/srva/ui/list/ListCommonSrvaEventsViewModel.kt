package fi.riista.common.domain.srva.ui.list

import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.model.EntitiesByYearMonth
import fi.riista.common.model.groupByYearMonth
import fi.riista.common.model.yearMonth

data class ListCommonSrvaEventsViewModel(
    val srvaEventYears: List<Int>,
    val srvaSpecies: List<Species>,

    val filterYear: Int?,
    val filterSpecies: List<Species>?,

    val filteredSrvaEvents: List<CommonSrvaEvent>,
) {
    val filteringEnabled: Boolean by lazy {
        filterYear != null || (filterSpecies != null && filterSpecies.isNotEmpty())
    }

    /**
     * The filtered events that are grouped by year-month.
     */
    val filteredSrvaEventsByYearMonth: List<EntitiesByYearMonth<CommonSrvaEvent>> by lazy {
        filteredSrvaEvents.groupByYearMonth { it.pointOfTime.yearMonth() }
    }

    fun getByLocalId(localId: Long) = filteredSrvaEvents.firstOrNull { it.localId == localId }
}
