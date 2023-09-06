package fi.riista.common.domain.observation.ui.list

import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.model.EntitiesByYearMonth
import fi.riista.common.model.groupByYearMonth
import fi.riista.common.model.yearMonth

data class ListCommonObservationsViewModel(
    val observationHuntingYears: List<Int>,

    val filterHuntingYear: Int?,
    val filterSpecies: List<Species>?,

    val filteredObservations: List<CommonObservation>,
) {
    val filteringEnabled: Boolean by lazy {
        filterHuntingYear != null || (filterSpecies != null && filterSpecies.isNotEmpty())
    }

    /**
     * The filtered events that are grouped by year-month.
     */
    val filteredObservationsByHuntingYearMonth: List<EntitiesByYearMonth<CommonObservation>> by lazy {
        filteredObservations.groupByYearMonth { it.pointOfTime.yearMonth() }
    }

    fun getByLocalId(localId: Long) = filteredObservations.firstOrNull { it.localId == localId }
}
