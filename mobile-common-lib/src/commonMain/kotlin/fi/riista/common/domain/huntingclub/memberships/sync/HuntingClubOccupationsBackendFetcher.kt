package fi.riista.common.domain.huntingclub.memberships.sync

import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.dto.toOccupation
import fi.riista.common.domain.model.Occupation
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider

internal class HuntingClubOccupationsBackendFetcher(
    val backendApiProvider: BackendApiProvider,
): HuntingClubOccupationsFetcher {
    override suspend fun fetchOccupations(): OperationResultWithData<List<Occupation>> {
        val occupationsResponse = backendApiProvider.backendAPI.fetchHuntingClubMemberships()
        occupationsResponse.onSuccess { statusCode, occupationsData ->
            val occupations = occupationsData.typed.map { it.toOccupation() }
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = occupations
            )
        }

        occupationsResponse.onError { statusCode, exception ->
            logger.d { "Failed to fetch club occupations/memberships ($statusCode): ${exception?.message}" }
            return OperationResultWithData.Failure(statusCode)
        }

        return OperationResultWithData.Failure(statusCode = null)
    }

    companion object {
        private val logger by getLogger(HuntingClubOccupationsBackendFetcher::class)
    }
}
