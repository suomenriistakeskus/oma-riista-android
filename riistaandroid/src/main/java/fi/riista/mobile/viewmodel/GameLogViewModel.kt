package fi.riista.mobile.viewmodel

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.models.srva.SrvaEvent
import fi.riista.mobile.observation.ObservationDatabase
import fi.riista.mobile.srva.SrvaDatabase
import fi.riista.mobile.utils.DateTimeUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.*
import java.util.Collections.emptyList
import javax.inject.Inject

class GameLogViewModel @Inject constructor(val harvestDatabase: HarvestDatabase,
                                           val observationDatabase: ObservationDatabase) : ViewModel() {

    private val typeSelected: MutableLiveData<String> = MutableLiveData(GameLog.TYPE_HARVEST)
    private val seasonSelected: MutableLiveData<Int> = MutableLiveData(DateTimeUtils.getHuntingYearForCalendar(Calendar.getInstance()))
    private val speciesSelected: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    private val categorySelected: MutableLiveData<Int> = MutableLiveData()

    private val seasons: MutableLiveData<List<Int>> = MutableLiveData()

    private val harvestSeasons: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    private val observationSeasons: MutableLiveData<List<Int>> = MutableLiveData(emptyList())
    private val srvaSeasons: MutableLiveData<List<Int>> = MutableLiveData(emptyList())

    init {
        refreshSeasons()
    }

    fun getTypeSelected(): LiveData<String> {
        return typeSelected
    }

    private fun setTypeSelected(@Nullable type: String) {
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

    fun getSpeciesSelected(): LiveData<List<Int>> {
        return speciesSelected
    }

    private fun setSpeciesSelected(@NonNull speciesIds: List<Int>) {
        speciesSelected.value = speciesIds
    }

    fun getCategorySelected(): LiveData<Int> {
        return categorySelected
    }

    private fun setCategorySelected(@Nullable categoryId: Int?) {
        categorySelected.value = categoryId
    }

    fun getSeasons(): LiveData<List<Int>> {
        return seasons
    }

    fun selectLogType(@NonNull type: String) {
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

    fun selectSpeciesIds(@NonNull speciesIds: List<Int>) {
        setCategorySelected(null)
        setSpeciesSelected(speciesIds)
    }

    fun selectSpeciesCategory(@NonNull categoryId: Int?) {
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

        // FIXME Executing a database query in the main thread!! This has been a source of ANR.
        val huntingYearsOfLocalHarvests = harvestDatabase.huntingYearsOfHarvests

        if (!huntingYearsOfLocalHarvests.contains(currentHuntingYear)) {
            huntingYearsOfLocalHarvests.add(currentHuntingYear)
        }

        harvestSeasons.value = huntingYearsOfLocalHarvests.sortedDescending()
        updateSeasons(typeSelected.value!!)

        observationDatabase.loadObservationYears { years ->
            if (!years.contains(currentHuntingYear)) {
                years.add(currentHuntingYear)
            }

            observationSeasons.value = years.sortedDescending()
            updateSeasons(typeSelected.value!!)
        }

        SrvaDatabase.getInstance().loadSrvaYears { years ->
            val currentCalendarYear = LocalDate.now().year

            if (!years.contains(currentCalendarYear)) {
                years.add(currentCalendarYear)
            }

            srvaSeasons.value = years.sortedDescending()
            updateSeasons(typeSelected.value!!)
        }
    }

    private fun updateSeasons(typeSelected: String) = when (typeSelected) {
        GameLog.TYPE_HARVEST -> harvestSeasons.value.let { seasons.value = it }
        GameLog.TYPE_OBSERVATION -> observationSeasons.value.let { seasons.value = it }
        GameLog.TYPE_SRVA -> srvaSeasons.value.let { seasons.value = it }
        else -> seasons.value = null
    }

    fun filterHarvestsWithCurrent(items: List<GameHarvest>): List<GameHarvest> {
        val season = seasonSelected.value!!
        val startDate = DateTimeUtils.getHuntingYearStart(season)
        val endDate = DateTimeUtils.getHuntingYearEnd(season)

        val filtered = ArrayList<GameHarvest>(items.size)

        for (event in items) {
            val eventTime = DateTime(event.mTime)

            if (eventTime.isAfter(startDate) && eventTime.isBefore(endDate)) {
                filtered.add(event)
            }
        }
        return filterHarvestsForSpecies(filtered)
    }

    private fun filterHarvestsForSpecies(items: List<GameHarvest>): List<GameHarvest> {
        val speciesCodes = speciesSelected.value

        if (speciesCodes == null || speciesCodes.isEmpty()) {
            return items
        }

        val filtered = ArrayList<GameHarvest>(items.size)

        for (event in items) {
            if (speciesCodes.contains(event.mSpeciesID)) {
                filtered.add(event)
            }
        }

        return filtered
    }

    fun filterObservationsWithCurrent(items: List<GameObservation>): List<GameObservation> {
        val season = seasonSelected.value!!
        val startDate = DateTimeUtils.getHuntingYearStart(season)
        val endDate = DateTimeUtils.getHuntingYearEnd(season)

        val filtered = ArrayList<GameObservation>(items.size)

        for (event in items) {
            val eventTime = event.toDateTime()

            if (eventTime.isAfter(startDate) && eventTime.isBefore(endDate)) {
                filtered.add(event)
            }
        }

        return filterObservationsForSpecies(filtered)
    }

    private fun filterObservationsForSpecies(items: List<GameObservation>): List<GameObservation> {
        val speciesCodes = speciesSelected.value

        if (speciesCodes == null || speciesCodes.isEmpty()) {
            return items
        }

        val filtered = ArrayList<GameObservation>(items.size)

        for (event in items) {
            if (speciesCodes.contains(event.gameSpeciesCode)) {
                filtered.add(event)
            }
        }

        return filtered
    }

    fun filterSrvasWithCurrent(items: List<SrvaEvent>): List<SrvaEvent> {
        val filtered = ArrayList<SrvaEvent>(items.size)
        val calendarYear = seasonSelected.value!!

        for (event in items) {
            if (event.toDateTime().year == calendarYear) {
                filtered.add(event)
            }
        }
        return filterSrvasForSpecies(filtered)
    }

    private fun filterSrvasForSpecies(items: List<SrvaEvent>): List<SrvaEvent> {
        val speciesCodes = speciesSelected.value

        if (speciesCodes == null || speciesCodes.isEmpty()) {
            return items
        }

        val filtered = ArrayList<SrvaEvent>(items.size)

        for (event in items) {
            if (speciesCodes.contains(event.gameSpeciesCode)) {
                filtered.add(event)
            }
        }

        return filtered
    }
}
