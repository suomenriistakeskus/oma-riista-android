package fi.riista.common.domain.srva.ui.view

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.toSrvaEventData
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.domain.srva.ui.SrvaEventFields
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for viewing [CommonSrvaEvent] information
 */
class ViewSrvaEventController(
    metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewSrvaEventViewModel>() {

    private val srvaEventFields = SrvaEventFields(metadataProvider = metadataProvider)
    private val dataFieldProducer = ViewSrvaEventFieldProducer(stringProvider = stringProvider)

    var srvaEvent: CommonSrvaEvent? = null

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ViewSrvaEventViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val event = srvaEvent?.toSrvaEventData()
        if (event != null) {
            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    srvaEvent = event,
                )
            ))
        } else {
            logger.w { "Did you forget to set SRVA event before loading viewModel?" }
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        srvaEvent: CommonSrvaEventData,
    ): ViewSrvaEventViewModel {
        return ViewSrvaEventViewModel(
            srvaEvent = srvaEvent,
            fields = produceDataFields(srvaEvent),
            canEdit = srvaEvent.canEdit,
        )
    }

    private fun produceDataFields(srvaEvent: CommonSrvaEventData): List<DataField<SrvaEventField>> {
        val fieldsToBeDisplayed = srvaEventFields.getFieldsToBeDisplayed(
            SrvaEventFields.Context(
                srvaEvent = srvaEvent,
                mode = SrvaEventFields.Context.Mode.VIEW
            )
        )

        return fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
            dataFieldProducer.createField(
                fieldSpecification = fieldSpecification,
                srvaEvent = srvaEvent
            )
        }
    }

    companion object {
        private val logger by getLogger(ViewSrvaEventController::class)
    }
}

