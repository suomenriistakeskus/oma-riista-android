package fi.riista.common.domain.groupHunting.huntingDays.modify

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingDayUpdateResponse
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.CreateGroupHuntingDayController
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

class CreateGroupHuntingDayControllerTest {

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
        assertEquals(GroupHuntingDayId.local(expectedStartTime.date), viewModel.huntingDay.id)
        assertNull(viewModel.huntingDay.rev)
        assertEquals(MockGroupHuntingData.FirstHuntingGroupId, viewModel.huntingDay.huntingGroupId)
        assertEquals(expectedStartTime, viewModel.huntingDay.startDateTime)
        assertEquals(expectedEndTime, viewModel.huntingDay.endDateTime)
        assertEquals(465, viewModel.huntingDay.durationInMinutes)
        assertEquals(465, viewModel.huntingDay.activeHuntingDurationInMinutes)
        assertNull(viewModel.huntingDay.breakDurationInMinutes)
        assertNull(viewModel.huntingDay.snowDepth)
        assertNull(viewModel.huntingDay.huntingMethod.rawBackendEnumValue)
        assertNull(viewModel.huntingDay.numberOfHunters)
        assertNull(viewModel.huntingDay.numberOfHounds)
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
            assertEquals(expectedStartTime, it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
            assertFalse(it.settings.readOnly)
            assertFalse(it.settings.readOnlyDate)
            assertFalse(it.settings.readOnlyTime)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getDateTimeField(expectedIndex++, GroupHuntingDayField.END_DATE_AND_TIME).let {
            assertEquals(expectedEndTime, it.dateAndTime)
            assertEquals("end_date_and_time", it.settings.label)
            assertFalse(it.settings.readOnly)
            assertFalse(it.settings.readOnlyDate)
            assertFalse(it.settings.readOnlyTime)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getIntField(expectedIndex++, GroupHuntingDayField.NUMBER_OF_HUNTERS).let {
            assertNull(it.value, "number of hunters")
            assertEquals("number_of_hunters", it.settings.label)
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
        }
        fields.getStringListField(expectedIndex++, GroupHuntingDayField.HUNTING_METHOD).let {
            assertEquals(listOf(-1L), it.selected) // -1, indicates null value. See LocalizableEnumExtensions.kt
            assertEquals("hunting_method", it.settings.label)
            assertEquals(GroupHuntingMethodType.values().size + 1, it.values.size) // +1 == empty value
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)

            val stringProvider = getStringProvider()
            GroupHuntingMethodType.values().forEach { huntingMethod ->
                assertEquals(
                    stringProvider.getString(huntingMethod.resourcesStringId),
                    it.values[huntingMethod.ordinal + 1].string // +1, empty value as first one
                )
            }
        }
        fields.getIntField(expectedIndex++, GroupHuntingDayField.NUMBER_OF_HOUNDS).let {
            assertNull(it.value)
            assertEquals("number_of_hounds", it.settings.label)
            // should be voluntary since hunting method does not require hounds
            assertEquals(FieldRequirement.voluntary(), it.settings.requirementStatus)
        }
        fields.getIntField(expectedIndex++, GroupHuntingDayField.SNOW_DEPTH).let {
            assertNull(it.value)
            assertEquals("snow_depth_centimeters", it.settings.label)
            assertEquals(FieldRequirement.voluntary(), it.settings.requirementStatus)
        }
        fields.getSelectDurationField(expectedIndex, GroupHuntingDayField.BREAK_DURATION).let {
            assertEquals(HoursAndMinutes(0, 0), it.value)
            assertEquals("break_duration_minutes", it.settings.label)
            assertEquals(FieldRequirement.voluntary(), it.settings.requirementStatus)

            val dayDurationInMinutes = 465
            val fullHours = (dayDurationInMinutes - 1).div(60)
            val remainingMinutes = (dayDurationInMinutes - 1).rem(60)
            assertEquals(16, it.possibleValues.size, "possible durations")
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
    fun testStartDateCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        val newDateTime = LocalDateTime(2015, 9, 4, 6, 0, 0)
        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = newDateTime
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(GroupHuntingDayId.local(newDateTime.date), viewModel.huntingDay.id)
        assertEquals(newDateTime, viewModel.huntingDay.startDateTime)
        viewModel.fields.getDateTimeField(0, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 4, 6, 0, 0), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
        }
    }

