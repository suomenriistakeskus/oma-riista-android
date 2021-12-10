package fi.riista.common.poi

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PoiLocationGroupProviderTest {
    @Test
    fun testPoiLocationGroupsCanBeFetched() {
        val poiLocationGroupProvider = getPoiLocationGroupProvider(externalId = "DZFM5KSKAY")
        assertTrue(poiLocationGroupProvider.loadStatus.value.notLoaded)

        runBlocking {
            poiLocationGroupProvider.fetch(refresh = false)
        }

        assertTrue(poiLocationGroupProvider.loadStatus.value.loaded)
        assertNotNull(poiLocationGroupProvider.poiLocationGroups)
        assertEquals(3, poiLocationGroupProvider.poiLocationGroups!!.size)
    }

    private fun getPoiLocationGroupProvider(
        backendAPI: BackendAPI = BackendAPIMock(),
        externalId: String,
    ): PoiLocationGroupProvider {
        return PoiLocationGroupFromNetworkProvider(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            },
            externalId = externalId,
        )
    }
}
