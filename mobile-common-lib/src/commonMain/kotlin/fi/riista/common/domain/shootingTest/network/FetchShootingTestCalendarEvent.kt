package fi.riista.common.domain.shootingTest.network

import fi.riista.common.domain.shootingTest.dto.ShootingTestCalendarEventDTO
import fi.riista.common.domain.shootingTest.model.CalendarEventId
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class FetchShootingTestCalendarEvent(
    private val calendarEventId: CalendarEventId,
) : NetworkRequest<ShootingTestCalendarEventDTO> {

    override suspend fun request(client: NetworkClient): NetworkResponse<ShootingTestCalendarEventDTO> {
        val url = "${client.serverBaseAddress}/api/mobile/v2/shootingtest/calendarevent/$calendarEventId"
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
