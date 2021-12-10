package fi.riista.common.domain.season

import fi.riista.common.domain.season.model.HuntingSeason
import fi.riista.common.model.Date
import fi.riista.common.model.DatePeriod
import fi.riista.common.model.SpeciesCodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HarvestSeasonOverridesProviderTest {

    @Test
    fun testSettingOverrides() {
        val provider = getSeasonProvider()

        assertTrue(provider.getSpeciesHavingSeasons().isEmpty())

        val season = createSeason()

        provider.setHuntingSeasons(
                speciesCode = SpeciesCodes.GREY_SEAL_ID,
                huntingSeasons = listOf(season)
        )

        assertEquals(listOf(SpeciesCodes.GREY_SEAL_ID),
                     provider.getSpeciesHavingSeasons())
        assertEquals(listOf(season), provider.getHuntingSeasons(SpeciesCodes.GREY_SEAL_ID))
    }

    @Test
    fun testParsingOverrides() {
        val provider = getSeasonProvider()

        assertTrue(provider.getSpeciesHavingSeasons().isEmpty())

        provider.parseOverridesFromJson(
                """
                [
                    {
                        "speciesCode": 27381,
                        "huntingSeasons": [{
                            "startYear": 2020,
                            "endYear": null,
                            "yearlySeasonPeriods": [
                                {
                                    "beginDate": { "month": 8, "day": 20 },
                                    "endDate": { "month": 12, "day": 31 }
                                }
                            ]
                        }]
                    }, {
                        "speciesCode": 47507,
                        "huntingSeasons": [{
                            "startYear": 2017,
                            "endYear": 2018,
                            "yearlySeasonPeriods": [
                                {
                                    "beginDate": { "month": 9, "day": 1 },
                                    "endDate": { "month": 2, "day": 15 }
                                }, {
                                    "beginDate": { "month": 5, "day": 16 },
                                    "endDate": { "month": 6, "day": 15 }
                                }
                            ]
                        }, {
                            "startYear": 2019,
                            "endYear": null,
                            "yearlySeasonPeriods": [
                                {
                                    "beginDate": { "month": 10, "day": 1 },
                                    "endDate": { "month": 3, "day": 15 }
                                }, {
                                    "beginDate": { "month": 6, "day": 16 },
                                    "endDate": { "month": 7, "day": 15 }
                                }
                            ]
                        }]
                    }
                ]
                """.trimIndent()
        )

        with (provider.getSpeciesHavingSeasons()) {
            assertTrue(contains(47507))
            assertTrue(contains(27381))
            assertEquals(2, size)
        }

        with (provider.getHuntingSeasons(27381)) {
            assertEquals(1, size)

            with(first()) {
                assertEquals(2020, startYear)
                assertNull(endYear)
                assertEquals(
                        listOf(DatePeriod(Date(8, 20), Date(12, 31))),
                        yearlySeasonPeriods
                )
            }
        }

        with (provider.getHuntingSeasons(47507)) {
            assertEquals(2, size)

            with(first()) {
                assertEquals(2017, startYear)
                assertEquals(2018, endYear)
                assertEquals(
                        listOf(
                                DatePeriod(Date(9, 1), Date(2, 15)),
                                DatePeriod(Date(5, 16), Date(6, 15)),
                        ),
                        yearlySeasonPeriods
                )
            }

            with(get(1)) {
                assertEquals(2019, startYear)
                assertNull(endYear)
                assertEquals(
                        listOf(
                                DatePeriod(Date(10, 1), Date(3, 15)),
                                DatePeriod(Date(6, 16), Date(7, 15)),
                        ),
                        yearlySeasonPeriods
                )
            }
        }
    }

    private fun getSeasonProvider() = HarvestSeasonOverridesProvider()

    private fun createSeason(): HuntingSeason {
        return HuntingSeason(
                startYear = 2017,
                endYear = null,
                yearlySeasonPeriods = listOf(
                        DatePeriod(
                                beginDate = Date(4, 16),
                                endDate = Date(12, 31)
                        )
                )
        )
    }
}