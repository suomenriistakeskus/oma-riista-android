package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class HuntingControlRhyFromNetworkProviderTest {
    @Test
    fun testRhysCanBeFetched() {
        val rhyProvider = huntingControlRhyFromNetworkProvider()
        assertTrue(rhyProvider.loadStatus.value.notLoaded)

        runBlocking {
            rhyProvider.fetch(refresh = false)
        }

        assertTrue(rhyProvider.loadStatus.value.loaded)
        assertNotNull(rhyProvider.rhys)
        assertEquals(1, rhyProvider.rhys!!.size)
    }

    @Test
    fun test401ErrorClearsPreviousRhys() {
        val backendAPI = BackendAPIMock()
        val rhyProvider = huntingControlRhyFromNetworkProvider(backendAPI = backendAPI)

        runBlocking {
            rhyProvider.fetch(refresh = false)
        }
        assertTrue(rhyProvider.loadStatus.value.loaded)
        assertNotNull(rhyProvider.rhys)

        backendAPI.huntingControlRhysResponse = MockResponse.error(401)
        runBlocking {
            rhyProvider.fetch(refresh = true)
        }
        assertTrue(rhyProvider.loadStatus.value.error)
        assertNull(rhyProvider.rhys)
    }

    private fun huntingControlRhyFromNetworkProvider(
        backendAPI: BackendAPI = BackendAPIMock()
    ): HuntingControlRhyFromNetworkProvider {
        return HuntingControlRhyFromNetworkProvider(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            }
        )
    }
}
