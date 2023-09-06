package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.model.LoadStatus
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class GroupHuntingContextTest {

    @Test
    fun testNoClubContextsIfNotAvailable() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                groupHuntingEnabledForAll = false,
                groupHuntingEnabledForHunters = listOf()
        )
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val groupHuntingContext = userContext.groupHuntingContext
        runBlocking {
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertFalse(userContext.groupHuntingContext.groupHuntingAvailable)
        assertEquals(0, groupHuntingContext.clubContexts.size)
    }

    @Test
    fun testClubContextExistsIfGroupHuntingEnabledForAll() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.OneClub)
                ),
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        )
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val groupHuntingContext = userContext.groupHuntingContext
        runBlocking {
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertTrue(userContext.groupHuntingContext.groupHuntingAvailable)
        assertEquals(1, groupHuntingContext.clubContexts.size)
    }

    @Test
    fun testClubContextExistsIfGroupHuntingEnabledForHunter() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.OneClub)
                ),
                groupHuntingEnabledForAll = false,
                groupHuntingEnabledForHunters = listOf(MockUserInfo.PenttiHunterNumber)
        )
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val groupHuntingContext = userContext.groupHuntingContext
        runBlocking {
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertTrue(userContext.groupHuntingContext.groupHuntingAvailable)
        assertEquals(1, groupHuntingContext.clubContexts.size)
    }

    @Test
    fun testClubContextsAreCreated() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.TwoClubs)
                ),
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        )
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val groupHuntingContext = userContext.groupHuntingContext
        runBlocking {
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertTrue(userContext.groupHuntingContext.groupHuntingAvailable)
        assertEquals(2, groupHuntingContext.clubContexts.size)
        assertNotNull(groupHuntingContext.clubContexts.firstOrNull {
            it.club.id == MockGroupHuntingData.FirstClubId
        })
        assertNotNull(groupHuntingContext.clubContexts.firstOrNull {
            it.club.id == MockGroupHuntingData.SecondClubId
        })
    }

    @Test
    fun testGroupHuntingLoadStatusIsInitiallyNotLoaded() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.OneClub)
                ),
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        )
        val groupHuntingContext = userContextProvider.userContext.groupHuntingContext
        assertTrue(groupHuntingContext.clubContextsProvider.loadStatus.value is LoadStatus.NotLoaded)
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
        assertTrue(groupHuntingContext.clubContextsProvider.loadStatus.value is LoadStatus.NotLoaded)
    }

    @Test
    fun testGroupHuntingStatusIsLoadingBeforeFinished() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.OneClub)
                ),
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        )
        val groupHuntingContext = userContextProvider.userContext.groupHuntingContext
        var loadingStatusObserved = false
        groupHuntingContext.clubContextsProvider.loadStatus.bind { loadStatus ->
            if (loadStatus.loading) {
                loadingStatusObserved = true
            }
        }
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertTrue(loadingStatusObserved)
        assertTrue(groupHuntingContext.clubContextsProvider.loadStatus.value is LoadStatus.Loaded)
    }

    @Test
    fun testGroupHuntingClubsAreNotLoadedIfNotAvailable() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.OneClub)
                ),
                groupHuntingEnabledForAll = false,
                groupHuntingEnabledForHunters = listOf()
        )
        val groupHuntingContext = userContextProvider.userContext.groupHuntingContext
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        with (userContextProvider.userContext.groupHuntingContext) {
            assertFalse(groupHuntingAvailable)
            assertTrue(clubContextsProvider.loadStatus.value is LoadStatus.NotLoaded)
        }
    }

    @Test
    fun testGroupHuntingAvailabilityStatusLoadedCanBeObserved() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.OneClub)
                ),
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        )
        val groupHuntingContext = userContextProvider.userContext.groupHuntingContext
        var observedGroupHuntingAvailability: Boolean? = null
        groupHuntingContext.clubContextsProvider.loadStatus.bind { loadStatus ->
            if (loadStatus is LoadStatus.Loaded) {
                observedGroupHuntingAvailability = groupHuntingContext.groupHuntingAvailable
            }
        }
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertEquals(true, observedGroupHuntingAvailability)
        assertTrue(groupHuntingContext.groupHuntingAvailable)
    }

    @Test
    fun testGroupHuntingContextIsClearedAfterLoggingOut() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                backendAPI = BackendAPIMock(
                        groupHuntingClubsAndGroupsResponse = MockResponse.success(MockGroupHuntingData.TwoClubs)
                ),
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        )
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val groupHuntingContext = userContext.groupHuntingContext
        runBlocking {
            groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
        assertTrue(userContext.groupHuntingContext.groupHuntingAvailable)
        assertEquals(2, groupHuntingContext.clubContexts.size)

        runBlocking {
            userContext.userLoggedOut()
        }

        assertFalse(userContext.groupHuntingContext.groupHuntingAvailable)
        assertEquals(0, groupHuntingContext.clubContexts.size)
    }

    @Test
    fun testSearchPersonByHunterNumber() {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = false,
            groupHuntingEnabledForHunters = listOf()
        )
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        val userContext = userContextProvider.userContext
        assertNotNull(userContext)

        val groupHuntingContext = userContext.groupHuntingContext
        val person = runBlocking {
             groupHuntingContext.searchPersonByHunterNumber("88888888")
        }
        assertNotNull(person)
        assertEquals("Pena", person.byName)
        assertEquals("88888888", person.hunterNumber)


        val person2 = runBlocking {
            groupHuntingContext.searchPersonByHunterNumber("99999999") // doesn't exist
        }
        assertNull(person2)
    }
}
