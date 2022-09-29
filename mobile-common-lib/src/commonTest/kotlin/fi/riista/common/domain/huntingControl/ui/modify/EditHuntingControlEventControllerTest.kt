@file:Suppress("ComplexRedundantLet")

package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.helpers.*
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.HuntingControlRepository
import fi.riista.common.domain.huntingControl.model.HuntingControlCooperationType
import fi.riista.common.domain.huntingControl.model.HuntingControlEventInspector
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.model.HuntingControlEventType
import fi.riista.common.domain.huntingControl.sync.HuntingControlRhyToDatabaseUpdater
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.domain.huntingControl.sync.dto.toLoadRhyHuntingControlEvents
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.huntingControl.MockHuntingControlData
import fi.riista.common.io.CommonFileProvider
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

class EditHuntingControlEventControllerTest {

    private val serverAddress = "https://oma.riista.fi"

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(rhysAndEvents)
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        assertNotNull(controller.getLoadedViewModel().event)
    }

    @Test
    fun testSpecVersionIsUpdatedWhenEditingEvent() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        val x = listOf(rhysAndEvents[0].copy(events = listOf(rhysAndEvents[0].events[0].copy(specVersion = 0))))
        updater.update(x)
        var dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        assertEquals(0, dbEvents[0].specVersion)
        controller.loadViewModel()
        controller.saveHuntingControlEvent()
        dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)
        assertEquals(Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION, dbEvents[0].specVersion)
    }

    @Test
    fun testModifiedIsTrueAfterEditingEvent() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        val x = listOf(rhysAndEvents[0].copy(events = listOf(rhysAndEvents[0].events[0].copy(specVersion = 0))))
        updater.update(x)
        var dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        assertFalse(dbEvents[0].modified)
        controller.loadViewModel()
        controller.intEventDispatcher.dispatchIntChanged(HuntingControlEventField.Type.NUMBER_OF_INSPECTORS.toField(), 10)
        controller.saveHuntingControlEvent()
        dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)
        assertTrue(dbEvents[0].modified)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(rhysAndEvents)
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        controller.loadViewModel()
        val viewModel = controller.getLoadedViewModel()
        val fields = viewModel.fields
        assertEquals(19, fields.size)
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
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.LOCATION_DESCRIPTION.toField()).let {
            assertEquals("Pyynikin uimaranta", it.value)
            assertEquals("location_description", it.settings.label)
            assertFalse(it.settings.singleLine)
            assertFalse(it.settings.readOnly)
        }
        fields.getBooleanField(expectedIndex++, HuntingControlEventField.Type.WOLF_TERRITORY.toField()).let {
            assertEquals("wolf_territory", it.settings.label)
            assertFalse(it.value!!)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, HuntingControlEventField.Type.EVENT_TYPE.toField()).let {
            val eventTypes = listOf(
                StringWithId(
                    string = "event_type_mooselike",
                    id = HuntingControlEventType.MOOSELIKE_HUNTING_CONTROL.ordinal.toLong()
                ),
                StringWithId(
                    string = "event_type_large_carnivore",
                    id = HuntingControlEventType.LARGE_CARNIVORE_HUNTING_CONTROL.ordinal.toLong()
                ),
                StringWithId(
                    string = "event_type_grouse",
                    id = HuntingControlEventType.GROUSE_HUNTING_CONTROL.ordinal.toLong()
                ),
                StringWithId(
                    string = "event_type_waterfowl",
                    id = HuntingControlEventType.WATERFOWL_HUNTING_CONTROL.ordinal.toLong()
                ),
                StringWithId(
                    string = "event_type_dog_discipline",
                    id = HuntingControlEventType.DOG_DISCIPLINE_CONTROL.ordinal.toLong()
                ),
                StringWithId(
                    string = "event_type_other",
                    id = HuntingControlEventType.OTHER.ordinal.toLong()
                ),
            )
            assertEquals(eventTypes, it.values)
            assertEquals(
                listOf(HuntingControlEventType.DOG_DISCIPLINE_CONTROL.ordinal.toLong()),
                it.selected
            )
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.EVENT_DESCRIPTION.toField()).let {
            assertEquals(
                "Kuulemma uimarannalla pidettiin koiria vapaana. Käytiin katsomassa ettei vesilintuja häritty. Yksi masentunut ankka löytyi. Ks. liite.",
                it.value
            )
            assertEquals("event_description", it.settings.label)
            assertFalse(it.settings.singleLine)
            assertFalse(it.settings.readOnly)
        }
        fields.getDateField(expectedIndex++, HuntingControlEventField.Type.DATE.toField()).let {
            assertEquals(LocalDate(2022, 1, 13), it.date)
            assertEquals("date", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getTimespanField(expectedIndex++, HuntingControlEventField.Type.START_AND_END_TIME.toField()).let {
            assertEquals(LocalTime(11, 0, 0), it.startTime)
            assertEquals(LocalTime(12, 0, 0), it.endTime)
            assertEquals("start_time", it.settings.startLabel)
            assertEquals("end_time", it.settings.endLabel)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.DURATION.toField()).let {
            assertEquals("1 hour", it.value)
            assertEquals("duration", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, HuntingControlEventField.Type.INSPECTORS.toField()).let {
            val inspectors = listOf(
                StringWithId(string = "Pentti Mujunen", id = 4),
                StringWithId(string = "Asko Partanen", id = 3),
            )
            assertEquals(inspectors, it.values)
            assertEquals(listOf(4L, 3L).toSet(), it.selected?.toSet())
            assertFalse(it.settings.readOnly)
        }
        fields.getChipField(expectedIndex++, HuntingControlEventField.Type.INSPECTOR_NAMES.toField()).let {
            val inspectors = listOf(
                StringWithId(string = "Pentti Mujunen", id = 4),
                StringWithId(string = "Asko Partanen", id = 3),
            )
            assertEquals(inspectors, it.chips)
            assertEquals(ChipField.Mode.DELETE, it.settings.mode)
            assertNull(it.settings.label)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.NUMBER_OF_INSPECTORS.toField()).let {
            assertEquals("2", it.value)
            assertEquals("number_of_inspectors", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getChipField(expectedIndex++, HuntingControlEventField.Type.COOPERATION.toField()).let {
            val cooperation = listOf(
                StringWithId(string = "cooperation_type_poliisi", id = HuntingControlCooperationType.POLIISI.ordinal.toLong()),
                StringWithId(string = "cooperation_type_rajavartiosto", id = HuntingControlCooperationType.RAJAVARTIOSTO.ordinal.toLong()),
                StringWithId(string = "cooperation_type_mh", id = HuntingControlCooperationType.METSAHALLITUS.ordinal.toLong()),
                StringWithId(string = "cooperation_type_oma", id = HuntingControlCooperationType.OMA.ordinal.toLong()),
            )
            assertEquals(cooperation, it.chips)
            assertEquals(
                listOf(
                    HuntingControlCooperationType.POLIISI.ordinal.toLong(),
                    HuntingControlCooperationType.OMA.ordinal.toLong()
                ).toSet(), it.selectedIds?.toSet()
            )
            assertEquals("cooperation_type", it.settings.label)
            assertEquals(ChipField.Mode.TOGGLE, it.settings.mode)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, HuntingControlEventField.Type.OTHER_PARTICIPANTS.toField()).let {
            assertEquals("Poliisipartio", it.value)
            assertEquals("other_participants", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS.toField()).let {
            assertEquals(1, it.value)
            assertEquals("number_of_customers", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS.toField()).let {
            assertEquals(1, it.value)
            assertEquals("number_of_proof_orders", it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, HuntingControlEventField.Type.HEADLINE_ATTACHMENTS.toField()).let {
            assertEquals("attachments", it.text)
        }
        fields.getAttachmentField(expectedIndex++, HuntingControlEventField.Type.ATTACHMENT.toField(index = 0)).let {
            assertEquals("IMG_1387.jpg", it.filename)
            assertTrue(it.isImage)
            assertFalse(it.settings.readOnly)
        }
        fields.getAttachmentField(expectedIndex++, HuntingControlEventField.Type.ATTACHMENT.toField(index = 1)).let {
            assertEquals("__file.txt", it.filename)
            assertFalse(it.isImage)
            assertFalse(it.settings.readOnly)
        }
        fields.getButtonField(expectedIndex, HuntingControlEventField.Type.ADD_ATTACHMENT.toField()).let {
            assertEquals("add_attachment", it.text)
        }
    }

    @Test
    fun testErrorIsShownWhenNoInspectorsAvailable() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(
            listOf(rhysAndEvents[0].copy(events = listOf(rhysAndEvents[0].events[0].copy(date = LocalDate(2021, 12, 12)))))
        )
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        controller.loadViewModel()
        val viewModel = controller.getLoadedViewModel()
        val fields = viewModel.fields
        fields.getLabelField(9, HuntingControlEventField.Type.ERROR_NO_INSPECTORS_FOR_DATE.toField()).let {
            assertEquals("no_inspectors_for_selected_date", it.text)
        }
    }

    @Test
    fun testAttachmentCanBeDeleted() = runBlockingTest {
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
        updater.update(
            listOf(
                rhysAndEvents[0].copy(
                    events = listOf(
                        rhysAndEvents[0].events[0].copy(
                            date = LocalDate(
                                2021,
                                12,
                                12
                            )
                        )
                    )
                )
            )
        )
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        controller.loadViewModel()
        var viewModel = controller.getLoadedViewModel()
        assertFalse(viewModel.event.attachments[1].deleted)
        controller.attachmentActionEventDispatcher.dispatchEvent(HuntingControlEventField.Type.ATTACHMENT.toField(index = 1))
        viewModel = controller.getLoadedViewModel()
        assertTrue(viewModel.event.attachments[1].deleted)
    }

    @Test
    fun removeInspector() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(
            listOf(
                rhysAndEvents[0].copy(
                    events = listOf(
                        rhysAndEvents[0].events[0].copy(
                            inspectors = rhysAndEvents[0].events[0].inspectors + listOf(
                                HuntingControlEventInspector(
                                    id = 789L,
                                    firstName = "Teppo",
                                    lastName = "Tulkku",
                                )
                            )
                        )
                    )
                )
            )
        )
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        controller.loadViewModel()
        var viewModel = controller.getLoadedViewModel()

        assertEquals(3, viewModel.event.inspectors.size)
        val inspector1Id = viewModel.event.inspectors[0].id
        val inspector2Id = viewModel.event.inspectors[1].id
        val inspector3Id = viewModel.event.inspectors[2].id

        controller.stringWithIdClickEventDispatcher.dispatchStringWithIdClicked(
            fieldId = HuntingControlEventField.Type.INSPECTOR_NAMES.toField(0),
            value = StringWithId(id = inspector2Id, string = "")
        )

        viewModel = controller.getLoadedViewModel()
        assertEquals(2, viewModel.event.inspectors.size)
        assertEquals(inspector1Id, viewModel.event.inspectors[0].id)
        assertEquals(inspector3Id, viewModel.event.inspectors[1].id)
    }

    @Test
    fun toggleCooperationType() = runBlockingTest {
        // Insert Data to DB
        val username = MockUserInfo.PenttiUsername
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        val repository = HuntingControlRepository(database)

        val rhysAndEventsDTO = JsonHelper.deserializeFromJsonUnsafe<LoadRhysAndHuntingControlEventsDTO>(
            MockHuntingControlData.HuntingControlRhys
        )
        val rhysAndEvents = rhysAndEventsDTO.map { it.toLoadRhyHuntingControlEvents(logger) }
        val updater = HuntingControlRhyToDatabaseUpdater(database, username)
        updater.update(
            listOf(
                rhysAndEvents[0].copy(
                    events = listOf(
                        rhysAndEvents[0].events[0].copy(
                            cooperationTypes = listOf(
                                HuntingControlCooperationType.OMA.toBackendEnum(),
                                HuntingControlCooperationType.POLIISI.toBackendEnum(),
                                HuntingControlCooperationType.RAJAVARTIOSTO.toBackendEnum(),
                            )
                        )
                    )
                )
            )
        )
        val dbEvents = repository.getHuntingControlEvents(username, MockHuntingControlData.RhyId)

        val controller = getController(
            huntingControlContext = getHuntingControlContext(dbDriverFactory),
            huntingControlEventTarget = getHuntingControlEventTarget(eventId = dbEvents[0].localId),
        )

        controller.loadViewModel()
        var viewModel = controller.getLoadedViewModel()

        assertEquals(3, viewModel.event.cooperationTypes.size)
        val coop1 = viewModel.event.cooperationTypes[0].toLocalizedStringWithId(getStringProvider())
        val coop2 = viewModel.event.cooperationTypes[1].toLocalizedStringWithId(getStringProvider())
        val coop3 = viewModel.event.cooperationTypes[2].toLocalizedStringWithId(getStringProvider())

        controller.stringWithIdClickEventDispatcher.dispatchStringWithIdClicked(
            fieldId = HuntingControlEventField.Type.COOPERATION.toField(0),
            value = coop2,
        )

        viewModel = controller.getLoadedViewModel()
        assertEquals(2, viewModel.event.cooperationTypes.size)
        assertEquals(coop1, viewModel.event.cooperationTypes[0].toLocalizedStringWithId(getStringProvider()))
        assertEquals(coop3, viewModel.event.cooperationTypes[1].toLocalizedStringWithId(getStringProvider()))

        val coop4 = HuntingControlCooperationType.METSAHALLITUS.toLocalizedStringWithId(getStringProvider())
        controller.stringWithIdClickEventDispatcher.dispatchStringWithIdClicked(
            fieldId = HuntingControlEventField.Type.COOPERATION.toField(0),
            value = coop4,
        )

        viewModel = controller.getLoadedViewModel()
        assertEquals(3, viewModel.event.cooperationTypes.size)
        assertEquals(coop1, viewModel.event.cooperationTypes[0].toLocalizedStringWithId(getStringProvider()))
        assertEquals(coop3, viewModel.event.cooperationTypes[1].toLocalizedStringWithId(getStringProvider()))
        assertEquals(coop4, viewModel.event.cooperationTypes[2].toLocalizedStringWithId(getStringProvider()))
    }

    private fun getController(
        dbDriverFactory: DatabaseDriverFactory = createDatabaseDriverFactory(),
        huntingControlContext: HuntingControlContext = getHuntingControlContext(dbDriverFactory),
        huntingControlEventTarget: HuntingControlEventTarget = getHuntingControlEventTarget(),
        stringProvider: StringProvider = getStringProvider(),
        commonFileProvider: CommonFileProvider = CommonFileProviderMock()
    ) = EditHuntingControlEventController(
        huntingControlContext = huntingControlContext,
        huntingControlEventTarget = huntingControlEventTarget,
        stringProvider = stringProvider,
        commonFileProvider = commonFileProvider
    )

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
