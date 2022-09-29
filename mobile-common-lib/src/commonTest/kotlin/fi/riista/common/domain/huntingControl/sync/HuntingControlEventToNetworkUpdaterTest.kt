package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.model.*
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.huntingControl.MockHuntingControlData
import fi.riista.common.io.CommonFile
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.domain.model.*
import fi.riista.common.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HuntingControlEventToNetworkUpdaterTest {
    @Test
    fun testSendNewEventToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(
            driver = dbDriverFactory.createDriver(),
        )
        val repository = HuntingControlRepository(database)
        val rhyQueries = database.dbHuntingControlRhyQueries
        val eventQueries = database.dbHuntingControlEventQueries

        rhyQueries.insertRhy(
            username = username,
            remote_id = 1L,
            name_fi = "Test RHY",
            name_sv = null,
            name_en = null,
            official_code = "1234",
        )
        val createdEvent = repository.createHuntingControlEvent(
            username = username,
            event = getHuntingControlEventData()
        )

        val backendApi = BackendAPIMock()
        val updater = huntingControlEventToNetworkUpdater(database, backendApi)
        val modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        updater.update(modifiedEvents)

        assertEquals(1, backendApi.callCount(BackendAPI::createHuntingControlEvent.name))
        @Suppress("UNCHECKED_CAST")
        val pair =
            backendApi.callParameter(BackendAPI::createHuntingControlEvent.name) as Pair<Long, HuntingControlEventCreateDTO>
        assertEquals(1L, pair.first)
        val event = pair.second
        assertEquals(Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION, event.specVersion)
        assertEquals(1234L, event.mobileClientRefId)
        assertEquals(HuntingControlEventType.DOG_DISCIPLINE_CONTROL.name, event.eventType)
        assertEquals(HuntingControlEventStatus.PROPOSED.name, event.status)
        assertEquals(1, event.inspectors.size)
        val inspectorDTO = event.inspectors[0]
        assertEquals(888L, inspectorDTO.id)
        assertEquals("Byte", inspectorDTO.firstName)
        assertEquals("Vakt", inspectorDTO.lastName)
        assertEquals(listOf(HuntingControlCooperationType.OMA.name), event.cooperationTypes)
        assertEquals(false, event.wolfTerritory)
        assertEquals("Pena ja Turo", event.otherParticipants)
        assertEquals(7000, event.geoLocation.latitude)
        assertEquals(9000, event.geoLocation.longitude)
        assertEquals(GeoLocationSource.GPS_DEVICE.name, event.geoLocation.source)
        assertEquals(13.9, event.geoLocation.accuracy)
        assertEquals(19.4, event.geoLocation.altitude)
        assertEquals(12.23, event.geoLocation.altitudeAccuracy)
        assertEquals("Pyynikin tori", event.locationDescription)
        assertEquals("2022-01-03", event.date)
        assertEquals("10:12", event.beginTime)
        assertEquals("12:22", event.endTime)
        assertEquals(2, event.customers)
        assertEquals(1, event.proofOrders)
        assertEquals("Kuvausta", event.description)

        // modified must be false after it has been sent to backend and remoteId must be set
        val eventFromDbAfterUpdate = eventQueries.selectByLocalId(local_id = createdEvent.localId).executeAsOne()
        assertEquals(false, eventFromDbAfterUpdate.modified)
        assertEquals(1, eventFromDbAfterUpdate.remote_id)
    }

    @Test
    fun testSendExistingUpdatedEventToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(
            driver = dbDriverFactory.createDriver(),
        )
        val repository = HuntingControlRepository(database)
        val rhyQueries = database.dbHuntingControlRhyQueries
        val eventQueries = database.dbHuntingControlEventQueries

        rhyQueries.insertRhy(
            username = username,
            remote_id = 1L,
            name_fi = "Test RHY",
            name_sv = null,
            name_en = null,
            official_code = "1234",
        )
        val createdEvent = repository.createHuntingControlEvent(
            username = username,
            event = getHuntingControlEventData().copy(remoteId = 123L, rev = 3)
        )

        val backendApi = BackendAPIMock()
        val updater = huntingControlEventToNetworkUpdater(database, backendApi)
        val modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        updater.update(modifiedEvents)

        assertEquals(1, backendApi.callCount(BackendAPI::updateHuntingControlEvent.name))
        @Suppress("UNCHECKED_CAST")
        val pair =
            backendApi.callParameter(BackendAPI::updateHuntingControlEvent.name) as Pair<Long, HuntingControlEventDTO>
        assertEquals(1L, pair.first)
        val event = pair.second
        assertEquals(Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION, event.specVersion)
        assertEquals(3, event.rev)
        assertEquals(1234L, event.mobileClientRefId)
        assertEquals(HuntingControlEventType.DOG_DISCIPLINE_CONTROL.name, event.eventType)
        assertEquals(HuntingControlEventStatus.PROPOSED.name, event.status)
        assertEquals(1, event.inspectors.size)
        val inspectorDTO = event.inspectors[0]
        assertEquals(888L, inspectorDTO.id)
        assertEquals("Byte", inspectorDTO.firstName)
        assertEquals("Vakt", inspectorDTO.lastName)
        assertEquals(listOf(HuntingControlCooperationType.OMA.name), event.cooperationTypes)
        assertEquals(false, event.wolfTerritory)
        assertEquals("Pena ja Turo", event.otherParticipants)
        assertEquals(7000, event.geoLocation.latitude)
        assertEquals(9000, event.geoLocation.longitude)
        assertEquals(GeoLocationSource.GPS_DEVICE.name, event.geoLocation.source)
        assertEquals(13.9, event.geoLocation.accuracy)
        assertEquals(19.4, event.geoLocation.altitude)
        assertEquals(12.23, event.geoLocation.altitudeAccuracy)
        assertEquals("Pyynikin tori", event.locationDescription)
        assertEquals("2022-01-03", event.date)
        assertEquals("10:12", event.beginTime)
        assertEquals("12:22", event.endTime)
        assertEquals(2, event.customers)
        assertEquals(1, event.proofOrders)
        assertEquals("Kuvausta", event.description)

        // modified must be false after it has been sent to backend
        val eventFromDbAfterUpdate = eventQueries.selectByLocalId(local_id = createdEvent.localId).executeAsOne()
        assertEquals(false, eventFromDbAfterUpdate.modified)
    }

    @Test
    fun testSendNewAttachmentToBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(
            driver = dbDriverFactory.createDriver(),
        )
        val repository = HuntingControlRepository(database)
        val rhyQueries = database.dbHuntingControlRhyQueries

        val rhyId = 1L
        rhyQueries.insertRhy(
            username = username,
            remote_id = rhyId,
            name_fi = "Test RHY",
            name_sv = null,
            name_en = null,
            official_code = "1234",
        )

        repository.createHuntingControlEvent(
            username = username,
            event = getHuntingControlEventData().copy(attachments = listOf(getAttachment()))
        )

        val backendApi = BackendAPIMock()
        val updater = huntingControlEventToNetworkUpdater(database, backendApi)
        val modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        updater.update(modifiedEvents)

        assertEquals(1, backendApi.callCount(BackendAPI::uploadHuntingControlEventAttachment.name))
        val callParameters =
            backendApi.callParameter(BackendAPI::uploadHuntingControlEventAttachment.name)
                    as BackendAPIMock.UploadHuntingControlEventAttachmentCallParameters
        assertEquals(1L, callParameters.eventRemoteId)
        assertEquals("05e763fc-13e1-400c-ba86-bea02037b10c", callParameters.uuid)
        assertEquals("test.txt", callParameters.fileName)
        assertEquals("text/plain", callParameters.contentType)

        // Verify that attachment remote id has been saved to DB
        val events = repository.getHuntingControlEvents(username, rhyId)
        assertEquals(1, events.size)
        val attachment = events[0].attachments.firstOrNull { attachment -> attachment.uuid == "05e763fc-13e1-400c-ba86-bea02037b10c" }
        assertNotNull(attachment)
        assertEquals(MockHuntingControlData.UploadedAttachmentRemoteId, attachment.remoteId)
    }

    @Test
    fun deleteDeletedAttachmentFromBackend() = runBlocking {
        val username = "user"
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(
            driver = dbDriverFactory.createDriver(),
        )
        val repository = HuntingControlRepository(database)
        val rhyQueries = database.dbHuntingControlRhyQueries

        rhyQueries.insertRhy(
            username = username,
            remote_id = 1L,
            name_fi = "Test RHY",
            name_sv = null,
            name_en = null,
            official_code = "1234",
        )
        val createdEvent = repository.createHuntingControlEvent(
            username = username,
            event = getHuntingControlEventData().copy(
                remoteId = 1L,
                rev = 1,
                attachments = listOf(getAttachment().copy(remoteId = 1L))
            )
        )

        val deletedAttachment = createdEvent.attachments[0].copy(deleted = true)
        val updatedEvent = createdEvent.copy(attachments = listOf(deletedAttachment))
        repository.updateHuntingControlEvent(updatedEvent.toHuntingControlEventData())

        val backendApi = BackendAPIMock()
        val updater = huntingControlEventToNetworkUpdater(database, backendApi)
        val modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        updater.update(modifiedEvents)

        assertEquals(1, backendApi.callCount(BackendAPI::deleteHuntingControlEventAttachment.name))
        assertEquals(1L, backendApi.callParameter(BackendAPI::deleteHuntingControlEventAttachment.name))
    }

    private fun huntingControlEventToNetworkUpdater(
        database: RiistaDatabase,
        backendAPI: BackendAPI = BackendAPIMock(),
        commonFileProvider: CommonFileProvider = CommonFileProviderMock(commonFile),
    ): HuntingControlEventToNetworkUpdater {
        return HuntingControlEventToNetworkUpdater(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPI
            },
            database = database,
            commonFileProvider = commonFileProvider,
        )
    }

    private fun getHuntingControlEventData(): HuntingControlEventData {
        return HuntingControlEventData(
            specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
            mobileClientRefId = 1234L,
            rhyId = 1L,
            eventType = HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(),
            status = HuntingControlEventStatus.PROPOSED.toBackendEnum(),
            inspectors = listOf(
                HuntingControlEventInspector(
                    id = 888,
                    firstName = "Byte",
                    lastName = "Vakt",
                )
            ),
            cooperationTypes = listOf(
                HuntingControlCooperationType.OMA.toBackendEnum(),
            ),
            wolfTerritory = false,
            otherParticipants = "Pena ja Turo",
            location = ETRMSGeoLocation(
                latitude = 7000,
                longitude = 9000,
                source = GeoLocationSource.GPS_DEVICE.toBackendEnum(),
                accuracy = 13.9,
                altitude = 19.4,
                altitudeAccuracy = 12.23,
            ).asKnownLocation(),
            locationDescription = "Pyynikin tori",
            date = LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 3),
            startTime = LocalTime(hour = 10, minute = 12, second = 33),
            endTime = LocalTime(hour = 12, minute = 22, second = 2),
            customerCount = 2,
            proofOrderCount = 1,
            description = "Kuvausta",
            canEdit = false,
            modified = true,
            attachments = listOf(),
        )
    }

    private fun getAttachment(): HuntingControlAttachment {
        return HuntingControlAttachment(
            fileName = "test.txt",
            uuid = "05e763fc-13e1-400c-ba86-bea02037b10c",
            mimeType = "text/plain",
        )
    }

    private val commonFile = object : CommonFile {
        override val path: String
            get() = "/images/$fileUuid"

        override val fileUuid: String = "image.gif"

        override fun delete() {}
        override fun exists(): Boolean = true
        override fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers) {
            // nop
        }
    }
}
