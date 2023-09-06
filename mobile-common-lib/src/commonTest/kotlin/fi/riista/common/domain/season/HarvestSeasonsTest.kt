package fi.riista.common.domain.season

import fi.riista.common.model.LocalDate
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.season.provider.BackendHarvestSeasonsProviderImpl
import fi.riista.common.domain.season.storage.MockHarvestSeasonsRepository
import fi.riista.common.domain.season.storage.MockHarvestSeasonsStorage
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.Date
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toKotlinxLocalDate
import fi.riista.common.model.toLocalDateWithinHuntingYear
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

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
        val harvestSeasons = TestHarvestSeasons.createMockHarvestSeasons()

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
    fun `fallback seasons are used if there are no seasons for the given hunting year`() = runBlockingTest {
        val now = LocalDateTime(2023, 8, 10, 12, 0, 0)
        assertEquals(2023, now.date.getHuntingYear())

        val dateTimeProvider = MockDateTimeProvider(now)
        val backendHarvestSeasonsProvider = BackendHarvestSeasonsProviderImpl(
            harvestSeasonsStorage = MockHarvestSeasonsStorage(
                repository = MockHarvestSeasonsRepository()
            ),
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI = BackendAPIMock()
            },
            preferences = MockPreferences(),
            localDateTimeProvider = dateTimeProvider,
        )
        val harvestSeasons = TestHarvestSeasons.createMockHarvestSeasons(
            backendHarvestSeasonsProvider = backendHarvestSeasonsProvider
        )

        assertMatchesHardcoded(harvestSeasons, SpeciesCodes.COOT_ID)

        assertFalse(harvestSeasons.isDuringHarvestSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 8, 19)
        ))
        assertTrue(harvestSeasons.isDuringHarvestSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 8, 20)
        ))
        assertTrue(harvestSeasons.isDuringHarvestSeason(
                SpeciesCodes.COOT_ID, LocalDate(2020, 12, 31)
        ))
        assertFalse(harvestSeasons.isDuringHarvestSeason(
                SpeciesCodes.COOT_ID, LocalDate(2021, 1, 1)
        ))

        // synchronize
        backendHarvestSeasonsProvider.synchronizationContext.startSynchronization(config = SynchronizationConfig.DEFAULT)

        assertFalse(backendHarvestSeasonsProvider.hasHarvestSeasons(2020), "2020")
        assertFalse(backendHarvestSeasonsProvider.hasHarvestSeasons(2021), "2021")
        assertTrue(backendHarvestSeasonsProvider.hasHarvestSeasons(2022), "2022")
        assertTrue(backendHarvestSeasonsProvider.hasHarvestSeasons(2023), "2023")
        assertFalse(backendHarvestSeasonsProvider.hasHarvestSeasons(2024), "2024")

        var matchesHardcoded = true
        try {
            assertMatchesHardcoded(harvestSeasons, SpeciesCodes.COOT_ID)
        } catch (e: Throwable) {
            matchesHardcoded = false
        }

        if (matchesHardcoded) {
            fail("Matched hardcoded even though shouldn't have")
        }

        // hardcoded values
        assertFalse(harvestSeasons.isDuringHarvestSeason(
            SpeciesCodes.COOT_ID, LocalDate(2020, 8, 19)
        ))
        assertTrue(harvestSeasons.isDuringHarvestSeason(
            SpeciesCodes.COOT_ID, LocalDate(2020, 8, 20)
        ))
        assertTrue(harvestSeasons.isDuringHarvestSeason(
            SpeciesCodes.COOT_ID, LocalDate(2020, 12, 31)
        ))
        assertFalse(harvestSeasons.isDuringHarvestSeason(
            SpeciesCodes.COOT_ID, LocalDate(2021, 1, 1)
        ))

        listOf(2022, 2023).forEach { huntingYear ->
            Date(3, 31).toLocalDateWithinHuntingYear(huntingYear).let { date ->
                assertFalse(
                    actual = harvestSeasons.isDuringHarvestSeason(SpeciesCodes.COOT_ID, date),
                    message = "huntingYear = $huntingYear, date = ${date.toStringISO8601()}"
                )
            }
            Date(4, 1).toLocalDateWithinHuntingYear(huntingYear).let { date ->
                assertTrue(
                    actual = harvestSeasons.isDuringHarvestSeason(SpeciesCodes.COOT_ID, date),
                    message = "huntingYear = $huntingYear, date = ${date.toStringISO8601()}"
                )
            }
            Date(4, 21).toLocalDateWithinHuntingYear(huntingYear).let { date ->
                assertTrue(
                    actual = harvestSeasons.isDuringHarvestSeason(SpeciesCodes.COOT_ID, date),
                    message = "huntingYear = $huntingYear, date = ${date.toStringISO8601()}"
                )
            }
            Date(4, 22).toLocalDateWithinHuntingYear(huntingYear).let { date ->
                assertFalse(
                    actual = harvestSeasons.isDuringHarvestSeason(SpeciesCodes.COOT_ID, date),
                    message = "huntingYear = $huntingYear, date = ${date.toStringISO8601()}"
                )
            }
        }
    }

    private fun assertMatchesHardcoded(
        harvestSeasons: HarvestSeasons, speciesCode: SpeciesCode
    ) {
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
                                       date: LocalDate
    ) {
        val harvestSeasonUtilResult = HardCodedSeasons.isInsideHarvestSeason(date, speciesCode)
        val harvestSeasonsResult = harvestSeasons.isDuringHarvestSeason(speciesCode, date)

        assertEquals(harvestSeasonUtilResult, harvestSeasonsResult,
                     "Species $speciesCode, date: $date")
    }
}
