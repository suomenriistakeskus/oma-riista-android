package fi.riista.common.domain.huntingclub

import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HuntingClubsContextTest {

    @Test
    fun testMembershipsAndInvitationsAreCreated() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val huntingClubsContext = userContext.huntingClubsContext

        runBlocking {
            huntingClubsContext.fetchInvitations()
            huntingClubsContext.fetchMemberships()
        }

        assertEquals(1, huntingClubsContext.invitations!!.size)
        assertEquals(1, huntingClubsContext.memberships!!.size)
    }

    @Test
    fun testEmptyInvitationsAndMembershipsAreLoaded() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            backendAPI = BackendAPIMock(
                huntingClubMembershipResponse = MockResponse.success(MockHuntingClubData.EmptyHuntingClubMemberships),
                huntingClubMemberInvitationsResponse = MockResponse.success(MockHuntingClubData.EmptyHuntingClubMemberInvitations),
            ),
        )

        userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val huntingClubsContext = userContext.huntingClubsContext

        runBlocking {
            huntingClubsContext.fetchInvitations()
            huntingClubsContext.fetchMemberships()
        }

        assertEquals(true, huntingClubsContext.huntingClubMembershipProvider.loadStatus.value.loaded)
        assertEquals(true, huntingClubsContext.huntingClubMemberInvitationProvider.loadStatus.value.loaded)
        assertEquals(0, huntingClubsContext.invitations!!.size)
        assertEquals(0, huntingClubsContext.memberships!!.size)
    }

    @Test
    fun testHuntingClubsContextIsClearedAfterLoggingOut() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val huntingClubsContext = userContext.huntingClubsContext

        runBlocking {
            huntingClubsContext.fetchInvitations()
            huntingClubsContext.fetchMemberships()
        }

        assertEquals(1, huntingClubsContext.invitations!!.size)
        assertEquals(1, huntingClubsContext.memberships!!.size)

        userContext.userLoggedOut()

        assertNull(huntingClubsContext.invitations)
        assertNull(huntingClubsContext.memberships)
    }
}
