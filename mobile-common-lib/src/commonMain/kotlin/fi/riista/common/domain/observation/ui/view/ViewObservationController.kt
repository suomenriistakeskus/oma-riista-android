package fi.riista.common.domain.observation.ui.view

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.toObservationData
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.ui.ObservationFields
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for viewing [CommonObservation] information
 */
class ViewObservationController(
    private val userContext: UserContext,
    metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewObservationViewModel>() {

    private val observationFields = ObservationFields(metadataProvider)
    private val dataFieldProducer = ViewObservationFieldProducer(userContext, metadataProvider, stringProvider)

    var observation: CommonObservation? = null

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ViewObservationViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val observationData = observation?.toObservationData()
        if (observationData != null) {
            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    observation = observationData,
                )
            ))
        } else {
            logger.w { "Did you forget to set observation before loading viewModel?" }
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        observation: CommonObservationData,
    ): ViewObservationViewModel {
        return ViewObservationViewModel(
            observation = observation,
            fields = produceDataFields(observation),
            canEdit = observation.canEdit,
        )
    }

    private fun produceDataFields(observation: CommonObservationData): List<DataField<CommonObservationField>> {
        val fieldsToBeDisplayed = observationFields.getFieldsToBeDisplayed(
            ObservationFields.Context(
                observation = observation,
                userIsCarnivoreAuthority = userContext.userIsCarnivoreAuthority,
                mode = ObservationFields.Context.Mode.VIEW
            )
        )

        return fieldsToBeDisplayed.map { fieldSpecification ->
            dataFieldProducer.createField(
                fieldSpecification = fieldSpecification,
                observation = observation
            )
        }
    }

    companion object {
        private val logger by getLogger(ViewObservationController::class)
    }
}

