package fi.riista.common.domain.permit.metsahallitusPermit.storage

import fi.riista.common.domain.permit.metsahallitusPermit.MetsahallitusPermitProvider
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit

internal interface MetsahallitusPermitStorage: MetsahallitusPermitProvider {
    suspend fun replacePermits(username: String, permits: List<CommonMetsahallitusPermit>)
}
