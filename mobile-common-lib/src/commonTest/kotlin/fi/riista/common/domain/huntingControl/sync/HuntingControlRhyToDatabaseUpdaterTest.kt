package fi.riista.common.domain.huntingControl.sync

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.model.*
import fi.riista.common.domain.huntingControl.sync.model.GameWarden
import fi.riista.common.domain.huntingControl.sync.model.LoadHuntingControlEvent
import fi.riista.common.domain.model.*
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.*
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HuntingControlRhyToDatabaseUpdaterTest {

    @Test
    fun testDataInsertedToEmptyDatabase() {
        val username = MockUserInfo.PenttiUsername
        runBlocking {
            currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        assertEquals(0, repository.getRhys(username).size)

        val rhyEvents = SyncTestData.getRhyEvents()
        runBlocking {
            updater.update(listOf(rhyEvents))
        }

        val insertedRhys = repository.getRhys(username)
        assertEquals(1, insertedRhys.size)
        val rhy = insertedRhys[0]
        assertEquals(101, rhy.id)
        assertEquals("Test RHY fi", rhy.name.fi)
        assertEquals("Test RHY sv", rhy.name.sv)
        assertEquals("Test RHY en", rhy.name.en)
        assertEquals("1234", rhy.officialCode)

        val gameWardens = repository.getGameWardens(username, rhy.id)
        val expectedGameWardens = setOf(
            HuntingControlGameWarden(
                remoteId = 999,
                firstName = "Game",
                lastName = "Warden",
                startDate = LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 12),
                endDate = LocalDate(year = 2022, monthNumber = 12, dayOfMonth = 31),
            ),
            HuntingControlGameWarden(
                remoteId = 888,
                firstName = "Other",
                lastName = "Warden",
                startDate = null,
                endDate = null
            )
        )
        assertEquals(expectedGameWardens, gameWardens.toSet())

        val events = runBlocking {
            repository.getHuntingControlEvents(username, rhy.id)
        }
        assertEquals(1, events.size)
        val event = events[0]
        assertEquals(Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION, event.specVersion)
        assertEquals(123L, event.remoteId)
        assertEquals(1, event.rev)
        assertNull(event.mobileClientRefId)
        assertEquals(HuntingControlEventType.MOOSELIKE_HUNTING_CONTROL.toBackendEnum(), event.eventType)
        assertEquals(HuntingControlEventStatus.PROPOSED.toBackendEnum(), event.status)
        assertEquals(1, event.inspectors.size)
        val inspector = event.inspectors[0]
        assertEquals(999L, inspector.id)
        assertEquals("Game", inspector.firstName)
        assertEquals("Warden", inspector.lastName)
        assertEquals(listOf(HuntingControlCooperationType.METSAHALLITUS.toBackendEnum()), event.cooperationTypes)
        assertEquals(true, event.wolfTerritory)
        assertEquals("Pena", event.otherParticipants)
        assertEquals(6000, event.geoLocation.latitude)
        assertEquals(8000, event.geoLocation.longitude)
        assertEquals(GeoLocationSource.MANUAL.toBackendEnum(), event.geoLocation.source)
        assertEquals(12.9, event.geoLocation.accuracy)
        assertEquals(9.4, event.geoLocation.altitude)
        assertEquals(15.23, event.geoLocation.altitudeAccuracy)
        assertEquals("Pyynikki", event.locationDescription)
        assertEquals(LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 2), event.date)
        assertEquals(LocalTime(hour = 10, minute = 13, second = 0), event.startTime) // Seconds are not saved to DB
        assertEquals(LocalTime(hour = 12, minute = 21, second = 0), event.endTime) // Seconds are not saved to DB
        assertEquals(3, event.customerCount)
        assertEquals(2, event.proofOrderCount)
        assertEquals("Kuvausta", event.description)
        assertEquals(true, event.canEdit)
    }

    @Test
    fun testUpdateWithNoRhysRemovesDataFromDatabase() {
        val username = MockUserInfo.PenttiUsername
        runBlocking {
            currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        val rhyEvents = SyncTestData.getRhyEvents()
        runBlocking {
            updater.update(listOf(rhyEvents))
        }
        val insertedRhys = repository.getRhys(username)
        assertEquals(1, insertedRhys.size)
        val rhyId = insertedRhys[0].id

        runBlocking {
            updater.update(listOf())
        }

        val rhysAfterEmptyInsert = repository.getRhys(username)
        assertEquals(0, rhysAfterEmptyInsert.size)
        val events = runBlocking {
            repository.getHuntingControlEvents(username, rhyId)
        }
        assertEquals(0, events.size)
        val gameWardens = repository.getGameWardens(username, rhyId)
        assertEquals(0, gameWardens.size)
    }

    @Test
    fun testGameWardensAreUpdated() {
        val username = MockUserInfo.PenttiUsername
        runBlocking {
            currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        val rhyEvents = SyncTestData.getRhyEvents()
        runBlocking {
            updater.update(listOf(rhyEvents))
        }

        val rhyEventsWithUpdatedGameWardens = rhyEvents.copy(
            gameWardens = listOf(
                GameWarden(
                    inspector = HuntingControlEventInspector(
                        id = 999,
                        firstName = "Byte",
                        lastName = "Vakt",
                    ),
                    beginDate = LocalDate(
                        year = 2023,
                        monthNumber = 2,
                        dayOfMonth = 11,
                    ),
                    endDate = LocalDate(
                        year = 2023,
                        monthNumber = 3,
                        dayOfMonth = 12,
                    ),
                ),
                GameWarden(
                    inspector = HuntingControlEventInspector(
                        id = 1010,
                        firstName = "New",
                        lastName = "Warden",
                    ),
                    beginDate = LocalDate(
                        year = 2023,
                        monthNumber = 1,
                        dayOfMonth = 1,
                    ),
                    endDate = LocalDate(
                        year = 2023,
                        monthNumber = 12,
                        dayOfMonth = 12,
                    ),
                )
            )
        )

        runBlocking {
            updater.update(listOf(rhyEventsWithUpdatedGameWardens))
        }

        val gameWardens = repository.getGameWardens(username, rhyEvents.rhy.id)
        assertEquals(2, gameWardens.size)
        val gameWarden1 = gameWardens[0]
        assertEquals(999, gameWarden1.remoteId)
        assertEquals("Byte", gameWarden1.firstName)
        assertEquals("Vakt", gameWarden1.lastName)
        assertEquals(LocalDate(year = 2023, monthNumber = 2, dayOfMonth = 11), gameWarden1.startDate)
        assertEquals(LocalDate(year = 2023, monthNumber = 3, dayOfMonth = 12), gameWarden1.endDate)
        val gameWarden2 = gameWardens[1]
        assertEquals(1010, gameWarden2.remoteId)
        assertEquals("New", gameWarden2.firstName)
        assertEquals("Warden", gameWarden2.lastName)
        assertEquals(LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 1), gameWarden2.startDate)
        assertEquals(LocalDate(year = 2023, monthNumber = 12, dayOfMonth = 12), gameWarden2.endDate)
    }

    @Test
    fun testGameWardensNameChanged() = runBlockingTest {
        val username = MockUserInfo.PenttiUsername
        currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        val rhyAndGameWardens = SyncTestData.getRhyEvents().copy(events = listOf())
        val origGameWarden = rhyAndGameWardens.gameWardens[0]
        runBlocking {
            updater.update(listOf(rhyAndGameWardens))
        }

        // Add a local event
        repository.createHuntingControlEvent(
            username = username,
            event = getLocalEvent(rhyAndGameWardens.rhy.id, origGameWarden)
        )

        var modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        assertEquals("Game", modifiedEvents[0].inspectors[0].firstName)
        assertEquals("Warden", modifiedEvents[0].inspectors[0].lastName)

        // Change Game warden name
        val rhyEventsWithUpdatedGameWardens = rhyAndGameWardens.copy(
            gameWardens = listOf(
                GameWarden(
                    inspector = HuntingControlEventInspector(
                        id = origGameWarden.inspector.id,
                        firstName = "New firstname",
                        lastName = "New lastname",
                    ),
                    beginDate = origGameWarden.beginDate,
                    endDate = origGameWarden.endDate,
                )
            )
        )
        runBlocking {
            // Add RHY and game wardens
            updater.update(listOf(rhyEventsWithUpdatedGameWardens))
        }

        modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        assertEquals(1, modifiedEvents[0].inspectors.size)
        assertEquals("New firstname", modifiedEvents[0].inspectors[0].firstName)
        assertEquals("New lastname", modifiedEvents[0].inspectors[0].lastName)
    }

    @Test
    fun testGameWardenIsRemovedFromModifiedEvent() = runBlockingTest {
        val username = MockUserInfo.PenttiUsername
        currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        val rhyAndGameWardens = SyncTestData.getRhyEvents().copy(events = listOf())
        val origGameWarden = rhyAndGameWardens.gameWardens[0]
        runBlocking {
            updater.update(listOf(rhyAndGameWardens))
        }

        // Add a local event
        repository.createHuntingControlEvent(
            username = username,
            event = getLocalEvent(rhyAndGameWardens.rhy.id, origGameWarden)
        )

        // Change Game warden name
        val rhyEventsWithUpdatedGameWardens = rhyAndGameWardens.copy(
            gameWardens = listOf(
                GameWarden(
                    inspector = HuntingControlEventInspector(
                        id = 700L,
                        firstName = "Another",
                        lastName = "Warden",
                    ),
                    beginDate = origGameWarden.beginDate,
                    endDate = origGameWarden.endDate,
                )
            )
        )
        runBlocking {
            // Add RHY and game wardens
            updater.update(listOf(rhyEventsWithUpdatedGameWardens))
        }

        val modifiedEvents = repository.getModifiedHuntingControlEvents(username)
        assertEquals(1, modifiedEvents.size)
        assertEquals(0, modifiedEvents[0].inspectors.size)
    }

    @Test
    fun testEventIsUpdated() {
        val username = MockUserInfo.PenttiUsername
        runBlocking {
            currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        val rhyEvents = SyncTestData.getRhyEvents()
        runBlocking {
            updater.update(listOf(rhyEvents))
        }

        val rhyEventsWithUpdatedEvent = rhyEvents.copy(
            events = listOf(
                LoadHuntingControlEvent(
                    specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
                    id = 123L,
                    rev = 2,
                    mobileClientRefId = null,
                    eventType = HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(),
                    status = HuntingControlEventStatus.ACCEPTED.toBackendEnum(),
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
                    geoLocation = ETRMSGeoLocation(
                        latitude = 7000,
                        longitude = 9000,
                        source = GeoLocationSource.GPS_DEVICE.toBackendEnum(),
                        accuracy = 13.9,
                        altitude = 19.4,
                        altitudeAccuracy = 12.23,
                    ),
                    locationDescription = "Pyynikin tori",
                    date = LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 3),
                    beginTime = LocalTime(hour = 10, minute = 12, second = 33),
                    endTime = LocalTime(hour = 12, minute = 22, second = 2),
                    customers = 2,
                    proofOrders = 1,
                    description = "Kuvausta enemmän",
                    attachments = listOf(), // TODO: Add attachments
                    canEdit = false,
                )
            )
        )

        runBlocking {
            updater.update(listOf(rhyEventsWithUpdatedEvent))
        }

        val events = runBlocking {
            repository.getHuntingControlEvents(username, rhyEvents.rhy.id)
        }
        assertEquals(1, events.size)
        val event = events[0]
        assertEquals(Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION, event.specVersion)
        assertEquals(123L, event.remoteId)
        assertEquals(2, event.rev)
        assertNull(event.mobileClientRefId)
        assertEquals(HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(), event.eventType)
        assertEquals(HuntingControlEventStatus.ACCEPTED.toBackendEnum(), event.status)
        assertEquals(1, event.inspectors.size)
        val inspector = event.inspectors[0]
        assertEquals(888L, inspector.id)
        assertEquals("Byte", inspector.firstName)
        assertEquals("Vakt", inspector.lastName)
        assertEquals(listOf(HuntingControlCooperationType.OMA.toBackendEnum()), event.cooperationTypes)
        assertEquals(false, event.wolfTerritory)
        assertEquals("Pena ja Turo", event.otherParticipants)
        assertEquals(7000, event.geoLocation.latitude)
        assertEquals(9000, event.geoLocation.longitude)
        assertEquals(GeoLocationSource.GPS_DEVICE.toBackendEnum(), event.geoLocation.source)
        assertEquals(13.9, event.geoLocation.accuracy)
        assertEquals(19.4, event.geoLocation.altitude)
        assertEquals(12.23, event.geoLocation.altitudeAccuracy)
        assertEquals("Pyynikin tori", event.locationDescription)
        assertEquals(LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 3), event.date)
        assertEquals(LocalTime(hour = 10, minute = 12, second = 0), event.startTime) // Seconds are not saved to DB
        assertEquals(LocalTime(hour = 12, minute = 22, second = 0), event.endTime) // Seconds are not saved to DB
        assertEquals(2, event.customerCount)
        assertEquals(1, event.proofOrderCount)
        assertEquals("Kuvausta enemmän", event.description)
        assertEquals(false, event.canEdit)
    }

    @Test
    fun testUpdateWithAddedEvent() {
        val username = MockUserInfo.PenttiUsername
        runBlocking {
            currentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val updater = huntingControlRhyToDatabaseUpdater(database)
        val repository = HuntingControlRepository(database)

        val rhyEvents = SyncTestData.getRhyEvents()
        runBlocking {
            updater.update(listOf(rhyEvents))
        }

        val rhyEventsWithAddedEvent = rhyEvents.copy(
            events = listOf(
                LoadHuntingControlEvent(
                    specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
                    id = 456L,
                    rev = 1,
                    mobileClientRefId = null,
                    eventType = HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(),
                    status = HuntingControlEventStatus.ACCEPTED.toBackendEnum(),
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
                    geoLocation = ETRMSGeoLocation(
                        latitude = 7000,
                        longitude = 9000,
                        source = GeoLocationSource.GPS_DEVICE.toBackendEnum(),
                        accuracy = 13.9,
                        altitude = 19.4,
                        altitudeAccuracy = 12.23,
                    ),
                    locationDescription = "Pyynikin tori",
                    date = LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 3),
                    beginTime = LocalTime(hour = 10, minute = 12, second = 33),
                    endTime = LocalTime(hour = 12, minute = 22, second = 2),
                    customers = 2,
                    proofOrders = 1,
                    description = "Kuvausta enemmän",
                    attachments = listOf(), // TODO: Add attachments
                    canEdit = false,
                )
            )
        )
        runBlocking {
            updater.update(listOf(rhyEventsWithAddedEvent))
        }

        val events = runBlocking {
            repository.getHuntingControlEvents(username, rhyEvents.rhy.id)
        }
        assertEquals(2, events.size)

        val event = events.first { event -> event.remoteId == 456L }
        assertEquals(Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION, event.specVersion)
        assertEquals(456L, event.remoteId)
        assertEquals(1, event.rev)
        assertNull(event.mobileClientRefId)
        assertEquals(HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(), event.eventType)
        assertEquals(HuntingControlEventStatus.ACCEPTED.toBackendEnum(), event.status)
        assertEquals(1, event.inspectors.size)
        val inspector = event.inspectors[0]
        assertEquals(888L, inspector.id)
        assertEquals("Byte", inspector.firstName)
        assertEquals("Vakt", inspector.lastName)
        assertEquals(listOf(HuntingControlCooperationType.OMA.toBackendEnum()), event.cooperationTypes)
        assertEquals(false, event.wolfTerritory)
        assertEquals("Pena ja Turo", event.otherParticipants)
        assertEquals(7000, event.geoLocation.latitude)
        assertEquals(9000, event.geoLocation.longitude)
        assertEquals(GeoLocationSource.GPS_DEVICE.toBackendEnum(), event.geoLocation.source)
        assertEquals(13.9, event.geoLocation.accuracy)
        assertEquals(19.4, event.geoLocation.altitude)
        assertEquals(12.23, event.geoLocation.altitudeAccuracy)
        assertEquals("Pyynikin tori", event.locationDescription)
        assertEquals(LocalDate(year = 2022, monthNumber = 1, dayOfMonth = 3), event.date)
        assertEquals(LocalTime(hour = 10, minute = 12, second = 0), event.startTime) // Seconds are not saved to DB
        assertEquals(LocalTime(hour = 12, minute = 22, second = 0), event.endTime) // Seconds are not saved to DB
        assertEquals(2, event.customerCount)
        assertEquals(1, event.proofOrderCount)
        assertEquals("Kuvausta enemmän", event.description)
        assertEquals(false, event.canEdit)

    }

    private fun huntingControlRhyToDatabaseUpdater(
        database: RiistaDatabase,
    ): HuntingControlRhyToDatabaseUpdater {
        return HuntingControlRhyToDatabaseUpdater(
            database = database,
            currentUserContextProvider = currentUserContextProvider,
        )
    }

    private fun getLocalEvent(rhyId: OrganizationId, gameWarden: GameWarden): HuntingControlEventData {
        return HuntingControlEventData(
            specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
            rhyId = rhyId,
            mobileClientRefId = Random.nextLong(),
            eventType = HuntingControlEventType.DOG_DISCIPLINE_CONTROL.toBackendEnum(),
            status = HuntingControlEventStatus.ACCEPTED.toBackendEnum(),
            inspectors = listOf(
                HuntingControlEventInspector(
                    id = gameWarden.inspector.id,
                    firstName = gameWarden.inspector.firstName,
                    lastName = gameWarden.inspector.lastName,
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
            date = LocalDate(year = 2022, monthNumber = 2, dayOfMonth = 3),
            startTime = LocalTime(hour = 10, minute = 12, second = 33),
            endTime = LocalTime(hour = 12, minute = 22, second = 2),
            customerCount = 2,
            proofOrderCount = 1,
            description = "Kuvausta",
            canEdit = true,
            modified = true,
            attachments = listOf(),
        )
    }

    private val currentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked()
}
