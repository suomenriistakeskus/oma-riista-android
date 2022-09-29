package fi.riista.common.domain.groupHunting

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HuntingGroupMembersProviderTest {

    @Test
    fun testMembersCanBeFetched() {
        val huntingGroupMembers = getHuntingGroupMembers()
        assertTrue(huntingGroupMembers.loadStatus.value.notLoaded)
        assertNull(huntingGroupMembers.members)

        runBlocking {
            huntingGroupMembers.fetch(refresh = false)
        }

        assertTrue(huntingGroupMembers.loadStatus.value.loaded)
        assertEquals(4, huntingGroupMembers.members?.size)
    }

    @Test
    fun testMembersLoadErrorsCanBeRecovered() {
        val backendAPI = BackendAPIMock(groupHuntingGroupMembersResponse = MockResponse.error(404))
        val huntingGroupMembers = getHuntingGroupMembers(backendAPI)
        runBlocking {
            huntingGroupMembers.fetch(refresh = false)
        }
        assertTrue(huntingGroupMembers.loadStatus.value.error)
        assertNull(huntingGroupMembers.members) // not loaded before, members should be null

        backendAPI.groupHuntingGroupMembersResponse = MockResponse.success(MockGroupHuntingData.Members)
        runBlocking {
            huntingGroupMembers.fetch(refresh = false)
        }
        assertTrue(huntingGroupMembers.loadStatus.value.loaded)
        assertEquals(4, huntingGroupMembers.members?.size)
    }

    @Test
    fun testNetworkErrorDoesNotClearPreviousMembers() {
        val backendAPI = BackendAPIMock()
        val huntingGroupMembers = getHuntingGroupMembers(backendAPI)
        runBlocking {
            huntingGroupMembers.fetch(refresh = false)
        }
        assertTrue(huntingGroupMembers.loadStatus.value.loaded)
        assertEquals(4, huntingGroupMembers.members?.size)

        backendAPI.groupHuntingGroupMembersResponse = MockResponse.error(null)
        runBlocking {
            huntingGroupMembers.fetch(refresh = true)
        }
        assertTrue(huntingGroupMembers.loadStatus.value.error)
        assertEquals(4, huntingGroupMembers.members?.size)
    }

    @Test
    fun test401ErrorClearsPreviousMembers() {
        val backendAPI = BackendAPIMock()
        val huntingGroupMembers = getHuntingGroupMembers(backendAPI)
        runBlocking {
            huntingGroupMembers.fetch(refresh = false)
        }
        assertTrue(huntingGroupMembers.loadStatus.value.loaded)
        assertEquals(4, huntingGroupMembers.members?.size)

        backendAPI.groupHuntingGroupMembersResponse = MockResponse.error(401)
        runBlocking {
            huntingGroupMembers.fetch(refresh = true)
        }
        assertTrue(huntingGroupMembers.loadStatus.value.error)
        assertNull(huntingGroupMembers.members)
    }

    private fun getHuntingGroupMembers(backendAPI: BackendAPI = BackendAPIMock()): HuntingGroupMembersProvider {
        return HuntingGroupMembersFromNetworkProvider(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI: BackendAPI = backendAPI
                },
                huntingGroupId = 1
        )
    }
}
