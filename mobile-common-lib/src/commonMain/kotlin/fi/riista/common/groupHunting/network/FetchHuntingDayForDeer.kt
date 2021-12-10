package fi.riista.common.groupHunting.network

import fi.riista.common.groupHunting.dto.GroupHuntingDayDTO
import fi.riista.common.groupHunting.dto.GroupHuntingDayForDeerDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchHuntingDayForDeer(
    private val huntingDayForDeerDTO: GroupHuntingDayForDeerDTO
) : NetworkRequest<GroupHuntingDayDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingDayDTO> {
        val payload = huntingDayForDeerDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize hunting day data to json"
        }

        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/huntingday/get-or-create") {
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
