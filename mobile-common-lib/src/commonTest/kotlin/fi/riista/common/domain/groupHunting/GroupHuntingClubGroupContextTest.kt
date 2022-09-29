package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.model.HuntingGroup
import fi.riista.common.domain.groupHunting.model.HuntingGroupPermit
import fi.riista.common.domain.groupHunting.model.toGroupHuntingObservationData
import fi.riista.common.model.LocalizedString
import fi.riista.common.domain.model.Organization
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class GroupHuntingClubGroupContextTest {

    @Test
    fun testFetchingAllDataFetchesMembers() {
        val groupContext = getGroupContext()
        assertTrue(groupContext.membersProvider.loadStatus.value.notLoaded)
        assertNull(groupContext.membersProvider.members)
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.membersProvider.loadStatus.value.loaded)
        assertEquals(4, groupContext.membersProvider.members?.size)
    }

    @Test
    fun testFetchingAllDataFetchesArea() {
        val groupContext = getGroupContext()
        assertTrue(groupContext.huntingAreaProvider.loadStatus.value.notLoaded)
        assertNull(groupContext.huntingAreaProvider.area)
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.huntingAreaProvider.loadStatus.value.loaded)
        assertNotNull(groupContext.huntingAreaProvider.area)
    }

    @Test
    fun testFetchingAllDataFetchesStatus() {
        val groupContext = getGroupContext()
        assertTrue(groupContext.huntingStatusProvider.loadStatus.value.notLoaded)
        assertNull(groupContext.huntingStatusProvider.status)
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.huntingStatusProvider.loadStatus.value.loaded)
        assertNotNull(groupContext.huntingStatusProvider.status)
    }

    @Test
    fun testFetchingAllDataFetchesHuntingDays() {
        val groupContext = getGroupContext()
        assertTrue(groupContext.huntingDaysProvider.loadStatus.value.notLoaded)
        assertNull(groupContext.huntingDaysProvider.huntingDays)
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.huntingDaysProvider.loadStatus.value.loaded)
        assertNotNull(groupContext.huntingDaysProvider.huntingDays)
        assertEquals(2, groupContext.huntingDaysProvider.huntingDays!!.size)
    }

    @Test
    fun testFetchingAllDataFetchesGroupHuntingDiary() {
        val groupContext = getGroupContext()
        assertTrue(groupContext.diaryProvider.loadStatus.value.notLoaded)
        assertEquals(0, groupContext.diaryProvider.diary.harvests.size)
        assertEquals(0, groupContext.diaryProvider.diary.observations.size)
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        assertEquals(3, groupContext.diaryProvider.diary.harvests.size)
        assertEquals(2, groupContext.diaryProvider.diary.observations.size)
    }

    @Test
    fun testFetchingOneDataPieceDoesNotFetchOthers() {
        val groupContext = getGroupContext()
        assertTrue(groupContext.diaryProvider.loadStatus.value.notLoaded)
        assertTrue(groupContext.huntingDaysProvider.loadStatus.value.notLoaded)
        runBlocking {
            groupContext.fetchDataPieces(listOf(HuntingClubGroupDataPiece.DIARY), false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        assertTrue(groupContext.huntingDaysProvider.loadStatus.value.notLoaded)
        assertTrue(groupContext.membersProvider.loadStatus.value.notLoaded)
        assertTrue(groupContext.huntingAreaProvider.loadStatus.value.notLoaded)
        assertTrue(groupContext.huntingStatusProvider.loadStatus.value.notLoaded)

        runBlocking {
            groupContext.fetchDataPieces(listOf(HuntingClubGroupDataPiece.MEMBERS), false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        assertTrue(groupContext.membersProvider.loadStatus.value.loaded)
        assertTrue(groupContext.huntingDaysProvider.loadStatus.value.notLoaded)
        assertTrue(groupContext.huntingAreaProvider.loadStatus.value.notLoaded)
        assertTrue(groupContext.huntingStatusProvider.loadStatus.value.notLoaded)
    }

    @Test
    fun testAcceptHarvestReturnsUpdatedHarvestWhenSuccessful() {
        val groupContext = getGroupContext()
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val harvest = groupContext.diaryProvider.diary.harvests[0]
        val result = runBlocking {
            return@runBlocking groupContext.acceptHarvest(harvest)
        }
        assertTrue(result is GroupHuntingHarvestOperationResponse.Success)
        assertEquals(3, result.harvest.rev)
    }

    @Test
    fun testAcceptHarvestReturnsNetworkStatusCodeWhenFails() {
        val groupContext = getGroupContext(
            backendAPI = BackendAPIMock(groupHuntingAcceptHarvestResponse = MockResponse.error(400))
        )
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val harvest = groupContext.diaryProvider.diary.harvests[0]
        val result = runBlocking {
            return@runBlocking groupContext.acceptHarvest(harvest)
        }
        assertTrue(result is GroupHuntingHarvestOperationResponse.Failure)
        assertEquals(400, result.networkStatusCode)
    }

    @Test
    fun testAcceptHarvestReturnsErrorWhenHuntingDayIsMissing() {
        val groupContext = getGroupContext(
            backendAPI = BackendAPIMock(groupHuntingAcceptHarvestResponse = MockResponse.error(400))
        )
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val harvest = groupContext.diaryProvider.diary.harvests[0]
        val harvestWithoutHuntingDayId = harvest.copy(huntingDayId = null)
        val result = runBlocking {
            return@runBlocking groupContext.acceptHarvest(harvestWithoutHuntingDayId)
        }
        assertTrue(result is GroupHuntingHarvestOperationResponse.Error)
    }

    @Test
    fun rejectHarvestReturnsRejectedWhenSuccessful() {
        val groupContext = getGroupContext()
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val harvest = groupContext.diaryProvider.diary.harvests[0]
        val result = runBlocking {
            return@runBlocking groupContext.rejectHarvest(harvest)
        }
        assertTrue(result is GroupHuntingHarvestOperationResponse.Success)
    }

    @Test
    fun rejectHarvestReturnsNetworkStatusCodeWhenFails() {
        val groupContext = getGroupContext(
            backendAPI = BackendAPIMock(groupHuntingRejectDiaryEntryResponse = MockResponse.error(400))
        )
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val harvest = groupContext.diaryProvider.diary.harvests[0]
        val result = runBlocking {
            return@runBlocking groupContext.rejectHarvest(harvest)
        }
        assertTrue(result is GroupHuntingHarvestOperationResponse.Failure)
        assertEquals(400, result.networkStatusCode)
    }

    @Test
    fun rejectObservationReturnsRejectedWhenSuccessful() {
        val groupContext = getGroupContext()
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val observation = groupContext.diaryProvider.diary.observations[0]
        val result = runBlocking {
            return@runBlocking groupContext.rejectObservation(observation)
        }
        assertTrue(result is GroupHuntingObservationOperationResponse.Success)
    }

    @Test
    fun rejectObservationReturnsNetworkStatusCodeWhenFails() {
        val groupContext = getGroupContext(
            backendAPI = BackendAPIMock(groupHuntingRejectDiaryEntryResponse = MockResponse.error(400))
        )
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val observation = groupContext.diaryProvider.diary.observations[0]
        val result = runBlocking {
            return@runBlocking groupContext.rejectObservation(observation)
        }
        assertTrue(result is GroupHuntingObservationOperationResponse.Failure)
        assertEquals(400, result.networkStatusCode)
    }

    @Test
    fun createObservationReturnsCreatedObservation() {
        val groupContext = getGroupContext()
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val observation = groupContext.diaryProvider.diary.observations[0].copy(huntingDayId = MockGroupHuntingData.FirstHuntingDayId)
        val result = runBlocking {
            return@runBlocking groupContext.createObservation(
                observation.toGroupHuntingObservationData(groupMembers = listOf())
            )
        }
        assertTrue(result is GroupHuntingObservationOperationResponse.Success)
        assertEquals(MockGroupHuntingData.SecondObservationId, result.observation.id)
    }

    @Test
    fun creatingObservationWithoutHuntingDayIdReturnsError() {
        val groupContext = getGroupContext()
        runBlocking {
            groupContext.fetchAllData(refresh = false)
        }
        assertTrue(groupContext.diaryProvider.loadStatus.value.loaded)
        val observation = groupContext.diaryProvider.diary.observations[0]
        val result = runBlocking {
            return@runBlocking groupContext.createObservation(
                observation.toGroupHuntingObservationData(groupMembers = listOf())
            )
        }
        assertTrue(result is GroupHuntingObservationOperationResponse.Error)
    }

    private fun getGroupContext(backendAPI: BackendAPI = BackendAPIMock()): GroupHuntingClubGroupContext {
        return GroupHuntingClubGroupContext(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI: BackendAPI = backendAPI
                },
                huntingGroup = HuntingGroup(
                        id = 1,
                        club = Organization(
                                id = 2,
                                name = LocalizedString("Seura", null, null),
                                officialCode = "Seura2"
                        ),
                        speciesCode = 3,
                        huntingYear = 2021,
                        permit = HuntingGroupPermit("Porukka1_3", listOf()),
                        name = LocalizedString("Porukka1", null, null),
                )
        )
    }
}
