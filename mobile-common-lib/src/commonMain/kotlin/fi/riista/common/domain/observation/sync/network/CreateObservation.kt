package fi.riista.common.domain.observation.sync.network

import fi.riista.common.domain.observation.sync.dto.ObservationCreateDTO
import fi.riista.common.domain.observation.sync.dto.ObservationDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class CreateObservation(
    private val observation: ObservationCreateDTO,
) : NetworkRequest<ObservationDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<ObservationDTO> {
        val payload = observation.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize observation data to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/gamediary/observation") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(body = payload)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
