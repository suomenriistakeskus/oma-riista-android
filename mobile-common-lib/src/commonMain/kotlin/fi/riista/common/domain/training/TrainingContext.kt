package fi.riista.common.domain.training

import fi.riista.common.domain.training.model.Trainings
import fi.riista.common.network.BackendApiProvider

class TrainingContext internal constructor(
    private val backendApiProvider: BackendApiProvider,
): BackendApiProvider by backendApiProvider {
    private val trainingsProvider = TrainingsFromNetworkProvider(backendApiProvider)

    val trainings: Trainings?
        get() = trainingsProvider.trainings

    suspend fun fetchTrainings(refresh: Boolean = false) {
        trainingsProvider.fetch(refresh)
    }

    fun clear() {
        trainingsProvider.clear()
    }
}
