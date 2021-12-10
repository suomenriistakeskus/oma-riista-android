package fi.riista.common.network.calls

import fi.riista.common.logging.getLogger


sealed class NetworkResponse<out DataType>(
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val isCancelled: Boolean = false,
) {
    class SuccessWithNoData(override val statusCode: Int)
        : NetworkResponse<Nothing>(isSuccess = true)
    class Success<DataType>(override val statusCode: Int, val data: NetworkResponseData<DataType>)
        : NetworkResponse<DataType>(isSuccess = true)

    class ResponseError(override val statusCode: Int): NetworkResponse<Nothing>(isError = true)
    class NetworkError(val exception: Throwable?) : NetworkResponse<Nothing>(isError = true)
    class OtherError(val exception: Throwable?) : NetworkResponse<Nothing>(isError = true)

    class Cancelled(val exception: Throwable?): NetworkResponse<Nothing>(isCancelled = true)

    inline fun onSuccess(handler: (statusCode: Int, data: NetworkResponseData<out DataType>) -> Unit) {
        if (this is Success) {
            handler(statusCode, data)
        }
    }

    inline fun <T> transformSuccessData(handler: (statusCode: Int, data: NetworkResponseData<out DataType>) -> T?): T? {
        return if (this is Success) {
            handler(statusCode, data)
        } else {
            null
        }
    }

    inline fun onSuccessWithoutData(handler: (statusCode: Int) -> Unit) {
        when (this) {
            is SuccessWithNoData -> handler(statusCode)
            is Success -> {
                logger.d { "There would've been data for statusCode $statusCode..." }
                handler(statusCode)
            }
            is ResponseError, is NetworkError, is OtherError, is Cancelled -> {
                // nop, didn't succeed
                return
            }
        }
    }

    inline fun onError(handler: (statusCode: Int?, exception: Throwable?) -> Unit) {
        when (this) {
            is Success, is SuccessWithNoData -> {
                // didn't fail, nothing to do
                return
            }
            is ResponseError -> handler(statusCode, null)
            is NetworkError -> handler(null, exception)
            is OtherError -> handler(null, exception)
            is Cancelled -> {
                // don't treat cancellation as an error
            }
        }
    }

    inline fun onCancel(handler: () -> Unit) {
        if (this is Cancelled) {
            handler()
        }
    }

    /**
     * The status code if any. Subclasses may wish to override the value.
     */
    open val statusCode: Int? = null

    companion object {
        val logger by getLogger(NetworkResponse::class)
    }
}

class NetworkResponseData<DataType>(val raw: String, val typed: DataType)
