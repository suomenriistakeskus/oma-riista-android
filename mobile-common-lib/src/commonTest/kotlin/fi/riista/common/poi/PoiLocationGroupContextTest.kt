package fi.riista.common.poi

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class PoiLocationGroupContextTest {

    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testPoiLocationGroupsAreLoaded() {
        val poiContext = getPoiContext()
        val locationGroupContext = poiContext.getPoiLocationGroupContext("DZFM5KSKAY")

        runBlocking {
            locationGroupContext.fetch()
        }

        assertEquals(3, locationGroupContext.poiLocationGroups!!.size)
    }

    @Test
    fun testEmptyPoiLocationGroupsAreLoaded() {
        val poiContext = getPoiContext(
            backendAPI = BackendAPIMock(
                poiLocationGroupsResponse = MockResponse.success(MockPoiData.EmptyPoiLocationGroups),
            ),
        )
        val locationGroupContext = poiContext.getPoiLocationGroupContext("DZFM5KSKAY")

        runBlocking {
            locationGroupContext.fetch()
        }

        assertEquals(0, locationGroupContext.poiLocationGroups!!.size)
    }

    private fun getPoiContext(backendAPI: BackendAPI = BackendAPIMock()): PoiContext {
        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = createDatabaseDriverFactory(),
            mockBackendAPI = backendAPI,
        )
        return RiistaSDK.poiContext
    }
}
