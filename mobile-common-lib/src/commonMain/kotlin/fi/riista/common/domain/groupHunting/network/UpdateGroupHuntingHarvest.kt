package fi.riista.common.domain.groupHunting.network

import fi.riista.common.domain.groupHunting.dto.GroupHuntingHarvestDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.util.serializeToJson
import io.ktor.client.request.*
import io.ktor.http.*

internal class UpdateGroupHuntingHarvest(
    private val harvest: GroupHuntingHarvestDTO
) : NetworkRequest<GroupHuntingHarvestDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<GroupHuntingHarvestDTO> {

        val harvestId = harvest.id
        val payload = harvest.serializeToJson()

        requireNotNull(payload) {
            "Failed to serialize harvest data to json"
        }

        return client.request(
                request = {
                    put(urlString = "${client.serverBaseAddress}/api/mobile/v2/grouphunting/harvest/$harvestId") {
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
