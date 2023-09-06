package fi.riista.common.domain.poi.ui

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.poi.MockPoiData
import fi.riista.common.domain.poi.PoiLocationGroupContext
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PoiLocationControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = PoiLocationController(
            locationGroupContext = getPoiLocationGroupContext(),
            poiLocationGroupId = 1L,
            initiallySelectedPoiLocationId = 1L,
        )
        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = PoiLocationController(
            locationGroupContext = getPoiLocationGroupContext(),
            poiLocationGroupId = MockPoiData.Passi1GroupId.toLong(),
            initiallySelectedPoiLocationId = MockPoiData.Passi1Location1Id.toLong(),
        )
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
    }

    @Test
    fun testRefreshLoadsDataFromNetwork() = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = PoiLocationController(
            locationGroupContext = getPoiLocationGroupContext(backendAPI = backendAPI),
            poiLocationGroupId = MockPoiData.Passi1GroupId.toLong(),
            initiallySelectedPoiLocationId = MockPoiData.Passi1Location1Id.toLong(),
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        assertEquals(1, backendAPI.callCount(BackendAPI::fetchPoiLocationGroups.name))
        controller.loadViewModel(refresh = true)
        assertEquals(2, backendAPI.callCount(BackendAPI::fetchPoiLocationGroups.name))
        controller.loadViewModel(refresh = false)
        assertEquals(2, backendAPI.callCount(BackendAPI::fetchPoiLocationGroups.name))
    }

    @Test
    fun testStateCanBeRestored() = runBlockingTest {
        var controller = PoiLocationController(
            locationGroupContext = getPoiLocationGroupContext(),
            poiLocationGroupId = MockPoiData.Passi1GroupId.toLong(),
            initiallySelectedPoiLocationId = MockPoiData.Passi1Location1Id.toLong(),
        )
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        assertEquals(0, controller.getLoadedViewModelOrNull()?.selectedIndex)
        controller.eventDispatcher.dispatchPoiLocationSelected(1)
        assertEquals(1, controller.getLoadedViewModelOrNull()?.selectedIndex)

        val state = controller.getUnreproducibleState()
        controller = PoiLocationController(
            locationGroupContext = getPoiLocationGroupContext(),
            poiLocationGroupId = MockPoiData.Passi1GroupId.toLong(),
            initiallySelectedPoiLocationId = MockPoiData.Passi1Location1Id.toLong(),
        )
        controller.restoreUnreproducibleState(state!!)
        controller.loadViewModel()
        assertEquals(1, controller.getLoadedViewModelOrNull()?.selectedIndex)
    }

    private fun getPoiLocationGroupContext(backendAPI: BackendAPI = BackendAPIMock()): PoiLocationGroupContext {
        RiistaSDK.initializeMocked(
            mockBackendAPI = backendAPI,
        )
        return RiistaSDK.poiContext.getPoiLocationGroupContext("DZFM5KSKAY")
    }
}
