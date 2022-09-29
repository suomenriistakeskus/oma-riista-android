package fi.riista.common.domain.huntingControl.ui.view

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.helpers.*
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.sync.HuntingControlRhyToDatabaseUpdater
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingControl.sync.dto.toLoadRhyHuntingControlEvents
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.huntingControl.MockHuntingControlData
import fi.riista.common.domain.huntingControl.ui.modify.EditHuntingControlEventControllerTest
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.logging.getLogger
import fi.riista.common.domain.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.ChipField
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.model.*
import fi.riista.common.util.JsonHelper
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

class ViewHuntingControlEventControllerTest {
    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testDataInitiallyNotLoaded() {
        val dbDriverFactory = createDatabaseDriverFactory()
        val controller = ViewHuntingControlEventController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(),
            stringProvider = getStringProvider()
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(rhysAndEvents)
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val huntingControlContext = getHuntingControlContext(dbDriverFactory)
        val controller = ViewHuntingControlEventController(
            huntingControlContext = huntingControlContext,
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        assertNotNull(controller.getLoadedViewModel().huntingControlEvent)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(rhysAndEvents)
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val huntingControlContext = getHuntingControlContext(dbDriverFactory)
        val controller = ViewHuntingControlEventController(
            huntingControlContext = huntingControlContext,
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()
        val fields = viewModel.fields
        assertEquals(17, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, HuntingControlEventField.Type.LOCATION.toField()).let {
            val location = ETRMSGeoLocation(
                latitude = 6822000,
                longitude = 326316,
                source = BackendEnum.create(GeoLocationSource.MANUAL),
                accuracy =  null,
                altitude = null,
                altitudeAccuracy = null,
            ).asKnownLocation()
            assertEquals(location, it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.LOCATION_DESCRIPTION.toField()).let {
            assertEquals("Pyynikin uimaranta", it.value)
            assertEquals("location_description", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.WOLF_TERRITORY.toField()).let {
            assertEquals("no", it.value)
            assertEquals("wolf_territory", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.EVENT_TYPE.toField()).let {
            assertEquals("event_type_dog_discipline", it.value)
            assertEquals("event_type", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.EVENT_DESCRIPTION.toField()).let {
            assertEquals("Kuulemma uimarannalla pidettiin koiria vapaana. Käytiin katsomassa ettei vesilintuja häritty. Yksi masentunut ankka löytyi. Ks. liite.", it.value)
            assertEquals("event_description", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getDateField(expectedIndex++, HuntingControlEventField.Type.DATE.toField()).let {
            assertEquals(LocalDate(2022, 1, 13), it.date)
            assertNull(it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getTimespanField(expectedIndex++, HuntingControlEventField.Type.START_AND_END_TIME.toField()).let {
            assertEquals(LocalTime(11, 0, 0), it.startTime)
            assertEquals(LocalTime(12, 0, 0), it.endTime)
            assertEquals("start_time", it.settings.startLabel)
            assertEquals("end_time", it.settings.endLabel)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.DURATION.toField()).let {
            assertEquals("1 hour", it.value)
            assertEquals("duration", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.INSPECTORS.toField()).let {
            assertEquals("Pentti Mujunen\nAsko Partanen", it.value)
            assertEquals("inspectors", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.NUMBER_OF_INSPECTORS.toField()).let {
            assertEquals("2", it.value)
            assertEquals("number_of_inspectors", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getChipField(expectedIndex++, HuntingControlEventField.Type.COOPERATION.toField()).let {
            val cooperation = listOf(
                HuntingControlCooperationType.POLIISI.toLocalizedStringWithId(getStringProvider()),
                HuntingControlCooperationType.OMA.toLocalizedStringWithId(getStringProvider()),
            )
            assertEquals(cooperation, it.chips)
            assertEquals("cooperation_type", it.settings.label)
            assertEquals(ChipField.Mode.VIEW, it.settings.mode)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.OTHER_PARTICIPANTS.toField()).let {
            assertEquals("Poliisipartio", it.value)
            assertEquals("other_participants", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS.toField()).let {
            assertEquals("1", it.value)
            assertEquals("number_of_customers", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS.toField()).let {
            assertEquals("1", it.value)
            assertEquals("number_of_proof_orders", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, HuntingControlEventField.Type.HEADLINE_ATTACHMENTS.toField()).let {
            assertEquals("attachments", it.text)
        }
        fields.getAttachmentField(expectedIndex++, HuntingControlEventField.Type.ATTACHMENT.toField(index = 0)).let {
            assertEquals("IMG_1387.jpg", it.filename)
            assertTrue(it.isImage)
        }
        fields.getAttachmentField(expectedIndex, HuntingControlEventField.Type.ATTACHMENT.toField(index = 1)).let {
            assertEquals("__file.txt", it.filename)
            assertFalse(it.isImage)
            assertTrue(it.settings.readOnly)
        }
    }

    private fun getHuntingControlContext(
        databaseDriverFactory: DatabaseDriverFactory,
        backendApi: BackendAPI = BackendAPIMock(),
    ): HuntingControlContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))

        val configuration = RiistaSdkConfiguration("1", "2", serverAddress)
        RiistaSDK.initializeMocked(
            sdkConfiguration = configuration,
            databaseDriverFactory = databaseDriverFactory,
            mockBackendAPI = backendApi,
            mockCurrentUserContextProvider = userContextProvider,
            mockLocalDateTimeProvider = MockDateTimeProvider(),
            mockMainScopeProvider = MockMainScopeProvider(),
            mockFileProvider = CommonFileProviderMock(),
        )

        return userContextProvider.userContext.huntingControlContext
    }

    private fun getHuntingControlEventTarget(
        eventId: Long = MockHuntingControlData.FirstEventId
    ): HuntingControlEventTarget {
        return HuntingControlEventTarget(
            rhyId = MockHuntingControlData.RhyId,
            eventId = eventId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
    private val logger by getLogger(EditHuntingControlEventControllerTest::class)
}

