package fi.riista.common.domain.permit.harvestPermit

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.model.LocalDatePeriod

@kotlinx.serialization.Serializable
data class CommonHarvestPermitSpeciesAmount(
    val speciesCode: SpeciesCode?,

    // date ranges when permit is valid / applicable
    val validityPeriods: List<LocalDatePeriod>,

    // Young animal may count as half for calculating allowed harvest amounts.
    val amount: Double?,

    // these exist but are most likely irrelevant as same information is already
    // available via CommonHarvestFields
    val ageRequired: Boolean,
    val genderRequired: Boolean,
    val weightRequired: Boolean,
)
