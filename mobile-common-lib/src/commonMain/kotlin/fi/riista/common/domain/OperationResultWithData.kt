package fi.riista.common.domain

sealed class OperationResultWithData<out DataType>(
    val isSuccess: Boolean = false,
    val isFailure: Boolean = false,
) {
    class Success<DataType>(override val statusCode: Int, val data: DataType) :
        OperationResultWithData<DataType>(isSuccess = true)

    class Failure(override val statusCode: Int?) : OperationResultWithData<Nothing>(isFailure = true)

    /**
     * The status code if any. Subclasses may wish to override the value.
     */
    open val statusCode: Int? = null

    fun handle(
        onSuccess: (DataType) -> Unit,
        onFailure: (Int?) -> Unit,
    ) {
        when (this) {
            is Failure -> onFailure(statusCode)
            is Success -> onSuccess(data)
        }
    }
}
