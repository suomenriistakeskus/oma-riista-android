package fi.riista.common.domain.poi.ui

import fi.riista.common.domain.poi.PoiContext
import fi.riista.common.domain.poi.model.PoiLocation
import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.domain.poi.model.PoiLocationGroupId
import fi.riista.common.domain.poi.model.PoiLocationId
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class PoiListController(
    private val poiContext: PoiContext,
    private val externalId: String?,
    private val filter: PoiFilter,
) : ControllerWithLoadableModel<PoiListViewModel>(),
    IntentHandler<PoiListEventIntent>,
    HasUnreproducibleState<PoiListController.State> {

    val eventDispatcher: PoiListEventDispatcher = PoiListEventToIntentMapper(intentHandler = this)

    private var stateToRestore: State? = null


    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<PoiListViewModel>> = flow {
        updateStateToRestore()
        emit(ViewModelLoadStatus.Loading)

        val extId = externalId
        if (extId.isNullOrBlank()) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val locationGroupContext = poiContext.getPoiLocationGroupContext(extId)
        locationGroupContext.fetch(refresh)
        val locationGroups = locationGroupContext.poiLocationGroups

        if (locationGroups == null) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val expandedIds = stateToRestore?.expandedIds ?: listOf()
        clearStateToRestore()

        val items = createPoiListItems(locationGroups, expandedIds)
        emit(
            ViewModelLoadStatus.Loaded(
                PoiListViewModel(
                    locationGroups = locationGroups,
                    allItems = items,
                    visibleItems = visibleItems(items),
                )
            )
        )
    }

    override fun handleIntent(intent: PoiListEventIntent) {
        val viewModelLoadStatus = viewModelLoadStatus.value
        if (viewModelLoadStatus !is ViewModelLoadStatus.Loaded) {
            // nothing can be done if data has not been loaded
            return
        }

        when (intent) {
            is PoiListEventIntent.SelectPoiGroup -> selectLocationGroup(intent.id)
        }
    }

    fun findPoiLocationAndItsGroup(poiLocationId: PoiLocationId): Pair<PoiLocationGroup, PoiLocation>? {
        getLoadedViewModelOrNull()?.locationGroups?.forEach { poiLocationGroup ->
            poiLocationGroup.locations.forEach { poiLocation ->
                if (poiLocation.id == poiLocationId) {
                    return Pair(poiLocationGroup, poiLocation)
                }
            }
        }
        return null
    }

    private fun createPoiListItems(locationGroups: List<PoiLocationGroup>, expandedIds: List<Long>): List<PoiListItem> {
        var separatorIndex = 0
        val items = locationGroups
            // First filter out items that don't match the filter user selected
            .filter { locationGroup ->
                filter.matches(locationGroup.type.value)
            }
            // Then add all remaining items to a list with a separator after every POI group
            .flatMap { locationGroup ->
                listOf(
                    PoiListItem.PoiGroupItem(
                        id = locationGroup.id,
                        text = locationGroup.description ?: "",
                        type = locationGroup.type,
                        expanded = expandedIds.contains(locationGroup.id),
                    )
                ) +
                locationGroup.locations.map { location ->
                    PoiListItem.PoiItem(
                        id = location.id,
                        text = "${locationGroup.visibleId}-${location.visibleId}",
                        description = location.description,
                        groupId = locationGroup.id,
                    )
                } +
                listOf(
                    PoiListItem.Separator(
                        id = "separators_${separatorIndex++}".hashCode().toLong(),
                    )
                )
            }
        return items
    }

    private fun selectLocationGroup(groupId: PoiLocationGroupId) {
        val viewModel = getLoadedViewModelOrNull() ?: return

        // Toggle item
        val allItems = viewModel.allItems.map { item ->
            if (item is PoiListItem.PoiGroupItem && item.id == groupId) {
                item.copy(expanded = !item.expanded)
            } else {
                item
            }
        }

        updateViewModel(
            ViewModelLoadStatus.Loaded(
                viewModel.copy(
                    allItems = allItems,
                    visibleItems = visibleItems(allItems),
                )
            )
        )
    }

    private fun visibleItems(allItems: List<PoiListItem>): List<PoiListItem> {
        val visibleGroupIds = allItems.filter { item ->
            item is PoiListItem.PoiGroupItem && item.expanded
        }.map { item ->
            item.id
        }

        return allItems.filter { item ->
            item is PoiListItem.PoiGroupItem || item is PoiListItem.Separator ||
                (item is PoiListItem.PoiItem && visibleGroupIds.contains(item.groupId))
        }
    }

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

        val expandedIds = viewModelLoadStatus.viewModel.allItems
            .filter { item -> item is PoiListItem.PoiGroupItem && item.expanded }
            .map { item -> item.id }
        return State(expandedIds)
    }

    override fun restoreUnreproducibleState(state: State) {
        stateToRestore = state
    }

    /**
     * The state that cannot be restored from network.
     */
    @Serializable
    data class State(
        val expandedIds: List<Long>,
    )
}
