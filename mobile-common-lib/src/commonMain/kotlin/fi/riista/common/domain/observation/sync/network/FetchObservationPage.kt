package fi.riista.common.domain.observation.sync.network

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.observation.sync.dto.ObservationPageDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchObservationPage(
    private val modifiedAfter: LocalDateTime?,
) : NetworkRequest<ObservationPageDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<ObservationPageDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/gamediary/observations/page?observationSpecVersion=${Constants.OBSERVATION_SPEC_VERSION}"
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
