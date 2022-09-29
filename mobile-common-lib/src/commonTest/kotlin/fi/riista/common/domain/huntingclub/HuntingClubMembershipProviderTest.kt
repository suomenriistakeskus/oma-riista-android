package fi.riista.common.domain.huntingclub

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class HuntingClubMembershipProviderTest {

    @Test
    fun testHuntingClubMembershipsCanBeFetched() {
        val membershipProvider = getHuntingClubMembershipProvider()
        assertTrue(membershipProvider.loadStatus.value.notLoaded)

        runBlocking {
            membershipProvider.fetch(refresh = false)
        }

        assertTrue(membershipProvider.loadStatus.value.loaded)
        assertNotNull(membershipProvider.memberships)
        assertEquals(1, membershipProvider.memberships!!.size)
    }

    @Test
    fun test401ErrorClearsPreviousMemberships() {
        val backendAPI = BackendAPIMock()
        val membershipProvider = getHuntingClubMembershipProvider(backendAPI = backendAPI)

        runBlocking {
            membershipProvider.fetch(refresh = false)
        }
        assertTrue(membershipProvider.loadStatus.value.loaded)
        assertNotNull(membershipProvider.memberships)

        backendAPI.huntingClubMembershipResponse = MockResponse.error(401)
        runBlocking {
            membershipProvider.fetch(refresh = true)
        }
        assertTrue(membershipProvider.loadStatus.value.error)
        assertNull(membershipProvider.memberships)
    }

    private fun getHuntingClubMembershipProvider(
        backendAPI: BackendAPI = BackendAPIMock()
    ): HuntingClubMembershipProvider {
        return HuntingClubMembershipFromNetworkProvider(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            }
        )
    }

}
