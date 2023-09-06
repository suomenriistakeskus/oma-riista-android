package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.observation.ObservationContext
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for editing [CommonObservation] data.
 */
class EditObservationController(
    userContext: UserContext,
    observationContext: ObservationContext,
    metadataProvider: MetadataProvider,
    localDateTimeProvider: LocalDateTimeProvider,
    stringProvider: StringProvider,
) : ModifyObservationController(userContext, observationContext, metadataProvider, localDateTimeProvider, stringProvider) {

    constructor(
        userContext: UserContext,
        observationContext: ObservationContext,
        metadataProvider: MetadataProvider,
        stringProvider: StringProvider,
    ): this(
        userContext = userContext,
        observationContext = observationContext,
        metadataProvider = metadataProvider,
        localDateTimeProvider = SystemDateTimeProvider(),
        stringProvider = stringProvider,
    )

    var editableObservation: EditableObservation? = null

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyObservationViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val observationData = restoredObservationData
            ?: editableObservation?.observation?.copy(
                // transform to latest spec version when editing
                observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION
            )

        if (observationData != null) {
            val viewModel = createViewModel(
                observation = observationData,
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }
}

