package fi.riista.common.domain.groupHunting.network

import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingDayUpdateDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.*
import io.ktor.http.*

internal class UpdateHuntingGroupHuntingDay(
    private val huntingDayDTO: GroupHuntingDayUpdateDTO
): NetworkRequest<GroupHuntingDayDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingDayDTO> {
        val huntingDayId = huntingDayDTO.id
        val payload = huntingDayDTO.serializeToJson()
        requireNotNull(payload) {
            "Failed to serialize hunting day data to json"
        }

        return client.request(
                request = {
                    put(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/" +
                            "huntingday/$huntingDayId") {
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