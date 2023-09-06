package fi.riista.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.riista.common.RiistaSDK
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.feature.filter.SharedEntityFilterState
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.utils.DateTimeUtils
import org.joda.time.LocalDate
import java.util.Collections.emptyList
import javax.inject.Inject

class GameLogViewModel @Inject constructor(
    private val sharedEntityFilterState: SharedEntityFilterState
) : ViewModel() {

    private val ownHarvests: MutableLiveData<Boolean> by sharedEntityFilterState::ownHarvests
    private val typeSelected: MutableLiveData<String> by sharedEntityFilterState::typeSelected
    private val seasonSelected: MutableLiveData<Int> by sharedEntityFilterState::seasonSelected
    private val speciesSelected: MutableLiveData<List<Int?>> by sharedEntityFilterState::speciesSelected
    private val categorySelected: MutableLiveData<Int?> by sharedEntityFilterState::categorySelected

    private val seasons: MutableLiveData<List<Int>?> = MutableLiveData()

    private val harvestSeasons: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    private val observationSeasons: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    private val srvaSeasons: MutableLiveData<List<Int>> = MutableLiveData(emptyList())

    init {
        refreshSeasons()
    }

    fun isOwnHarvests(): LiveData<Boolean> {
        return ownHarvests
    }

    fun setOwnHarvests(ownHarvests: Boolean) {
        if (this.ownHarvests.value != ownHarvests) {
            this.ownHarvests.value = ownHarvests
        }
    }

    fun getTypeSelected(): LiveData<String> {
        return typeSelected
    }

    private fun setTypeSelected(type: String) {
        if (!typeSelected.value.equals(type)) {
            typeSelected.value = type

            updateSeasons(type)
        }
    }

    fun getSeasonSelected(): LiveData<Int> {
        return seasonSelected
    }

    private fun setSeasonSelected(season: Int) {
        if (seasonSelected.value != season) {
            seasonSelected.value = season
        }
    }

    fun getSpeciesSelected(): LiveData<List<Int?>> {
        return speciesSelected
    }

    private fun setSpeciesSelected(speciesIds: List<Int?>) {
        speciesSelected.value = speciesIds
    }

    fun getCategorySelected(): LiveData<Int?> {
        return categorySelected
    }

    private fun setCategorySelected(categoryId: Int?) {
        categorySelected.value = categoryId
    }

    fun getSeasons(): LiveData<List<Int>?> {
        return seasons
    }

    fun selectLogType(type: String) {
        setTypeSelected(type)

        when (type) {
            GameLog.TYPE_HARVEST -> {
                val seasons = harvestSeasons.value
                if (seasons != null && seasons.isNotEmpty() && !seasons.contains(getSeasonSelected().value)) {
                    seasonSelected.value = harvestSeasons.value!![0]
                }
            }
            GameLog.TYPE_OBSERVATION -> {
                val seasons = observationSeasons.value
                if (seasons != null && seasons.isNotEmpty() && !seasons.contains(getSeasonSelected().value)) {
                    seasonSelected.value = observationSeasons.value!![0]
                }
            }
            GameLog.TYPE_SRVA -> {
                val seasons = srvaSeasons.value
                if (seasons != null && seasons.isNotEmpty() && !seasons.contains(getSeasonSelected().value)) {
                    seasonSelected.value = srvaSeasons.value!![0]
                }
            }
        }
    }

    fun selectLogSeason(season: Int) {
        setSeasonSelected(season)
    }

    fun selectSpeciesIds(speciesIds: List<Int>) {
        setCategorySelected(null)
        setSpeciesSelected(speciesIds)
    }

    fun selectSpeciesCategory(categoryId: Int?) {
        setCategorySelected(categoryId)

        if (categoryId != null) {
            val speciesList = SpeciesInformation.getSpeciesForCategory(categoryId)
            val ids = ArrayList<Int>(speciesList.size)

            for (species in speciesList) {
                ids.add(species.mId)
            }

            setSpeciesSelected(ids)
        } else {
            setSpeciesSelected(emptyList())
        }
    }

    fun refreshSeasons() {
        val currentHuntingYear = DateTimeUtils.getHuntingYearForDate(LocalDate.now())

        RiistaSDK.harvestContext.getHarvestHuntingYears().let { harvestYears ->
            val years = harvestYears.ensureContainsYear(currentHuntingYear)
            harvestSeasons.value = years.sortedDescending()
        }

        RiistaSDK.observationContext.getObservationHuntingYears().let { observationYears ->
            val years = observationYears.ensureContainsYear(currentHuntingYear)
            observationSeasons.value = years.sortedDescending()
        }

        RiistaSDK.srvaContext.getSrvaYears().let { srvaYears ->
            val years = srvaYears.ensureContainsYear(LocalDate.now().year)
            srvaSeasons.value = years.sortedDescending()
        }

        updateSeasons(typeSelected.value!!)
    }

    private fun List<Int>.ensureContainsYear(year: Int): List<Int> {
        return if (this.contains(year)) {
            this
        } else {
            this + listOf(year)
        }
    }

    private fun updateSeasons(typeSelected: String) = when (typeSelected) {
        GameLog.TYPE_HARVEST -> harvestSeasons.value.let { seasons.value = it }
        GameLog.TYPE_OBSERVATION -> observationSeasons.value.let { seasons.value = it }
        GameLog.TYPE_SRVA -> srvaSeasons.value.let { seasons.value = it }
        else -> seasons.value = null
    }
}
