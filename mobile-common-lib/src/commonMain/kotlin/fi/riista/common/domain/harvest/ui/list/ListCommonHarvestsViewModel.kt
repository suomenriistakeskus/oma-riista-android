package fi.riista.common.domain.harvest.ui.list

import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.Species
import fi.riista.common.model.EntitiesByYearMonth
import fi.riista.common.model.groupByYearMonth
import fi.riista.common.model.yearMonth

data class ListCommonHarvestsViewModel(
    val harvestHuntingYears: List<Int>,

    val filterOwnHarvests: Boolean,
    val filterHuntingYear: Int?,
    val filterSpecies: List<Species>?,

    val filteredHarvests: List<CommonHarvest>,
) {
    val filteringEnabled: Boolean by lazy {
        filterHuntingYear != null || (filterSpecies != null && filterSpecies.isNotEmpty())
    }

    /**
     * The filtered harvests that are grouped by year-month.
     */
    val filteredHarvestsByHuntingYearMonth: List<EntitiesByYearMonth<CommonHarvest>> by lazy {
        filteredHarvests.groupByYearMonth { it.pointOfTime.yearMonth() }
    }

    fun getByLocalId(localId: Long) = filteredHarvests.firstOrNull { it.localId == localId }
}
