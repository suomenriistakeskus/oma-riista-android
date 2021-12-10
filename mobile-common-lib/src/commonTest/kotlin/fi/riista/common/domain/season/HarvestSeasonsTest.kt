package fi.riista.common.domain.season

import fi.riista.common.model.LocalDate
import fi.riista.common.model.SpeciesCode
import fi.riista.common.model.SpeciesCodes
import fi.riista.common.model.toKotlinxLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HarvestSeasonsTest {

    // first hardcoded seasons are defined for 2017 but let's test starting from previous year
    private val firstDate = LocalDate(2016, 1, 1).toKotlinxLocalDate()
    // also let the last date be in the future in order to fully ensure latest values are checked
    private val lastDate = LocalDate(2023, 12, 31).toKotlinxLocalDate()

    private val species = listOf(
            SpeciesCodes.BEAN_GOOSE_ID,
            SpeciesCodes.BEAR_ID,
            SpeciesCodes.COMMON_EIDER_ID,
            SpeciesCodes.COOT_ID,
            SpeciesCodes.GARGANEY_ID,
            SpeciesCodes.GOOSANDER_ID,
            SpeciesCodes.GREYLAG_GOOSE_ID,
            SpeciesCodes.GREY_SEAL_ID,
            SpeciesCodes.LONG_TAILED_DUCK_ID,
            SpeciesCodes.PINTAIL_ID,
            SpeciesCodes.POCHARD_ID,
            SpeciesCodes.POLECAT_ID,
            SpeciesCodes.RED_BREASTED_MERGANSER_ID,
            SpeciesCodes.RINGED_SEAL_ID,
            SpeciesCodes.ROE_DEER_ID,
            SpeciesCodes.SHOVELER_ID,
            SpeciesCodes.TUFTED_DUCK_ID,
            SpeciesCodes.WIGEON_ID,
            SpeciesCodes.WILD_BOAR_ID,
    )

    @Test
    fun testHardcodedHarvestSeasonsAreEqual() {
        val harvestSeasons = HarvestSeasons()

        var date = firstDate
        while (date <= lastDate) {
            species.forEach { speciesCode ->
                assertMatchesHardcoded(
                        harvestSeasons = harvestSeasons,
                        speciesCode = speciesCode,
                        date = LocalDate(date.year, date.monthNumber, date.dayOfMonth)
                )
            }

            date = date.plus(1, DateTimeUnit.DAY)
        }
    }

    @Test
    fun testOverridingWorks() {
        val harvestSeasons = HarvestSeasons()

        assertMatchesHardcoded(harvestSeasons, SpeciesCodes.COOT_ID)

        assertFalse(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 8, 19)))
        assertTrue(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 8, 20)))
        assertTrue(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 12, 31)))
        assertFalse(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2021, 1, 1)))

        harvestSeasons.overridesProvider.parseOverridesFromJson(
                """
                    [{
                        "speciesCode": 27381,
                        "huntingSeasons": [{
                            "startYear": 2020,
                            "endYear": null,
                            "yearlySeasonPeriods": [
                                {
                                    "beginDate": { "month": 8, "day": 21 },
                                    "endDate": { "month": 12, "day": 30 }
                                }
                            ]
                        }]
                    }]
                """.trimIndent()
        )

        assertFalse(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 8, 20)))
        assertTrue(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 8, 21)))
        assertTrue(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 12, 30)))
        assertFalse(harvestSeasons.isDuringHuntingSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 12, 31)))
    }

    private fun assertMatchesHardcoded(
        harvestSeasons: HarvestSeasons, speciesCode: SpeciesCode) {
        var date = firstDate
        while (date <= lastDate) {
            assertMatchesHardcoded(
                    harvestSeasons = harvestSeasons,
                    speciesCode = speciesCode,
                    date = LocalDate(date.year, date.monthNumber, date.dayOfMonth)
            )

            date = date.plus(1, DateTimeUnit.DAY)
        }
    }

    private fun assertMatchesHardcoded(harvestSeasons: HarvestSeasons,
                                       speciesCode: Int,
                                       date: LocalDate) {
        val harvestSeasonUtilResult = HardCodedSeasons.isInsideHuntingSeason(date, speciesCode)
        val harvestSeasonsResult = harvestSeasons.isDuringHuntingSeason(speciesCode, date)

        assertEquals(harvestSeasonUtilResult, harvestSeasonsResult,
                     "Species $speciesCode, date: $date")
    }
}