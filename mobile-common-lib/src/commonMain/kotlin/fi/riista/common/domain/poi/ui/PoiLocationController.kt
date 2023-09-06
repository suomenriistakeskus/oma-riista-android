package fi.riista.common.domain.poi.ui

import fi.riista.common.domain.poi.PoiLocationGroupContext
import fi.riista.common.domain.poi.model.PoiLocation
import fi.riista.common.domain.poi.model.PoiLocationGroupId
import fi.riista.common.domain.poi.model.PoiLocationId
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class PoiLocationController(
    private val locationGroupContext: PoiLocationGroupContext,
    private val poiLocationGroupId: PoiLocationGroupId,
    private val initiallySelectedPoiLocationId: PoiLocationId,
) : ControllerWithLoadableModel<PoiLocationsViewModel>(),
    IntentHandler<PoiLocationEventIntent>,
    HasUnreproducibleState<PoiLocationController.State> {

    val eventDispatcher: PoiLocationEventDispatcher = PoiLocationEventToIntentMapper(intentHandler = this)

    private var selectedId: PoiLocationId = 0
    private var stateToRestore: State? = null


    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<PoiLocationsViewModel>> = flow {
        updateStateToRestore()
        emit(ViewModelLoadStatus.Loading)

        locationGroupContext.fetch(refresh = refresh)

        val poiLocationGroup = locationGroupContext.findPoiLocationGroup(poiLocationGroupId)

        if (poiLocationGroup == null) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val poiLocations = poiLocationGroup.locations

        selectedId = stateToRestore?.selectedId ?: initiallySelectedPoiLocationId
        val selectedIndex = getIndexForId(selectedId, poiLocations)
        clearStateToRestore()

        emit(
            ViewModelLoadStatus.Loaded(
                PoiLocationsViewModel(
                    selectedIndex = selectedIndex,
                    poiLocations = poiLocations.map { poiLocation ->
                        PoiLocationViewModel(
                            groupVisibleId = poiLocationGroup.visibleId,
                            groupDescription = poiLocationGroup.description,
                            groupType = poiLocationGroup.type,
                            id = poiLocation.id,
                            visibleId = poiLocation.visibleId,
                            description = poiLocation.description,
                            location = poiLocation.geoLocation,
                        )
                    }
                )
            )
        )
    }

    private fun getIndexForId(id: PoiLocationId, poiLocations: List<PoiLocation>) : Int {
        for (i in poiLocations.indices) {
            if (poiLocations[i].id == id) {
                return i
            }
        }
        return 0
    }

    override fun handleIntent(intent: PoiLocationEventIntent) {
        val viewModel = getLoadedViewModelOrNull() ?: return

        val poiLocations = viewModel.poiLocations
        when (intent) {
            is PoiLocationEventIntent.SelectPoiLocation -> {
                if (intent.index in poiLocations.indices) {
                    selectedId = poiLocations[intent.index].id
                    updateViewModel(
                        ViewModelLoadStatus.Loaded(
                            viewModel.copy(
                                selectedIndex = intent.index
                            )
                        )
                    )
                } else {
                    logger.w { "Invalid index when selecting POI location ${intent.index}" }
                }
            }
        }
    }

    private fun updateStateToRestore() {
        // since thin function is called before entering the loading state we can try to
        // obtain the current state. This way we can restore selected index if we're just
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

        return State(selectedId)
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    /**
     * The state that cannot be restored from network.
     */
    @Serializable
    data class State(
        val selectedId: PoiLocationId,
    )

    companion object {
        private val logger by getLogger(PoiLocationController::class)
    }
}
