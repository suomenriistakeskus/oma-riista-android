package fi.riista.common.domain.groupHunting.huntingDays.modify

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingDayUpdateResponse
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayTarget
import fi.riista.common.domain.groupHunting.model.GroupHuntingMethodType
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.EditGroupHuntingDayController
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.GroupHuntingDayField
import fi.riista.common.helpers.*
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.model.*
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

class EditGroupHuntingDayControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = createController()

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testHuntingDayIsLoadedCorrectly() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = controller.getLoadedViewModel()
        assertEquals(MockGroupHuntingData.FirstHuntingDayId, viewModel.huntingDay.id)
        assertEquals(MockGroupHuntingData.FirstHuntingGroupId, viewModel.huntingDay.huntingGroupId)
        assertEquals(LocalDateTime(2015, 9, 1, 6, 0, 0), viewModel.huntingDay.startDateTime)
        assertEquals(LocalDateTime(2015, 9, 1, 21, 0, 0), viewModel.huntingDay.endDateTime)
        assertEquals(900, viewModel.huntingDay.durationInMinutes)
        assertEquals(900, viewModel.huntingDay.activeHuntingDurationInMinutes)
        assertEquals(0, viewModel.huntingDay.breakDurationInMinutes)
        assertEquals(123, viewModel.huntingDay.snowDepth)
        assertEquals(
            GroupHuntingMethodType.PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA,
            viewModel.huntingDay.huntingMethod.value
        )
        assertEquals(23, viewModel.huntingDay.numberOfHunters)
        assertEquals(1, viewModel.huntingDay.numberOfHounds)
        assertFalse(viewModel.huntingDay.createdBySystem)
    }

    @Test
    fun testProducedDataFieldsMatchHuntingDay() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(7, fields.size)
        var expectedIndex = 0
        fields.getDateTimeField(expectedIndex++, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 1, 6, 0, 0), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.readOnlyDate)
            assertFalse(it.settings.readOnlyTime)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getDateTimeField(expectedIndex++, GroupHuntingDayField.END_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 1, 21, 0, 0), it.dateAndTime)
            assertEquals("end_date_and_time", it.settings.label)
            assertFalse(it.settings.readOnly)
            assertFalse(it.settings.readOnlyDate)
            assertFalse(it.settings.readOnlyTime)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getIntField(expectedIndex++, GroupHuntingDayField.NUMBER_OF_HUNTERS).let {
            assertEquals(23, it.value)
            assertEquals("number_of_hunters", it.settings.label)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getStringListField(expectedIndex++, GroupHuntingDayField.HUNTING_METHOD).let {
            assertEquals(listOf(0L), it.selected) // enum ordinal, PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA
            assertEquals("hunting_method", it.settings.label)
            assertEquals(GroupHuntingMethodType.values().size, it.values.size)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)

            val stringProvider = getStringProvider()
            GroupHuntingMethodType.values().forEach { huntingMethod ->
                assertEquals(
                    stringProvider.getString(huntingMethod.resourcesStringId),
                    it.values[huntingMethod.ordinal].string
                )
            }
        }
        fields.getIntField(expectedIndex++, GroupHuntingDayField.NUMBER_OF_HOUNDS).let {
            assertEquals(1, it.value)
            assertEquals("number_of_hounds", it.settings.label)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getIntField(expectedIndex++, GroupHuntingDayField.SNOW_DEPTH).let {
            assertEquals(123, it.value)
            assertEquals("snow_depth_centimeters", it.settings.label)
            assertEquals(FieldRequirement.voluntary(), it.settings.requirementStatus)
        }
        fields.getSelectDurationField(expectedIndex, GroupHuntingDayField.BREAK_DURATION).let {
            assertEquals(HoursAndMinutes(0, 0), it.value)
            assertEquals("break_duration_minutes", it.settings.label)
            assertEquals(FieldRequirement.voluntary(), it.settings.requirementStatus)

            val dayDurationInMinutes = 900
            val fullHours = (dayDurationInMinutes - 1).div(60)
            val remainingMinutes = (dayDurationInMinutes - 1).rem(60)
            assertEquals(30, it.possibleValues.size)
            for (hour in 0..fullHours) {
                assertTrue(
                    it.possibleValues.contains(HoursAndMinutes(hour, 0)),
                    "HoursAndMinutes($hour, 0) should've been possible!"
                )
                if (hour != fullHours || remainingMinutes >= 30) {
                    assertTrue(
                        it.possibleValues.contains(HoursAndMinutes(hour, 30)),
                        "HoursAndMinutes($hour, 30) should've been possible!"
                    )
                }
            }
        }
    }

    @Test
    fun testProducedDataFieldsMatchHuntingDayForDeer() = runBlockingTest {
        val backendApi = BackendAPIMock(
            groupHuntingGroupHuntingDaysResponse = MockResponse.success(
                """
                            [
                                ${MockGroupHuntingData.FirstHuntingDay},
                                ${MockGroupHuntingData.SecondHuntingDay},
                                ${MockGroupHuntingData.DeerHuntingDay}
                            ]
                        """
            )
        )
        val dayTarget = GroupHuntingDayTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = MockGroupHuntingData.SecondHuntingGroupId,
            huntingDayId = MockGroupHuntingData.DeerHuntingDayId,
        )
        val controller = createController(
            groupHuntingContext = getGroupHuntingContext(backendApi),
            huntingDayTarget = dayTarget
        )
        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()

        val fields = viewModel.fields
        assertEquals(2, fields.size)
        var expectedIndex = 0
        fields.getDateTimeField(expectedIndex++, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 3, 6, 0, 0), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.readOnlyDate)
            assertFalse(it.settings.readOnlyTime)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getDateTimeField(expectedIndex, GroupHuntingDayField.END_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 3, 21, 0, 0), it.dateAndTime)
            assertEquals("end_date_and_time", it.settings.label)
            assertFalse(it.settings.readOnly)
            assertFalse(it.settings.readOnlyDate)
            assertFalse(it.settings.readOnlyTime)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
    }

    @Test
    fun testStartDateCannotBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = LocalDateTime(2015, 10, 1, 6, 0, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 9, 1, 6, 0, 0),
            viewModel.huntingDay.startDateTime
        )
        viewModel.fields.getDateTimeField(0, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 1, 6, 0, 0), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
        }
    }

    @Test
    fun testStartTimeCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = LocalDateTime(2015, 10, 1, 8, 0, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 9, 1, 8, 0, 0),
            viewModel.huntingDay.startDateTime
        )
        viewModel.fields.getDateTimeField(0, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 1, 8, 0, 0), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
        }
    }

    @Test
    fun testEndDateCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 4, 8, 0, 0)
        )
        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 4, 19, 0, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 9, 4, 19, 0, 0),
            viewModel.huntingDay.endDateTime
        )
        viewModel.fields.getDateTimeField(1, GroupHuntingDayField.END_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 4, 19, 0, 0), it.dateAndTime)
            assertEquals("end_date_and_time", it.settings.label)
        }
    }

    @Test
    fun testChangingEndDateOutsideOfPermitIsRestricted() = runBlockingTest {
        val controller = createController(now = mockedNow.changeDate(LocalDate(2015, 11, 1)))

        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 11, 1, 19, 0, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 10, 31, 23, 59, 0),
            viewModel.huntingDay.endDateTime
        )
        viewModel.fields.getDateTimeField(1, GroupHuntingDayField.END_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 10, 31, 23, 59, 0), it.dateAndTime)
            assertEquals("end_date_and_time", it.settings.label)
            assertEquals(it.settings.maxDateTime, it.dateAndTime, "should equal max")
        }
    }

    @Test
    fun testChangingEndDateAffectsPossibleBreakDurations() = runBlockingTest {
        val controller = createController(now = mockedNow.changeDate(LocalDate(2015, 11, 1)))

        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 1, 6, 0, 0)
        )

        // should have only 'no breaks' option
        with(controller.getLoadedViewModel()) {
            assertEquals(0, huntingDay.durationInMinutes)
            fields.getSelectDurationField(6, GroupHuntingDayField.BREAK_DURATION).let {
                assertEquals(HoursAndMinutes(0, 0), it.value, "duration == 0")
                assertEquals(1, it.possibleValues.size, "duration == 0")
                assertEquals(0, it.possibleValues[0].toTotalMinutes(), "duration == 0")
            }
        }

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 1, 6, 30, 0)
        )

        // should have only 'no breaks' option
        with(controller.getLoadedViewModel()) {
            assertEquals(30, huntingDay.durationInMinutes)
            fields.getSelectDurationField(6, GroupHuntingDayField.BREAK_DURATION).let {
                assertEquals(HoursAndMinutes(0, 0), it.value, "duration == 30")
                assertEquals(1, it.possibleValues.size, "duration == 30")
                assertEquals(0, it.possibleValues[0].toTotalMinutes(), "duration == 30")
            }
        }

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 1, 6, 31, 0)
        )

        // should have 'no breaks' option + 30 minutes option
        with(controller.getLoadedViewModel()) {
            assertEquals(31, huntingDay.durationInMinutes)
            fields.getSelectDurationField(6, GroupHuntingDayField.BREAK_DURATION).let {
                assertEquals(HoursAndMinutes(0, 0), it.value, "duration == 31")
                assertEquals(2, it.possibleValues.size, "duration == 31")
                assertEquals(0, it.possibleValues[0].toTotalMinutes(), "duration == 31")
                assertEquals(30, it.possibleValues[1].toTotalMinutes(), "duration == 31")
            }
        }
    }

    @Test
    fun testBreakDurationAffectsChangingEndDate() = runBlockingTest {
        val controller = createController()

        controller.loadViewModel()

        controller.eventDispatchers.durationEventDispatcher.dispatchHoursAndMinutesChanged(
            fieldId = GroupHuntingDayField.BREAK_DURATION,
            value = HoursAndMinutes(2, 0)
        )
        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 1, 7, 0, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 9, 1, 8, 0, 0),
            viewModel.huntingDay.endDateTime
        )
        viewModel.fields.getDateTimeField(1, GroupHuntingDayField.END_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 1, 8, 0, 0), it.dateAndTime)
            assertEquals("end_date_and_time", it.settings.label)
            assertEquals(it.settings.minDateTime, it.dateAndTime, "should equal min")
        }
    }

    @Test
    fun testNumberOfHunterCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.NUMBER_OF_HUNTERS,
            value = 7
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(7, viewModel.huntingDay.numberOfHunters)
        viewModel.fields.getIntField(2, GroupHuntingDayField.NUMBER_OF_HUNTERS).let {
            assertEquals(7, it.value)
            assertEquals("number_of_hunters", it.settings.label)
        }
    }

    @Test
    fun testHuntingMethodCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.stringWithIdDispatcher.dispatchStringWithIdChanged(
            fieldId = GroupHuntingDayField.HUNTING_METHOD,
            value = listOf(StringWithId("hiipiminen_pysayttavalle_koiralle", 1))
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            GroupHuntingMethodType.HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE,
            viewModel.huntingDay.huntingMethod.value
        )
        viewModel.fields.getStringListField(3, GroupHuntingDayField.HUNTING_METHOD).let {
            assertEquals(listOf(1L), it.selected)
            assertEquals("hunting_method", it.settings.label)
        }
    }

    @Test
    fun testChangingHuntingMethodCanHideNumberOfHounds() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        assertNotNull(controller.getLoadedViewModel().fields.find {
            it.id == GroupHuntingDayField.NUMBER_OF_HOUNDS
        })

        controller.eventDispatchers.stringWithIdDispatcher.dispatchStringWithIdChanged(
            fieldId = GroupHuntingDayField.HUNTING_METHOD,
            value = listOf(StringWithId("passilinja_ja_tiivis_ajoketju", 2))
        )

        assertNull(controller.getLoadedViewModel().fields.find {
            it.id == GroupHuntingDayField.NUMBER_OF_HOUNDS
        })
    }

    @Test
    fun testNumberOfHoundsCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.NUMBER_OF_HOUNDS,
            value = 8
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(8, viewModel.huntingDay.numberOfHounds)
        viewModel.fields.getIntField(4, GroupHuntingDayField.NUMBER_OF_HOUNDS).let {
            assertEquals(8, it.value)
            assertEquals("number_of_hounds", it.settings.label)
        }
    }

    @Test
    fun testSnowDepthCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.SNOW_DEPTH,
            value = 70
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(70, viewModel.huntingDay.snowDepth)
        viewModel.fields.getIntField(5, GroupHuntingDayField.SNOW_DEPTH).let {
            assertEquals(70, it.value)
            assertEquals("snow_depth_centimeters", it.settings.label)
        }
    }

    @Test
    fun testBreakDurationCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.durationEventDispatcher.dispatchHoursAndMinutesChanged(
            fieldId = GroupHuntingDayField.BREAK_DURATION,
            value = HoursAndMinutes(hours = 1, minutes = 30)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(90, viewModel.huntingDay.breakDurationInMinutes)
        viewModel.fields.getSelectDurationField(6, GroupHuntingDayField.BREAK_DURATION).let {
            assertEquals(HoursAndMinutes(hours = 1, minutes = 30), it.value)
            assertEquals("break_duration_minutes", it.settings.label)
        }
    }

    @Test
    fun testSavingHuntingDayFetchesNewValuesFromBackend() = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val groupHuntingContext = getGroupHuntingContext(backendAPI)
        val controller = createController(groupHuntingContext)
        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 1, 7, 0, 0)
        )
        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 1, 20, 0, 0)
        )
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.NUMBER_OF_HUNTERS,
            value = 20
        )
        controller.eventDispatchers.stringWithIdDispatcher.dispatchStringWithIdChanged(
            fieldId = GroupHuntingDayField.HUNTING_METHOD,
            value = listOf(StringWithId("hiipiminen_pysayttavalle_koiralle", 1))
        )
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.NUMBER_OF_HOUNDS,
            value = 2
        )
        controller.eventDispatchers.durationEventDispatcher.dispatchHoursAndMinutesChanged(
            fieldId = GroupHuntingDayField.BREAK_DURATION,
            value = HoursAndMinutes(0, 30)
        )
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.SNOW_DEPTH,
            value = 120
        )

        backendAPI.groupHuntingGroupHuntingDaysResponse =
            MockResponse.success(MockGroupHuntingData.GroupHuntingDaysAfterUpdate)

        assertTrue(
            controller.saveHuntingDay() is GroupHuntingDayUpdateResponse.Updated,
            "save should succeed"
        )

        // ensure the controller hunting day as well as provider hunting day have been updated
        val huntingDayTarget = getHuntingDayTarget()
        val groupContext = groupHuntingContext.findHuntingGroupContext(huntingDayTarget)
        assertNotNull(groupContext, "group context should exist")

        val providerHuntingDay = groupContext.findHuntingDay(huntingDayTarget)
        assertNotNull(providerHuntingDay, "Hunting day should exist")

        val controllerHuntingDay = controller.getLoadedViewModel().huntingDay

        listOf(providerHuntingDay, controllerHuntingDay).forEach { huntingDay ->
            assertEquals(MockGroupHuntingData.FirstHuntingDayId, huntingDay.id)
            assertEquals(MockGroupHuntingData.FirstHuntingGroupId, huntingDay.huntingGroupId)
            assertEquals(LocalDateTime(2015, 9, 1, 7, 0, 0), huntingDay.startDateTime)
            assertEquals(LocalDateTime(2015, 9, 1, 20, 0, 0), huntingDay.endDateTime)
            assertEquals(780, huntingDay.durationInMinutes)
            assertEquals(750, huntingDay.activeHuntingDurationInMinutes)
            assertEquals(30, huntingDay.breakDurationInMinutes)
            assertEquals(120, huntingDay.snowDepth)
            assertEquals(GroupHuntingMethodType.HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE, huntingDay.huntingMethod.value)
            assertEquals(20, huntingDay.numberOfHunters)
            assertEquals(2, huntingDay.numberOfHounds)
            assertFalse(huntingDay.createdBySystem)
        }
    }

    private fun createController(
        now: LocalDateTime = mockedNow,
        huntingDayTarget: GroupHuntingDayTarget = getHuntingDayTarget(),
    ): EditGroupHuntingDayController {
        return createController(getGroupHuntingContext(BackendAPIMock()), now, huntingDayTarget)
    }

    private fun createController(
        groupHuntingContext: GroupHuntingContext,
        now: LocalDateTime = mockedNow,
        huntingDayTarget: GroupHuntingDayTarget = getHuntingDayTarget()
    ): EditGroupHuntingDayController {
        return EditGroupHuntingDayController(
            groupHuntingContext = groupHuntingContext,
            huntingDayTarget = huntingDayTarget,
            stringProvider = getStringProvider(),
            currentTimeProvider = MockDateTimeProvider(now),
        )
    }

    private fun getGroupHuntingContext(backendAPI: BackendAPIMock): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = true,
            backendAPI = backendAPI
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getHuntingDayTarget(): GroupHuntingDayTarget {
        return GroupHuntingDayTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = MockGroupHuntingData.FirstHuntingGroupId,
            huntingDayId = MockGroupHuntingData.FirstHuntingDayId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

    companion object {
        private val mockedNow = LocalDateTime(2015, 9, 5, 13, 45, 0)
    }
}
