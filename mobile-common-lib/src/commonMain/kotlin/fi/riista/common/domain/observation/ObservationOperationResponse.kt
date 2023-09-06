package fi.riista.common.domain.observation

import fi.riista.common.domain.observation.model.CommonObservation

sealed class ObservationOperationResponse {
    /**
     * The operation succeeded completely i.e. observation was saved to database
     * and possible network operation succeeded as well (if required to be performed).
     */
    data class Success(val observation: CommonObservation): ObservationOperationResponse()

    /**
     * The observation was saved to database but the following network operation failed either
     * because of a network error (not reachable) or with given [statusCode]
     *
     * The optionally produced [errorMessage] may be useful in debugging the situation but
     * it is not intended for user to see.
     */
    data class NetworkFailure(val statusCode: Int?, val errorMessage: String?): ObservationOperationResponse()

    /**
     * The operation failed i.e. observation was not saved to database.
     *
     * The optionally produced [errorMessage] may be useful in debugging the situation but
     * it is not intended for user to see.
     */
    data class SaveFailure(val errorMessage: String?): ObservationOperationResponse()

    /**
     * The observation data was invalid or some other precondition failed.
     */
    data class Error(val errorMessage: String?): ObservationOperationResponse()
}

data class SaveObservationResponse(
    val databaseSaveResponse: ObservationOperationResponse,
    val networkSaveResponse: ObservationOperationResponse? = null,
) {
    val errorMessage: String?
        get() {
            val databaseErrorMessage = when (databaseSaveResponse) {
                is ObservationOperationResponse.Error -> databaseSaveResponse.errorMessage
                is ObservationOperationResponse.NetworkFailure -> databaseSaveResponse.errorMessage
                is ObservationOperationResponse.SaveFailure -> databaseSaveResponse.errorMessage
                is ObservationOperationResponse.Success -> null
            }
            val networkErrorMessage = when (networkSaveResponse) {
                is ObservationOperationResponse.Error -> networkSaveResponse.errorMessage
                is ObservationOperationResponse.NetworkFailure -> networkSaveResponse.errorMessage
                is ObservationOperationResponse.SaveFailure -> networkSaveResponse.errorMessage
                is ObservationOperationResponse.Success -> null
                null -> null
            }
            if (databaseErrorMessage == null && networkErrorMessage == null) {
                return null
            }
            return "databaseErrorMessage=$databaseErrorMessage, networkErrorMessage=$networkErrorMessage"
        }
}
