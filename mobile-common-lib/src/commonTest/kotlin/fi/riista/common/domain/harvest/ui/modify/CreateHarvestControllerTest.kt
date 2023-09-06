package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.HarvestTestData
import fi.riista.common.domain.huntingclub.selectableForEntries.MockHuntingClubsSelectableForEntries
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.season.TestHarvestSeasons
import fi.riista.common.domain.userInfo.MockUsernameProvider
import fi.riista.common.helpers.TestHarvestPermitProvider
import fi.riista.common.helpers.TestSpeciesResolver
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.resources.MockLanguageProvider
import fi.riista.common.resources.StringProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateHarvestControllerTest {
    @Test
    fun testSavingHarvestCallsBackendWhenRequested() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val harvestContext = HarvestTestData.getHarvestContext(backendAPIMock)
        val controller = getController(backendAPIMock = backendAPIMock, harvestContext = harvestContext)

        controller.loadViewModel(true)

        var saveResponse = controller.saveHarvest(updateToBackend = true)
        // Saving invalid harvest should fail
        assertTrue(saveResponse.databaseSaveResponse is HarvestOperationResponse.Error)
        assertEquals(0, backendAPIMock.totalCallCount())

        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            CommonHarvestField.SPECIES_CODE_AND_IMAGE, Species.Known(SpeciesCodes.BEAR_ID)
        )
        controller.eventDispatchers.locationEventDispatcher.dispatchLocationChanged(
            CommonHarvestField.LOCATION, HarvestTestData.HARVEST_LOCATION
        )
        controller.eventDispatchers.permitEventDispatcher.selectPermit(HarvestTestData.PERMIT, SpeciesCodes.BEAR_ID)
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
            CommonHarvestField.AGE, GameAge.ADULT
        )
        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
            CommonHarvestField.GENDER, Gender.FEMALE
        )
        controller.eventDispatchers.doubleEventDispatcher.dispatchDoubleChanged(
            CommonHarvestField.WEIGHT, 123.3
        )

        saveResponse = controller.saveHarvest(updateToBackend = false)
        assertTrue(saveResponse.databaseSaveResponse is HarvestOperationResponse.Success)
        assertEquals(0, backendAPIMock.totalCallCount())

        saveResponse = controller.saveHarvest(updateToBackend = true)
        assertTrue(saveResponse.databaseSaveResponse is HarvestOperationResponse.Success)
        assertEquals(1, backendAPIMock.totalCallCount())
        assertEquals(1, backendAPIMock.callCount(BackendAPIMock::createHarvest))
        assertTrue(saveResponse.networkSaveResponse is HarvestOperationResponse.Success)
    }

    private fun getController(
        backendAPIMock: BackendAPIMock,
        harvestContext: HarvestContext = HarvestTestData.getHarvestContext(backendAPIMock),
    ) = CreateHarvestController(
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
