package fi.riista.common.domain.huntingControl.sync.network

import fi.riista.common.domain.constants.Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION
import fi.riista.common.domain.huntingControl.sync.dto.LoadRhysAndHuntingControlEventsDTO
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchRhysAndHuntingControlEvents(
    private val modifiedAfter: LocalDateTime?,
) : NetworkRequest<LoadRhysAndHuntingControlEventsDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<LoadRhysAndHuntingControlEventsDTO> {
        val baseUrl = "${client.serverBaseAddress}/api/mobile/v2/huntingcontrol/event?requestedSpecVersion=${HUNTING_CONTROL_EVENT_SPEC_VERSION}"
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
