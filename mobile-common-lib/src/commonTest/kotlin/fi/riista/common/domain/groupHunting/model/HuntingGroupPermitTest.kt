package fi.riista.common.domain.groupHunting.model

import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import kotlin.test.Test
import kotlin.test.assertEquals

class HuntingGroupPermitTest {

    @Test
    fun testCoercingLocalDateToPermitValidityPeriods_emptyPeriods() {
        val permit = HuntingGroupPermit("permit", listOf())
        for (day in 1..30) {
            val date = LocalDate(2021, 5, day)
            assertEquals(date, date.coerceInPermitValidityPeriods(permit))
        }
    }

    @Test
    fun testCoercingLocalDateToPermitValidityPeriods_onePeriod() {
        val period = LocalDatePeriod(
                beginDate = LocalDate(2021, 5, 10),
                endDate = LocalDate(2021, 5, 15)
        )
        val permit = HuntingGroupPermit("permit", listOf(period))

        for (day in 1..30) {
            val date = LocalDate(2021, 5, day)
            val expectedDate = when {
                day < 10 -> period.beginDate
                day > 15 -> period.endDate
                else -> date
            }
            assertEquals(expectedDate, date.coerceInPermitValidityPeriods(permit))
        }
    }

    @Test
    fun testCoercingLocalDateToPermitValidityPeriods_twoSeparatePeriods() {
        val period1 = LocalDatePeriod(
                beginDate = LocalDate(2021, 5, 10),
                endDate = LocalDate(2021, 5, 15)
        )
        val period2 = LocalDatePeriod(
                beginDate = LocalDate(2021, 5, 20),
                endDate = LocalDate(2021, 5, 25)
        )
        val permit1 = HuntingGroupPermit("permit1", listOf(period1, period2))
        val permit2 = HuntingGroupPermit("permit2", listOf(period2, period1))

        for (day in 1..30) {
            val date = LocalDate(2021, 5, day)
            val expectedDate = when {
                day < 10 -> period1.beginDate
                day in 16..19 -> period1.endDate
                day > 25 -> period2.endDate
                else -> date
            }
            assertEquals(expectedDate, date.coerceInPermitValidityPeriods(permit1), "permit1")
            assertEquals(expectedDate, date.coerceInPermitValidityPeriods(permit2), "permit2")
        }
    }

    @Test
    fun testCoercingLocalDateToPermitValidityPeriods_twoOverlappingPeriods() {
        val period1 = LocalDatePeriod(
                beginDate = LocalDate(2021, 5, 10),
                endDate = LocalDate(2021, 5, 20)
        )
        val period2 = LocalDatePeriod(
                beginDate = LocalDate(2021, 5, 15),
                endDate = LocalDate(2021, 5, 25)
        )
        val permit1 = HuntingGroupPermit("permit", listOf(period1, period2))
        val permit2 = HuntingGroupPermit("permit", listOf(period2, period1))

        for (day in 1..30) {
            val date = LocalDate(2021, 5, day)
            val expectedDate = when {
                day < 10 -> period1.beginDate
                day > 25 -> period2.endDate
                else -> date
            }
            assertEquals(expectedDate, date.coerceInPermitValidityPeriods(permit1))
            assertEquals(expectedDate, date.coerceInPermitValidityPeriods(permit2))
        }
    }
}