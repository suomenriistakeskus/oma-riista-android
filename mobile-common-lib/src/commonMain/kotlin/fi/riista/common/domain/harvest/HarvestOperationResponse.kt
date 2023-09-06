package fi.riista.common.domain.harvest

import fi.riista.common.domain.harvest.model.CommonHarvest

sealed class HarvestOperationResponse {
    /**
     * The operation succeeded completely i.e. harvest was saved to database
     * and possible network operation succeeded as well (if required to be performed).
     */
    data class Success(val harvest: CommonHarvest): HarvestOperationResponse()

    /**
     * The harvest was saved to database but the following network operation failed either
     * because of a network error (not reachable) or with given [statusCode]
     *
     * The optionally produced [errorMessage] may be useful in debugging the situation but
     * it is not intended for user to see.
     */
    data class NetworkFailure(val statusCode: Int?, val errorMessage: String?): HarvestOperationResponse()

    /**
     * The operation failed i.e. harvest was not saved to database.
     *
     * The optionally produced [errorMessage] may be useful in debugging the situation but
     * it is not intended for user to see.
     */
    data class SaveFailure(val errorMessage: String?): HarvestOperationResponse()

    /**
     * The harvest data was invalid or some other precondition failed.
     */
    data class Error(val errorMessage: String?): HarvestOperationResponse()
}

data class SaveHarvestResponse(
    val databaseSaveResponse: HarvestOperationResponse,
    val networkSaveResponse: HarvestOperationResponse? = null,
) {
    val errorMessage: String?
        get() {
            val databaseErrorMessage = when (databaseSaveResponse) {
                is HarvestOperationResponse.Error -> databaseSaveResponse.errorMessage
                is HarvestOperationResponse.NetworkFailure -> databaseSaveResponse.errorMessage
                is HarvestOperationResponse.SaveFailure -> databaseSaveResponse.errorMessage
                is HarvestOperationResponse.Success -> null
            }
            val networkErrorMessage = when (networkSaveResponse) {
                is HarvestOperationResponse.Error -> networkSaveResponse.errorMessage
                is HarvestOperationResponse.NetworkFailure -> networkSaveResponse.errorMessage
                is HarvestOperationResponse.SaveFailure -> networkSaveResponse.errorMessage
                is HarvestOperationResponse.Success -> null
                null -> null
            }
            if (databaseErrorMessage == null && networkErrorMessage == null) {
                return null
            }
            return "databaseErrorMessage=$databaseErrorMessage, networkErrorMessage=$networkErrorMessage"
        }
}
