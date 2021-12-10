package fi.riista.common.groupHunting.ui.diary

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.groupHunting.GroupHuntingContext
import fi.riista.common.groupHunting.model.*
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDate
import fi.riista.common.model.maxDate
import fi.riista.common.model.minDate
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class DiaryController(
    private val groupHuntingContext: GroupHuntingContext,
    private val groupTarget: HuntingGroupTarget,
) : ControllerWithLoadableModel<ListDiaryEventsViewModel>(),
    IntentHandler<DiaryEventIntent>,
    HasUnreproducibleState<DiaryController.State> {

    /**
     * The restored state (e.g. if activity was destroyed on android)
     */
    private var stateToRestore: State? = null

    init {
        ensureNeverFrozen()
    }

    val eventDispatcher: DiaryEventDispatcher =
            DiaryEventToIntentMapper(intentHandler = this)

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ListDiaryEventsViewModel>> = flow {

        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = groupTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain context for the hunting group ${groupTarget.huntingGroupId}" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val diaryProvider = groupContext.diaryProvider
        diaryProvider.fetch(refresh = refresh)

        val areaProvider = groupContext.huntingAreaProvider
        areaProvider.fetch(refresh = refresh)

        val harvests = diaryProvider.diary.harvests + diaryProvider.diary.rejectedHarvests
        val observations = diaryProvider.diary.observations + diaryProvider.diary.rejectedObservations
        val huntingGroupArea = areaProvider.area

        if (harvests.isNullOrEmpty() && observations.isNullOrEmpty()) {
            // No harvests or observations. This can happen in two cases
            // 1. there are no harvests and no observations
            // 2. loading failed
            //
            // check for the latter and emit failure
            if (!diaryProvider.loadStatus.value.loaded) {
                emit(ViewModelLoadStatus.LoadFailed)
            } else {
                emit(ViewModelLoadStatus.Loaded(ListDiaryEventsViewModel(events = null)))
            }
        } else {
            val sortedHarvests = harvests.sortedBy { it.pointOfTime }
            val sortedObservations = observations.sortedBy { it.pointOfTime }

            val firstHarvestDate = sortedHarvests.firstOrNull()?.pointOfTime?.date
            val lastHarvestDate = sortedHarvests.lastOrNull()?.pointOfTime?.date

            val firstObservationDate = sortedObservations.firstOrNull()?.pointOfTime?.date
            val lastObservationDate = sortedObservations.lastOrNull()?.pointOfTime?.date

            val minFilterDate = minDate(firstHarvestDate, firstObservationDate)
            val maxFilterDate = maxDate(lastHarvestDate, lastObservationDate)

            val filterStartDate = stateToRestore?.filterStartDate?.takeIf {
                it in minFilterDate..maxFilterDate
            } ?: minFilterDate

            val filterEndDate = stateToRestore?.filterEndDate?.takeIf {
                it in filterStartDate..maxFilterDate
            } ?: maxFilterDate

            val diaryFilter = stateToRestore?.diaryFilter
                ?: DiaryFilter(DiaryFilter.EventType.ALL, DiaryFilter.AcceptStatus.ALL)
            val allEventsModel = DiaryViewModel(sortedHarvests, sortedObservations)

            // We've got the diary and thus state restoration was done (either from
            // previous values or from unreproducible state set from outside). Clear the
            // stateToRestore in order to NOT use same values again when restoring.
            clearStateToRestore()

            emit(
                ViewModelLoadStatus.Loaded(
                    ListDiaryEventsViewModel(
                        events = DiaryEvent(
                            filterStartDate = filterStartDate,
                            filterEndDate = filterEndDate,
                            minFilterDate = minFilterDate,
                            maxFilterDate = maxFilterDate,
                            diaryFilter = diaryFilter,
                            huntingGroupArea = huntingGroupArea,
                            filteredEvents = filterMarkingsOnMapViewModels(
                                allMarkings = allEventsModel,
                                filterStartDate = filterStartDate,
                                filterEndDate = filterEndDate,
                                diaryFilter = diaryFilter,
                            ),
                            allEvents = allEventsModel,
                        )
                    )
                )
            )
        }
    }

    override fun handleIntent(intent: DiaryEventIntent) {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // nothing can be done if data has not been loaded
            return
        }

        val viewModel = viewModelLoadStatus.viewModel
        when (intent) {
            is DiaryEventIntent.ChangeFilterStartDate ->
                changeFilterDates(viewModel, newFilterStartDate = intent.startDate)
            is DiaryEventIntent.ChangeFilterEndDate ->
                changeFilterDates(viewModel, newFilterEndDate = intent.endDate)
            is DiaryEventIntent.ChangeDiaryFilter ->
                changeFilterTypes(viewModel, newDiaryFilter = intent.diaryFilter)
        }
    }

    /**
     * Call this function before updating viewmodel to loading state!
     */
    private fun updateStateToRestore() {
        // since thin function is called before entering the loading state we can try to
        // obtain the current state. This way we can restore filter dates if we're just
        // refreshing the diary (i.e. viewmodel is in loaded state).
        val currentState = if (viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            getUnreproducibleState()
        } else {
            null
        }

        if (currentState != null) {
            stateToRestore = currentState
        }
    }

    private fun clearStateToRestore() {
        stateToRestore = null
    }

    override fun getUnreproducibleState(): State? {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // no state if data has not been loaded
            return null
        }

        return viewModelLoadStatus.viewModel.events?.let {
            State(it.filterStartDate, it.filterEndDate, it.diaryFilter)
        }
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    fun findHarvest(harvestTarget: GroupHuntingHarvestTarget): GroupHuntingHarvest? {
        return groupHuntingContext
            .findHuntingGroupContext(harvestTarget)
            ?.findHarvest(harvestTarget)
    }

    fun findObservation(observationTarget: GroupHuntingObservationTarget): GroupHuntingObservation? {
        return groupHuntingContext
            .findHuntingGroupContext(observationTarget)
            ?.findObservation(observationTarget)
    }

    private fun changeFilterDates(viewModel: ListDiaryEventsViewModel,
                                  newFilterStartDate: LocalDate? = null,
                                  newFilterEndDate: LocalDate? = null) {

        // nothing to do if there are no markings
        val events = viewModel.events ?: return

        val filterStartDate = newFilterStartDate ?: events.filterStartDate
        val filterEndDate = newFilterEndDate ?: events.filterEndDate

        val filteredEvents = filterMarkingsOnMapViewModels(
                allMarkings = events.allEvents,
                filterStartDate = filterStartDate,
                filterEndDate = filterEndDate,
                diaryFilter = viewModel.events.diaryFilter,
        )

        updateViewModel(ViewModelLoadStatus.Loaded(viewModel.copy(
                events = events.copy(
                        filterStartDate = filterStartDate,
                        filterEndDate = filterEndDate,
                        filteredEvents = filteredEvents
                )
        )))
    }

    private fun changeFilterTypes(
        viewModel: ListDiaryEventsViewModel,
        newDiaryFilter: DiaryFilter? = null,
    ) {
        // nothing to do if there are no markings
        val events = viewModel.events ?: return

        val diaryFilter = newDiaryFilter ?: events.diaryFilter

        val filteredEvents = filterMarkingsOnMapViewModels(
            allMarkings = events.allEvents,
            filterStartDate = viewModel.events.filterStartDate,
            filterEndDate = viewModel.events.filterEndDate,
            diaryFilter = diaryFilter,
        )

        updateViewModel(
            ViewModelLoadStatus.Loaded(
                viewModel.copy(
                    events = events.copy(
                        diaryFilter = diaryFilter,
                        filteredEvents = filteredEvents
                    )
                )
            )
        )
    }

    private fun filterMarkingsOnMapViewModels(
        allMarkings: DiaryViewModel,
        filterStartDate: LocalDate,
        filterEndDate: LocalDate,
        diaryFilter: DiaryFilter,
    ): DiaryViewModel {
        return DiaryViewModel(
            harvests = allMarkings.harvests.filter {
                it.pointOfTime.date in filterStartDate..filterEndDate &&
                        acceptStatusMatches(it.acceptStatus, diaryFilter.acceptStatus) &&
                        showHarvests(diaryFilter.eventType)
            },
            observations = allMarkings.observations.filter {
                it.pointOfTime.date in filterStartDate..filterEndDate &&
                        acceptStatusMatches(it.acceptStatus, diaryFilter.acceptStatus) &&
                        showObservations(diaryFilter.eventType)
            },
        )
    }

    private fun acceptStatusMatches(eventAcceptStatus: AcceptStatus, filterAcceptStatus: DiaryFilter.AcceptStatus): Boolean {
        return when (filterAcceptStatus) {
            DiaryFilter.AcceptStatus.ALL -> true
            DiaryFilter.AcceptStatus.ACCEPTED -> eventAcceptStatus == AcceptStatus.ACCEPTED
            DiaryFilter.AcceptStatus.PROPOSED -> eventAcceptStatus == AcceptStatus.PROPOSED
            DiaryFilter.AcceptStatus.REJECTED -> eventAcceptStatus == AcceptStatus.REJECTED
        }
    }

    private fun showHarvests(filterEventType: DiaryFilter.EventType): Boolean {
        return filterEventType == DiaryFilter.EventType.ALL ||
                filterEventType == DiaryFilter.EventType.HARVESTS
    }

    private fun showObservations(filterEventType: DiaryFilter.EventType): Boolean {
        return filterEventType == DiaryFilter.EventType.ALL ||
                filterEventType == DiaryFilter.EventType.OBSERVATIONS
    }

    /**
     * The state that cannot be restored from network.
     */
    @Serializable
    data class State(
            val filterStartDate: LocalDate,
            val filterEndDate: LocalDate,
            val diaryFilter: DiaryFilter,
    )

    companion object {
        private val logger by getLogger(DiaryController::class)
    }
}

