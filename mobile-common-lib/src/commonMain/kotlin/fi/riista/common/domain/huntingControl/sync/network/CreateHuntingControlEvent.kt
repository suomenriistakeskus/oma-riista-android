package fi.riista.common.domain.huntingControl.sync.network

import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventCreateDTO
import fi.riista.common.domain.huntingControl.sync.dto.HuntingControlEventDTO
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class CreateHuntingControlEvent(
    private val rhyId: OrganizationId,
    private val event: HuntingControlEventCreateDTO,
) : NetworkRequest<HuntingControlEventDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HuntingControlEventDTO> {
        val payload = event.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize hunting control event data to json"
        }

        val specVersion = event.specVersion
        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/huntingcontrol/event/$rhyId?requestedSpecVersion=$specVersion") {
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
