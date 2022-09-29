package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.HuntingControlEventOperationResponse
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.model.toHuntingControlEventData
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EditHuntingControlEventController(
    private val huntingControlContext: HuntingControlContext,
    private val huntingControlEventTarget: HuntingControlEventTarget,
    stringProvider: StringProvider,
    commonFileProvider: CommonFileProvider,
) : ModifyHuntingControlEventController(stringProvider, commonFileProvider) {

    suspend fun saveHuntingControlEvent(): HuntingControlEventOperationResponse {
        val eventData = getLoadedViewModelOrNull()?.event ?: kotlin.run {
            logger.w { "Failed to obtain hunting control event from viewModel in order to create it" }
            return HuntingControlEventOperationResponse.Error
        }

        val rhyContext = huntingControlContext.findRhyContext(
            identifiesRhy = huntingControlEventTarget,
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the RHY (id: ${huntingControlEventTarget.rhyId})" }
            return HuntingControlEventOperationResponse.Error
        }

        moveAttachmentsToAttachmentsDirectory()

        val eventToBeSaved = eventData.copy(modified = true)
        return rhyContext.updateHuntingControlEvent(eventToBeSaved)
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ModifyHuntingControlEventViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        huntingControlContext.fetchRhys(refresh = refresh)

        val rhyContext = huntingControlContext.findRhyContext(
            identifiesRhy = huntingControlEventTarget,
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the RHY (id: ${huntingControlEventTarget.rhyId})" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        rhyContext.fetchAllData(refresh = refresh)
        val gameWardens = rhyContext.gameWardens
        val fetchedEvent = rhyContext.findHuntingControlEvent(huntingControlEventTarget)
        val eventData = restoredEvent ?: fetchedEvent?.toHuntingControlEventData()
            ?.copy(specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION) // Use latest specVersion when modifying an event

        if (eventData != null && gameWardens != null) {
            val viewModel = createViewModel(
                event = eventData,
                allGameWardens = gameWardens,
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
