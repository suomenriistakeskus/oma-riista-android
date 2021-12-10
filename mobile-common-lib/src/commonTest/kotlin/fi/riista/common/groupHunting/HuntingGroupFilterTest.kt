package fi.riista.common.groupHunting

import fi.riista.common.groupHunting.model.HuntingGroup
import fi.riista.common.groupHunting.model.HuntingGroupPermit
import fi.riista.common.model.HuntingYear
import fi.riista.common.model.LocalizedString
import fi.riista.common.model.Organization
import fi.riista.common.model.SpeciesCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val club = Organization(
        id = 1,
        name = LocalizedString("HuntingGroup", null, null),
        officialCode = "1"
)

class HuntingGroupFilterTest {

    // hunting years

    @Test
    fun testNoSelectableYears() {
        val filter = HuntingGroupFilter(listOf())
        assertEquals(0, filter.selectableHuntingYears.size)
    }

    @Test
    fun testOneSelectableYear() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
                createHuntingGroup(id = 3, speciesCode = 3, huntingYear = 2019),
        ))
        assertEquals(1, filter.selectableHuntingYears.size)
        assertEquals(2019, filter.selectableHuntingYears[0])
    }

    @Test
    fun testTwoSelectableYears() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
                createHuntingGroup(id = 3, speciesCode = 1, huntingYear = 2020),
                createHuntingGroup(id = 4, speciesCode = 1, huntingYear = 2020),
        ))
        assertEquals(2, filter.selectableHuntingYears.size)
        assertEquals(2019, filter.selectableHuntingYears[0])
        assertEquals(2020, filter.selectableHuntingYears[1])
    }

    @Test
    fun testHuntingYearIsAutoSelected() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
        ))
        assertEquals(2019, filter.selectedHuntingYear)
    }

    @Test
    fun testHuntingYearIsNotAutoSelected() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
                createHuntingGroup(id = 3, speciesCode = 3, huntingYear = 2020),
        ))
        assertNull(filter.selectedHuntingYear)
    }


    // species

    @Test
    fun testSelectableSpeciesCount() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
        ))
        assertEquals(2, filter.selectableSpecies.size)
        assertEquals(1, filter.selectableSpecies[0])
        assertEquals(2, filter.selectableSpecies[1])
    }

    @Test
    fun testSelectableSpeciesCountAfterYearSelection() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
                createHuntingGroup(id = 3, speciesCode = 3, huntingYear = 2020),
        ))
        assertEquals(0, filter.selectableSpecies.size)
        filter.selectedHuntingYear = 2019
        assertEquals(2, filter.selectableSpecies.size)
        assertEquals(1, filter.selectableSpecies[0])
        assertEquals(2, filter.selectableSpecies[1])
        filter.selectedHuntingYear = 2020
        assertEquals(1, filter.selectableSpecies.size)
        assertEquals(3, filter.selectableSpecies[0])

        // unselect
        filter.selectedHuntingYear = null
        assertEquals(0, filter.selectableSpecies.size)
    }

    @Test
    fun testSpeciesAutoSelectOneYearOneSpecies() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 1, huntingYear = 2019),
        ))
        assertEquals(1, filter.selectableSpecies.size)
        assertEquals(1, filter.selectableSpecies[0])
        assertEquals(1, filter.selectedSpecies)
    }

    @Test
    fun testSpeciesAutoSelectAfterYearSelection() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 3, speciesCode = 2, huntingYear = 2020),
        ))
        assertNull(filter.selectedSpecies)
        filter.selectedHuntingYear = 2019
        assertEquals(1, filter.selectableSpecies.size)
        assertEquals(1, filter.selectableSpecies[0])
        assertEquals(1, filter.selectedSpecies)
    }

    @Test
    fun testSpeciesIsNotAutoSelectedAfterYearSelection() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2019),
                createHuntingGroup(id = 3, speciesCode = 2, huntingYear = 2020),
        ))
        assertNull(filter.selectedSpecies)
        assertEquals(0, filter.selectableSpecies.size)
        filter.selectedHuntingYear = 2019
        assertNull(filter.selectedSpecies)
        assertEquals(2, filter.selectableSpecies.size)
        assertEquals(1, filter.selectableSpecies[0])
        assertEquals(2, filter.selectableSpecies[1])
    }


    // hunting groups

    @Test
    fun testHuntingGroupAutoSelect() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
        ))
        assertEquals(1, filter.selectableHuntingGroups.size)
        assertEquals(1, filter.selectableHuntingGroups[0].id)
        assertEquals(1, filter.selectedHuntingGroup?.id)
    }

    @Test
    fun testHuntingGroupNotAutoSelected() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 1, huntingYear = 2019),
        ))
        assertEquals(2, filter.selectableHuntingGroups.size)
        assertEquals(1, filter.selectableHuntingGroups[0].id)
        assertEquals(2, filter.selectableHuntingGroups[1].id)
        assertNull(filter.selectedHuntingGroup)
    }

    @Test
    fun testHuntingGroupAutoSelectAfterYearSelection() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2019),
                createHuntingGroup(id = 2, speciesCode = 1, huntingYear = 2020),
        ))
        assertEquals(filter.selectableHuntingGroups.size, 0)
        assertNull(filter.selectedHuntingGroup)

        filter.selectedHuntingYear = 2020

        assertEquals(1, filter.selectableHuntingGroups.size)
        assertEquals(2, filter.selectableHuntingGroups[0].id)
        assertEquals(2, filter.selectedHuntingGroup?.id)
    }

    @Test
    fun testHuntingGroupAutoSelectAfterSpeciesSelection() {
        val filter = HuntingGroupFilter(listOf(
                createHuntingGroup(id = 1, speciesCode = 1, huntingYear = 2020),
                createHuntingGroup(id = 2, speciesCode = 2, huntingYear = 2020),
        ))
        assertEquals(filter.selectableHuntingGroups.size, 0)
        assertNull(filter.selectedHuntingGroup)

        filter.selectedSpecies = 2

        assertEquals(1, filter.selectableHuntingGroups.size)
        assertEquals(2, filter.selectableHuntingGroups[0].id)
        assertEquals(2, filter.selectedHuntingGroup?.id)
    }


    private fun createHuntingGroup(id: Long, speciesCode: SpeciesCode,
                                   huntingYear: HuntingYear): HuntingGroup {
        return HuntingGroup(
                id = id,
                club = club,
                speciesCode = speciesCode,
                huntingYear = huntingYear,
                permit = HuntingGroupPermit("permit-$speciesCode-$id", listOf()),
                name = LocalizedString(id.toString(), null, null)
        )
    }
}
