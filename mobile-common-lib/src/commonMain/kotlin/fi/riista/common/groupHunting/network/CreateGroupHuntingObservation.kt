package fi.riista.common.groupHunting.network

import fi.riista.common.groupHunting.dto.GroupHuntingObservationCreateDTO
import fi.riista.common.groupHunting.dto.GroupHuntingObservationDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.*
import io.ktor.http.*

internal class CreateGroupHuntingObservation(
    private val observationDTO: GroupHuntingObservationCreateDTO
): NetworkRequest<GroupHuntingObservationDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingObservationDTO> {
        val payload = observationDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize observation data to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/observation") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    body = payload
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
