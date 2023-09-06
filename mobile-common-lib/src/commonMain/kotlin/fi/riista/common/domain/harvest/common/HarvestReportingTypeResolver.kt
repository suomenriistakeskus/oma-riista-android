package fi.riista.common.domain.harvest.common

import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.season.HarvestSeasons

internal class HarvestReportingTypeResolver(
    private val harvestSeasons: HarvestSeasons,
) {
    fun resolveHarvestReportingType(harvest: CommonHarvestData): HarvestReportingType {
        val speciesCode = harvest.species.knownSpeciesCodeOrNull()
        val duringHarvestSeason = speciesCode?.let {
            harvestSeasons.isDuringHarvestSeason(
                speciesCode = it,
                date = harvest.pointOfTime.date
            )
        } ?: false

        return when {
            harvest.permitNumber.isNullOrBlank().not() -> HarvestReportingType.PERMIT
            duringHarvestSeason -> HarvestReportingType.SEASON
            else -> HarvestReportingType.BASIC
        }
    }
}
