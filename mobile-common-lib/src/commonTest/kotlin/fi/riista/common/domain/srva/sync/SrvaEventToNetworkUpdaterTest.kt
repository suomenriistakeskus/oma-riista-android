package fi.riista.common.domain.srva.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.SrvaEventRepository
import fi.riista.common.domain.srva.model.*
import fi.riista.common.domain.srva.sync.dto.SrvaEventCreateDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.domain.srva.sync.dto.SrvaMethodDTO
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

class SrvaEventToNetworkUpdaterTest {
    @Test
    fun testSendNewEventToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)
        val backendApi = BackendAPIMock()
        val updater = srvaEventToNetworkUpdater(backendApi, database)

        repository.upsertSrvaEvent(username, getSrvaEvent())
        val modifiedEvents = repository.getModifiedEvents(username)
        assertEquals(1, modifiedEvents.size)

        updater.update(username, modifiedEvents)
        assertEquals(1, backendApi.totalCallCount())
        assertEquals(1, backendApi.callCount(BackendAPI::createSrvaEvent.name))
        val event = backendApi.callParameter(BackendAPI::createSrvaEvent.name) as SrvaEventCreateDTO
        assertNotNull(event)
        assertEquals("SRVA", event.type)
        assertEquals(33L, event.mobileClientRefId)
        assertEquals(Constants.SRVA_SPEC_VERSION, event.srvaEventSpecVersion)
        assertEquals("UNFINISHED", event.state)
        assertEquals(12, event.rhyId)
        assertEquals(12, event.geoLocation.latitude)
        assertEquals(13, event.geoLocation.longitude)
        assertEquals("MANUAL", event.geoLocation.source)
        assertEquals("2022-01-01T13:14:15", event.pointOfTime)
        assertEquals(13, event.authorInfo?.id)
        assertEquals(14, event.authorInfo?.rev)
        assertEquals("Pena", event.authorInfo?.byName)
        assertEquals("Mujunen", event.authorInfo?.lastName)
        assertEquals("Asko", event.approverInfo?.firstName)
        assertEquals("Partanen", event.approverInfo?.lastName)
        assertEquals(SpeciesCodes.MOOSE_ID, event.gameSpeciesCode)
        assertEquals(1, event.specimens?.size)
        val specimen = event.specimens?.get(0)
        assertEquals("ADULT", specimen?.age)
        assertEquals("MALE", specimen?.gender)
        assertEquals("DEPORTATION", event.eventName)
        assertEquals("OTHER", event.eventType)
        assertEquals("123456", event.deportationOrderNumber)
        assertEquals("Other event", event.otherTypeDescription)
        assertEquals("OTHER", event.eventTypeDetail)
        assertEquals("Other detail", event.otherEventTypeDetailDescription)
        assertEquals("ANIMAL_NOT_FOUND", event.eventResult)
        assertEquals("UNCERTAIN_RESULT", event.eventResultDetail)
        assertEquals(3, event.methods.size)
        val expectedMethods = setOf(
            SrvaMethodDTO(name = "TRACED_WITH_DOG", isChecked = true),
            SrvaMethodDTO(name = "SOUND_EQUIPMENT", isChecked = false),
            SrvaMethodDTO(name = "OTHER", isChecked = true),
        )
        assertEquals(expectedMethods, event.methods.toSet())
        assertEquals("Other method", event.otherMethodDescription)
        assertEquals(2, event.personCount)
        assertEquals(3, event.timeSpent)
        assertEquals("Käytiin etsimässä, ei löytynyt", event.description)
        assertEquals(0, event.imageIds.size)
    }

    @Test
    fun testSendUpdatedEventToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = SrvaEventRepository(database)
        val backendApi = BackendAPIMock()
        val updater = srvaEventToNetworkUpdater(backendApi, database)

        repository.upsertSrvaEvent(username, getSrvaEvent(remoteId = 999L, revision = 1L))
        val modifiedEvents = repository.getModifiedEvents(username)
        assertEquals(1, modifiedEvents.size)

        updater.update(username, modifiedEvents)
        assertEquals(1, backendApi.totalCallCount())
        assertEquals(1, backendApi.callCount(BackendAPI::updateSrvaEvent.name))
        val event = backendApi.callParameter(BackendAPI::updateSrvaEvent.name) as SrvaEventDTO
        assertNotNull(event)
        assertEquals(999L, event.id)
        assertEquals(1L, event.rev)
        assertEquals("SRVA", event.type)
        assertEquals(33L, event.mobileClientRefId)
        assertEquals(Constants.SRVA_SPEC_VERSION, event.srvaEventSpecVersion)
        assertEquals("UNFINISHED", event.state)
        assertEquals(12, event.rhyId)
        assertEquals(12, event.geoLocation.latitude)
        assertEquals(13, event.geoLocation.longitude)
        assertEquals("MANUAL", event.geoLocation.source)
        assertEquals("2022-01-01T13:14:15", event.pointOfTime)
        assertEquals(13, event.authorInfo?.id)
        assertEquals(14, event.authorInfo?.rev)
        assertEquals("Pena", event.authorInfo?.byName)
        assertEquals("Mujunen", event.authorInfo?.lastName)
        assertEquals("Asko", event.approverInfo?.firstName)
        assertEquals("Partanen", event.approverInfo?.lastName)
        assertEquals(SpeciesCodes.MOOSE_ID, event.gameSpeciesCode)
        assertEquals(1, event.specimens?.size)
        val specimen = event.specimens?.get(0)
        assertEquals("ADULT", specimen?.age)
        assertEquals("MALE", specimen?.gender)
        assertEquals("DEPORTATION", event.eventName)
        assertEquals("OTHER", event.eventType)
        assertEquals("123456", event.deportationOrderNumber)
        assertEquals("Other event", event.otherTypeDescription)
        assertEquals("OTHER", event.eventTypeDetail)
        assertEquals("Other detail", event.otherEventTypeDetailDescription)
        assertEquals("ANIMAL_NOT_FOUND", event.eventResult)
        assertEquals("UNCERTAIN_RESULT", event.eventResultDetail)
        assertEquals(3, event.methods.size)
        val expectedMethods = setOf(
            SrvaMethodDTO(name = "TRACED_WITH_DOG", isChecked = true),
            SrvaMethodDTO(name = "SOUND_EQUIPMENT", isChecked = false),
            SrvaMethodDTO(name = "OTHER", isChecked = true),
        )
        assertEquals(expectedMethods, event.methods.toSet())
        assertEquals("Other method", event.otherMethodDescription)
        assertEquals(2, event.personCount)
        assertEquals(3, event.timeSpent)
        assertEquals("Käytiin etsimässä, ei löytynyt", event.description)
        assertEquals(0, event.imageIds.size)
    }

    private fun getSrvaEvent(remoteId: Long? = null, revision: Long? = null): CommonSrvaEvent {
        return CommonSrvaEvent(
            localId = null,
            localUrl = null,
            remoteId = remoteId,
            revision = revision,
            mobileClientRefId = 33L,
            srvaSpecVersion = Constants.SRVA_SPEC_VERSION,
            state = SrvaEventState.UNFINISHED.toBackendEnum(),
            rhyId = 12,
            canEdit = true,
            modified = true,
            deleted = false,
            location = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = fi.riista.common.model.GeoLocationSource.MANUAL.toBackendEnum()
            ),
            pointOfTime = LocalDateTime(2022, 1, 1, 13, 14, 15),
            author = CommonSrvaEventAuthor(
                id = 13,
                revision = 14,
                byName = "Pena",
                lastName = "Mujunen"
            ),
            approver = CommonSrvaEventApprover(
                firstName = "Asko",
                lastName = "Partanen"
            ),
            species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            otherSpeciesDescription = null,
            totalSpecimenAmount = 1,
            specimens = listOf(
                CommonSrvaSpecimen(
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                )
            ),
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            deportationOrderNumber = "123456",
            eventType = SrvaEventType.OTHER.toBackendEnum(),
            otherEventTypeDescription = "Other event",
            eventTypeDetail = SrvaEventTypeDetail.OTHER.toBackendEnum(),
            otherEventTypeDetailDescription = "Other detail",
            eventResult = SrvaEventResult.ANIMAL_NOT_FOUND.toBackendEnum(),
            eventResultDetail = SrvaEventResultDetail.UNCERTAIN_RESULT.toBackendEnum(),
            methods = listOf(
                SrvaMethodType.TRACED_WITH_DOG.toBackendEnum().toCommonSrvaMethod(selected = true),
                SrvaMethodType.SOUND_EQUIPMENT.toBackendEnum().toCommonSrvaMethod(selected = false),
                SrvaMethodType.OTHER.toBackendEnum().toCommonSrvaMethod(selected = true),
            ),
            otherMethodDescription = "Other method",
            personCount = 2,
            hoursSpent = 3,
            description = "Käytiin etsimässä, ei löytynyt",
            images = EntityImages.noImages(),
        )
    }

    private fun srvaEventToNetworkUpdater(
        backendApi: BackendAPI,
        database: RiistaDatabase,
    ): SrvaEventToNetworkUpdater {
        return SrvaEventToNetworkUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendApi
            },
            database = database,
        )
    }
}
