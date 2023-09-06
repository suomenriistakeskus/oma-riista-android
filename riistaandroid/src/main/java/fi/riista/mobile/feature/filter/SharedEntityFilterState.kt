package fi.riista.mobile.feature.filter

import androidx.lifecycle.MutableLiveData
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.utils.DateTimeUtils
import java.util.Calendar
import java.util.Collections

class SharedEntityFilterState {
    val ownHarvests: MutableLiveData<Boolean> = MutableLiveData(true)
    val typeSelected: MutableLiveData<String> = MutableLiveData(GameLog.TYPE_HARVEST)
    val seasonSelected: MutableLiveData<Int> = MutableLiveData(DateTimeUtils.getHuntingYearForCalendar(Calendar.getInstance()))
    val speciesSelected: MutableLiveData<List<Int?>> = MutableLiveData(Collections.emptyList()) // Species Muu is null
    val categorySelected: MutableLiveData<Int?> = MutableLiveData()

    fun ensureHarvestIsShown(harvest: CommonHarvest) {
        typeSelected.value = GameLog.TYPE_HARVEST
        ownHarvests.value = harvest.actorInfo is GroupHuntingPerson.Unknown
        seasonSelected.value = harvest.pointOfTime.date.getHuntingYear()
        ensureSpeciesIsDisplayed(harvest.species)
    }

    fun ensureObservationIsShown(observation: CommonObservation) {
        typeSelected.value = GameLog.TYPE_OBSERVATION
        ownHarvests.value = true
        seasonSelected.value = observation.pointOfTime.date.getHuntingYear()
        ensureSpeciesIsDisplayed(observation.species)
    }

    fun ensureSrvaIsShown(srva: CommonSrvaEvent) {
        typeSelected.value = GameLog.TYPE_SRVA
        ownHarvests.value = true
        seasonSelected.value = srva.pointOfTime.year
        ensureSpeciesIsDisplayed(srva.species)
    }

    private fun ensureSpeciesIsDisplayed(species: Species) {
        val currentSpecies = speciesSelected.value
        val speciesCode = when (species) {
            is Species.Known -> species.speciesCode
            Species.Other -> null // null represents other in speciesSelected
            Species.Unknown -> {
                // cannot ensure this one is displayed
                return
            }
        }

        if (currentSpecies.isNullOrEmpty() || currentSpecies.contains(speciesCode)) {
            // either no filtering or given species already in list
            return
        }

        speciesSelected.value = currentSpecies + speciesCode
        categorySelected.value = null
    }
}
