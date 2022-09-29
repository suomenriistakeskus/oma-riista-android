package fi.riista.common.domain.groupHunting.huntingDays

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.MockGroupHuntingData
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.ListHuntingDaysController
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.MockResponse
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlin.test.*

class ListHuntingDaysControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val huntingDays = controller.getLoadedViewModel().huntingDays!!
        assertEquals(3, huntingDays.filteredHuntingDays.size)
    }

    @Test
    fun testProposedHarvestIsAssignedToHuntingDayByStartDate() = runBlockingTest {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        val huntingDays = controller.getLoadedViewModel().huntingDays!!
        val proposedHarvest = huntingDays.filteredHuntingDays.find {
            it.huntingDay.startDateTime.date == LocalDate(2015, 9, 1)
        }!!.harvests.find { it.id == 946L }
        assertNotNull(proposedHarvest)
        assertEquals(AcceptStatus.PROPOSED, proposedHarvest.acceptStatus)
    }

    @Test
    fun testHuntingDayIsSuggestedForProposedHarvestAndObservation() = runBlockingTest {
        val backendAPI = BackendAPIMock(
                groupHuntingGameDiaryResponse = MockResponse.success(
                    """
                    {
                        "harvests" : [${MockGroupHuntingData.ProposedHarvest}],
                        "observations" : [${MockGroupHuntingData.ProposedObservation}],
                        "rejectedHarvests": [],
                        "rejectedObservations": []
                    }
                    """
                )
        )
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(backendAPI),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        with (controller.getLoadedViewModel().huntingDays!!) {
            assertEquals(4, filteredHuntingDays.size)
            filteredHuntingDays.find {
                it.huntingDay.id == GroupHuntingDayId.remote(5)
            }.let {
                assertNotNull(it)
                assertEquals(HuntingDayViewModel.HuntingDayType.EXISTING, it.huntingDayType)
                assertFalse(it.canCreateHuntingDay)
            }
            filteredHuntingDays.find {
                it.huntingDay.id == GroupHuntingDayId.remote(6)
            }.let {
                assertNotNull(it)
                assertEquals(HuntingDayViewModel.HuntingDayType.EXISTING, it.huntingDayType)
                assertFalse(it.canCreateHuntingDay)
            }
            filteredHuntingDays.find {
                it.huntingDay.id == GroupHuntingDayId.local(LocalDate(2015, 9, 4)) &&
                        it.harvestCount == 1 && it.harvests.first().id == 980L
            }.let {
                assertNotNull(it)
                assertEquals(HuntingDayViewModel.HuntingDayType.SUGGESTED, it.huntingDayType)
                assertTrue(it.canCreateHuntingDay)
            }
            filteredHuntingDays.find {
                it.huntingDay.id == GroupHuntingDayId.local(LocalDate(2015, 9, 5)) &&
                        it.observationCount == 1 && it.observations.first().id == 40L
            }.let {
                assertNotNull(it)
                assertEquals(HuntingDayViewModel.HuntingDayType.SUGGESTED, it.huntingDayType)
                assertTrue(it.canCreateHuntingDay)
            }
        }
    }

    @Test
    fun testHuntingDaysMinAndMaxFilterDatesAreValid() = runBlockingTest {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        val viewModel = controller.getLoadedViewModel()
        assertTrue(viewModel.containsHuntingDaysAfterFiltering)
        assertEquals(3, viewModel.huntingDays!!.filteredHuntingDays.size)
        assertNull(viewModel.huntingDays!!.allHuntingDays.find {
            it.huntingDay.startDateTime.date < viewModel.huntingDays!!.minFilterDate
        })
        assertNull(viewModel.huntingDays!!.allHuntingDays.find {
            it.huntingDay.startDateTime.date > viewModel.huntingDays!!.maxFilterDate
        })
    }

    @Test
    fun testHuntingDaysCanBeFilteredWithStartDate() = runBlockingTest {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        assertTrue(viewModel.containsHuntingDaysAfterFiltering)
        assertEquals(3, viewModel.huntingDays!!.filteredHuntingDays.size)
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 5L })
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 6L })

        controller.eventDispatcher.dispatchFilterStartDateChanged(LocalDate(2015, 9, 2))

        viewModel = controller.getLoadedViewModel()
        assertEquals(2, viewModel.huntingDays!!.filteredHuntingDays.size)
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 6L })
    }

    @Test
    fun testHuntingDaysCanBeFilteredWithEndDate() = runBlockingTest {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        assertTrue(viewModel.containsHuntingDaysAfterFiltering)
        assertEquals(3, viewModel.huntingDays!!.filteredHuntingDays.size)
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 5L })
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 6L })

        controller.eventDispatcher.dispatchFilterEndDateChanged(LocalDate(2015, 9, 2))

        viewModel = controller.getLoadedViewModel()
        assertEquals(1, viewModel.huntingDays!!.filteredHuntingDays.size)
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 5L })
    }

    @Test
    fun testHuntingDayFilteringCanCauseNoResults() = runBlockingTest {
        val controller = ListHuntingDaysController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        assertTrue(viewModel.containsHuntingDaysAfterFiltering)
        assertEquals(3, viewModel.huntingDays!!.filteredHuntingDays.size)
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 5L })
        assertNotNull(viewModel.huntingDays!!.filteredHuntingDays.find { it.huntingDay.id.remoteId == 6L })

        controller.eventDispatcher.dispatchFilterStartDateChanged(LocalDate(2015, 9, 2))
        controller.eventDispatcher.dispatchFilterEndDateChanged(LocalDate(2015, 9, 2))

        viewModel = controller.getLoadedViewModel()
        assertFalse(viewModel.containsHuntingDaysAfterFiltering)
    }

    @Test
    fun testAddingNewHuntingDayAfterUpdatesFilter()  = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = ListHuntingDaysController(
            groupHuntingContext = getGroupHuntingContext(backendAPI = backendAPI),
            groupTarget = getHuntingGroupTarget(),
            stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        var filterEndDate = viewModel.huntingDays?.filterEndDate
        assertEquals(MockGroupHuntingData.ObservationDate, filterEndDate.toString())

        backendAPI.groupHuntingGroupHuntingDaysResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDaysOneDayAddedAfter)
        controller.loadViewModel(refresh = true)
        viewModel = controller.getLoadedViewModel()
        filterEndDate = viewModel.huntingDays?.filterEndDate
        assertEquals(MockGroupHuntingData.AddedAfterHuntingDayDate, filterEndDate.toString())
    }

    @Test
    fun testAddingNewHuntingDayBeforeUpdatesFilter()  = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = ListHuntingDaysController(
            groupHuntingContext = getGroupHuntingContext(backendAPI = backendAPI),
            groupTarget = getHuntingGroupTarget(),
            stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        var filterStartDate = viewModel.huntingDays?.filterStartDate
        assertEquals(MockGroupHuntingData.FirstHuntingDayDate, filterStartDate.toString())

        backendAPI.groupHuntingGroupHuntingDaysResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDaysOneDayAddedBefore)
        controller.loadViewModel(refresh = true)
        viewModel = controller.getLoadedViewModel()
        filterStartDate = viewModel.huntingDays?.filterStartDate
        assertEquals(MockGroupHuntingData.AddedBeforeHuntingDayDate, filterStartDate.toString())
    }

    @Test
    fun testAddingNewHuntingDayBeforeAndAfterUpdatesFilter()  = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = ListHuntingDaysController(
            groupHuntingContext = getGroupHuntingContext(backendAPI = backendAPI),
            groupTarget = getHuntingGroupTarget(),
            stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        var viewModel = controller.getLoadedViewModel()
        var filterStartDate = viewModel.huntingDays?.filterStartDate
        var filterEndDate = viewModel.huntingDays?.filterEndDate
        assertEquals(MockGroupHuntingData.FirstHuntingDayDate, filterStartDate.toString())
        assertEquals(MockGroupHuntingData.ObservationDate, filterEndDate.toString())

        backendAPI.groupHuntingGroupHuntingDaysResponse = MockResponse.success(MockGroupHuntingData.GroupHuntingDaysOneDayAddedBeforeAndAfter)
        controller.loadViewModel(refresh = true)
        viewModel = controller.getLoadedViewModel()
        filterStartDate = viewModel.huntingDays?.filterStartDate
        filterEndDate =  viewModel.huntingDays?.filterEndDate
        assertEquals(MockGroupHuntingData.AddedBeforeHuntingDayDate, filterStartDate.toString())
        assertEquals(MockGroupHuntingData.AddedAfterHuntingDayDate, filterEndDate.toString())
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
                huntingGroupId = MockGroupHuntingData.FirstHuntingGroupId
        )
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
}
