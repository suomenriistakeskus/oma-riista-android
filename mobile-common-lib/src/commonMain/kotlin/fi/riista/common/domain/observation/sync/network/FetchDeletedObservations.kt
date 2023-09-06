package fi.riista.common.domain.observation.sync.network

import fi.riista.common.domain.observation.sync.dto.DeletedObservationsDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchDeletedObservations(
    private val deletedAfter: LocalDateTime?
) : NetworkRequest<DeletedObservationsDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<DeletedObservationsDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/gamediary/observation/deleted"
        val url = if (deletedAfter != null) {
            "$baseUrl?deletedAfter=${deletedAfter.toStringISO8601()}"
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
