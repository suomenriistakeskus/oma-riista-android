package fi.riista.common.domain.season.storage

import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.getDateWithoutYear
import fi.riista.common.model.toLocalDateDTO
import fi.riista.common.model.toLocalDateWithinHuntingYear

internal class MockHarvestSeasonsStorage(
    repository: HarvestSeasonsRepository,
): HarvestSeasonsOfflineStorage(repository) {
    override suspend fun setHarvestSeasons(huntingYear: HuntingYear, harvestSeasonDTOs: List<HarvestSeasonDTO>) {
        // patch the harvest season dto's do that they are for the correct hunting year. Mock data
        // has hard-coded years after all
        val harvestSeasonDTOsWithFixedYears = harvestSeasonDTOs.map { dto ->
            dto.copy(
                beginDate = dto.beginDate?.fixDateForHuntingYear(huntingYear),
                endDate = dto.endDate?.fixDateForHuntingYear(huntingYear),
                beginDate2 = dto.beginDate2?.fixDateForHuntingYear(huntingYear),
                endDate2 = dto.endDate2?.fixDateForHuntingYear(huntingYear),
            )
        }

        super.setHarvestSeasons(huntingYear, harvestSeasonDTOsWithFixedYears)
    }
}

private fun LocalDateDTO.fixDateForHuntingYear(huntingYear: HuntingYear): LocalDateDTO? {
    return this.toLocalDate()
        ?.getDateWithoutYear()
        ?.toLocalDateWithinHuntingYear(huntingYear)
        ?.toLocalDateDTO()
}