package fi.riista.common.domain.groupHunting.diary

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.ui.diary.DiaryController
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlin.test.*

class DiaryControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = DiaryController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget()
        )

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = DiaryController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val events = controller.getLoadedViewModel().events
        assertEquals(3, events!!.filteredEvents.harvests.size)
        assertEquals(2, events.filteredEvents.observations.size)
    }

    @Test
    fun testRefreshLoadsDataFromNetwork() = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = DiaryController(
            groupHuntingContext = getGroupHuntingContext(backendAPI = backendAPI),
            groupTarget = getHuntingGroupTarget()
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        assertEquals(1, backendAPI.callCount(BackendAPI::fetchGroupHuntingDiary.name))
        controller.loadViewModel(refresh = true)
        assertEquals(2, backendAPI.callCount(BackendAPI::fetchGroupHuntingDiary.name))
        controller.loadViewModel(refresh = false)
        assertEquals(2, backendAPI.callCount(BackendAPI::fetchGroupHuntingDiary.name))
    }

    @Test
    fun testMinAndMaxFilterDatesAreValid() = runBlockingTest {
        val controller = DiaryController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget()
        )

        controller.loadViewModel()

        val events = controller.getLoadedViewModel().events
        assertEquals(3, events!!.filteredEvents.harvests.size)
        assertEquals(2, events.filteredEvents.observations.size)
        assertNull(events.allEvents.harvests.find {
            it.pointOfTime.date < events.minFilterDate
        })
        assertNull(events.allEvents.harvests.find {
            it.pointOfTime.date > events.maxFilterDate
        })
        assertNull(events.allEvents.observations.find {
            it.pointOfTime.date < events.minFilterDate
        })
        assertNull(events.allEvents.observations.find {
            it.pointOfTime.date > events.maxFilterDate
        })
    }

    @Test
    fun testHuntingEventsCanBeFilteredWithStartDate() = runBlockingTest {
        val controller = DiaryController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget()
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        assertEquals(3, viewModel.events!!.filteredEvents.harvests.size)
        assertEquals(2, viewModel.events!!.filteredEvents.observations.size)

        assertNotNull(viewModel.events!!.filteredEvents.harvests.find { it.id == 946L })
        assertNotNull(viewModel.events!!.filteredEvents.harvests.find { it.id == 949L })
        assertNotNull(viewModel.events!!.filteredEvents.observations.find { it.id == 29L })
        assertNotNull(viewModel.events!!.filteredEvents.observations.find { it.id == 30L })

        controller.eventDispatcher.dispatchFilterStartDateChanged(LocalDate(2015, 9, 2))

        viewModel = controller.getLoadedViewModel()
        assertEquals(0, viewModel.events!!.filteredEvents.harvests.size)
        assertEquals(2, viewModel.events!!.filteredEvents.observations.size)
        assertNotNull(viewModel.events!!.filteredEvents.observations.find { it.id == 29L })
        assertNotNull(viewModel.events!!.filteredEvents.observations.find { it.id == 30L })
    }

    @Test
    fun testHuntingEventsCanBeFilteredWithEndDate() = runBlockingTest {
        val controller = DiaryController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget()
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        assertEquals(3, viewModel.events!!.filteredEvents.harvests.size)
        assertEquals(2, viewModel.events!!.filteredEvents.observations.size)

        assertNotNull(viewModel.events!!.filteredEvents.harvests.find { it.id == 946L })
        assertNotNull(viewModel.events!!.filteredEvents.harvests.find { it.id == 949L })
        assertNotNull(viewModel.events!!.filteredEvents.observations.find { it.id == 29L })
        assertNotNull(viewModel.events!!.filteredEvents.observations.find { it.id == 30L })

        controller.eventDispatcher.dispatchFilterEndDateChanged(LocalDate(2015,9,1))

        viewModel = controller.getLoadedViewModel()
        assertEquals(3, viewModel.events!!.filteredEvents.harvests.size)
        assertEquals(0, viewModel.events!!.filteredEvents.observations.size)
        assertEquals("3EM48F3PXA", viewModel.events!!.huntingGroupArea?.externalId)
    }

    @Test
    fun testDataLoadedButThereAreNoHarvestsOrObservations() = runBlockingTest {
        val controller = DiaryController(
                groupHuntingContext = getGroupHuntingContextWithEmptyDiary(),
                groupTarget = getHuntingGroupTarget()
        )

        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()
        assertNull(viewModel.events)
    }

    private fun getGroupHuntingContextWithEmptyDiary(): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                groupHuntingEnabledForAll = true,
                backendAPI = BackendAPIMock(
                        groupHuntingGameDiaryResponse = MockResponse.success(MockGroupHuntingData.EmptyGroupHuntingDiary)
                )
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getGroupHuntingContext(backendAPI: BackendAPI = BackendAPIMock()): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                groupHuntingEnabledForAll = true,
                backendAPI = backendAPI
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getHuntingGroupTarget(): HuntingGroupTarget {
        return HuntingGroupTarget(
                clubId = MockGroupHuntingData.FirstClubId,
                huntingGroupId = 344
        )
    }
}
