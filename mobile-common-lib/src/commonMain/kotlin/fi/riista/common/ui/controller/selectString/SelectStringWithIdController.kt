package fi.riista.common.ui.controller.selectString

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class SelectStringWithIdController(
    private val possibleValues: List<StringWithId>,
    private val initiallySelectedValue: StringId?,
) : ControllerWithLoadableModel<SelectStringWithIdViewModel>(),
    IntentHandler<SelectStringWithIdIntent>,
    HasUnreproducibleState<SelectStringWithIdController.State> {

    private var stateToRestore: State? = null
    var selectedValue: StringWithId? = null

    val eventDispatcher: SelectStringWithIdEventDispatcher = SelectStringWithIdEventToIntentMapper(intentHandler = this)

    init {
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<SelectStringWithIdViewModel>> = flow {

        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        // no asynchronous loading -> no need for loading

        val filter = stateToRestore?.filter ?: ""
        selectedValue = stateToRestore?.selectedValue
            ?: possibleValues.find { it.id == initiallySelectedValue }
        val selectedValueId = selectedValue?.id

        // state restoration was done -> Clear the stateToRestore in order to NOT use
        // same values again when restoring.
        clearStateToRestore()

        val allValues = possibleValues.map { value ->
            SelectableStringWithId(
                value = value,
                selected = (value.id == selectedValueId)
            )
        }
        emit(
            ViewModelLoadStatus.Loaded(
                viewModel = SelectStringWithIdViewModel(
                    allValues = allValues,
                    filteredValues = filterValues(allValues, filter),
                    filter = filter,
                    selectedValue = selectedValue,
                )
            )
        )
    }

    override fun handleIntent(intent: SelectStringWithIdIntent) {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // nothing can be done if data has not been loaded
            return
        }

        val viewModel = viewModelLoadStatus.viewModel
        when (intent) {
            is SelectStringWithIdIntent.ChangeFilter ->
                changeFilter(viewModel, newFilter = intent.filter)
            is SelectStringWithIdIntent.SelectStringWithId ->
                selectValue(viewModel, intent.value)
        }
    }

    private fun changeFilter(
        viewModel: SelectStringWithIdViewModel,
        newFilter: String? = null
    ) {
        val filter = newFilter ?: viewModel.filter
        updateViewModel(ViewModelLoadStatus.Loaded(viewModel.copy(
            filter = filter,
            filteredValues = filterValues(viewModel.allValues, filter),
        )))
    }

    private fun filterValues(
        allMembers: List<SelectableStringWithId>,
        filter: String,
    ): List<SelectableStringWithId> {
        return if (filter.isEmpty()) {
            allMembers
        } else {
            // Include the selected value even if it doesn't match the filter
            allMembers.filter { member ->
                member.value.string.lowercase().contains(filter.lowercase()) || member.selected
            }
        }
    }

    private fun selectValue(
        viewModel: SelectStringWithIdViewModel,
        newSelectedValue: StringWithId?,
    ) {
        selectedValue = newSelectedValue

        val newAllValues = viewModel.allValues.map { model ->
            SelectableStringWithId(
                value = model.value,
                selected = (model.value.id == newSelectedValue?.id)
            )
        }
        val newFilteredValues = viewModel.filteredValues.map { model ->
            SelectableStringWithId(
                value = model.value,
                selected = (model.value.id == newSelectedValue?.id)
            )
        }
        updateViewModel(ViewModelLoadStatus.Loaded(viewModel.copy(
            selectedValue = newSelectedValue,
            allValues = newAllValues,
            filteredValues = newFilteredValues,
        )))
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
        return getLoadedViewModelOrNull()?.let { viewModel ->
            State(
                filter = viewModel.filter,
                selectedValue =  viewModel.selectedValue,
            )
        }
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    /**
     * The state that cannot be restored from network.
     */
    @Serializable
    data class State(
        val filter: String,
        val selectedValue: StringWithId?,
    )
}
