package fi.riista.common.domain.srva.sync.network

import fi.riista.common.domain.srva.sync.dto.SrvaEventCreateDTO
import fi.riista.common.domain.srva.sync.dto.SrvaEventDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class CreateSrvaEvent(
    private val event: SrvaEventCreateDTO,
) : NetworkRequest<SrvaEventDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<SrvaEventDTO> {
        val payload = event.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize SRVA event data to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/srva/srvaevent") {
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