    @Test
    fun testStartTimeCanBeChanged() = runBlockingTest {
        val controller = createController()
        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 5, 8, 0, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 9, 5, 8, 0, 0),
            viewModel.huntingDay.startDateTime
        )
        viewModel.fields.getDateTimeField(0, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 9, 5, 8, 0, 0), it.dateAndTime)
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
    fun testChangingStartDateOutsideOfPermitIsRestricted() = runBlockingTest {
        val controller = createController(now = mockedNow.changeDate(LocalDate(2015, 11, 1)))
        controller.loadViewModel()

        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = LocalDateTime(2015, 5, 1, 6, 30, 0)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(
            LocalDateTime(2015, 6, 2, 0, 0, 0),
            viewModel.huntingDay.startDateTime
        )
        viewModel.fields.getDateTimeField(0, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(LocalDateTime(2015, 6, 2, 0, 0, 0), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
            assertEquals(it.settings.minDateTime, it.dateAndTime, "should equal min")
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
    fun testBreakDurationAffectsChangingStartDate() = runBlockingTest {
        val controller = createController()

        controller.loadViewModel()

        controller.eventDispatchers.durationEventDispatcher.dispatchHoursAndMinutesChanged(
            fieldId = GroupHuntingDayField.BREAK_DURATION,
            value = HoursAndMinutes(2, 0)
        )
        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.START_DATE_AND_TIME,
            value = mockedNow.changeTime(minute = 30)
        )

        val viewModel = controller.getLoadedViewModel()
        assertEquals(mockedNow.changeTime(hour = 11), viewModel.huntingDay.startDateTime)
        viewModel.fields.getDateTimeField(0, GroupHuntingDayField.START_DATE_AND_TIME).let {
            assertEquals(mockedNow.changeTime(hour = 11), it.dateAndTime)
            assertEquals("start_date_and_time", it.settings.label)
            assertEquals(it.settings.maxDateTime, it.dateAndTime, "should equal max")
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

        viewModel.fields.getIntField(4, GroupHuntingDayField.NUMBER_OF_HOUNDS).let {
            assertNull(it.value)
            assertEquals("number_of_hounds", it.settings.label)
            // should be required now since hunting method requires hounds
            assertEquals(FieldRequirement.required(), it.settings.requirementStatus)
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
            value = LocalDateTime(2015, 9, 4, 8, 30, 0)
        )
        controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
            fieldId = GroupHuntingDayField.END_DATE_AND_TIME,
            value = LocalDateTime(2015, 9, 4, 19, 30, 0)
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
            value = HoursAndMinutes(1, 30)
        )
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(
            fieldId = GroupHuntingDayField.SNOW_DEPTH,
            value = 90
        )

        backendAPI.groupHuntingGroupHuntingDaysResponse =
            MockResponse.success(MockGroupHuntingData.GroupHuntingDaysAfterCreate)

        assertTrue(
            controller.saveHuntingDay() is GroupHuntingDayUpdateResponse.Updated,
            "save should succeed"
        )

        // ensure the controller hunting day as well as provider hunting day have been updated
        val huntingDayTarget = getHuntingGroupTarget().createTargetForHuntingDay(GroupHuntingDayId.remote(7))
        val groupContext = groupHuntingContext.findHuntingGroupContext(huntingDayTarget)
        assertNotNull(groupContext, "group context should exist")

        val providerHuntingDay = groupContext.findHuntingDay(huntingDayTarget)
        assertNotNull(providerHuntingDay, "Hunting day should exist")

        val controllerHuntingDay = controller.getLoadedViewModel().huntingDay

        val assertCorrectValues = { huntingDay: GroupHuntingDay, huntingDaySource: String ->
            assertEquals(
                MockGroupHuntingData.FirstHuntingGroupId, huntingDay.huntingGroupId,
                huntingDaySource
            )
            assertEquals(
                LocalDateTime(2015, 9, 4, 8, 30, 0),
                huntingDay.startDateTime, huntingDaySource
            )
            assertEquals(
                LocalDateTime(2015, 9, 4, 19, 30, 0),
                huntingDay.endDateTime, huntingDaySource
            )
            assertEquals(660, huntingDay.durationInMinutes, huntingDaySource)
            assertEquals(570, huntingDay.activeHuntingDurationInMinutes, huntingDaySource)
            assertEquals(90, huntingDay.breakDurationInMinutes, huntingDaySource)
            assertEquals(90, huntingDay.snowDepth, huntingDaySource)
            assertEquals(
                GroupHuntingMethodType.HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE,
                huntingDay.huntingMethod.value, huntingDaySource
            )
            assertEquals(20, huntingDay.numberOfHunters, huntingDaySource)
            assertEquals(2, huntingDay.numberOfHounds, huntingDaySource)
            assertFalse(huntingDay.createdBySystem, huntingDaySource)
        }
        assertCorrectValues(providerHuntingDay, "provider")
        assertEquals(7, providerHuntingDay.id.remoteId, "provider")
        assertEquals(0, providerHuntingDay.rev, "provider")

        // controller should not update its values (otherwise it should become editcontroller)
        assertCorrectValues(controllerHuntingDay, "controller")
        assertEquals(
            GroupHuntingDayId.local(LocalDate(2015, 9, 4)),
            controllerHuntingDay.id, "controller"
        )
        assertNull(controllerHuntingDay.rev, "controller")
    }

    private fun createController(now: LocalDateTime = mockedNow) =
        createController(getGroupHuntingContext(BackendAPIMock()), now)

    private fun createController(groupHuntingContext: GroupHuntingContext, now: LocalDateTime = mockedNow) =
        CreateGroupHuntingDayController(
            groupHuntingContext = groupHuntingContext,
            huntingGroupTarget = getHuntingGroupTarget(),
            currentTimeProvider = MockDateTimeProvider(now),
            stringProvider = getStringProvider(),
        )

    private fun getGroupHuntingContext(backendAPI: BackendAPIMock): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = true,
            backendAPI = backendAPI
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getHuntingGroupTarget(): HuntingGroupTarget {
        return HuntingGroupTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = MockGroupHuntingData.FirstHuntingGroupId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

    companion object {
        private val mockedNow = LocalDateTime(2015, 9, 5, 13, 45, 0)
        private val expectedStartTime = mockedNow.changeTime(LocalTime(6, 0, 0))
        private val expectedEndTime = mockedNow
    }
}
