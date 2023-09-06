package fi.riista.common.domain.permit.metsahallitusPermit.sync

import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit

internal interface MetsahallitusPermitFetcher {
    suspend fun fetchPermits(): OperationResultWithData<List<CommonMetsahallitusPermit>>
}
