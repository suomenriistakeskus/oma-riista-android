package fi.riista.common.domain.training

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.domain.training.dto.toTrainings
import fi.riista.common.domain.training.model.Trainings
import fi.riista.common.model.LoadStatus
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher

interface TrainingProvider: DataFetcher {
    val trainings: Trainings?
}

internal class TrainingsFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
) : TrainingProvider,
    NetworkDataFetcher<TrainingsDTO>(),
    BackendApiProvider by backendApiProvider
{
    private var _trainings = AtomicReference<Trainings?>(null)
    override val trainings: Trainings?
        get() = _trainings.value

    override suspend fun fetchFromNetwork(): NetworkResponse<TrainingsDTO> =
        backendAPI.fetchTrainings()

    override fun handleSuccess(statusCode: Int, responseData: NetworkResponseData<out TrainingsDTO>) {
        val fetchedTrainings = responseData.typed.toTrainings()
        _trainings.set(fetchedTrainings)
    }

    override fun handleError401() {
        _trainings.set(null)
    }

    fun clear() {
        _trainings.set(null)
        loadStatus.set(LoadStatus.NotLoaded())
    }
}
