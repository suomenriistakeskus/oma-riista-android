package fi.riista.common.domain.huntingControl.ui.eventSelection

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.model.HuntingControlRhyTarget
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.model.StringWithId
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.network.sync.SyncDataPiece
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

    private var selectedRhy: Organization? = null
    private var stateToRestore: SelectedFilterValues? = null

    override fun createLoadViewModelFlow(
        refresh: Boolean,
    ): Flow<ViewModelLoadStatus<SelectHuntingControlEventViewModel>> = flow {
        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        if (!huntingControlContext.huntingControlAvailable.value || refresh) {
            emit(ViewModelLoadStatus.Loading)
            if (refresh) {
                RiistaSDK.synchronize(SyncDataPiece.HUNTING_CONTROL)
            }
        }

        if (!huntingControlContext.huntingControlAvailable.value) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val rhys = huntingControlContext.fetchRhys()
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

        this@SelectHuntingControlEventController.selectedRhy = selectedRhy

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
                    updateViewModelSuspended {
                        selectedRhy = huntingControlContext.findRhy(intent.rhyId)
                        updateViewModel(
                            ViewModelLoadStatus.Loaded(
                                viewModel = viewModel.copy(
                                    selectedRhy = selectedRhy?.toStringWithId(),
                                    events = findEventsForRhy(selectedRhy),
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun findEventsForRhy(rhy: Organization?): List<SelectHuntingControlEvent>? {
        return if (rhy != null) {
            huntingControlContext.findRhyContext(HuntingControlRhyTarget(rhy.id))
                ?.fetchHuntingControlEvents()
                ?.map { event ->
                    SelectHuntingControlEvent(
                        id = event.localId,
                        date = event.date,
                        title = event.eventType.localized(stringProvider),
                        modified = event.modified,
                    )
                }
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
        return selectedRhy?.let { rhy ->
            SelectedFilterValues(
                rhyId = rhy.id
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
}
