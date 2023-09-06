package fi.riista.common.domain.season.dto

import fi.riista.common.domain.dto.SpeciesCodeDTO
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.dto.toLocalizedString
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import kotlinx.serialization.Serializable

@Serializable
data class HarvestSeasonDTO(
    val name: LocalizedStringDTO? = null,

    val gameSpeciesCode: SpeciesCodeDTO,

    val beginDate: LocalDateDTO? = null,
    val endDate: LocalDateDTO? = null,
    val endOfReportingDate: LocalDateDTO? = null,

    val beginDate2: LocalDateDTO? = null,
    val endDate2: LocalDateDTO? = null,
    val endOfReportingDate2: LocalDateDTO? = null,
)

internal fun HarvestSeasonDTO.toHarvestSeason(huntingYear: HuntingYear): HarvestSeason {
    // note: there are no restrictions for season periods i.e. it is possible that season periods are
    // not fully (or even at all!) during the specified hunting year!
    val seasonPeriods = listOfNotNull(
        LocalDatePeriod.createSeason(
            beginDate = beginDate?.toLocalDate(),
            endDate = endDate?.toLocalDate(),
        ),
        LocalDatePeriod.createSeason(
            beginDate = beginDate2?.toLocalDate(),
            endDate = endDate2?.toLocalDate(),
        ),
    )

    return HarvestSeason(
        speciesCode = gameSpeciesCode,
        huntingYear = huntingYear,
        seasonPeriods = seasonPeriods,
        name = name?.toLocalizedString(),
    )
}

private fun LocalDatePeriod.Companion.createSeason(
    beginDate: LocalDate?,
    endDate: LocalDate?,
): LocalDatePeriod? {
    // season needs to define both begin and end date
    if (beginDate == null || endDate == null) {
        return null
    }

    return LocalDatePeriod(beginDate, endDate)
}