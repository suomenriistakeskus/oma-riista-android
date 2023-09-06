package fi.riista.common.domain.harvest.sync.network

import fi.riista.common.domain.harvest.sync.dto.HarvestCreateDTO
import fi.riista.common.domain.harvest.sync.dto.HarvestDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class CreateHarvest(
    private val harvest: HarvestCreateDTO,
) : NetworkRequest<HarvestDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<HarvestDTO> {
        val payload = harvest.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize harvest data to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/gamediary/harvest") {
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
