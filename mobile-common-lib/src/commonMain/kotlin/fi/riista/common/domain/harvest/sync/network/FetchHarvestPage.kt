package fi.riista.common.domain.harvest.sync.network

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.harvest.sync.dto.HarvestPageDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchHarvestPage(
    private val modifiedAfter: LocalDateTime?,
) : NetworkRequest<HarvestPageDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HarvestPageDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/gamediary/harvests/page?harvestSpecVersion=${Constants.HARVEST_SPEC_VERSION}&reportedForOthers=true"
        val url = if (modifiedAfter != null) {
            "$baseUrl&modifiedAfter=${modifiedAfter.toStringISO8601()}"
        } else {
            baseUrl
        }

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

