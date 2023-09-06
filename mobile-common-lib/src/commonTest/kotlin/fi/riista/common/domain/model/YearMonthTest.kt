package fi.riista.common.domain.model

import fi.riista.common.model.YearMonth
import fi.riista.common.model.groupByYearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YearMonthTest {
    @Test
    fun testCompare() {
        assertEquals(YearMonth(2022, 1), YearMonth(2022, 1), "==")
        assertTrue(YearMonth(2022, 2) > YearMonth(2022, 1), "> 1")
        assertTrue(YearMonth(2022, 1) > YearMonth(2021, 1), "> 2")
        assertTrue(YearMonth(2022, 1) > YearMonth(2021, 12), "> 3")

        assertTrue(YearMonth(2022, 1) < YearMonth(2022, 2), "< 1")
        assertTrue(YearMonth(2021, 1) < YearMonth(2022, 1), "< 2")
        assertTrue(YearMonth(2021, 12) < YearMonth(2022, 1), "< 3")
    }
}
