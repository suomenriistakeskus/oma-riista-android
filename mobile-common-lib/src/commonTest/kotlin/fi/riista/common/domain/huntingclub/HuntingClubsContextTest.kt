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
    fun testInvitationsAreCreated() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val huntingClubsContext = userContext.huntingClubsContext

        runBlocking {
            huntingClubsContext.fetchInvitations()
        }

        assertEquals(1, huntingClubsContext.invitations!!.size)
    }

    @Test
    fun testEmptyInvitationsAreLoaded() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            backendAPI = BackendAPIMock(
                huntingClubMemberInvitationsResponse = MockResponse.success(MockHuntingClubData.EmptyHuntingClubMemberInvitations),
            ),
        )

        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val huntingClubsContext = userContext.huntingClubsContext

        runBlocking {
            huntingClubsContext.fetchInvitations()
        }

        assertEquals(true, huntingClubsContext.huntingClubMemberInvitationProvider.loadStatus.value.loaded)
        assertEquals(0, huntingClubsContext.invitations!!.size)
    }

    @Test
    fun testHuntingClubsContextIsClearedAfterLoggingOut() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val huntingClubsContext = userContext.huntingClubsContext

        runBlocking {
            huntingClubsContext.fetchInvitations()
        }

        assertEquals(1, huntingClubsContext.invitations!!.size)

        runBlocking {
            userContext.userLoggedOut()
        }

        assertNull(huntingClubsContext.invitations)
    }
}
