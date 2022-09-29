package fi.riista.common.domain.huntingControl.ui.eventSelection

import co.touchlab.stately.concurrency.AtomicLong
import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.HuntingControlRhyContext
import fi.riista.common.domain.huntingControl.model.HuntingControlRhyTarget
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.logging.getLogger
import fi.riista.common.model.StringWithId
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.network.SyncDataPiece
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class SelectHuntingControlEventController(
    private val huntingControlContext: HuntingControlContext,
    private val languageProvider: LanguageProvider,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<SelectHuntingControlEventViewModel>(),
    IntentHandler<SelectHuntingControlEventIntent>,
    HasUnreproducibleState<SelectHuntingControlEventController.SelectedFilterValues> {

    val eventDispatcher: SelectHuntingControlEventDispatcher =
        SelectHuntingControlEventEventToIntentMapper(intentHandler = this)

    private class SelectionState(private val huntingControlContext: HuntingControlContext) {
        var selectedRhy: Organization? = null
        val rhyContext: HuntingControlRhyContext?
            get() {
                val rhy = selectedRhy
                return if (rhy != null) {
                    huntingControlContext.findRhyContext(HuntingControlRhyTarget(rhy.id))
                } else {
                    null
                }
            }
    }

    private var selectionState: SelectionState? = null
    private var stateToRestore: SelectedFilterValues? = null

    /**
     * The id of the RHY for which data fetch was performed last time?
     *
     * Allows limiting fetching data again.
     */
    private val lastDataFetchRhyId = AtomicLong(-1L)

    init {
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(
        refresh: Boolean,
    ): Flow<ViewModelLoadStatus<SelectHuntingControlEventViewModel>> = flow {
        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        if (!huntingControlContext.huntingControlAvailable || refresh) {
            emit(ViewModelLoadStatus.Loading)
            if (refresh) {
                RiistaSDK.synchronizeDataPieces(listOf(SyncDataPiece.HUNTING_CONTROL))
            }
            huntingControlContext.fetchRhys(refresh = refresh)
        }

        if (!huntingControlContext.huntingControlAvailable) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val rhys = huntingControlContext.huntingControlRhys
        if (rhys == null) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val selectedRhy = rhys.firstOrNull {
            it.id == stateToRestore?.rhyId
        } ?: if (rhys.size == 1) {
            rhys[0]
        } else {
            null
        }

        // state restoration has been done. Clear the stateToRestore in order to
        // NOT use same values again when restoring
        clearStateToRestore()

        selectionState = SelectionState(huntingControlContext)
        selectionState?.selectedRhy = selectedRhy

        emit(
            ViewModelLoadStatus.Loaded(
                SelectHuntingControlEventViewModel(
                    showRhy = rhys.size > 1,
                    rhys = rhys.mapNotNull { organization -> organization.toStringWithId() },
                    selectedRhy = selectedRhy?.toStringWithId(),
                    events = findEventsForRhy(selectedRhy),
                )
            )
        )
    }

    override fun handleIntent(intent: SelectHuntingControlEventIntent) {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            when (intent) {
                is SelectHuntingControlEventIntent.SelectRhy -> {
                    val selectedRhy = huntingControlContext.findRhy(intent.rhyId)
                    selectionState?.selectedRhy = selectedRhy
                    updateViewModel(
                        ViewModelLoadStatus.Loaded(
                            viewModel = viewModel.copy(
                                selectedRhy = selectedRhy?.toStringWithId(),
                                events = findEventsForRhy(selectedRhy),
                            )
                        ))
                }
            }
        }
    }

    suspend fun fetchRhyDataIfNeeded(refresh: Boolean = false) {
        val rhyContext = selectionState?.rhyContext
            ?: kotlin.run {
                logger.v { "Refusing to fetch data, no RHY context available!" }
                return
            }

        val rhyId = rhyContext.rhyId
        if (rhyId == lastDataFetchRhyId.get() && !refresh) {
            logger.v { "Data has been already fetched for RHY $rhyId." }
            return
        }

        lastDataFetchRhyId.set(rhyId)

        rhyContext.fetchAllData(refresh)

        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(
                ViewModelLoadStatus.Loaded(
                    viewModel = viewModel.copy(
                        selectedRhy = selectionState?.selectedRhy?.toStringWithId(),
                        events = findEventsForRhy(selectionState?.selectedRhy),
                    )
                )
            )
        }
    }

    private fun findEventsForRhy(rhy: Organization?): List<SelectHuntingControlEvent>? {
        return if (rhy != null) {
            huntingControlContext.findRhyContext(HuntingControlRhyTarget(rhy.id))?.huntingControlEvents?.map { event ->
                SelectHuntingControlEvent(
                    id = event.localId,
                    date = event.date,
                    title = event.eventType.localized(stringProvider),
                    modified = event.modified,
                )
            }?.sortedByDescending { event -> event.date }
        } else {
            null
        }
    }

    /**
     * Call this function before updating viewmodel to loading state!
     */
    private fun updateStateToRestore() {
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

    override fun getUnreproducibleState(): SelectedFilterValues? {
        return selectionState?.let { state ->
            SelectedFilterValues(
                rhyId = state.selectedRhy?.id
            )
        }
    }

    override fun restoreUnreproducibleState(state: SelectedFilterValues) {
        stateToRestore = state
    }

    @Serializable
    data class SelectedFilterValues(
        val rhyId: OrganizationId?,
    )

    /**
     * Converts RHYs (i.e [Organization]s) to [StringWithId]
     */
    private fun Organization.toStringWithId(): StringWithId? {
        return name.localizedWithFallbacks(languageProvider)
            ?.let { name ->
                StringWithId(name, id)
            }
    }

    companion object {
        private val logger by getLogger(SelectHuntingControlEventController::class)
    }
}
