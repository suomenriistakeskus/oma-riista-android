package fi.riista.common.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DatePeriodTest {

    @Test
    fun testConversionToLocalDatePeriod() {
        assertEquals(
                expected = LocalDatePeriod(
                        beginDate = LocalDate(2021, 6, 1),
                        endDate = LocalDate(2021, 6, 24)
                ),
                actual = DatePeriod(
                        beginDate = Date(6, 1),
                        endDate = Date(6, 24)
                ).toLocalDatePeriod(2021, 2021)
        )

        assertEquals(
                expected = LocalDatePeriod(
                        beginDate = LocalDate(2021, 6, 1),
                        endDate = LocalDate(2022, 6, 24)
                ),
                actual = DatePeriod(
                        beginDate = Date(6, 1),
                        endDate = Date(6, 24)
                ).toLocalDatePeriod(2021, 2022)
        )
    }

    @Test
    fun testConversionToLocalDatePeriodWithinHuntingYear() {
        // happy paths, beginning of hunting year
        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2021, 8, 1),
                                endDate = LocalDate(2021, 8, 24)
                        )
                ),
                actual = DatePeriod(
                                beginDate = Date(8, 1),
                                endDate = Date(8, 24)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )

        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2021, 8, 1),
                                endDate = LocalDate(2021, 12, 31)
                        )
                ),
                actual = DatePeriod(
                        beginDate = Date(8, 1),
                        endDate = Date(12, 31)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )

        // over to the next calendar year i.e. pass the new year's eve
        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2021, 12, 1),
                                endDate = LocalDate(2022, 1, 24)
                        )
                ),
                actual = DatePeriod(
                        beginDate = Date(12, 1),
                        endDate = Date(1, 24)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )

        // fully in next calendar year
        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2022, 1, 1),
                                endDate = LocalDate(2022, 6, 24)
                        )
                ),
                actual = DatePeriod(
                        beginDate = Date(1, 1),
                        endDate = Date(6, 24)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )

        // quite rare case, season is not fully within one hunting year but extends
        // to the next one.
        // -> should be split and created periods should be within given hunting year
        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2021, 8, 1),
                                endDate = LocalDate(2021, 8, 24)
                        ),
                        LocalDatePeriod(
                                beginDate = LocalDate(2022, 7, 1),
                                endDate = LocalDate(2022, 7, 31)
                        )
                ),
                actual = DatePeriod(
                        beginDate = Date(7, 1),
                        endDate = Date(8, 24)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )

        // even more improbable case, season is not fully within one hunting year but extends
        // to the next one and to the next calendar year.
        // - starts in july, ends in june next year
        // -> should be split and created periods should be within given hunting year
        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2021, 8, 1),
                                endDate = LocalDate(2022, 6, 24)
                        ),
                        LocalDatePeriod(
                                beginDate = LocalDate(2022, 7, 1),
                                endDate = LocalDate(2022, 7, 31)
                        )
                ),
                actual = DatePeriod(
                        beginDate = Date(7, 1),
                        endDate = Date(6, 24)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )

        // corner case, full year
        // -> should be split and created periods should be within given hunting year.
        // (the created pieces could actually be joined but that's not currently implemented)
        assertEquals(
                expected = listOf(
                        LocalDatePeriod(
                                beginDate = LocalDate(2021, 8, 1),
                                endDate = LocalDate(2021, 12, 31)
                        ),
                        LocalDatePeriod(
                                beginDate = LocalDate(2022, 1, 1),
                                endDate = LocalDate(2022, 7, 31)
                        )
                ),
                actual = DatePeriod(
                        beginDate = Date(1, 1),
                        endDate = Date(12, 31)
                ).toLocalDatePeriodsWithinHuntingYear(2021)
        )
    }
}
