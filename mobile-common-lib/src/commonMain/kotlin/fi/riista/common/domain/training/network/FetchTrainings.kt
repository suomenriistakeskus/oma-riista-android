package fi.riista.common.domain.training.network

import fi.riista.common.domain.training.dto.TrainingsDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchTrainings: NetworkRequest<TrainingsDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<TrainingsDTO> {
        return client.request(
             request = {
                get(urlString = "${client.serverBaseAddress}/api/mobile/v2/trainings") {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
