package fi.riista.common.poi.ui

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.poi.PoiContext
import fi.riista.common.poi.model.PoiLocation
import fi.riista.common.poi.model.PoiLocationGroup
import fi.riista.common.poi.model.PoiLocationId
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class PoiController(
    private val poiContext: PoiContext,
    externalId: String? = null,
    private val initialFilter: PoiFilter = PoiFilter(PoiFilter.PoiFilterType.ALL),
) : ControllerWithLoadableModel<PoisViewModel>(),
    IntentHandler<PoiEventIntent>,
    HasUnreproducibleState<PoiController.State> {

    val eventDispatcher: PoiEventDispatcher = PoiEventToIntentMapper(intentHandler = this)

    var externalId: String? = null
        /**
         * If given externalId is different than previous one, then sets a new externalId
         * and marks the model as not loaded. Call loadViewModel afterwards.
         */
        set(value) {
            if (value == field) {
                return
            }
            field = value
            filterBeforeExternalIdChange = getLoadedViewModelOrNull()?.pois?.filter
            updateViewModel(ViewModelLoadStatus.NotLoaded)
        }

    init {
        this.externalId = externalId
    }

    private var stateToRestore: State? = null
    private var filterBeforeExternalIdChange: PoiFilter? = null

    init {
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<PoisViewModel>> = flow {
        updateStateToRestore()
        emit(ViewModelLoadStatus.Loading)

        val filter = stateToRestore?.poiFilter ?: filterBeforeExternalIdChange ?: initialFilter
        filterBeforeExternalIdChange = null

        val extId = externalId
        if (extId.isNullOrBlank()) {
            emit(
                ViewModelLoadStatus.Loaded(
                    PoisViewModel(
                        pois = PoiViewModel(
                            filter = filter,
                            filteredPois = listOf(),
                            allPois = listOf(),
                        )
                    )
                )
            )
            return@flow
        }

        val locationGroupContext = poiContext.getPoiLocationGroupContext(extId)
        locationGroupContext.fetch(refresh)

        if (locationGroupContext.poiLocationGroups == null) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val allPois = locationGroupContext.poiLocationGroups ?: listOf()

        // We've got the pois and thus state restoration was done (either from
        // previous values or from unreproducible state set from outside). Clear the
        // stateToRestore in order to NOT use same values again when restoring.
        clearStateToRestore()

        emit(
            ViewModelLoadStatus.Loaded(
                PoisViewModel(
                    pois = PoiViewModel(
                        filter = filter,
                        filteredPois = filterPois(allPois, filter),
                        allPois = allPois,
                    )
                )
            )
        )
    }

    override fun handleIntent(intent: PoiEventIntent) {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // nothing can be done if data has not been loaded
            return
        }

        when (intent) {
            is PoiEventIntent.ChangePoiFilter -> changeFilterType(viewModelLoadStatus.viewModel, intent.poiFilter)
        }
    }

    fun findPoiLocationAndItsGroup(poiLocationId: PoiLocationId): Pair<PoiLocationGroup, PoiLocation>? {
        getLoadedViewModelOrNull()?.pois?.allPois?.forEach { poiLocationGroup ->
            poiLocationGroup.locations.forEach { poiLocation ->
                if (poiLocation.id == poiLocationId) {
                    return Pair(poiLocationGroup, poiLocation)
                }
            }
        }
        return null
    }

    private fun changeFilterType(
        viewModel: PoisViewModel,
        newFilter: PoiFilter,
    ) {
        // nothing to do if there are no pois
        val pois = viewModel.pois ?: return

        updateViewModel(
            ViewModelLoadStatus.Loaded(
                viewModel.copy(
                    pois = pois.copy(
                        filter = newFilter,
                        filteredPois = filterPois(viewModel.pois.allPois, newFilter)
                    )
                )
            )
        )
    }

    private fun filterPois(allPois: List<PoiLocationGroup>, filter: PoiFilter): List<PoiLocationGroup> {
        return allPois.filter { poi -> filter.matches(poi.type.value) }
    }

    /**
     * Call this function before updating viewmodel to loading state!
     */
    private fun updateStateToRestore() {
        // since thin function is called before entering the loading state we can try to
        // obtain the current state. This way we can restore filter if we're just
        // refreshing the pois (i.e. viewmodel is in loaded state).
        val currentState =
            if (viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
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

        return viewModelLoadStatus.viewModel.pois?.let {
            State(it.filter)
        }
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    @Serializable
    data class State(
        val poiFilter: PoiFilter,
    )
}
