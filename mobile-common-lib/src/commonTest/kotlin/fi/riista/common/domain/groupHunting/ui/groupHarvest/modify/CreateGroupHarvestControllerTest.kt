package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestCreateDTO
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.domain.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.StringWithId
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

class CreateGroupHarvestControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
    }


    @Test
    fun testHuntingDayIdIsFetchedForDeer() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(backendAPIMock),
                huntingGroupTarget = getHuntingGroupTarget(MockGroupHuntingData.SecondHuntingGroupId),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // make sure harvest is valid first
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                GroupHarvestField.ACTOR,
                listOf(StringWithId("Pentti Mujunen", 4))
        )
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
                GroupHarvestField.AGE, GameAge.ADULT
        )
        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
                GroupHarvestField.GENDER, Gender.MALE
        )
        controller.eventDispatchers.huntingDayEventDispatcher.dispatchHuntingDayChanged(
                GroupHarvestField.HUNTING_DAY_AND_TIME, MockGroupHuntingData.FirstHuntingDayId
        )

        val response = controller.createHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        assertEquals(1, backendAPIMock.callCount(backendAPIMock::fetchHuntingGroupHuntingDayForDeer.name))
    }

    @Test
    fun testHuntingDayIsAutomaticallySelectedForMooseIfNoHuntingDay() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(MockGroupHuntingData.FirstHuntingGroupId),
                stringProvider = getStringProvider(),
                localDateTimeProvider = MockDateTimeProvider(
                        now = LocalDateTime(2015, 9, 1, 12, 0, 0)
                ),
        )

        controller.loadViewModel()

        val viewModel = requireNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(MockGroupHuntingData.FirstHuntingDayId, viewModel.harvest.huntingDayId)
    }

    @Test
    fun testNoHuntingDayAutomaticSelectionIfNoHuntingDay() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(MockGroupHuntingData.SecondHuntingGroupId),
                stringProvider = getStringProvider(),
                localDateTimeProvider = MockDateTimeProvider(
                        now = LocalDateTime(2021, 9, 1, 12, 0, 0)
                ),
        )

        controller.loadViewModel()

        val viewModel = requireNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertNull(viewModel.harvest.huntingDayId)
    }

    @Test
    fun testMooseCantBeCreatedWithoutHuntingDayId() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(backendApi = BackendAPIMock(
                        // no hunting days
                        groupHuntingGroupHuntingDaysResponse = MockResponse.success("[]")
                )),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        val response = controller.createHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Error)
    }

    @Test
    fun testSpecimenFieldsHaveDefaultValues() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        val viewModel = requireNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        with (viewModel.harvest.specimens.first()) {
            assertEquals(false, notEdible)
            assertEquals(false, alone)
            assertEquals(false, antlersLost)
        }
    }

    @Test
    fun testAntlerFieldsAreClearedForYoung() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(backendAPIMock),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // First set antlers fields for adult male
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                GroupHarvestField.ACTOR,
                listOf(StringWithId("Pentti Mujunen", 4))
        )
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
                GroupHarvestField.AGE, GameAge.ADULT
        )
        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
                GroupHarvestField.GENDER, Gender.MALE
        )
        controller.eventDispatchers.huntingDayEventDispatcher.dispatchHuntingDayChanged(
                GroupHarvestField.HUNTING_DAY_AND_TIME, MockGroupHuntingData.FirstHuntingDayId
        )


        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            GroupHarvestField.ANTLERS_TYPE,
            listOf(StringWithId("hanko", GameAntlersType.CERVINE.ordinal.toLong()))
        )
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLERS_WIDTH, 2)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLER_POINTS_LEFT, 3)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLER_POINTS_RIGHT, 4)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLERS_GIRTH, 5)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLERS_LENGTH, 6)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLERS_INNER_WIDTH, 7)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(GroupHarvestField.ANTLER_SHAFT_WIDTH, 8)

        val harvest = controller.getLoadedViewModel().harvest

        with(harvest.specimens[0]) {
            assertEquals(GameAntlersType.CERVINE, antlersType?.value)
            assertEquals(2, antlersWidth)
            assertEquals(3, antlerPointsLeft)
            assertEquals(4, antlerPointsRight)
            assertEquals(5, antlersGirth)
            assertEquals(6, antlersLength)
            assertEquals(7, antlersInnerWidth)
            assertEquals(8, antlerShaftWidth)
        }

        // Then change age to young (+ set alone flag), save and verify that antlers fields are null
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.YOUNG)
        controller.eventDispatchers.booleanEventDispatcher.dispatchBooleanChanged(GroupHarvestField.ALONE, false)

        val response = controller.createHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        val savedHarvest = backendAPIMock.callParameter(BackendAPIMock::createGroupHuntingHarvest.name) as GroupHuntingHarvestCreateDTO
        with(savedHarvest.specimens[0]) {
            assertEquals("YOUNG", age)
            assertNull(antlersType)
            assertNull(antlersWidth)
            assertNull(antlerPointsLeft)
            assertNull(antlerPointsRight)
            assertNull(antlersGirth)
            assertNull(antlersLength)
            assertNull(antlersInnerWidth)
            assertNull(antlerShaftWidth)
        }
    }

    @Test
    fun testAloneFieldIsClearedForAdult() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(backendAPIMock),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // make sure harvest is valid first
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                GroupHarvestField.ACTOR,
                listOf(StringWithId("Pentti Mujunen", 4))
        )
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
                GroupHarvestField.AGE, GameAge.ADULT
        )
        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
                GroupHarvestField.GENDER, Gender.MALE
        )
        controller.eventDispatchers.huntingDayEventDispatcher.dispatchHuntingDayChanged(
                GroupHarvestField.HUNTING_DAY_AND_TIME, MockGroupHuntingData.FirstHuntingDayId
        )


        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.YOUNG)
        controller.eventDispatchers.booleanEventDispatcher.dispatchBooleanChanged(GroupHarvestField.ALONE, true)

        val harvest = controller.getLoadedViewModel().harvest

        assertEquals(GameAge.YOUNG, harvest.specimens[0].age?.value)
        assertTrue(harvest.specimens[0].alone!!)

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.ADULT)

        val response = controller.createHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        val savedHarvest = backendAPIMock.callParameter(BackendAPIMock::createGroupHuntingHarvest.name) as GroupHuntingHarvestCreateDTO
        assertNull(savedHarvest.specimens[0].alone)
    }

    @Test
    fun testHarvestIsNotValidWithoutAge() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // make sure harvest is valid first
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                GroupHarvestField.ACTOR,
                listOf(StringWithId("Pentti Mujunen", 4))
        )
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
                GroupHarvestField.AGE, GameAge.ADULT
        )
        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
                GroupHarvestField.GENDER, Gender.MALE
        )
        controller.eventDispatchers.huntingDayEventDispatcher.dispatchHuntingDayChanged(
                GroupHarvestField.HUNTING_DAY_AND_TIME, MockGroupHuntingData.FirstHuntingDayId
        )

        assertTrue(controller.getLoadedViewModel().harvestIsValid)

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.UNKNOWN)

        assertFalse(controller.getLoadedViewModel().harvestIsValid)
    }

    @Test
    fun testHarvestIsNotValidWithoutGender() = runBlockingTest {
        val controller = CreateGroupHarvestController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingGroupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // make sure harvest is valid first
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                GroupHarvestField.ACTOR,
                listOf(StringWithId("Pentti Mujunen", 4))
        )
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
                GroupHarvestField.AGE, GameAge.ADULT
        )
        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
                GroupHarvestField.GENDER, Gender.MALE
        )
        controller.eventDispatchers.huntingDayEventDispatcher.dispatchHuntingDayChanged(
                GroupHarvestField.HUNTING_DAY_AND_TIME, MockGroupHuntingData.FirstHuntingDayId
        )

        assertTrue(controller.getLoadedViewModel().harvestIsValid)

        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(GroupHarvestField.GENDER, Gender.UNKNOWN)

        assertFalse(controller.getLoadedViewModel().harvestIsValid)
    }

    private fun getGroupHuntingContext(backendApi: BackendAPI = BackendAPIMock()): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = true,
            backendAPI = backendApi,
        )
        return userContextProvider.userContext.groupHuntingContext
    }


    private fun getHuntingGroupTarget(
        huntingGroupId: HuntingGroupId = MockGroupHuntingData.SecondHuntingGroupId
    ): HuntingGroupTarget {
        return HuntingGroupTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = huntingGroupId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
}

