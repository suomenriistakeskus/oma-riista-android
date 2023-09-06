package fi.riista.common.domain.groupHunting.network

import fi.riista.common.domain.groupHunting.dto.GroupHuntingObservationDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingObservationUpdateDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.accept
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class UpdateGroupHuntingObservation(
    private val observation: GroupHuntingObservationUpdateDTO
) : NetworkRequest<GroupHuntingObservationDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingObservationDTO> {
        val observationId = observation.id
        val payload = observation.serializeToJson()

        requireNotNull(payload) {
            "Failed to serialize observation data to json"
        }

        return client.request(
            request = {
                put(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/observation/$observationId") {
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
