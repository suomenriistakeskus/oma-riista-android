package fi.riista.common.domain.groupHunting

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class HuntingGroupAreaProviderTest {

    @Test
    fun testAreaCanBeFetched() {
        val groupAreaProvider = getHuntingGroupAreaProvider()
        assertTrue(groupAreaProvider.loadStatus.value.notLoaded)

        runBlocking {
            groupAreaProvider.fetch(refresh = false)
        }

        assertTrue(groupAreaProvider.loadStatus.value.loaded)
        assertNotNull(groupAreaProvider.area)
        assertEquals(groupAreaProvider.area!!.areaId, 28)
    }

    @Test
    fun testAreaLoadErrorsCanBeRecovered() {
        val backendAPI = BackendAPIMock(groupHuntingGroupHuntingAreaResponse = MockResponse.error(404))
        val groupAreaProvider = getHuntingGroupAreaProvider(backendAPI)
        runBlocking {
            groupAreaProvider.fetch(refresh = false)
        }
        assertTrue(groupAreaProvider.loadStatus.value.error)
        assertNull(groupAreaProvider.area)

        backendAPI.groupHuntingGroupHuntingAreaResponse = MockResponse.success(MockGroupHuntingData.HuntingArea)
        runBlocking {
            groupAreaProvider.fetch(refresh = false)
        }
        assertTrue(groupAreaProvider.loadStatus.value.loaded)
        assertNotNull(groupAreaProvider.area)
        assertEquals(groupAreaProvider.area!!.areaId, 28)
    }

    @Test
    fun testNetworkErrorDoesNotClearPreviousArea() {
        val backendAPI = BackendAPIMock()
        val groupAreaProvider = getHuntingGroupAreaProvider(backendAPI)
        runBlocking {
            groupAreaProvider.fetch(refresh = false)
        }
        assertTrue(groupAreaProvider.loadStatus.value.loaded)
        assertNotNull(groupAreaProvider.area)

        backendAPI.groupHuntingGroupHuntingAreaResponse = MockResponse.error(null)
        runBlocking {
            groupAreaProvider.fetch(refresh = true)
        }
        assertTrue(groupAreaProvider.loadStatus.value.error)
        assertNotNull(groupAreaProvider.area)
    }

    @Test
    fun test401ErrorClearsPreviousArea() {
        val backendAPI = BackendAPIMock()
        val groupAreaProvider = getHuntingGroupAreaProvider(backendAPI)
        runBlocking {
            groupAreaProvider.fetch(refresh = false)
        }
        assertTrue(groupAreaProvider.loadStatus.value.loaded)
        assertNotNull(groupAreaProvider.area)

        backendAPI.groupHuntingGroupHuntingAreaResponse = MockResponse.error(401)
        runBlocking {
            groupAreaProvider.fetch(refresh = true)
        }
        assertTrue(groupAreaProvider.loadStatus.value.error)
        assertNull(groupAreaProvider.area)
    }

    private fun getHuntingGroupAreaProvider(backendAPI: BackendAPI = BackendAPIMock()): HuntingGroupAreaProvider {
        return HuntingGroupAreaFromNetworkProvider(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI: BackendAPI = backendAPI
                },
                huntingGroupId = 1
        )
    }
}