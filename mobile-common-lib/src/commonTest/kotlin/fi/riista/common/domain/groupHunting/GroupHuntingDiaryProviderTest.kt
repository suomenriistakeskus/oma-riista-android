package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.model.*
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class GroupHuntingDiaryProviderTest {

    @Test
    fun testEntriesCanBeFetched() {
        val getDiary = getGroupHuntingDiary()
        assertTrue(getDiary.loadStatus.value.notLoaded)

        runBlocking {
            getDiary.fetch(refresh = false)
        }

        assertTrue(getDiary.loadStatus.value.loaded)
        assertEquals(3, getDiary.diary.harvests.size)
        assertEquals(2, getDiary.diary.observations.size)
    }

    @Test
    fun testEntriesAreNotRefetched() {
        val getDiary = getGroupHuntingDiary()
        runBlocking {
            getDiary.fetch(refresh = false)
        }

        getDiary.loadStatus.bind {
            fail("Should not refetch")
        }

        runBlocking {
            getDiary.fetch(refresh = false)
        }
    }

    @Test
    fun testGroupHuntingDiaryCanBeRefetched() {
        val getDiary = getGroupHuntingDiary()
        runBlocking {
            getDiary.fetch(refresh = false)
        }

        var loadingObserved = false
        getDiary.loadStatus.bind {
            if (it.loading) {
                loadingObserved = true
            }
        }

        runBlocking {
            getDiary.fetch(refresh = true)
        }

        assertTrue(loadingObserved)
    }

    @Test
    fun testGroupHuntingDiaryLoadErrorsAreReported() {
        val getDiary = getGroupHuntingDiary(
                backendAPI = BackendAPIMock(groupHuntingGameDiaryResponse = MockResponse.error(404))
        )
        runBlocking {
            getDiary.fetch(refresh = false)
        }

        assertTrue(getDiary.loadStatus.value.error)
        assertEquals(0, getDiary.diary.harvests.size)
        assertEquals(0, getDiary.diary.observations.size)
    }

    @Test
    fun testGroupHuntingDiaryLoadErrorsCanBeRecovered() {
        val backendAPI = BackendAPIMock(groupHuntingGameDiaryResponse = MockResponse.error(404))
        val getDiary = getGroupHuntingDiary(backendAPI)
        runBlocking {
            getDiary.fetch(refresh = false)
        }
        assertTrue(getDiary.loadStatus.value.error)
        assertEquals(0, getDiary.diary.harvests.size)
        assertEquals(0, getDiary.diary.observations.size)

        backendAPI.groupHuntingGameDiaryResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDiary)
        runBlocking {
            getDiary.fetch(refresh = false)
        }
        assertTrue(getDiary.loadStatus.value.loaded)
        assertEquals(3, getDiary.diary.harvests.size)
        assertEquals(2, getDiary.diary.observations.size)
    }

    @Test
    fun testNetworkErrorDoesNotClearPreviousDiary() {
        val backendAPI = BackendAPIMock()
        val getDiary = getGroupHuntingDiary(backendAPI)
        runBlocking {
            getDiary.fetch(refresh = false)
        }
        assertTrue(getDiary.loadStatus.value.loaded)
        assertEquals(3, getDiary.diary.harvests.size)
        assertEquals(2, getDiary.diary.observations.size)

        backendAPI.groupHuntingGameDiaryResponse = MockResponse.error(null)
        runBlocking {
            getDiary.fetch(refresh = true)
        }
        assertTrue(getDiary.loadStatus.value.error)
        assertEquals(3, getDiary.diary.harvests.size)
        assertEquals(2, getDiary.diary.observations.size)
    }

    @Test
    fun test401ErrorClearsPreviousDiary() {
        val backendAPI = BackendAPIMock()
        val getDiary = getGroupHuntingDiary(backendAPI)
        runBlocking {
            getDiary.fetch(refresh = false)
        }
        assertTrue(getDiary.loadStatus.value.loaded)
        assertEquals(3, getDiary.diary.harvests.size)
        assertEquals(2, getDiary.diary.observations.size)

        backendAPI.groupHuntingGameDiaryResponse = MockResponse.error(401)
        runBlocking {
            getDiary.fetch(refresh = true)
        }
        assertTrue(getDiary.loadStatus.value.error)
        assertTrue(getDiary.diary.harvests.isEmpty())
        assertTrue(getDiary.diary.observations.isEmpty())
    }

    @Test
    fun testObservationHasCorrectValues() {
        val getDiary = getGroupHuntingDiary()
        runBlocking {
            getDiary.fetch(refresh = false)
        }

        val obs = getDiary.diary.observations[0]
        assertEquals(29, obs.id)
        assertEquals(1, obs.rev)
        assertEquals(6789568, obs.geoLocation.latitude)
        assertEquals(330224, obs.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL, obs.geoLocation.source.value)
        assertEquals(LocalDateTime(2015, 9, 10, 13, 0, 0), obs.pointOfTime)
        assertEquals("It was an animal", obs.description)
        assertFalse(obs.canEdit)
        assertEquals(listOf("9a632219-d58c-4882-b91d-439229df1644"), obs.imageIds)
        assertEquals(47503, obs.gameSpeciesCode)
        assertEquals(ObservationType.NAKO, obs.observationType.value)
        assertEquals(ObservationCategory.MOOSE_HUNTING, obs.observationCategory.value)
        assertNull(obs.observerName)
        assertNull(obs.observerPhoneNumber)
        assertNull(obs.mobileClientRefId)
        assertEquals(3, obs.observationSpecVersion)
        assertEquals(1, obs.specimens.size)
        val specimen = obs.specimens[0]
        assertEquals(20, specimen.remoteId)
        assertEquals(0, specimen.revision)
        assertEquals(Gender.FEMALE, specimen.gender.value)
        assertEquals(GameAge.UNKNOWN, specimen.age.value)
        assertEquals(ObservationSpecimenState.WOUNDED, specimen.stateOfHealth.value)
        assertEquals(ObservationSpecimenMarking.EARMARK, specimen.marking.value)
        assertNull(specimen.widthOfPaw)
        assertNull(specimen.lengthOfPaw)
        assertTrue(obs.litter ?: false)
        assertTrue(obs.pack ?: false)
        assertFalse(obs.linkedToGroupHuntingDay)
        assertNull(obs.huntingDayId)
        assertEquals(1, obs.totalSpecimenAmount)
        assertEquals(0, obs.mooselikeMaleAmount)
        assertEquals(1, obs.mooselikeFemaleAmount)
        assertEquals(2, obs.mooselikeCalfAmount)
        assertEquals(3, obs.mooselikeFemale1CalfAmount)
        assertEquals(4, obs.mooselikeFemale2CalfsAmount)
        assertEquals(5, obs.mooselikeFemale3CalfsAmount)
        assertNull(obs.mooselikeFemale4CalfsAmount)
        assertEquals(6, obs.mooselikeUnknownSpecimenAmount)
        assertNull(obs.inYardDistanceToResidence)
        assertNull(obs.verifiedByCarnivoreAuthority)
        assertNull(obs.officialAdditionalInfo)

        val author = obs.authorInfo
        assertEquals(4, author.id)
        assertEquals(13, author.rev)
        assertEquals("Pena", author.byName)
        assertEquals("Mujunen", author.lastName)
        assertEquals("88888888", author.hunterNumber)
        assertNull(author.extendedName)

        val actor = obs.actorInfo
        assertEquals(5, actor.id)
        assertEquals(14, actor.rev)
        assertEquals("Pentti", actor.byName)
        assertEquals("Makunen", actor.lastName)
        assertEquals("99999999", actor.hunterNumber)
        assertEquals("Mr.", actor.extendedName)
    }

    @Test
    fun testHarvestHasCorrectValues() {
        val getDiary = getGroupHuntingDiary()
        runBlocking {
            getDiary.fetch(refresh = false)
        }

        val harvest = getDiary.diary.harvests[0]
        assertEquals(949, harvest.id)
        assertEquals(2, harvest.rev)
        assertEquals(6820960, harvest.geoLocation.latitude)
        assertEquals(318112, harvest.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL, harvest.geoLocation.source.value)
        assertEquals(LocalDateTime(2015,9,1,14,0,0), harvest.pointOfTime)
        assertEquals("1234", harvest.description)
        assertTrue(harvest.canEdit)
        assertEquals(0, harvest.imageIds.size)
        assertEquals(47503, harvest.gameSpeciesCode)
        assertEquals(8, harvest.harvestSpecVersion)
        assertFalse(harvest.harvestReportRequired)
        assertEquals(HarvestReportState.SENT_FOR_APPROVAL, harvest.harvestReportState.value)
        assertEquals("12323-33323", harvest.permitNumber)
        assertEquals(StateAcceptedToHarvestPermit.PROPOSED, harvest.stateAcceptedToHarvestPermit.value)

        assertEquals(1, harvest.specimens.size)
        val specimen = harvest.specimens[0]
        assertEquals(413, specimen.id)
        assertEquals(1, specimen.rev)
        assertEquals(Gender.MALE, specimen.gender?.value)
        assertEquals(GameAge.ADULT, specimen.age?.value)
        assertNull(specimen.weight)
        assertEquals(34.0, specimen.weightEstimated)
        assertEquals(4.0, specimen.weightMeasured)
        assertEquals(GameFitnessClass.EXHAUSTED, specimen.fitnessClass?.value)
        assertEquals(GameAntlersType.CERVINE, specimen.antlersType?.value)
        assertEquals(24, specimen.antlersWidth)
        assertEquals(4, specimen.antlerPointsLeft)
        assertEquals(1, specimen.antlerPointsRight)
        assertFalse(specimen.notEdible ?: true)
        assertEquals("additional_info", specimen.additionalInfo)

        assertEquals(DeerHuntingType.DOG_HUNTING, harvest.deerHuntingType.value)
        assertNull(harvest.deerHuntingOtherTypeDescription)
        assertNull(harvest.mobileClientRefId)
        assertEquals(1, harvest.amount)
        assertFalse(harvest.harvestReportDone)
        assertEquals(GroupHuntingDayId.remote(5), harvest.huntingDayId)

        val author = harvest.authorInfo
        assertEquals(4, author.id)
        assertEquals(13, author.rev)
        assertEquals("Pena", author.byName)
        assertEquals("Mujunen", author.lastName)
        assertEquals("88888888", author.hunterNumber)
        assertEquals("Mr.", author.extendedName)

        val actor = harvest.actorInfo
        assertEquals(5, actor.id)
        assertEquals(14, actor.rev)
        assertEquals("Pentti", actor.byName)
        assertEquals("Makunen", actor.lastName)
        assertEquals("99999999", actor.hunterNumber)
        assertNull(actor.extendedName)
    }

    private fun getGroupHuntingDiary(backendAPI: BackendAPI = BackendAPIMock()): GroupHuntingDiaryProvider {
        return GroupHuntingDiaryNetworkProvider(
                backendApiProvider = object : BackendApiProvider {
                    override val backendAPI: BackendAPI = backendAPI
                },
                huntingGroupId = 1
        )
    }

}
