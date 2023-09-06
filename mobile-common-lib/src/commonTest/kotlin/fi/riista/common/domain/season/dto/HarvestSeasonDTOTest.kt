package fi.riista.common.domain.season.dto

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.season.model.HarvestSeason
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.LocalizedString
import fi.riista.common.util.contains
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HarvestSeasonDTOTest {
    @Test
    fun `number of hunting seasons`() {
        val huntingYear = 2022

        convertToHarvestSeasons(
            huntingYear = huntingYear,
            harvestSeasonDTOs = listOf(
                createHarvestSeasonsDto(
                    beginDate = LocalDate(2022, 8, 1),
                    endDate = LocalDate(2022, 8, 21)
                )
            )
        ).let { harvestSeasons ->
            assertEquals(1, harvestSeasons.size)
        }

        convertToHarvestSeasons(
            huntingYear = huntingYear,
            harvestSeasonDTOs = listOf(
                createHarvestSeasonsDto(
                    beginDate = LocalDate(2022, 8, 1),
                    endDate = LocalDate(2022, 8, 21)
                ),
                createHarvestSeasonsDto(
                    beginDate = LocalDate(2022, 9, 1),
                    endDate = LocalDate(2022, 9, 21)
                )
            )
        ).let { harvestSeasons ->
            assertEquals(2, harvestSeasons.size)
        }
    }

    @Test
    fun `seasons only for current hunting year`() {
        val huntingYear = 2022
        val harvestSeasons = convertToHarvestSeasons(
            huntingYear = huntingYear,
            harvestSeasonDTOs = listOf(
                createHarvestSeasonsDto(
                    beginDate = LocalDate(2022, 8, 1),
                    endDate = LocalDate(2022, 8, 21)
                )
            )
        )

        assertEquals(1, harvestSeasons.size)
        assertFalse(harvestSeasons.contains { it.huntingYear != huntingYear })
    }

    @Test
    fun `seasons only for current hunting year although period extends to next year`() {
        val huntingYear = 2022
        val harvestSeasons = convertToHarvestSeasons(
            huntingYear = huntingYear,
            harvestSeasonDTOs = listOf(
                createHarvestSeasonsDto(
                    beginDate = LocalDate(2023, 7, 1),
                    // in real life end date should never extend to next season
                    endDate = LocalDate(2023, 8, 21)
                )
            )
        )

        assertEquals(1, harvestSeasons.size)
        assertFalse(harvestSeasons.contains { it.huntingYear != huntingYear })
    }

    @Test
    fun `converted data matches dto data`() {
        val huntingYear = 2022
        val harvestSeasons = convertToHarvestSeasons(
            huntingYear = huntingYear,
            harvestSeasonDTOs = listOf(
                createHarvestSeasonsDto(
                    beginDate = LocalDate(2022, 8, 1),
                    endDate = LocalDate(2022, 8, 21),
                    beginDate2 = LocalDate(2022, 10, 1),
                    endDate2 = LocalDate(2022, 10, 21),
                )
            )
        )

        assertEquals(1, harvestSeasons.size)
        with (harvestSeasons.first()) {
            assertEquals(SpeciesCodes.COOT_ID, speciesCode)
            assertEquals(LocalizedString("fi", "sv", "en"), name)
            assertEquals(2022, huntingYear)
            assertEquals(
                expected = listOf(
                    LocalDatePeriod(
                        beginDate = LocalDate(2022, 8, 1),
                        endDate = LocalDate(2022, 8, 21),
                    ),
                    LocalDatePeriod(
                        beginDate = LocalDate(2022, 10, 1),
                        endDate = LocalDate(2022, 10, 21),
                    ),
                ),
                actual = seasonPeriods
            )
        }
    }

    private fun convertToHarvestSeasons(
        huntingYear: Int,
        harvestSeasonDTOs: List<HarvestSeasonDTO>
    ): List<HarvestSeason> {
        return harvestSeasonDTOs.map { it.toHarvestSeason(huntingYear) }
    }

    private fun createHarvestSeasonsDto(
        beginDate: LocalDate,
        endDate: LocalDate,
        beginDate2: LocalDate? = null,
        endDate2: LocalDate? = null,
    ): HarvestSeasonDTO {
        return HarvestSeasonDTO(
            name = LocalizedStringDTO("fi", "sv", "en"),
            gameSpeciesCode = SpeciesCodes.COOT_ID,
            beginDate = beginDate.toStringISO8601(),
            endDate = endDate.toStringISO8601(),
            beginDate2 = beginDate2?.toStringISO8601(),
            endDate2 = endDate2?.toStringISO8601(),
        )
    }
}