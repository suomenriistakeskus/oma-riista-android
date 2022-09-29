package fi.riista.common.domain.groupHunting

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class HuntingGroupStatusProviderTest {

    @Test
    fun testStatusCanBeFetched() {
        val groupStatusProvider = getStatusProvider()
        assertTrue(groupStatusProvider.loadStatus.value.notLoaded)

        runBlocking {
            groupStatusProvider.fetch(refresh = false)
        }

        assertTrue(groupStatusProvider.loadStatus.value.loaded)
        groupStatusProvider.status.let {
            assertNotNull(it)
            assertTrue(it.canCreateHuntingDay)
            assertTrue(it.canCreateHarvest)
            assertTrue(it.canCreateObservation)
            assertTrue(it.canEditDiaryEntry)
            assertTrue(it.canEditHuntingDay)
        }
    }

    @Test
    fun testStatusLoadErrorsCanBeRecovered() {
        val backendAPI = BackendAPIMock(groupHuntingGroupStatusResponse = MockResponse.error(404))
        val groupStatusProvider = getStatusProvider(backendAPI)
        runBlocking {
            groupStatusProvider.fetch(refresh = false)
        }
        assertTrue(groupStatusProvider.loadStatus.value.error)
        assertNull(groupStatusProvider.status)

        backendAPI.groupHuntingGroupStatusResponse = MockResponse.success(MockGroupHuntingData.GroupStatus)
        runBlocking {
            groupStatusProvider.fetch(refresh = false)
        }
        assertTrue(groupStatusProvider.loadStatus.value.loaded)
        assertNotNull(groupStatusProvider.status)
    }

    @Test
    fun testNetworkErrorDoesNotClearPreviousStatus() {
        val backendAPI = BackendAPIMock()
        val groupStatusProvider = getStatusProvider(backendAPI)
        runBlocking {
            groupStatusProvider.fetch(refresh = false)
        }
        assertTrue(groupStatusProvider.loadStatus.value.loaded)
        assertNotNull(groupStatusProvider.status)

        backendAPI.groupHuntingGroupStatusResponse = MockResponse.error(null)
        runBlocking {
            groupStatusProvider.fetch(refresh = true)
        }
        assertTrue(groupStatusProvider.loadStatus.value.error)
        assertNotNull(groupStatusProvider.status)
    }

    @Test
    fun test401ErrorClearsPreviousStatus() {
        val backendAPI = BackendAPIMock()
        val groupStatusProvider = getStatusProvider(backendAPI)
        runBlocking {
            groupStatusProvider.fetch(refresh = false)
        }
        assertTrue(groupStatusProvider.loadStatus.value.loaded)
        assertNotNull(groupStatusProvider.status)

        backendAPI.groupHuntingGroupStatusResponse = MockResponse.error(401)
        runBlocking {
            groupStatusProvider.fetch(refresh = true)
        }
        assertTrue(groupStatusProvider.loadStatus.value.error)
        assertNull(groupStatusProvider.status)
    }

    private fun getStatusProvider(backendAPI: BackendAPI = BackendAPIMock()): HuntingGroupStatusProvider{
        return HuntingGroupStatusFromNetworkProvider(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI: BackendAPI = backendAPI
                },
                huntingGroupId = 1
        )
    }
}
