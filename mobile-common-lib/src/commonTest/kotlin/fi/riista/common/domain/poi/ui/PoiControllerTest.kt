package fi.riista.common.domain.poi.ui

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.helpers.MockMainScopeProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.domain.poi.PoiContext
import fi.riista.common.domain.poi.model.PointOfInterestType
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

class PoiControllerTest {

    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = PoiController(
            poiContext = getPoiContext(),
            externalId = "DZFM5KSKAY",
        )
        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = PoiController(
            poiContext = getPoiContext(),
            externalId = "DZFM5KSKAY",
        )
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
    }

    @Test
    fun testRefreshLoadsDataFromNetwork() = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = PoiController(
            poiContext = getPoiContext(backendAPI = backendAPI),
            externalId = "DZFM5KSKAY",
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
    fun testListPoisViewModelHasCorrectData() = runBlockingTest {
        val controller = PoiController(
            poiContext = getPoiContext(),
            externalId = "DZFM5KSKAY",
        )
        controller.loadViewModel()

        val viewModel = controller.viewModelLoadStatus.value.loadedViewModel
        assertEquals(3, viewModel!!.pois!!.allPois.size)
        assertEquals(3, viewModel.pois!!.filteredPois.size)
        assertEquals(PoiFilter.PoiFilterType.ALL, viewModel.pois!!.filter.poiFilterType)

        val pois = viewModel.pois!!.allPois
        val poi1 = pois[0]
        assertEquals(1, poi1.id)
        assertEquals(4, poi1.rev)
        assertEquals(1, poi1.visibleId)
        assertNull(poi1.clubId)
        assertEquals("Passi1", poi1.description)
        assertEquals(PointOfInterestType.SIGHTING_PLACE.toBackendEnum(), poi1.type)
        assertNull(poi1.lastModifiedDate)
        assertNull(poi1.lastModifierName)
        assertFalse(poi1.lastModifierRiistakeskus)
        assertEquals(2, poi1.locations.size)

        val poi1loc1 = poi1.locations[0]
        assertEquals(1, poi1loc1.id)
        assertEquals(1, poi1loc1.poiId)
        assertEquals("Eka passipaikka", poi1loc1.description)
        assertEquals(1, poi1loc1.visibleId)
        assertEquals(6826107, poi1loc1.geoLocation.latitude)
        assertEquals(312227, poi1loc1.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), poi1loc1.geoLocation.source)

        val poi1loc2 = poi1.locations[1]
        assertEquals(2, poi1loc2.id)
        assertEquals(1, poi1loc2.poiId)
        assertEquals("Toka passipaikka", poi1loc2.description)
        assertEquals(2, poi1loc2.visibleId)
        assertEquals(6825040, poi1loc2.geoLocation.latitude)
        assertEquals(313212, poi1loc2.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), poi1loc2.geoLocation.source)

        val poi2 = pois[1]
        assertEquals(2, poi2.id)
        assertEquals(1, poi2.rev)
        assertEquals(2, poi2.visibleId)
        assertNull(poi2.clubId)
        assertEquals("Ruokintapaikat", poi2.description)
        assertEquals(PointOfInterestType.FEEDING_PLACE.toBackendEnum(), poi2.type)
        assertNull(poi2.lastModifiedDate)
        assertNull(poi2.lastModifierName)
        assertFalse(poi2.lastModifierRiistakeskus)
        assertEquals(1, poi2.locations.size)

        val poi2loc1 = poi2.locations[0]
        assertEquals(3, poi2loc1.id)
        assertEquals(2, poi2loc1.poiId)
        assertEquals("Eka ruokintapaikka", poi2loc1.description)
        assertEquals(1, poi2loc1.visibleId)
        assertEquals(6826789, poi2loc1.geoLocation.latitude)
        assertEquals(313128, poi2loc1.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), poi2loc1.geoLocation.source)

        val poi3 = pois[2]
        assertEquals(4, poi3.id)
        assertEquals(1, poi3.rev)
        assertEquals(3, poi3.visibleId)
        assertNull(poi3.clubId)
        assertEquals("Toinen passiketju", poi3.description)
        assertEquals(PointOfInterestType.SIGHTING_PLACE.toBackendEnum(), poi3.type)
        assertNull(poi3.lastModifiedDate)
        assertNull(poi3.lastModifierName)
        assertFalse(poi3.lastModifierRiistakeskus)
        assertEquals(3, poi3.locations.size)

        val poi3loc1 = poi3.locations[0]
        assertEquals(8, poi3loc1.id)
        assertEquals(4, poi3loc1.poiId)
        assertEquals("Ensimmäinen passipaikka", poi3loc1.description)
        assertEquals(1, poi3loc1.visibleId)
        assertEquals(6826750, poi3loc1.geoLocation.latitude)
        assertEquals(311909, poi3loc1.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), poi3loc1.geoLocation.source)

        val poi3loc2 = poi3.locations[1]
        assertEquals(9, poi3loc2.id)
        assertEquals(4, poi3loc2.poiId)
        assertEquals("Toinen passipaikka", poi3loc2.description)
        assertEquals(2, poi3loc2.visibleId)
        assertEquals(6826980, poi3loc2.geoLocation.latitude)
        assertEquals(312645, poi3loc2.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), poi3loc2.geoLocation.source)

        val poi3loc3 = poi3.locations[2]
        assertEquals(10, poi3loc3.id)
        assertEquals(4, poi3loc3.poiId)
        assertEquals("Kolmas passipaikka", poi3loc3.description)
        assertEquals(3, poi3loc3.visibleId)
        assertEquals(6826868, poi3loc3.geoLocation.latitude)
        assertEquals(313370, poi3loc3.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), poi3loc3.geoLocation.source)
    }

    @Test
    fun tesPoisCanBeFiltered() = runBlockingTest {
        val controller = PoiController(
            poiContext = getPoiContext(),
            externalId = "DZFM5KSKAY",
        )
        controller.loadViewModel()

        var viewModel = controller.viewModelLoadStatus.value.loadedViewModel
        assertEquals(3, viewModel!!.pois!!.allPois.size)
        assertEquals(3, viewModel.pois!!.filteredPois.size)
        assertEquals(PoiFilter.PoiFilterType.ALL, viewModel.pois!!.filter.poiFilterType)

        controller.eventDispatcher.dispatchPoiFilterChanged(PoiFilter(PoiFilter.PoiFilterType.SIGHTING_PLACE))
        viewModel = controller.viewModelLoadStatus.value.loadedViewModel
        assertEquals(3, viewModel!!.pois!!.allPois.size)
        assertEquals(2, viewModel.pois!!.filteredPois.size)
        assertEquals(PoiFilter.PoiFilterType.SIGHTING_PLACE, viewModel.pois!!.filter.poiFilterType)
        assertEquals(PointOfInterestType.SIGHTING_PLACE.toBackendEnum(), viewModel.pois!!.filteredPois[0].type)
        assertEquals(PointOfInterestType.SIGHTING_PLACE.toBackendEnum(), viewModel.pois!!.filteredPois[1].type)
    }

    private fun getPoiContext(backendAPI: BackendAPI = BackendAPIMock()): PoiContext {
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = createDatabaseDriverFactory(),
            mockBackendAPI = backendAPI,
            mockCurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(),
            mockLocalDateTimeProvider = MockDateTimeProvider(),
            mockMainScopeProvider = MockMainScopeProvider(),
            mockFileProvider = CommonFileProviderMock(),
        )
        return RiistaSDK.poiContext
    }
}
