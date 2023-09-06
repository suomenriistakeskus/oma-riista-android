package fi.riista.common.domain.permit.harvestPermit

import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.model.PermitNumber

interface HarvestPermitProvider {
    fun getPermit(permitNumber: PermitNumber): CommonHarvestPermit?
}

internal fun HarvestPermitProvider.getPermit(harvest: CommonHarvestData): CommonHarvestPermit? =
    harvest.permitNumber?.let { permitNumber ->
        getPermit(permitNumber)
    }
