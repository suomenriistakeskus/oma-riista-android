package fi.riista.common.domain.huntingclub.memberships.sync

import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.model.Occupation

internal interface HuntingClubOccupationsFetcher {
    suspend fun fetchOccupations(): OperationResultWithData<List<Occupation>>
}
