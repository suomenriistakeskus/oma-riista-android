package fi.riista.common.network.calls

import fi.riista.common.logging.getLogger
import io.ktor.client.statement.*
import io.ktor.http.*

typealias StatusCodeHandler<DataType> = suspend (response: HttpResponse) -> NetworkResponse<DataType>

internal interface NetworkResponseHandler<DataType> {
    /**
     * The given handler will be called when a response with 2xx status code is received.
     */
    fun onSuccess(handler: StatusCodeHandler<DataType>)

    /**
     * The given handler will be called when a response with specified status code is received.
     * The handled will not be called with 2xx series status codes.
     */
    fun onStatusCode(statusCode: HttpStatusCode, handler: StatusCodeHandler<DataType>)

    /**
     * The given handler will be called when a response with non-2xx status is received and
     * it there was no status code handler for that status code.
     */
    fun onOtherStatusCodes(handler: StatusCodeHandler<DataType>)
}

internal class NetworkResponseHandlerImpl<DataType>(
    private var successHandler: StatusCodeHandler<DataType>) : NetworkResponseHandler<DataType> {

    private val statusCodeHandlers = mutableMapOf<HttpStatusCode, StatusCodeHandler<DataType>>()
    private var otherHandler: StatusCodeHandler<DataType>? = null

    override fun onSuccess(handler: StatusCodeHandler<DataType>) {
        successHandler = handler
    }

    suspend fun handleSuccess(response: HttpResponse): NetworkResponse<DataType> {
        return successHandler(response)
    }

    override fun onStatusCode(statusCode: HttpStatusCode, handler: StatusCodeHandler<DataType>) {
        statusCodeHandlers[statusCode] = handler
    }

    override fun onOtherStatusCodes(handler: StatusCodeHandler<DataType>) {
        otherHandler = handler
    }

    suspend fun handleResponseError(response: HttpResponse): NetworkResponse<DataType> {
        statusCodeHandlers[response.status]?.let { handler ->
            return handler(response)
        }

        return otherHandler?.let { otherHandler ->
            otherHandler(response)
        } ?: run {
            logger.w { "no handler for status code ${response.status}" }
            NetworkResponse.ResponseError(response.status.value)
        }
    }

    companion object {
        private val logger by getLogger(NetworkResponseHandler::class)
    }
}