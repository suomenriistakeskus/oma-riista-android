package fi.riista.common.helpers

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.PermitNumber
import fi.riista.common.domain.permit.harvestPermit.HarvestPermitProvider
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermitSpeciesAmount

@Suppress("MemberVisibilityCanBePrivate")
class TestHarvestPermitProvider: HarvestPermitProvider {

    internal var mockPermit = CommonHarvestPermit(
        permitNumber = "permitNumber",
        permitType = "mockPermit",
        speciesAmounts = listOf(
            CommonHarvestPermitSpeciesAmount(
                speciesCode = SpeciesCodes.MOOSE_ID,
                validityPeriods = listOf(),
                amount = 10.0,
                ageRequired = false,
                genderRequired = false,
                weightRequired = false,
            )
        ),
        available = true
    )

    override fun getPermit(permitNumber: PermitNumber): CommonHarvestPermit? {
        return mockPermit.takeIf { it.permitNumber == permitNumber }
    }

    companion object {
        val INSTANCE = TestHarvestPermitProvider()
    }
}
