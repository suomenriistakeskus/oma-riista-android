package fi.riista.common.domain.huntingclub.invitations

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HuntingClubMemberInvitationProviderTest {

    @Test
    fun testInvitationsCanBeFetched() {
        val invitationProvider = getHuntingClubMemberInvitationProvider()
        assertTrue(invitationProvider.loadStatus.value.notLoaded)

        runBlocking {
            invitationProvider.fetch(refresh = false)
        }

        assertTrue(invitationProvider.loadStatus.value.loaded)
        assertNotNull(invitationProvider.invitations)
        assertEquals(1, invitationProvider.invitations!!.size)
    }

    @Test
    fun test401ErrorClearsPreviousInvitations() {
        val backendAPI = BackendAPIMock()
        val invitationProvider = getHuntingClubMemberInvitationProvider(backendAPI = backendAPI)

        runBlocking {
            invitationProvider.fetch(refresh = false)
        }
        assertTrue(invitationProvider.loadStatus.value.loaded)
        assertNotNull(invitationProvider.invitations)

        backendAPI.huntingClubMemberInvitationsResponse = MockResponse.error(401)
        runBlocking {
            invitationProvider.fetch(refresh = true)
        }
        assertTrue(invitationProvider.loadStatus.value.error)
        assertNull(invitationProvider.invitations)
    }

    private fun getHuntingClubMemberInvitationProvider(
        backendAPI: BackendAPI = BackendAPIMock()
    ): HuntingClubMemberInvitationProvider {
        return HuntingClubMemberInvitationFromNetworkProvider(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            }
        )
    }
}
