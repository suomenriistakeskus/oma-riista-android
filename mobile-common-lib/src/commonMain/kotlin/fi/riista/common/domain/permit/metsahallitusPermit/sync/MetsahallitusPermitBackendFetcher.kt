package fi.riista.common.domain.permit.metsahallitusPermit.sync

import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.permit.metsahallitusPermit.dto.toCommonMetsahallitusPermit
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider

internal class MetsahallitusPermitBackendFetcher(
    val backendApiProvider: BackendApiProvider,
): MetsahallitusPermitFetcher {
    override suspend fun fetchPermits(): OperationResultWithData<List<CommonMetsahallitusPermit>> {
        val permitsResponse = backendApiProvider.backendAPI.fetchMetsahallitusPermits()
        permitsResponse.onSuccess { statusCode, permitData ->
            val permits = permitData.typed.map { it.toCommonMetsahallitusPermit() }
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = permits
            )
        }

        permitsResponse.onError { statusCode, exception ->
            logger.d { "Failed to fetch permits ($statusCode): ${exception?.message}" }
            return OperationResultWithData.Failure(statusCode)
        }

        return OperationResultWithData.Failure(statusCode = null)
    }

    companion object {
        private val logger by getLogger(MetsahallitusPermitBackendFetcher::class)
    }
}
