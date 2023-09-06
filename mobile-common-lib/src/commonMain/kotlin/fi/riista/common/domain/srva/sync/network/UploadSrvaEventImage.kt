package fi.riista.common.domain.srva.sync.network

import fi.riista.common.io.CommonFile
import fi.riista.common.io.appendFile
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkRequest
import fi.riista.common.network.calls.NetworkResponse
import io.ktor.client.request.forms.*
import io.ktor.http.*

internal class UploadSrvaEventImage(
    private val eventRemoteId: Long,
    private val uuid: String,
    private val contentType: String,
    private val file: CommonFile,
) : NetworkRequest<Unit> {

    override suspend fun request(client: NetworkClient): NetworkResponse<Unit> {
        return client.request(
            request = {
                submitFormWithBinaryData(
                    url = "${client.serverBaseAddress}/api/mobile/v2/srva/image/upload",
                    formData = formData {
                        append("srvaEventId", eventRemoteId)
                        append("uuid", uuid)
                        appendFile(
                            key = "file",
                            file = file,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                // File name left blank on purpose. Copied from old implementation
                                append(HttpHeaders.ContentDisposition, "filename=")
                            }
                        )
                    }
                )
            },
            configureResponseHandler = {
                onSuccess {
                    NetworkResponse.SuccessWithNoData(statusCode = it.status.value)
                }
            }
        )
    }
}
