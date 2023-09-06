package fi.riista.common.domain.groupHunting.ui.huntingDays

import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservation
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.model.toLocalHuntingDay
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class ListHuntingDaysController private constructor(
    private val groupHuntingContext: GroupHuntingContext,
    private val groupTarget: HuntingGroupTarget,
    private val stringProvider: StringProvider,
    private val currentTimeProvider: LocalDateTimeProvider,
) : ControllerWithLoadableModel<ListHuntingDaysViewModel>(),
    IntentHandler<ListHuntingDaysIntent>,
    HasUnreproducibleState<ListHuntingDaysController.State> {

    constructor(
        groupHuntingContext: GroupHuntingContext,
        groupTarget: HuntingGroupTarget,
        stringProvider: StringProvider,
    ): this(groupHuntingContext = groupHuntingContext,
            groupTarget = groupTarget,
            stringProvider = stringProvider,
            currentTimeProvider = SystemDateTimeProvider()
    )

    val eventDispatcher: ListHuntingDaysEventDispatcher =
        ListHuntingDaysEventToIntentMapper(intentHandler = this)

    /**
     * The state to be restored. Used for two things
     * - keeping current state over hunting day refresh
     * - restoring state for another controller e.g. if activity was destroyed on android
     */
    private var stateToRestore: State? = null

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ListHuntingDaysViewModel>> = flow {

        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = groupTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context for $groupTarget!" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val huntingDaysProvider = groupContext.huntingDaysProvider
        val diaryProvider = groupContext.diaryProvider
        val statusProvider = groupContext.huntingStatusProvider

        huntingDaysProvider.fetch(refresh = refresh)
        diaryProvider.fetch(refresh = refresh)
        statusProvider.fetch(refresh = refresh)

        val existingHuntingDays = huntingDaysProvider.huntingDays ?: listOf()

        val harvestsByHuntingDayId: Map<GroupHuntingDayId?, List<GroupHuntingHarvest>> =
            diaryProvider.diary.harvests.groupBy { harvest ->
                harvest.huntingDayId
                        ?: existingHuntingDays.findByPointOfTime(harvest.pointOfTime)?.id
                        // no hunting day exists -> suggest one
                        ?: GroupHuntingDayId.local(harvest.pointOfTime.date)
            }
        val observationsByHuntingDayId: Map<GroupHuntingDayId?, List<GroupHuntingObservation>> =
            diaryProvider.diary.observations.groupBy { observation ->
                observation.huntingDayId
                        ?: existingHuntingDays.findByPointOfTime(observation.pointOfTime)?.id
                        // no hunting day exists -> suggest one
                        ?: GroupHuntingDayId.local(observation.pointOfTime.date)
            }

        // there can be harvests and observations for which no existing hunting day was found
        // but instead a hunting day was suggested -> create hunting days for those
        val allHuntingDayIds = (harvestsByHuntingDayId.keys + observationsByHuntingDayId.keys)
        val now = currentTimeProvider.now()
        val suggestedHuntingDays = (allHuntingDayIds - existingHuntingDays.map { it.id })
            .mapNotNull { dayId ->
                dayId?.toLocalHuntingDay(
                        huntingGroupId = groupTarget.huntingGroupId,
                        speciesCode = groupContext.huntingGroup.speciesCode,
                        now = now,
                )
            }

        val huntingDays = (existingHuntingDays + suggestedHuntingDays)
            .sortedByDescending { huntingDay ->
                // sort primarily based on startDate. Use day id as secondary sort criteria.
                // - let the latest date exist first in the list
                huntingDay.startDateTime.toStringISO8601() + "_" + huntingDay.id
            }

        val canEditHuntingDays = statusProvider.status?.canEditHuntingDay ?: false
        val canCreateHuntingDay = statusProvider.status?.canCreateHuntingDay ?: false

        if (huntingDays.isNotEmpty()) {
            val huntingDayViewModels = huntingDays.map { huntingDay ->
                val harvests = harvestsByHuntingDayId[huntingDay.id] ?: listOf()
                val observations = observationsByHuntingDayId[huntingDay.id] ?: listOf()
                val hasProposedEntries =
                    (harvests.find { it.acceptStatus == AcceptStatus.PROPOSED } != null) ||
                            (observations.find { it.acceptStatus == AcceptStatus.PROPOSED } != null)

                val isMoose = groupContext.huntingGroup.speciesCode.isMoose()
                val canCreate = canCreateHuntingDay && huntingDay.type.isLocal() && isMoose

                HuntingDayViewModel(
                        huntingDay = huntingDay,
                        harvests = harvests.map { it.toHuntingDayHarvestViewModel() },
                        observations = observations.map { it.toHuntingDayObservationViewModel() },
                        hasProposedEntries = hasProposedEntries,
                        canEditHuntingDay = canEditHuntingDays && huntingDay.type.isRemote(),
                        canCreateHuntingDay = canCreate,
                        showHuntingDayDetails = isMoose
                )
            }

            // hunting days is not empty -> we can use first() and last()
            // - the latest date is the first
            val minFilterDate = huntingDays.last().startDateTime.date
            val maxFilterDate = huntingDays.first().startDateTime.date

            // If a new hunting day has been added then make sure it is included in the filter
            val newHuntingDays = stateToRestore?.huntingDayIds?.let { restoredHuntingDays ->
                huntingDays.filter { day -> !restoredHuntingDays.contains(day.id) }
            }

            val filterStartDate = listOfNotNull(
                stateToRestore?.filterStartDate?.takeIf {
                    it in minFilterDate..maxFilterDate
                },
                newHuntingDays?.map { huntingDay -> huntingDay.startDateTime.date }?.minOrNull()
            ).minOrNull() ?: minFilterDate

            val filterEndDate = listOfNotNull(
                stateToRestore?.filterEndDate?.takeIf {
                    it in filterStartDate..maxFilterDate
                },
                newHuntingDays?.map { huntingDay -> huntingDay.startDateTime.date }?.maxOrNull()
            ).maxOrNull() ?: maxFilterDate

            // We've got the hunting days and thus state restoration was done (either from
            // previous values or from unreproducible state set from outside). Clear the
            // stateToRestore in order to NOT use same values again when restoring.
            clearStateToRestore()

            emit(ViewModelLoadStatus.Loaded(ListHuntingDaysViewModel(
                    huntingDays = HuntingDays(
                            filterStartDate = filterStartDate,
                            filterEndDate = filterEndDate,
                            minFilterDate = minFilterDate,
                            maxFilterDate = maxFilterDate,
                            filteredHuntingDays = filterHuntingDayViewModels(
                                    allHuntingDays = huntingDayViewModels,
                                    filterStartDate = filterStartDate,
                                    filterEndDate = filterEndDate
                            ),
                            allHuntingDays = huntingDayViewModels
                    ),
                    canCreateHuntingDay = canCreateHuntingDay,
                    noHuntingDaysText = null,
            )))
        } else {
            val noHuntingDaysText = huntingDays.selectNoHuntingDaysText(
                    speciesCode = groupContext.huntingGroup.speciesCode,
                    canCreateHuntingDay = canCreateHuntingDay,
            )?.let {
                stringProvider.getString(it)
            }

            emit(ViewModelLoadStatus.Loaded(ListHuntingDaysViewModel(
                    huntingDays = null,
                    canCreateHuntingDay = canCreateHuntingDay,
                    noHuntingDaysText = noHuntingDaysText
            )))
        }
    }

    override fun handleIntent(intent: ListHuntingDaysIntent) {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // nothing can be done if data has not been loaded
            return
        }

        val viewModel = viewModelLoadStatus.viewModel
        when (intent) {
            is ListHuntingDaysIntent.ChangeFilterStartDate ->
                changeFilterDates(viewModel, newFilterStartDate = intent.startDate)
            is ListHuntingDaysIntent.ChangeFilterEndDate ->
                changeFilterDates(viewModel, newFilterEndDate = intent.endDate)
        }
    }

    /**
     * Call this function before updating viewmodel to loading state!
     */
    private fun updateStateToRestore() {
        // since thin function is called before entering the loading state we can try to
        // obtain the current state. This way we can restore filter dates if we're just
        // refreshing hunting days (i.e. viewmodel is in loaded state).
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

        return viewModelLoadStatus.viewModel.huntingDays?.let { huntingDays ->
            State(
                filterStartDate = huntingDays.filterStartDate,
                filterEndDate = huntingDays.filterEndDate,
                huntingDayIds = huntingDays.allHuntingDays.map { it.huntingDay.id }
            )
        }
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    private fun changeFilterDates(viewModel: ListHuntingDaysViewModel,
                                  newFilterStartDate: LocalDate? = null,
                                  newFilterEndDate: LocalDate? = null) {
        // nothing to do if there are no hunting days
        val huntingDays = viewModel.huntingDays ?: return

        val filterStartDate = newFilterStartDate ?: huntingDays.filterStartDate
        val filterEndDate = newFilterEndDate ?: huntingDays.filterEndDate

        val filteredHuntingDays = filterHuntingDayViewModels(
                allHuntingDays = huntingDays.allHuntingDays,
                filterStartDate = filterStartDate,
                filterEndDate = filterEndDate
        )

        updateViewModel(ViewModelLoadStatus.Loaded(viewModel.copy(
                huntingDays = huntingDays.copy(
                        filterStartDate = filterStartDate,
                        filterEndDate = filterEndDate,
                        filteredHuntingDays = filteredHuntingDays
                )
        )))
    }

    private fun filterHuntingDayViewModels(allHuntingDays: List<HuntingDayViewModel>,
                                           filterStartDate: LocalDate,
                                           filterEndDate: LocalDate
    ): List<HuntingDayViewModel> {
        return allHuntingDays.filter {
            it.huntingDay.startDateTime.date in filterStartDate..filterEndDate
        }
    }

    /**
     * The state that cannot be restored from network.
     */
    @Serializable
    data class State(
        val filterStartDate: LocalDate,
        val filterEndDate: LocalDate,
        val huntingDayIds: List<GroupHuntingDayId>,
    )

    companion object {
        private val logger by getLogger(ListHuntingDaysController::class)
    }
}

private fun List<GroupHuntingDay>.findByPointOfTime(localDateTime: LocalDateTime): GroupHuntingDay? {
    return firstOrNull { huntingDay ->
        huntingDay.startDateTime.date == localDateTime.date
    }
}
