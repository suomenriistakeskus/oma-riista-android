package fi.riista.common.domain.permit.metsahallitusPermit

import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit

interface MetsahallitusPermitProvider {
    fun hasPermits(username: String): Boolean

    fun getAllPermits(username: String): List<CommonMetsahallitusPermit>

    fun getPermit(username: String, permitIdentifier: String): CommonMetsahallitusPermit?
}
