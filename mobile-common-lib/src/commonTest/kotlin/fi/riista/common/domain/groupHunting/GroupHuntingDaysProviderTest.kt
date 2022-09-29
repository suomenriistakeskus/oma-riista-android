@file:Suppress("SpellCheckingInspection")

package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.model.GroupHuntingMethodType
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class GroupHuntingDaysProviderTest {

    @Test
    fun testHuntingDaysCanBeFetched() {
        val groupHuntingDayProvider = getHuntingDayProvider()
        assertTrue(groupHuntingDayProvider.loadStatus.value.notLoaded)

        runBlocking {
            groupHuntingDayProvider.fetch(refresh = false)
        }

        assertTrue(groupHuntingDayProvider.loadStatus.value.loaded)
        assertNotNull(groupHuntingDayProvider.huntingDays)
        assertEquals(2, groupHuntingDayProvider.huntingDays!!.size)
        with(groupHuntingDayProvider.huntingDays!![0]) {
            assertEquals(5, id.remoteId)
            assertEquals(0, rev)
            assertEquals(MockGroupHuntingData.FirstHuntingGroupId, huntingGroupId)
            assertEquals(LocalDateTime(2015, 9, 1, 6, 0, 0), startDateTime)
            assertEquals(LocalDateTime(2015, 9, 1, 21, 0, 0), endDateTime)
            assertEquals(900, durationInMinutes)
            assertEquals(0, breakDurationInMinutes)
            assertEquals(900, activeHuntingDurationInMinutes)
            assertEquals(123, snowDepth)
            assertEquals("PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA", huntingMethod.rawBackendEnumValue)
            assertEquals(GroupHuntingMethodType.PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA, huntingMethod.value)
            assertEquals(23, numberOfHunters)
            assertEquals(1, numberOfHounds)
            assertFalse(createdBySystem)
        }

        with(groupHuntingDayProvider.huntingDays!![1]) {
            assertEquals(6, id.remoteId)
            assertEquals(0, rev)
            assertEquals(MockGroupHuntingData.FirstHuntingGroupId, huntingGroupId)
            assertEquals(LocalDateTime(2015, 9, 3, 6, 0, 0), startDateTime)
            assertEquals(LocalDateTime(2015, 9, 3, 21, 0, 0), endDateTime)
            assertEquals(900, durationInMinutes)
            assertNull(breakDurationInMinutes)
            assertEquals(900, activeHuntingDurationInMinutes)
            assertNull(snowDepth)
            assertNotNull(huntingMethod)
            assertNull(huntingMethod.rawBackendEnumValue)
            assertNull(numberOfHunters)
            assertNull(numberOfHounds)
            assertFalse(createdBySystem)
        }
    }

    @Test
    fun testHuntingDayLoadErrorsCanBeRecovered() {
        val backendAPI = BackendAPIMock(groupHuntingGroupHuntingDaysResponse = MockResponse.error(404))
        val groupHuntingDayProvider = getHuntingDayProvider(backendAPI)
        runBlocking {
            groupHuntingDayProvider.fetch(refresh = false)
        }
        assertTrue(groupHuntingDayProvider.loadStatus.value.error)
        assertNull(groupHuntingDayProvider.huntingDays)

        backendAPI.groupHuntingGroupHuntingDaysResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDays)
        runBlocking {
            groupHuntingDayProvider.fetch(refresh = false)
        }
        assertTrue(groupHuntingDayProvider.loadStatus.value.loaded)
        assertNotNull(groupHuntingDayProvider.huntingDays)
    }

    @Test
    fun testNetworkErrorDoesNotClearPreviousHuntingDays() {
        val backendAPI = BackendAPIMock()
        val groupHuntingDayProvider = getHuntingDayProvider(backendAPI)
        runBlocking {
            groupHuntingDayProvider.fetch(refresh = false)
        }
        assertTrue(groupHuntingDayProvider.loadStatus.value.loaded)
        assertNotNull(groupHuntingDayProvider.huntingDays)

        backendAPI.groupHuntingGroupHuntingDaysResponse = MockResponse.error(null)
        runBlocking {
            groupHuntingDayProvider.fetch(refresh = true)
        }
        assertTrue(groupHuntingDayProvider.loadStatus.value.error)
        assertNotNull(groupHuntingDayProvider.huntingDays)
    }

    @Test
    fun test401ErrorClearsPreviousHuntingDays() {
        val backendAPI = BackendAPIMock()
        val groupHuntingDayProvider = getHuntingDayProvider(backendAPI)
        runBlocking {
            groupHuntingDayProvider.fetch(refresh = false)
        }
        assertTrue(groupHuntingDayProvider.loadStatus.value.loaded)
        assertNotNull(groupHuntingDayProvider.huntingDays)

        backendAPI.groupHuntingGroupHuntingDaysResponse = MockResponse.error(401)
        runBlocking {
            groupHuntingDayProvider.fetch(refresh = true)
        }
        assertTrue(groupHuntingDayProvider.loadStatus.value.error)
        assertNull(groupHuntingDayProvider.huntingDays)
    }

    private fun getHuntingDayProvider(backendAPI: BackendAPI = BackendAPIMock()): GroupHuntingDayProvider{
        return GroupHuntingDayFromNetworkProvider(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI: BackendAPI = backendAPI
                },
                huntingGroupId = 1
        )
    }
}
