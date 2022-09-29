package fi.riista.common.domain.groupHunting.network

import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestCreateDTO
import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.*
import io.ktor.http.*

internal class CreateGroupHuntingHarvest(
    private val harvest: GroupHuntingHarvestCreateDTO
) : NetworkRequest<GroupHuntingHarvestDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingHarvestDTO> {
        val payload = harvest.serializeToJson()

        requireNotNull(payload) {
            "Failed to serialize hunting day data to json"
        }

        return client.request(
                request = {
                    post(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/harvest") {
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
