package fi.riista.common.domain.season.network

import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*


internal class FetchHarvestSeasons(
    private val huntingYear: HuntingYear,
) : NetworkRequest<List<HarvestSeasonDTO>> {

    override suspend fun request(client: NetworkClient): NetworkResponse<List<HarvestSeasonDTO>> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/harvestseason/list/$huntingYear"

        return client.request(
            request = {
                get(urlString = url) {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}