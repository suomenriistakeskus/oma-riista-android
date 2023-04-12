package fi.riista.mobile.riistaSdkHelpers

import fi.riista.common.domain.permit.model.CommonPermit
import fi.riista.common.domain.permit.model.CommonPermitSpeciesAmount
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.util.letWith
import fi.riista.mobile.models.Permit
import fi.riista.mobile.models.PermitSpeciesAmount

fun Permit.toCommonPermit(): CommonPermit {
    return CommonPermit(
        permitNumber = permitNumber,
        permitType = permitType,
        speciesAmounts = speciesAmounts.map { it.toCommonPermitSpeciesAmount() },
        available = !unavailable
    )
}

fun PermitSpeciesAmount.toCommonPermitSpeciesAmount(): CommonPermitSpeciesAmount {
    return CommonPermitSpeciesAmount(
        speciesCode = gameSpeciesCode,
        validityPeriods = listOfNotNull(
            beginDate?.letWith(endDate) { begin, end ->
                LocalDatePeriod(
                    beginDate = LocalDate.fromJodaLocalDate(begin),
                    endDate = LocalDate.fromJodaLocalDate(end),
                )
            },
            beginDate2?.letWith(endDate2) { begin, end ->
                LocalDatePeriod(
                    beginDate = LocalDate.fromJodaLocalDate(begin),
                    endDate = LocalDate.fromJodaLocalDate(end),
                )
            },
        ),
        amount = amount,
        ageRequired = ageRequired,
        genderRequired = genderRequired,
        weightRequired = weightRequired,
    )
}

