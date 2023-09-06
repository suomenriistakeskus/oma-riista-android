package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.HarvestTestData
import fi.riista.common.domain.huntingclub.selectableForEntries.MockHuntingClubsSelectableForEntries
import fi.riista.common.domain.model.GreySealHuntingMethod
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.season.TestHarvestSeasons
import fi.riista.common.helpers.TestHarvestPermitProvider
import fi.riista.common.helpers.TestSpeciesResolver
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.findField
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.resources.MockLanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.GenderField
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditHarvestControllerTest {

    @Test
    fun testSavingHarvestCallsBackendWhenRequested() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val harvestContext = HarvestTestData.getHarvestContext(backendAPIMock)
        val controller = getController(backendAPIMock = backendAPIMock, harvestContext = harvestContext)

        controller.editableHarvest = EditableHarvest(HarvestTestData.createHarvest())
        controller.loadViewModel()

        var saveResponse = controller.saveHarvest(updateToBackend = false)
        assertTrue(saveResponse.databaseSaveResponse is HarvestOperationResponse.Success)
        assertEquals(0, backendAPIMock.totalCallCount())

        saveResponse = controller.saveHarvest(updateToBackend = true)
        assertTrue(saveResponse.databaseSaveResponse is HarvestOperationResponse.Success)
        assertEquals(1, backendAPIMock.totalCallCount())
        assertEquals(1, backendAPIMock.callCount(BackendAPIMock::updateHarvest))
        assertTrue(saveResponse.networkSaveResponse is HarvestOperationResponse.Success)
    }

    @Test
    fun `unknown gender should be available for grey seal if shot but lost`() {
        val backendAPIMock = BackendAPIMock()
        val harvestContext = HarvestTestData.getHarvestContext(backendAPIMock)
        val controller = getController(backendAPIMock = backendAPIMock, harvestContext = harvestContext)

        controller.editableHarvest = EditableHarvest(
            harvest = HarvestTestData.createHarvest().copy(
                species = Species.Known(speciesCode = SpeciesCodes.GREY_SEAL_ID),
            )
        )

        runBlocking {
            controller.loadViewModel()
        }

        var genderField: GenderField<CommonHarvestField> = controller.getLoadedViewModel().fields.findField(CommonHarvestField.GENDER)
        assertFalse(genderField.settings.showUnknown)

        listOf(
            Pair(GreySealHuntingMethod.SHOT, false),
            Pair(GreySealHuntingMethod.CAPTURED_ALIVE, false),
            Pair(GreySealHuntingMethod.SHOT_BUT_LOST, true)
        ).forEach { (huntingMethod, unknownShown) ->
            controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                fieldId = CommonHarvestField.GREY_SEAL_HUNTING_METHOD,
                value = listOf(
                    huntingMethod.toBackendEnum().toLocalizedStringWithId(TestStringProvider.INSTANCE)
                )
            )

            genderField = controller.getLoadedViewModel().fields.findField(CommonHarvestField.GENDER)
            assertEquals(
                expected = unknownShown,
                actual = genderField.settings.showUnknown,
                message = "hunting method: $huntingMethod"
            )
        }
    }

    @Test
    fun `unknown age should be available for grey seal if shot but lost`() {
        val backendAPIMock = BackendAPIMock()
        val harvestContext = HarvestTestData.getHarvestContext(backendAPIMock)
        val controller = getController(backendAPIMock = backendAPIMock, harvestContext = harvestContext)

        controller.editableHarvest = EditableHarvest(
            harvest = HarvestTestData.createHarvest().copy(
                species = Species.Known(speciesCode = SpeciesCodes.GREY_SEAL_ID),
            )
        )

        runBlocking {
            controller.loadViewModel()
        }

        var ageField: AgeField<CommonHarvestField> = controller.getLoadedViewModel().fields.findField(CommonHarvestField.AGE)
        assertFalse(ageField.settings.showUnknown)

        listOf(
            Pair(GreySealHuntingMethod.SHOT, false),
            Pair(GreySealHuntingMethod.CAPTURED_ALIVE, false),
            Pair(GreySealHuntingMethod.SHOT_BUT_LOST, true)
        ).forEach { (huntingMethod, unknownShown) ->
            controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                fieldId = CommonHarvestField.GREY_SEAL_HUNTING_METHOD,
                value = listOf(
                    huntingMethod.toBackendEnum().toLocalizedStringWithId(TestStringProvider.INSTANCE)
                )
            )

            ageField = controller.getLoadedViewModel().fields.findField(CommonHarvestField.AGE)
            assertEquals(
                expected = unknownShown,
                actual = ageField.settings.showUnknown,
                message = "hunting method: $huntingMethod"
            )
        }
    }

    private fun getController(
        backendAPIMock: BackendAPIMock,
        harvestContext: HarvestContext = HarvestTestData.getHarvestContext(backendAPIMock),
    ) = EditHarvestController(
        harvestSeasons = TestHarvestSeasons.createMockHarvestSeasons(),
        harvestContext = harvestContext,
        harvestPermitProvider = TestHarvestPermitProvider.INSTANCE,
        selectableHuntingClubs = MockHuntingClubsSelectableForEntries(),
        languageProvider = MockLanguageProvider(),
        preferences = MockPreferences(),
        speciesResolver = TestSpeciesResolver.INSTANCE,
        stringProvider = stringProvider
    )

    private val stringProvider: StringProvider = TestStringProvider()
}
