package fi.riista.common.domain.harvest.ui.view

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.ui.HarvestTestData
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields
import fi.riista.common.domain.season.TestHarvestSeasons
import fi.riista.common.helpers.TestHarvestPermitProvider
import fi.riista.common.helpers.TestSpeciesResolver
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.resources.MockLanguageProvider
import fi.riista.common.resources.StringProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewHarvestControllerTest {

    @Test
    fun testDeletingHarvestMarksItDeleted() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val harvestContext = HarvestTestData.getHarvestContext(backendAPIMock)
        val (controller, harvest) = getControllerAndHarvest(backendAPIMock, harvestContext)

        // not loaded yet
        assertNull(harvestContext.harvestProvider.harvests)

        controller.loadViewModel(refresh = false)

        assertEquals(1, harvestContext.harvestProvider.harvests?.size)

        assertTrue(controller.deleteHarvest(updateToBackend = false), "deletion")

        harvestContext.harvestProvider.fetch(refresh = true)

        assertEquals(0, harvestContext.harvestProvider.harvests?.size)

        assertNotNull(harvest.localId, "missing harvest local id")
        val deletedObservation = harvestContext.repository.getByLocalId(harvest.localId!!)
        assertTrue(deletedObservation.deleted, "harvest deleted")
    }

    @Test
    fun testDeletingObservationCallsBackend() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val harvestContext = HarvestTestData.getHarvestContext(backendAPIMock)
        val (controller, harvest) = getControllerAndHarvest(backendAPIMock, harvestContext)

        assertEquals(0, backendAPIMock.callCount(BackendAPI::deleteHarvest))

        controller.loadViewModel(refresh = false)
        assertTrue(controller.deleteHarvest(updateToBackend = true), "deletion")

        assertEquals(
            expected = 1,
            actual = backendAPIMock.callCount(BackendAPI::deleteHarvest),
            message = "delete count"
        )
        assertEquals(
            expected = harvest.id,
            actual = backendAPIMock.callParameter(BackendAPI::deleteHarvest),
            message = "deleted harvest"
        )
    }

    private suspend fun getControllerAndHarvest(
        backendAPIMock: BackendAPIMock,
        harvestContext: HarvestContext = HarvestTestData.getHarvestContext(backendAPIMock),
        speciesCode: SpeciesCode = HarvestTestData.HARVEST_SPECIES_CODE,
        harvest: CommonHarvest = HarvestTestData.createHarvest(speciesCode = speciesCode)
    ): Pair<ViewHarvestController, CommonHarvest> {
        val response = harvestContext.saveHarvest(harvest)
        val resultingHarvest = (response as? HarvestOperationResponse.Success)?.harvest

        assertNotNull(resultingHarvest, "Failed to save harvest")
        assertNotNull(resultingHarvest.localId, "Missing harvest id after save!")

        val harvestFields = CommonHarvestFields(
            harvestSeasons = TestHarvestSeasons.createMockHarvestSeasons(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            preferences = MockPreferences(),
        )

        val controller = ViewHarvestController(
            harvestId = resultingHarvest.localId!!,
            harvestContext = harvestContext,
            commonHarvestFields = harvestFields,
            harvestPermitProvider = TestHarvestPermitProvider.INSTANCE,
            stringProvider = stringProvider,
            languageProvider = MockLanguageProvider(),
        )

        return controller to resultingHarvest
    }

    private val stringProvider: StringProvider = TestStringProvider()
}
