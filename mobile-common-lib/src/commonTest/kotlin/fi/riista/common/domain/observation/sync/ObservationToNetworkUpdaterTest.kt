package fi.riista.common.domain.observation.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.ObservationRepository
import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.domain.observation.sync.dto.ObservationCreateDTO
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ObservationToNetworkUpdaterTest {
    @Test
    fun testSendNewEventToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = ObservationRepository(database)
        val backendApi = BackendAPIMock()
        val updater = observationToNetworkUpdater(backendApi, database)

        repository.upsertObservation(username, getObservation())
        val modifiedEvents = repository.getModifiedObservations(username)
        assertEquals(1, modifiedEvents.size)

        updater.update(username, modifiedEvents)
        assertEquals(1, backendApi.totalCallCount())
        assertEquals(1, backendApi.callCount(BackendAPI::createObservation.name))

        val observation = backendApi.callParameter(BackendAPI::createObservation.name) as ObservationCreateDTO
        assertEquals("OBSERVATION", observation.type)
        assertEquals(999, observation.mobileClientRefId)
        assertEquals(Constants.OBSERVATION_SPEC_VERSION, observation.observationSpecVersion)
        assertEquals(SpeciesCodes.MOOSE_ID, observation.gameSpeciesCode)
        assertEquals("NORMAL", observation.observationCategory)
        assertEquals("NAKO", observation.observationType)
        assertEquals("OTHER", observation.deerHuntingType)
        assertEquals("This is other type", observation.deerHuntingTypeDescription)
        assertEquals(12, observation.geoLocation.latitude)
        assertEquals(13, observation.geoLocation.longitude)
        assertEquals("MANUAL", observation.geoLocation.source)
        assertEquals("2022-01-01T13:14:15", observation.pointOfTime)
        assertEquals("This is an observation", observation.description)
        assertEquals(0, observation.imageIds.size)

        assertEquals(36, observation.totalSpecimenAmount)
        assertEquals(1, observation.specimens?.size)
        val expectedSpecimens = listOf(
            CommonObservationSpecimenDTO(
                id = null,
                rev = null,
                gender = "FEMALE",
                age = "ADULT",
                state = "HEALTHY",
                marking = "EARMARK",
                widthOfPaw = 2.2,
                lengthOfPaw = 6.5,
            ),
        )
        assertEquals(expectedSpecimens, observation.specimens)
        assertEquals(1, observation.mooselikeMaleAmount)
        assertEquals(2, observation.mooselikeFemaleAmount)
        assertEquals(3, observation.mooselikeFemale1CalfAmount)
        assertEquals(4, observation.mooselikeFemale2CalfsAmount)
        assertEquals(5, observation.mooselikeFemale3CalfsAmount)
        assertEquals(6, observation.mooselikeFemale4CalfsAmount)
        assertEquals(7, observation.mooselikeCalfAmount)
        assertEquals(8, observation.mooselikeUnknownSpecimenAmount)

        assertEquals("Matti", observation.observerName)
        assertEquals("1234567", observation.observerPhoneNumber)
        assertEquals("More information", observation.officialAdditionalInfo)
        assertTrue(assertNotNull(observation.verifiedByCarnivoreAuthority))
        assertEquals(15, observation.inYardDistanceToResidence)
        assertTrue(assertNotNull(observation.litter))
        assertTrue(assertNotNull(observation.pack))
    }

    @Test
    fun testSendUpdatedEventToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = ObservationRepository(database)
        val backendApi = BackendAPIMock()
        val updater = observationToNetworkUpdater(backendApi, database)

        repository.upsertObservation(username, getObservation(remoteId = 999L, revision = 1L))
        val modifiedObservations = repository.getModifiedObservations(username)
        assertEquals(1, modifiedObservations.size)

        updater.update(username, modifiedObservations)
        assertEquals(1, backendApi.totalCallCount())
        assertEquals(1, backendApi.callCount(BackendAPI::updateObservation.name))
        val observation = backendApi.callParameter(BackendAPI::updateObservation.name) as ObservationDTO
        assertNotNull(observation)
        assertEquals(999L, observation.id)
        assertEquals(1L, observation.rev)
        assertEquals("OBSERVATION", observation.type)
        assertEquals(999, observation.mobileClientRefId)
        assertEquals(Constants.OBSERVATION_SPEC_VERSION, observation.observationSpecVersion)
        assertEquals(SpeciesCodes.MOOSE_ID, observation.gameSpeciesCode)
        assertEquals("NORMAL", observation.observationCategory)
        assertEquals("NAKO", observation.observationType)
        assertEquals("OTHER", observation.deerHuntingType)
        assertEquals("This is other type", observation.deerHuntingTypeDescription)
        assertEquals(12, observation.geoLocation.latitude)
        assertEquals(13, observation.geoLocation.longitude)
        assertEquals("MANUAL", observation.geoLocation.source)
        assertEquals("2022-01-01T13:14:15", observation.pointOfTime)
        assertEquals("This is an observation", observation.description)
        assertEquals(0, observation.imageIds.size)
        assertEquals(36, observation.totalSpecimenAmount)

        assertEquals(1, observation.specimens?.size)
        val expectedSpecimens = listOf(
            CommonObservationSpecimenDTO(
                id = null,
                rev = null,
                gender = "FEMALE",
                age = "ADULT",
                state = "HEALTHY",
                marking = "EARMARK",
                widthOfPaw = 2.2,
                lengthOfPaw = 6.5,
            )
        )
        assertEquals(expectedSpecimens, observation.specimens)

        assertEquals(1, observation.mooselikeMaleAmount)
        assertEquals(2, observation.mooselikeFemaleAmount)
        assertEquals(3, observation.mooselikeFemale1CalfAmount)
        assertEquals(4, observation.mooselikeFemale2CalfsAmount)
        assertEquals(5, observation.mooselikeFemale3CalfsAmount)
        assertEquals(6, observation.mooselikeFemale4CalfsAmount)
        assertEquals(7, observation.mooselikeCalfAmount)
        assertEquals(8, observation.mooselikeUnknownSpecimenAmount)

        assertEquals("Matti", observation.observerName)
        assertEquals("1234567", observation.observerPhoneNumber)
        assertEquals("More information", observation.officialAdditionalInfo)
        assertTrue(assertNotNull(observation.verifiedByCarnivoreAuthority))
        assertEquals(15, observation.inYardDistanceToResidence)
        assertTrue(assertNotNull(observation.litter))
        assertTrue(assertNotNull(observation.pack))
    }

    private fun getObservation(remoteId: Long? = null, revision: Long? = null): CommonObservation {
        return CommonObservation(
            localId = null,
            localUrl = null,
            remoteId = remoteId,
            revision = revision,
            mobileClientRefId = 999L,
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            observationCategory = ObservationCategory.NORMAL.toBackendEnum(),
            observationType = ObservationType.NAKO.toBackendEnum(),
            deerHuntingType = DeerHuntingType.OTHER.toBackendEnum(),
            deerHuntingOtherTypeDescription = "This is other type",
            location = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = fi.riista.common.model.GeoLocationSource.MANUAL.toBackendEnum()
            ),
            pointOfTime = LocalDateTime(2022, 1, 1, 13, 14, 15),
            description = "This is an observation",
            images = EntityImages.noImages(),
            totalSpecimenAmount = 36,
            specimens = listOf(
                CommonObservationSpecimen(
                    remoteId = null,
                    revision = null,
                    gender = Gender.FEMALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                    stateOfHealth = ObservationSpecimenState.HEALTHY.toBackendEnum(),
                    marking = ObservationSpecimenMarking.EARMARK.toBackendEnum(),
                    widthOfPaw = 2.2,
                    lengthOfPaw = 6.5,
                )
            ),
            canEdit = true,
            modified = true,
            deleted = false,
            mooselikeMaleAmount = 1,
            mooselikeFemaleAmount = 2,
            mooselikeFemale1CalfAmount = 3,
            mooselikeFemale2CalfsAmount = 4,
            mooselikeFemale3CalfsAmount = 5,
            mooselikeFemale4CalfsAmount = 6,
            mooselikeCalfAmount = 7,
            mooselikeUnknownSpecimenAmount = 8,
            observerName = "Matti",
            observerPhoneNumber = "1234567",
            officialAdditionalInfo = "More information",
            verifiedByCarnivoreAuthority = true,
            inYardDistanceToResidence = 15,
            litter = true,
            pack = true,
        )
    }

    private fun observationToNetworkUpdater(
        backendApi: BackendAPI,
        database: RiistaDatabase,
    ): ObservationToNetworkUpdater {
        return ObservationToNetworkUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
            database = database,
        )
    }
}
