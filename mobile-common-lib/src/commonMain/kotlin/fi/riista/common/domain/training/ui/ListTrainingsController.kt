package fi.riista.common.domain.training.ui

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.training.TrainingContext
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ListTrainingsController(
    private val trainingContext: TrainingContext,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ListTrainingsViewModel>() {

    init {
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ListTrainingsViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        trainingContext.fetchTrainings(refresh = refresh)

        val trainings = trainingContext.trainings

        if (trainings == null) {
            emit(ViewModelLoadStatus.LoadFailed)
        } else {
            emit(
                ViewModelLoadStatus.Loaded(
                    ListTrainingsViewModel(
                        trainings = (trainings.jhtTrainings.map { jhtTraining ->
                            TrainingViewModel.JhtTraining(
                                id = "jhtTraining_${jhtTraining.id}".hashCode().toLong(),
                                trainingType = jhtTraining.trainingType.localized(stringProvider),
                                occupationType = jhtTraining.occupationType.localized(stringProvider),
                                date = jhtTraining.date,
                                location = jhtTraining.location,
                            )
                        } + trainings.occupationTrainings.map { occupationTraining ->
                            TrainingViewModel.OccupationTraining(
                                id = "occupationTraining_${occupationTraining.id}".hashCode().toLong(),
                                trainingType = occupationTraining.trainingType.localized(stringProvider),
                                occupationType = occupationTraining.occupationType.localized(stringProvider),
                                date = occupationTraining.date,
                            )
                        }).sortedByDescending { it.date }
                    )
                )
            )
        }
    }
}
