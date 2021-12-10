package fi.riista.common.poi.network

import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.poi.dto.PoiLocationGroupsDTO
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchPoiLocationGroups(
    private val externalId: String,
): NetworkRequest<PoiLocationGroupsDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<PoiLocationGroupsDTO> {
        return client.request(
            request = {
                get(urlString = "${client.serverBaseAddress}/api/mobile/v2/area/code/$externalId/pois") {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
