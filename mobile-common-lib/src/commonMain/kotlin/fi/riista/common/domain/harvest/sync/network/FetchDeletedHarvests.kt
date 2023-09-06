package fi.riista.common.domain.harvest.sync.network

import fi.riista.common.domain.harvest.sync.dto.DeletedHarvestsDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

class FetchDeletedHarvests(
    private val deletedAfter: LocalDateTime?
) : NetworkRequest<DeletedHarvestsDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<DeletedHarvestsDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/gamediary/harvest/deleted?supportReportedForOthers=true"
        val url = if (deletedAfter != null) {
            "$baseUrl&deletedAfter=${deletedAfter.toStringISO8601()}"
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
