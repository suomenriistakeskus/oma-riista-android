package fi.riista.common.domain

sealed class OperationResult(
    val isSuccess: Boolean = false,
    val isFailure: Boolean = false,
) {
    class Success(override val statusCode: Int?) : OperationResult(isSuccess = true)

    class Failure(override val statusCode: Int?) : OperationResult(isFailure = true)

    /**
     * The status code if any. Subclasses may wish to override the value.
     */
    open val statusCode: Int? = null

    fun handle(
        onSuccess: (Int?) -> Unit,
        onFailure: (Int?) -> Unit,
    ) {
        when (this) {
            is Failure -> onFailure(statusCode)
            is Success -> onSuccess(statusCode)
        }
    }
}
