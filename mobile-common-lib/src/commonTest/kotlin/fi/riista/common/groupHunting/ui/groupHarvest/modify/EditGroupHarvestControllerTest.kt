package fi.riista.common.groupHunting.ui.groupHarvest.modify

import fi.riista.common.groupHunting.GroupHuntingContext
import fi.riista.common.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.groupHunting.MockGroupHuntingData
import fi.riista.common.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.groupHunting.model.GroupHuntingHarvestTarget
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.GameAge
import fi.riista.common.model.GameAntlersType
import fi.riista.common.model.Gender
import fi.riista.common.model.StringWithId
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.RR
import fi.riista.common.resources.RStringId
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.userInfo.CurrentUserContextProviderFactory
import kotlin.test.*

class EditGroupHarvestControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            stringProvider = getStringProvider()
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
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
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        // First set antlers fields for adult male
        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            GroupHarvestField.ANTLERS_TYPE,
            StringWithId("hanko", GameAntlersType.CERVINE.ordinal.toLong())
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
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.YOUNG)
        controller.eventDispatchers.booleanEventDispatcher.dispatchBooleanChanged(GroupHarvestField.ALONE, true)

        val harvest = controller.getLoadedViewModel().harvest

        assertEquals(GameAge.YOUNG, harvest.specimens[0].age?.value)
        assertTrue(harvest.specimens[0].alone!!)

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.ADULT)

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
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

        assertTrue(controller.getLoadedViewModel().harvestIsValid)

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(GroupHarvestField.AGE, GameAge.UNKNOWN)

        assertFalse(controller.getLoadedViewModel().harvestIsValid)
    }

    @Test
    fun testHarvestIsNotValidWithoutGender() = runBlockingTest {
        val controller = EditGroupHarvestController(
            groupHuntingContext = getGroupHuntingContext(),
            harvestTarget = getHarvestTarget(),
            stringProvider = getStringProvider()
        )

        controller.loadViewModel()

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


    private fun getHarvestTarget(harvestId: Long = MockGroupHuntingData.FirstHarvestId): GroupHuntingHarvestTarget {
        return GroupHuntingHarvestTarget(
            clubId = MockGroupHuntingData.FirstClubId,
            huntingGroupId = 344,
            harvestId = harvestId,
        )
    }

    private fun getStringProvider(): StringProvider {
        return object : StringProvider {
            @Suppress("SpellCheckingInspection")
            override fun getString(stringId: RStringId): String {
                return when (stringId) {
                    RR.string.group_hunting_harvest_field_hunting_day_and_time -> "hunting_day_and_time"
                    RR.string.group_hunting_proposed_group_harvest_shooter -> "shooter"
                    RR.string.group_hunting_proposed_group_harvest_actor -> "actor"
                    RR.string.group_hunting_harvest_field_deer_hunting_type -> "deer_hunting_type"
                    RR.string.group_hunting_harvest_field_deer_hunting_other_type_description -> "deer_hunting_other_type_description"
                    RR.string.group_hunting_proposed_group_harvest_specimen -> "specimen"
                    RR.string.group_hunting_harvest_field_not_edible -> "not_edible"
                    RR.string.group_hunting_harvest_field_weight_estimated -> "weight_estimated"
                    RR.string.group_hunting_harvest_field_weight_measured -> "weight_measured"
                    RR.string.harvest_fitness_class_naantynyt -> "naantynyt"
                    RR.string.harvest_fitness_class_erinomainen -> "erinomainen"
                    RR.string.harvest_fitness_class_normaali -> "normaali"
                    RR.string.harvest_fitness_class_laiha -> "laiha"
                    RR.string.group_hunting_harvest_field_fitness_class -> "fitness_class"
                    RR.string.group_hunting_harvest_field_antlers_lost -> "antlers_lost"
                    RR.string.harvest_antler_type_hanko -> "hanko"
                    RR.string.harvest_antler_type_lapio -> "lapio"
                    RR.string.harvest_antler_type_seka -> "seka"
                    RR.string.group_hunting_harvest_field_antlers_type -> "antlers_type"
                    RR.string.group_hunting_harvest_field_antlers_width -> "antlers_width"
                    RR.string.group_hunting_harvest_field_antler_points_left -> "antler_points_left"
                    RR.string.group_hunting_harvest_field_antler_points_right -> "antler_points_right"
                    RR.string.group_hunting_harvest_field_antlers_girth -> "antlers_girth"
                    RR.string.group_hunting_harvest_field_antlers_length -> "antlers_length"
                    RR.string.group_hunting_harvest_field_antlers_inner_width -> "antlers_inner_width"
                    RR.string.group_hunting_harvest_field_additional_information -> "additional_information"
                    RR.string.group_hunting_harvest_field_alone -> "alone"
                    RR.string.deer_hunting_type_stand_hunting -> "stand_hunting"
                    RR.string.deer_hunting_type_dog_hunting -> "dog_hunting"
                    RR.string.deer_hunting_type_other -> "other"
                    RR.string.group_hunting_other_hunter -> "other"
                    RR.string.group_hunting_hunter_id -> "hunter id"
                    RR.string.group_hunting_harvest_field_additional_information_instructions ->
                        "additional_information_instructions"
                    RR.string.group_hunting_harvest_field_additional_information_instructions_white_tailed_deer ->
                        "additional_information_instructions_white_tailed_deer"
                    RR.string.group_member_selection_select_hunter -> "select_hunter"
                    RR.string.group_member_selection_select_observer -> "select_observer"
                    RR.string.group_member_selection_search_by_name -> "search_by_name"
                    RR.string.group_member_selection_name_hint -> "name_hint"
                    else -> throw RuntimeException("Unexpected stringId ($stringId) requested")
                }
            }

            override fun getFormattedString(stringId: RStringId, arg: String): String {
                throw RuntimeException("Unexpected stringId ($stringId) requested")
            }
        }
    }
}

