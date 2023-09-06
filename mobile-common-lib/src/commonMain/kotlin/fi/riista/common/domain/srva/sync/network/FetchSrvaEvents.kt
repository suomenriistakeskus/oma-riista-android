package fi.riista.common.domain.srva.sync.network

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.srva.sync.dto.SrvaEventPageDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchSrvaEvents(
    private val modifiedAfter: LocalDateTime?,
) : NetworkRequest<SrvaEventPageDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<SrvaEventPageDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/srva/srvaevents/page?srvaEventSpecVersion=${Constants.SRVA_SPEC_VERSION}"
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
