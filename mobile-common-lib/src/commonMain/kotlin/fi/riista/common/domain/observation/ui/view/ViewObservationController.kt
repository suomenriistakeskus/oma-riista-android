package fi.riista.common.domain.observation.ui.view

import fi.riista.common.domain.observation.ObservationContext
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
    private val observationId: Long,
    private val observationContext: ObservationContext,
    private val userContext: UserContext,
    metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewObservationViewModel>() {

    private val observationFields = ObservationFields(metadataProvider)
    private val dataFieldProducer = ViewObservationFieldProducer(userContext, metadataProvider, stringProvider)


    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ViewObservationViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        observationContext.observationProvider.fetch(refresh = refresh)

        val observationData = observationContext.observationProvider.getByLocalId(observationId)?.toObservationData()
        if (observationData != null) {
            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    observation = observationData,
                )
            ))
        } else {
            logger.w { "No observation found with local id $observationId." }
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    suspend fun deleteObservation(updateToBackend: Boolean): Boolean {
        val observationId = getLoadedViewModelOrNull()?.observation?.localId ?: kotlin.run {
            logger.w { "No observation found, cannot delete" }
            return false
        }

        val deletedObservation = observationContext.deleteObservation(observationId)
        return if (deletedObservation != null) {
            if (updateToBackend) {
                observationContext.deleteObservationInBackend(deletedObservation)
            }
            true
        } else {
            false
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

        return fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
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

