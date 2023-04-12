package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.PermitNumber
import fi.riista.common.domain.permit.PermitProvider
import fi.riista.common.domain.permit.model.CommonPermit
import fi.riista.mobile.database.PermitManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPermitProvider @Inject constructor(
    private val permitManager: PermitManager,
): PermitProvider {
    override fun getPermit(permitNumber: PermitNumber): CommonPermit? {
        return permitManager.getPermit(permitNumber)?.toCommonPermit()
    }
}