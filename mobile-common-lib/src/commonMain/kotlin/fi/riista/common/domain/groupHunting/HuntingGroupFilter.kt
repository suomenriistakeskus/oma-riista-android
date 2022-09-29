package fi.riista.common.domain.groupHunting

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.HuntingGroup
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.util.firstAndOnly

@Suppress("MemberVisibilityCanBePrivate")
class HuntingGroupFilter internal constructor(
    private val huntingGroups: List<HuntingGroup>,
) {
    val selectableHuntingYears: List<HuntingYear> by lazy {
        // we know we don't want to have duplicates so map to mutableSet and convert to
        // list afterwards. We could also use List.distinct() but that does the same thing
        // under the hood
        huntingGroups
            .mapTo(mutableSetOf()) { it.huntingYear }
            .toList()
            .sorted()
    }
    private val _selectedHuntingYear = AtomicReference<HuntingYear?>(null)
    var selectedHuntingYear: HuntingYear?
        get() = _selectedHuntingYear.value
        set(value) {
            val huntingYear = _selectedHuntingYear.value
            _selectedHuntingYear.value = value
            if (huntingYear != value) {
                updateSelectableSpecies()
            }
        }

    private val _selectableSpecies = AtomicReference<List<SpeciesCode>>(emptyList())
    var selectableSpecies: List<SpeciesCode>
        get() = _selectableSpecies.value
        private set(value) {
            _selectableSpecies.value = value
        }

    private val _selectedSpecies = AtomicReference<SpeciesCode?>(null)
    var selectedSpecies: SpeciesCode?
        get() = _selectedSpecies.value
        set(value) {
            _selectedSpecies.value = value

            // always update selectable hunting groups. It is possible that season
            // was changed which caused automatic selection of _same_ species. The
            // groups would be different though, so they should be updated always.
            updateSelectableHuntingGroups()
        }

    private val _selectableHuntingGroups = AtomicReference<List<HuntingGroup>>(emptyList())
    var selectableHuntingGroups: List<HuntingGroup>
        get() = _selectableHuntingGroups.value
        private set(value) {
            _selectableHuntingGroups.value = value
        }

    private val _selectedHuntingGroup = AtomicReference<HuntingGroup?>(null)
    var selectedHuntingGroup: HuntingGroup?
        get() = _selectedHuntingGroup.value
        set(value) {
            _selectedHuntingGroup.value = value
        }

    init {
        trySelectHuntingYear()
    }

    private fun trySelectHuntingYear() {
        selectedHuntingYear = selectableHuntingYears.firstAndOnly()
    }

    private fun updateSelectableSpecies() {
        selectableSpecies = huntingGroups
            .filter {
                // it's ok that selectedHuntingYear can be null
                it.huntingYear == selectedHuntingYear
            }
            .mapTo(mutableSetOf()) { it.speciesCode }
            .toList()
            .sorted()

        trySelectSpecies()
    }

    private fun trySelectSpecies() {
        selectedSpecies = selectableSpecies.firstAndOnly()
    }

    private fun updateSelectableHuntingGroups() {
        selectableHuntingGroups = huntingGroups
            .filter { group ->
                // it's ok that selectedHuntingYear and selectedSpecies can be null
                group.huntingYear == selectedHuntingYear &&
                group.speciesCode == selectedSpecies
            }
            .toList()
            .sortedBy { it.id }

        trySelectGroup()
    }

    private fun trySelectGroup() {
        selectedHuntingGroup = selectableHuntingGroups.firstAndOnly()
    }
}