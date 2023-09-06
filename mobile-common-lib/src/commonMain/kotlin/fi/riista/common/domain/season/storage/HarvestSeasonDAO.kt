package fi.riista.common.domain.season.storage

import fi.riista.common.database.dao.LocalDateDAO
import fi.riista.common.database.dao.LocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedStringDAO
import fi.riista.common.database.dao.toLocalizedStringDTO
import fi.riista.common.domain.dao.SpeciesCodeDAO
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import kotlinx.serialization.Serializable


/**
 * Remember to bump DAO_VERSION if/when making backwards incompatible changes.
 *
 * Also check where this class has been used in order to bump those versions as well.
 */
@Serializable
internal data class HarvestSeasonDAO(
    val name: LocalizedStringDAO?,

    val gameSpeciesCode: SpeciesCodeDAO,

    val beginDate: LocalDateDAO?,
    val endDate: LocalDateDAO?,
    val endOfReportingDate: LocalDateDAO?,

    val beginDate2: LocalDateDAO?,
    val endDate2: LocalDateDAO?,
    val endOfReportingDate2: LocalDateDAO?,
) {
    companion object {
        internal const val DAO_VERSION = 1
    }
}

internal fun HarvestSeasonDTO.toHarvestSeasonDAO() =
    HarvestSeasonDAO(
        name = name?.toLocalizedStringDAO(),
        gameSpeciesCode = gameSpeciesCode,
        beginDate = beginDate,
        endDate = endDate,
        endOfReportingDate = endOfReportingDate,
        beginDate2 = beginDate2,
        endDate2 = endDate2,
        endOfReportingDate2 = endOfReportingDate2,
    )

internal fun HarvestSeasonDAO.toHarvestSeasonDTO() =
    HarvestSeasonDTO(
        name = name?.toLocalizedStringDTO(),
        gameSpeciesCode = gameSpeciesCode,
        beginDate = beginDate,
        endDate = endDate,
        endOfReportingDate = endOfReportingDate,
        beginDate2 = beginDate2,
        endDate2 = endDate2,
        endOfReportingDate2 = endOfReportingDate2,
    )