package fi.riista.common.domain.groupHunting.groupSelection

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.ui.groupSelection.SelectHuntingGroupController
import fi.riista.common.domain.groupHunting.ui.groupSelection.SelectHuntingGroupField
import fi.riista.common.helpers.*
import fi.riista.common.model.Language
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.model.StringWithId
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class SelectHuntingGroupControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = createController()

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = controller.getLoadedViewModel()
        assertFalse(viewModel.huntingGroupSelected)
    }

    @Test
    fun testProducedDataFieldsMatchLoadedData() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(4, fields.size)
        var expectedIndex = 0
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.HUNTING_CLUB).let {
            assertEquals(listOf(329L), it.selected)
            assertEquals("club", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(1, it.values.size)
            assertEquals("Nokian metsästysseura", it.values[0].string)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SEASON).let {
            assertTrue(it.selected.isNullOrEmpty())
            assertEquals("season", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(2, it.values.size)
            assertEquals("2019 - 20", it.values[0].string)
            assertEquals(2019, it.values[0].id)
            assertEquals("2021 - 22", it.values[1].string)
            assertEquals(2021, it.values[1].id)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SPECIES).let {
            assertEquals(listOf(), it.selected)
            assertEquals("species", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(0, it.values.size)
        }
        fields.getStringListField(expectedIndex, SelectHuntingGroupField.HUNTING_GROUP).let {
            assertEquals(listOf(), it.selected)
            assertEquals("hunting_group", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(0, it.values.size)
        }
    }

    @Test
    fun testSelectingSeasonEnablesSpeciesSelection() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatcher.dispatchStringWithIdChanged(
            fieldId = SelectHuntingGroupField.SEASON,
            value = listOf(StringWithId("2019 - 20", 2019))
        )

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(4, fields.size)
        var expectedIndex = 1 // skip club
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SEASON).let {
            assertEquals(listOf(2019L), it.selected)
            assertEquals(2, it.values.size)
            assertEquals("2019 - 20", it.values[0].string)
            assertEquals(2019, it.values[0].id)
            assertEquals("2021 - 22", it.values[1].string)
            assertEquals(2021, it.values[1].id)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SPECIES).let {
            assertTrue(it.selected.isNullOrEmpty())
            assertEquals(2, it.values.size)
            assertEquals(SpeciesCodes.MOOSE_ID, it.values[0].id.toInt())
            assertEquals("moose", it.values[0].string)
            assertEquals(SpeciesCodes.WHITE_TAILED_DEER_ID, it.values[1].id.toInt())
            assertEquals("white_tailed_deer", it.values[1].string)
        }
        fields.getStringListField(expectedIndex, SelectHuntingGroupField.HUNTING_GROUP).let {
            assertEquals(listOf(), it.selected)
            assertEquals(0, it.values.size)
        }
    }

    @Test
    fun testSelectingSpeciesAlsoSelectsHuntingGroup() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatcher.dispatchStringWithIdChanged(
            fieldId = SelectHuntingGroupField.SEASON,
            value = listOf(StringWithId("2019 - 20", 2019))
        )

        controller.eventDispatcher.dispatchStringWithIdChanged(
            fieldId = SelectHuntingGroupField.SPECIES,
            value = listOf(StringWithId("moose", 47503))
        )

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(5, fields.size)
        var expectedIndex = 2 // skip club + season
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SPECIES).let {
            assertEquals(SpeciesCodes.MOOSE_ID, it.selected?.get(0)?.toInt())
            assertEquals(2, it.values.size)
            assertEquals(SpeciesCodes.MOOSE_ID, it.values[0].id.toInt())
            assertEquals("moose", it.values[0].string)
            assertEquals(SpeciesCodes.WHITE_TAILED_DEER_ID, it.values[1].id.toInt())
            assertEquals("white_tailed_deer", it.values[1].string)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.HUNTING_GROUP).let {
            assertEquals(listOf(344L), it.selected)
            assertEquals(1, it.values.size)
            assertEquals(344, it.values[0].id)
            assertEquals("Hirvi 2019 fi", it.values[0].string)
        }

        // we should also get the permit as label
        fields.getLabelField(expectedIndex, SelectHuntingGroupField.PERMIT_INFORMATION).let {
            assertEquals("permit: 2019-1-000-10000-6", it.text)
        }
    }

    @Test
    fun testHuntingFinishedIsDisplayed() = runBlockingTest {
        val controller = createController(BackendAPIMock(
            groupHuntingGroupStatusResponse = MockResponse.success(MockGroupHuntingData.GroupStatusHuntingEnded)
        ))
        controller.loadViewModel()

        controller.eventDispatcher.dispatchStringWithIdChanged(
            fieldId = SelectHuntingGroupField.SEASON,
            value = listOf(StringWithId("2019 - 20", 2019))
        )

        controller.eventDispatcher.dispatchStringWithIdChanged(
            fieldId = SelectHuntingGroupField.SPECIES,
            value = listOf(StringWithId("moose", 47503))
        )

        // hunting finished status should not exist yet
        with (controller.getLoadedViewModel()) {
            assertEquals(5, fields.size)
            assertNull(fields.find { it.id == SelectHuntingGroupField.HUNTING_HAS_ENDED})
        }

        // fetch necessary information required for determining whether hunting has finished or not
        controller.fetchHuntingGroupDataIfNeeded()

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(6, fields.size)
        var expectedIndex = 2 // skip club + season
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SPECIES).let {
            assertEquals(SpeciesCodes.MOOSE_ID, it.selected?.get(0)?.toInt())
            assertEquals(2, it.values.size)
            assertEquals(SpeciesCodes.MOOSE_ID, it.values[0].id.toInt())
            assertEquals("moose", it.values[0].string)
            assertEquals(SpeciesCodes.WHITE_TAILED_DEER_ID, it.values[1].id.toInt())
            assertEquals("white_tailed_deer", it.values[1].string)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.HUNTING_GROUP).let {
            assertEquals(listOf(344L), it.selected)
            assertEquals(1, it.values.size)
            assertEquals(344, it.values[0].id)
            assertEquals("Hirvi 2019 fi", it.values[0].string)
        }
        fields.getLabelField(expectedIndex++, SelectHuntingGroupField.PERMIT_INFORMATION).let {
            assertEquals("permit: 2019-1-000-10000-6", it.text)
        }

        // there should be a hunting ended label
        fields.getLabelField(expectedIndex, SelectHuntingGroupField.HUNTING_HAS_ENDED).let {
            assertEquals(LabelField.Type.ERROR, it.type)
            assertEquals("hunting_has_finished", it.text)
        }
    }

    @Test
    fun testTwoClubsProducedNoInitialClubSelection() = runBlockingTest {
        val controller = createController(BackendAPIMock(
                groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.TwoClubs)
        ))
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(4, fields.size)
        var expectedIndex = 0
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.HUNTING_CLUB).let {
            assertTrue(it.selected.isNullOrEmpty())
            assertEquals("club", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(2, it.values.size)
            assertEquals(MockGroupHuntingData.FirstClubId, it.values[0].id)
            assertEquals("Nokian metsästysseura", it.values[0].string)
            assertEquals(MockGroupHuntingData.SecondClubId, it.values[1].id)
            assertEquals("Pirkkalan porukka", it.values[1].string)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SEASON).let {
            assertEquals(listOf(), it.selected)
            assertEquals("season", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(0, it.values.size)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SPECIES).let {
            assertEquals(listOf(), it.selected)
            assertEquals("species", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(0, it.values.size)
        }
        fields.getStringListField(expectedIndex, SelectHuntingGroupField.HUNTING_GROUP).let {
            assertEquals(listOf(), it.selected)
            assertEquals("hunting_group", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(0, it.values.size)
        }
    }

    @Test
    fun testSelectingClubSelectsSeasonAndEnablesSpeciesSelection() = runBlockingTest {
        val controller = createController(BackendAPIMock(
                groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.TwoClubs)
        ))
        controller.loadViewModel()

        controller.eventDispatcher.dispatchStringWithIdChanged(
            fieldId = SelectHuntingGroupField.HUNTING_CLUB,
            value = listOf(StringWithId("Nokian metsästysseura", MockGroupHuntingData.FirstClubId))
        )

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(4, fields.size)
        var expectedIndex = 0
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.HUNTING_CLUB).let {
            assertEquals(MockGroupHuntingData.FirstClubId, it.selected?.get(0))
            assertEquals("club", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(2, it.values.size)
            assertEquals(MockGroupHuntingData.FirstClubId, it.values[0].id)
            assertEquals("Nokian metsästysseura", it.values[0].string)
            assertEquals(MockGroupHuntingData.SecondClubId, it.values[1].id)
            assertEquals("Pirkkalan porukka", it.values[1].string)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SEASON).let {
            assertEquals(listOf(2019L), it.selected) // only 1 season in mock data --> gets preselected
            assertEquals("season", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(1, it.values.size)
            assertEquals(2019, it.values[0].id)
        }
        fields.getStringListField(expectedIndex++, SelectHuntingGroupField.SPECIES).let {
            assertTrue(it.selected.isNullOrEmpty())
            assertEquals("species", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(2, it.values.size)
            assertEquals(SpeciesCodes.MOOSE_ID, it.values[0].id.toInt())
            assertEquals("moose", it.values[0].string)
            assertEquals(SpeciesCodes.WHITE_TAILED_DEER_ID, it.values[1].id.toInt())
            assertEquals("white_tailed_deer", it.values[1].string)
        }
        fields.getStringListField(expectedIndex, SelectHuntingGroupField.HUNTING_GROUP).let {
            assertEquals(listOf(), it.selected)
            assertEquals("hunting_group", it.settings.label)
            assertFalse(it.settings.readOnly)

            assertEquals(0, it.values.size)
        }
    }

    private fun createController(backendAPI: BackendAPIMock = BackendAPIMock()) =
        createController(getGroupHuntingContext(backendAPI))

    private fun createController(groupHuntingContext: GroupHuntingContext): SelectHuntingGroupController {
        runBlocking {
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }

        return SelectHuntingGroupController(
                groupHuntingContext = groupHuntingContext,
                stringProvider = getStringProvider(),
                languageProvider = object : LanguageProvider {
                    override fun getCurrentLanguage() = Language.FI
                },
                speciesResolver = object : SpeciesResolver {
                    override fun getSpeciesName(speciesCode: SpeciesCode): String {
                        return when (speciesCode) {
                            SpeciesCodes.WHITE_TAILED_DEER_ID -> "white_tailed_deer"
                            SpeciesCodes.MOOSE_ID -> "moose"
                            else -> throw RuntimeException("Unexpected speciesCode $speciesCode")
                        }
                    }
                }
        )
    }

    private fun getGroupHuntingContext(backendAPI: BackendAPIMock): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                groupHuntingEnabledForAll = true,
                backendAPI = backendAPI
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
}
