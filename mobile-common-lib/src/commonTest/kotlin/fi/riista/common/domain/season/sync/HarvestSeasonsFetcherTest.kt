package fi.riista.common.domain.season.sync

import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HarvestSeasonsFetcherTest {
    @Test
    fun `previous season will be re-fetched if previous fetch during that season`() {
        val fetcher = getFetcher(HarvestSeasonsFetcher.Season.PREVIOUS)
        val now = LocalDateTime(2023, 8, 1, 12, 0, 0)
        assertEquals(2023, now.date.getHuntingYear())

        assertTrue(fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = false, ignoreFetchCooldown = false))

        // last time season was fetched was during last season
        fetcher.saveHarvestSeasonsFetchTime(
            huntingYear = 2022,
            now = now.copy(date = now.date.copy(monthNumber = 7, dayOfMonth = 31))
        )

        assertTrue(fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = false))
    }

    @Test
    fun `previous season won't be re-fetched if already fetched after season end`() {
        val fetcher = getFetcher(HarvestSeasonsFetcher.Season.PREVIOUS)
        val now = LocalDateTime(2023, 8, 10, 12, 0, 0)
        assertEquals(2023, now.date.getHuntingYear())

        assertTrue(fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = false, ignoreFetchCooldown = false))

        // last time season was fetched was during current hunting year
        fetcher.saveHarvestSeasonsFetchTime(
            huntingYear = 2022,
            now = now.copy(date = now.date.copy(dayOfMonth = 1))
        )

        assertFalse(fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = false))
    }

    @Test
    fun `current season won't be re-fetched unless cooldown period has passed or ignoring cooldown`() {
        val fetcher = getFetcher(HarvestSeasonsFetcher.Season.CURRENT)
        val now = LocalDateTime(2023, 8, 10, 12, 0, 0)
        assertEquals(2023, now.date.getHuntingYear())

        assertTrue(fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = false, ignoreFetchCooldown = false))

        fetcher.saveHarvestSeasonsFetchTime(
            huntingYear = 2023,
            now = now.copy(time = now.time.copy(hour = 9))
        )

        assertFalse(
            fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = false),
            "3 hours"
        )
        assertTrue(
            fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = true),
            "3 hours, ignore"
        )

        fetcher.saveHarvestSeasonsFetchTime(
            huntingYear = 2023,
            now = now.copy(time = now.time.copy(hour = 8, minute = 59))
        )

        assertTrue(
            fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = false),
            "3 hours 1 minute"
        )
    }

    @Test
    fun `upcoming season should not be fetched until 30 days to season start`() {
        val fetcher = getFetcher(HarvestSeasonsFetcher.Season.NEXT)

        listOf(false, true).forEach { hasSeasonsForHuntingYear ->
            assertFalse(fetcher.shouldFetchHarvestSeasons(
                now = LocalDateTime(2023, 6, 30, 12, 0, 0),
                hasSeasonsForHuntingYear = hasSeasonsForHuntingYear,
                ignoreFetchCooldown = false
            ))
            assertTrue(fetcher.shouldFetchHarvestSeasons(
                now = LocalDateTime(2023, 7, 1, 12, 0, 0),
                hasSeasonsForHuntingYear = hasSeasonsForHuntingYear,
                ignoreFetchCooldown = false
            ))
            assertFalse(fetcher.shouldFetchHarvestSeasons(
                now = LocalDateTime(2023, 8, 1, 12, 0, 0),
                hasSeasonsForHuntingYear = hasSeasonsForHuntingYear,
                ignoreFetchCooldown = false
            ))
        }
    }

    @Test
    fun `upcoming season won't be re-fetched unless cooldown period has passed or ignoring cooldown`() {
        val fetcher = getFetcher(HarvestSeasonsFetcher.Season.NEXT)
        val now = LocalDateTime(2023, 7, 20, 20, 0, 0)
        assertEquals(2022, now.date.getHuntingYear())

        assertTrue(fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = false, ignoreFetchCooldown = false))

        fetcher.saveHarvestSeasonsFetchTime(
            huntingYear = 2023,
            now = now.copy(time = now.time.copy(hour = 8))
        )

        assertFalse(
            fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = false),
            "12 hours"
        )
        assertTrue(
            fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = true),
            "12 hours, ignore"
        )

        fetcher.saveHarvestSeasonsFetchTime(
            huntingYear = 2023,
            now = now.copy(time = now.time.copy(hour = 7, minute = 59))
        )

        assertTrue(
            fetcher.shouldFetchHarvestSeasons(now, hasSeasonsForHuntingYear = true, ignoreFetchCooldown = false),
            "12 hours 1 minute"
        )
    }

    @Test
    fun `fetching calls backend`() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()

        val now = LocalDateTime(2023, 7, 30, 12, 0, 0)
        assertEquals(2022, now.date.getHuntingYear())

        assertEquals(0, backendAPIMock.callCount(BackendAPI::fetchHarvestSeasons))

        var fetchCount = 0
        listOf(
            HarvestSeasonsFetcher.Season.PREVIOUS,
            HarvestSeasonsFetcher.Season.CURRENT,
            HarvestSeasonsFetcher.Season.NEXT,
        ).forEach { season ->
            val fetcher = getFetcher(season, backendAPIMock)
            assertNotNull(fetcher.fetchHarvestSeasons(now), "Result for $season")

            assertEquals(++fetchCount, backendAPIMock.callCount(BackendAPI::fetchHarvestSeasons))
            assertEquals(
                expected = fetcher.getHuntingYearToFetchFor(now),
                actual = backendAPIMock.callParameter(BackendAPI::fetchHarvestSeasons),
                message = "Fetched for $season"
            )
        }
    }

    private fun getFetcher(
        harvestSeason: HarvestSeasonsFetcher.Season,
        backendAPIMock: BackendAPIMock = BackendAPIMock()
    ): HarvestSeasonsFetcher {
        return HarvestSeasonsFetcher(
            harvestSeason = harvestSeason,
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI = backendAPIMock
            },
            preferences = MockPreferences()
        )
    }
}