package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestTarget
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.logging.getLogger
import fi.riista.common.domain.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.TestSpeciesResolver
import fi.riista.common.model.StringWithId
import kotlin.test.*

class EditGroupHarvestControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
    }

    @Test
    fun testFailingHuntingDayFetchFailsAccept() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(
                BackendAPIMock(groupHuntingHuntingDayForDeerResponse = MockResponse.error(404))),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.ThirdHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()
        val response = controller.acceptHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Failure)
    }

    @Test
    fun testHuntingDayIdIsFetchedForDeer() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendAPIMock),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.ThirdHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()
        val response = controller.acceptHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        assertEquals(1, backendAPIMock.callCount(backendAPIMock::fetchHuntingGroupHuntingDayForDeer.name))
    }

    @Test
    fun testHuntingDayIsAutomaticallySelectedIfNoHuntingDay() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.SecondHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        val viewModel = requireNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(MockGroupHuntingData.FirstHuntingDayId, viewModel.harvest.huntingDayId)
    }

    @Test
    fun testMooseCantBeAcceptedWithoutHuntingDayId() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendApi = BackendAPIMock(
                    // no hunting days
                    groupHuntingGroupHuntingDaysResponse = MockResponse.success("[]")
            )),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.SecondHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        val response = controller.acceptHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Error)
    }

    @Test
    fun testSpecimenFieldsHaveDefaultValues() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.SecondHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
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
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendAPIMock),
            harvestTarget = getHarvestTarget(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // First set antlers fields for adult male
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            CommonHarvestField.ANTLERS_TYPE,
            listOf(StringWithId("hanko", GameAntlersType.CERVINE.ordinal.toLong()))
        )
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLERS_WIDTH, 2)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLER_POINTS_LEFT, 3)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLER_POINTS_RIGHT, 4)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLERS_GIRTH, 5)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLERS_LENGTH, 6)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLERS_INNER_WIDTH, 7)
        controller.eventDispatchers.intEventDispatcher.dispatchIntChanged(CommonHarvestField.ANTLER_SHAFT_WIDTH, 8)

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
        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(CommonHarvestField.AGE, GameAge.YOUNG)
        controller.eventDispatchers.booleanEventDispatcher.dispatchBooleanChanged(CommonHarvestField.ALONE, false)

        val response = controller.acceptHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        val savedHarvest = backendAPIMock.callParameter(BackendAPIMock::updateGroupHuntingHarvest.name) as GroupHuntingHarvestDTO
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
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendAPIMock),
            harvestTarget = getHarvestTarget(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(CommonHarvestField.AGE, GameAge.YOUNG)
        controller.eventDispatchers.booleanEventDispatcher.dispatchBooleanChanged(CommonHarvestField.ALONE, true)

        val harvest = controller.getLoadedViewModel().harvest

        assertEquals(GameAge.YOUNG, harvest.specimens[0].age?.value)
        assertTrue(harvest.specimens[0].alone!!)

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(CommonHarvestField.AGE, GameAge.ADULT)

        val response = controller.acceptHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        val savedHarvest = backendAPIMock.callParameter(BackendAPIMock::updateGroupHuntingHarvest.name) as GroupHuntingHarvestDTO
        assertNull(savedHarvest.specimens[0].alone)
    }

    @Test
    fun testHarvestIsNotValidWithoutAge() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.getLoadedViewModel().harvestIsValid)

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(CommonHarvestField.AGE, GameAge.UNKNOWN)

        assertFalse(controller.getLoadedViewModel().harvestIsValid)
    }

    @Test
    fun testHarvestIsNotValidWithoutGender() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.getLoadedViewModel().harvestIsValid)

        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(CommonHarvestField.GENDER, Gender.UNKNOWN)

        assertFalse(controller.getLoadedViewModel().harvestIsValid)
    }

    @Test
    fun testAcceptingHarvestSavesItWithLatestSpecVersion() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendApi = backendAPIMock),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.SecondHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertEquals(8, controller.getLoadedViewModelOrNull()?.harvest?.harvestSpecVersion)

        val response = controller.acceptHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        val callParameter = backendAPIMock.callParameter(BackendAPIMock::updateGroupHuntingHarvest.name)
        val dto = callParameter as GroupHuntingHarvestDTO
        assertEquals(Constants.HARVEST_SPEC_VERSION, dto.harvestSpecVersion)
    }

    @Test
    fun testUpdatingHarvestSavesItWithLatestSpecVersion() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(backendApi = backendAPIMock),
            harvestTarget = getHarvestTarget(harvestId = MockGroupHuntingData.SecondHarvestId),
            speciesResolver = TestSpeciesResolver.INSTANCE,
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertEquals(8, controller.getLoadedViewModelOrNull()?.harvest?.harvestSpecVersion)

        val response = controller.updateHarvest()
        assertTrue(response is GroupHuntingHarvestOperationResponse.Success)
        val callParameter = backendAPIMock.callParameter(BackendAPIMock::updateGroupHuntingHarvest.name)
        val dto = callParameter as GroupHuntingHarvestDTO
        assertEquals(Constants.HARVEST_SPEC_VERSION, dto.harvestSpecVersion)
    }

    private fun getGroupHuntingContext(backendApi: BackendAPI = BackendAPIMock()): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            groupHuntingEnabledForAll = true,
            backendAPI = backendApi,
        )
        return userContextProvider.userContext.groupHuntingContext
    }


    private fun getHarvestTarget(harvestId: Long = MockGroupHuntingData.FirstHarvestId): GroupHuntingHarvestTarget {
        return GroupHuntingHarvestTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = 344,
            harvestId = harvestId,
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

    companion object {
        private val logger by getLogger(EditGroupHarvestControllerTest::class)
    }
}

