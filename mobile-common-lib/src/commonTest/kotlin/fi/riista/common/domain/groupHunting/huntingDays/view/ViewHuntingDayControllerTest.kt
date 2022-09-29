package fi.riista.common.domain.groupHunting.huntingDays.view

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayTarget
import fi.riista.common.domain.groupHunting.model.GroupHuntingMethodType
import fi.riista.common.domain.groupHunting.ui.huntingDays.view.ViewGroupHuntingDayController
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlin.test.*

class ViewGroupHuntingDayControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = ViewGroupHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingDayTarget = getHuntingDayTarget()
        )

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = ViewGroupHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingDayTarget = getHuntingDayTarget()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = controller.getLoadedViewModel()
        assertEquals(MockGroupHuntingData.FirstHuntingDayId, viewModel.huntingDay.id)
        assertEquals(MockGroupHuntingData.FirstHuntingGroupId, viewModel.huntingDay.huntingGroupId)
        assertEquals(LocalDateTime(2015, 9, 1, 6, 0, 0), viewModel.huntingDay.startDateTime)
        assertEquals(LocalDateTime(2015, 9, 1, 21, 0, 0), viewModel.huntingDay.endDateTime)
        assertEquals(900, viewModel.huntingDay.durationInMinutes)
        assertEquals(0, viewModel.huntingDay.breakDurationInMinutes)
        assertEquals(900, viewModel.huntingDay.activeHuntingDurationInMinutes)
        assertEquals(123, viewModel.huntingDay.snowDepth)
        assertEquals(GroupHuntingMethodType.PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA, viewModel.huntingDay.huntingMethod.value)
        assertEquals(23, viewModel.huntingDay.numberOfHunters)
        assertEquals(1, viewModel.huntingDay.numberOfHounds)
        assertFalse(viewModel.huntingDay.createdBySystem)
        assertTrue(viewModel.showHuntingDayDetails)
    }

    @Test
    fun testDataCanBeLoadedForDeer() = runBlockingTest {
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
        val controller = ViewGroupHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(backendApi),
                huntingDayTarget = GroupHuntingDayTarget(
                        clubId = MockGroupHuntingData.FirstClubId,
                        huntingGroupId = MockGroupHuntingData.SecondHuntingGroupId,
                        huntingDayId = MockGroupHuntingData.DeerHuntingDayId,
                )
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = controller.getLoadedViewModel()
        assertEquals(MockGroupHuntingData.DeerHuntingDayId, viewModel.huntingDay.id, "day id")
        assertEquals(MockGroupHuntingData.SecondHuntingGroupId, viewModel.huntingDay.huntingGroupId, "group id")
        assertEquals(LocalDateTime(2015, 9, 3, 6, 0, 0), viewModel.huntingDay.startDateTime)
        assertEquals(LocalDateTime(2015, 9, 3, 21, 0, 0), viewModel.huntingDay.endDateTime)
        assertEquals(900, viewModel.huntingDay.durationInMinutes)
        assertNull(viewModel.huntingDay.breakDurationInMinutes)
        assertEquals(900, viewModel.huntingDay.activeHuntingDurationInMinutes)
        assertNull(viewModel.huntingDay.snowDepth)
        assertNull(viewModel.huntingDay.huntingMethod.value)
        assertNull(viewModel.huntingDay.numberOfHunters)
        assertNull(viewModel.huntingDay.numberOfHounds)
        assertTrue(viewModel.huntingDay.createdBySystem)
        assertFalse(viewModel.showHuntingDayDetails)
    }

    @Test
    fun testHuntingDayContainsHarvests() = runBlockingTest {
        val controller = ViewGroupHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                huntingDayTarget = getHuntingDayTarget()
        )

        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()
        assertEquals(3, viewModel.harvestCount)
        assertNotNull(viewModel.harvests.find { it.id == 949L })
        assertNotNull(viewModel.harvests.find { it.id == 949L })
        assertNotNull(viewModel.harvests.find { it.id == 951L })
    }

    private fun getGroupHuntingContext(backendApi: BackendAPI = BackendAPIMock()): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                groupHuntingEnabledForAll = true,
                backendAPI = backendApi
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
}