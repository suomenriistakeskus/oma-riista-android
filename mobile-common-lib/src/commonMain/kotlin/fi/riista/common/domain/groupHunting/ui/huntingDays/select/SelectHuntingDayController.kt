package fi.riista.common.domain.groupHunting.ui.huntingDays.select

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.IdentifiesHuntingGroup
import fi.riista.common.domain.groupHunting.ui.huntingDays.selectNoHuntingDaysText
import fi.riista.common.model.LocalDate
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class SelectHuntingDayController(
    private val groupHuntingContext: GroupHuntingContext,
    private val groupTarget: IdentifiesHuntingGroup,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<SelectHuntingDayViewModel>(),
    IntentHandler<SelectHuntingDayIntent>,
    HasUnreproducibleState<SelectHuntingDayController.State> {

    init {
        ensureNeverFrozen()
    }

    /**
     * The preferred hunting day date if [initiallySelectedHuntingDayId] is null or
     * no such hunting day is found.
     */
    var preferredHuntingDayDate: LocalDate? = null

    val eventDispatcher: SelectHuntingDayEventDispatcher =
        SelectHuntingDayEventToIntentMapper(intentHandler = this)

    var selectedHuntingDayId: GroupHuntingDayId? = null
        private set

    var initiallySelectedHuntingDayId: GroupHuntingDayId? = null

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<SelectHuntingDayViewModel>> = flow {

        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = groupTarget,
                allowCached = true
        )

        val huntingDaysProvider = groupContext?.huntingDaysProvider
        val statusProvider = groupContext?.huntingStatusProvider

        huntingDaysProvider?.fetch(refresh = refresh)
        statusProvider?.fetch(refresh = refresh)

        val huntingDays = huntingDaysProvider?.huntingDays?.sortedByDescending { huntingDay ->
            // sort primarily based on startDate. Use day id as secondary sort criteria.
            // - let the latest date exist first in the list
            huntingDay.startDateTime.toStringISO8601() + "_" + huntingDay.id
        }

        if (huntingDays != null) {
            val huntingDayForPreferredDate = huntingDays.find {
                it.startDateTime.date == preferredHuntingDayDate
            }

            // invalidate selectedHuntingDayId if it no longer exists in the huntingDays
            if (huntingDays.find { it.id == selectedHuntingDayId } == null) {
                selectedHuntingDayId = null
            }

            val currentlySelectedHuntingDay = selectedHuntingDayId
                    ?: initiallySelectedHuntingDayId
                    ?: huntingDayForPreferredDate?.id

            // make selection visible to outside world
            // - it is possible that selectedHuntingDayId is null but we're still going to select
            //   a hunting day based on other criteria
            selectedHuntingDayId = currentlySelectedHuntingDay

            val huntingDayViewModels = huntingDays.map { huntingDay ->
                SelectableHuntingDayViewModel(
                        huntingDayId = huntingDay.id,
                        startDateTime = huntingDay.startDateTime,
                        endDateTime = huntingDay.endDateTime,
                        selected = huntingDay.id == currentlySelectedHuntingDay
                )
            }

            val canCreateHuntingDay = statusProvider?.status?.canCreateHuntingDay ?: false
            val noHuntingDaysText = huntingDays.selectNoHuntingDaysText(
                    speciesCode = groupContext.huntingGroup.speciesCode,
                    canCreateHuntingDay = canCreateHuntingDay
            )?.let {
                stringProvider.getString(it)
            }

            emit(ViewModelLoadStatus.Loaded(
                    SelectHuntingDayViewModel(
                            huntingDays = huntingDayViewModels,
                            selectedHuntingDayId = currentlySelectedHuntingDay,
                            canCreateHuntingDay = canCreateHuntingDay,
                            suggestedHuntingDayDate = preferredHuntingDayDate.takeIf {
                                canCreateHuntingDay && huntingDayForPreferredDate == null
                            },
                            noHuntingDaysText = noHuntingDaysText
                    )
            ))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    override fun handleIntent(intent: SelectHuntingDayIntent) {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // nothing can be done if data has not been loaded
            return
        }

        val viewModel = viewModelLoadStatus.viewModel
        val updatedViewModel = when (intent) {
            is SelectHuntingDayIntent.ChangeSelectedHuntingDay -> {
                selectedHuntingDayId = intent.huntingDayId

                viewModel.copy(
                        selectedHuntingDayId = intent.huntingDayId,
                        huntingDays = viewModel.huntingDays.map {
                            val selected = it.huntingDayId == intent.huntingDayId
                            it.copy(selected = selected)
                        }
                )
            }
        }

        updateViewModel(ViewModelLoadStatus.Loaded(updatedViewModel))
    }

    override fun getUnreproducibleState(): State? {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // no state if data has not been loaded
            return null
        }

        return State(
                selectedHuntingDayId = selectedHuntingDayId,
                initiallySelectedHuntingDayId = initiallySelectedHuntingDayId,
        )
    }

    override fun restoreUnreproducibleState(state: State) {
        selectedHuntingDayId = state.selectedHuntingDayId
        initiallySelectedHuntingDayId = state.initiallySelectedHuntingDayId
    }

    /**
     * The state that cannot be restored from network.
     */
    @Serializable
    data class State(
        val selectedHuntingDayId: GroupHuntingDayId?,
        val initiallySelectedHuntingDayId: GroupHuntingDayId?,
    )
}
