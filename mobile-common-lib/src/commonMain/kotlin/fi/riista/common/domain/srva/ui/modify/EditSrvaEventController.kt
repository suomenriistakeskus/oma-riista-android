package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for editing [CommonSrvaEvent] data.
 */
class EditSrvaEventController(
    metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
) : ModifySrvaEventController(metadataProvider, stringProvider) {

    var editableSrvaEvent: EditableSrvaEvent? = null

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifySrvaEventViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val srvaEventData = restoredSrvaEventData
            ?: editableSrvaEvent?.srvaEventData?.copy(
                // transform to latest version when editing SRVA
                srvaSpecVersion = Constants.SRVA_SPEC_VERSION
            )

        if (srvaEventData != null) {
            val viewModel = createViewModel(
                srvaEvent = srvaEventData,
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }
}

