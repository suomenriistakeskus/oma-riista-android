package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.model.PermitNumber
import fi.riista.common.domain.permit.harvestPermit.HarvestPermitProvider
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.mobile.database.PermitManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppHarvestPermitProvider @Inject constructor(
    private val permitManager: PermitManager,
): HarvestPermitProvider {
    override fun getPermit(permitNumber: PermitNumber): CommonHarvestPermit? {
        return permitManager.getPermit(permitNumber)?.toCommonPermit()
    }
}