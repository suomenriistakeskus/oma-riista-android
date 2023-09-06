package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.model.toHuntingControlEventData
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EditHuntingControlEventController(
    private val huntingControlEventTarget: HuntingControlEventTarget,
    stringProvider: StringProvider,
    huntingControlContext: HuntingControlContext,
    commonFileProvider: CommonFileProvider,
    userContext: UserContext,
) : ModifyHuntingControlEventController(stringProvider, huntingControlContext, userContext, commonFileProvider, huntingControlEventTarget) {

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ModifyHuntingControlEventViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val rhyContext = huntingControlContext.findRhyContext(
            identifiesRhy = huntingControlEventTarget,
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the RHY (id: ${huntingControlEventTarget.rhyId})" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val gameWardens = rhyContext.fetchGameWardens()
        val fetchedEvent = rhyContext.findHuntingControlEvent(huntingControlEventTarget)
        val eventData = restoredEvent ?: fetchedEvent?.toHuntingControlEventData()
            ?.copy(specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION) // Use latest specVersion when modifying an event

        if (eventData != null) {
            val viewModel = createViewModel(
                event = eventData,
                allGameWardens = gameWardens,
                selfInspectorWarning = false,
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    companion object {
        private val logger by getLogger(EditHuntingControlEventController::class)
    }
}
