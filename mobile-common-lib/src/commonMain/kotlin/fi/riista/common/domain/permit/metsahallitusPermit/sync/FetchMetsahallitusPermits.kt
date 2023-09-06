package fi.riista.common.domain.permit.metsahallitusPermit.sync

import fi.riista.common.domain.permit.metsahallitusPermit.dto.CommonMetsahallitusPermitDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType

internal class FetchMetsahallitusPermits : NetworkRequest<List<CommonMetsahallitusPermitDTO>> {
    override suspend fun request(client: NetworkClient): NetworkResponse<List<CommonMetsahallitusPermitDTO>> {
        return client.request(
            request = {
                get(urlString = "${client.serverBaseAddress}/api/mobile/v2/permit/mh") {
                    accept(ContentType.Application.Json)
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
