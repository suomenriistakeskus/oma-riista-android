package fi.riista.common.domain.srva

import fi.riista.common.domain.srva.model.CommonSrvaEvent

sealed class SrvaEventOperationResponse {
    /**
     * The operation succeeded completely i.e. srva event was saved to database
     * and possible network operation succeeded as well (if required to be performed).
     */
    data class Success(val srvaEvent: CommonSrvaEvent): SrvaEventOperationResponse()

    /**
     * The srva event was saved to database but the following network operation failed either
     * because of a network error (not reachable) or with given [statusCode]
     *
     * The optionally produced [errorMessage] may be useful in debugging the situation but
     * it is not intended for user to see.
     */
    data class NetworkFailure(val statusCode: Int?, val errorMessage: String?): SrvaEventOperationResponse()

    /**
     * The operation failed i.e. srva event was not saved to database.
     *
     * The optionally produced [errorMessage] may be useful in debugging the situation but
     * it is not intended for user to see.
     */
    data class SaveFailure(val errorMessage: String?): SrvaEventOperationResponse()

    /**
     * The event data was invalid or some other precondition failed.
     */
    data class Error(val errorMessage: String?): SrvaEventOperationResponse()
}

data class SaveSrvaResponse(
    val databaseSaveResponse: SrvaEventOperationResponse,
    val networkSaveResponse: SrvaEventOperationResponse? = null,
) {
    val errorMessage: String?
        get() {
            val databaseErrorMessage = when (databaseSaveResponse) {
                is SrvaEventOperationResponse.Error -> databaseSaveResponse.errorMessage
                is SrvaEventOperationResponse.NetworkFailure -> databaseSaveResponse.errorMessage
                is SrvaEventOperationResponse.SaveFailure -> databaseSaveResponse.errorMessage
                is SrvaEventOperationResponse.Success -> null
            }
            val networkErrorMessage = when (networkSaveResponse) {
                is SrvaEventOperationResponse.Error -> networkSaveResponse.errorMessage
                is SrvaEventOperationResponse.NetworkFailure -> networkSaveResponse.errorMessage
                is SrvaEventOperationResponse.SaveFailure -> networkSaveResponse.errorMessage
                is SrvaEventOperationResponse.Success -> null
                null -> null
            }
            if (databaseErrorMessage == null && networkErrorMessage == null) {
                return null
            }
            return "databaseErrorMessage=$databaseErrorMessage, networkErrorMessage=$networkErrorMessage"
        }
}
