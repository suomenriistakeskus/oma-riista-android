package fi.riista.common.domain.huntingclub.selectableForEntries.network

import fi.riista.common.domain.huntingclub.dto.HuntingClubNameAndCodeDTO
import fi.riista.common.domain.huntingclub.memberships.dto.HuntingClubMembershipsDTO
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.*
import io.ktor.http.*

internal class SearchHuntingClubByOfficialCode(
    private val officialCode: String,
): NetworkRequest<HuntingClubNameAndCodeDTO> {
    override suspend fun request(client: NetworkClient): NetworkResponse<HuntingClubNameAndCodeDTO> {
        return client.request(
            request = {
                post(urlString = "${client.serverBaseAddress}/api/mobile/v2/search/club/officialcode?officialCode=$officialCode") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(body = "")
                }
            },
            configureResponseHandler = {
                // nop, default response handling works just fine
            }
        )
    }
}
