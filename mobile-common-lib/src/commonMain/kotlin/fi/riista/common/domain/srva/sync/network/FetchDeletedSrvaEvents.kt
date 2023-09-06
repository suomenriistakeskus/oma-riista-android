package fi.riista.common.domain.srva.sync.network

import fi.riista.common.domain.srva.sync.dto.DeletedSrvaEventsDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchDeletedSrvaEvents(
    private val deletedAfter: LocalDateTime?
) : NetworkRequest<DeletedSrvaEventsDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<DeletedSrvaEventsDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/srva/srvaevents/deleted"
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
