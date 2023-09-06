package fi.riista.common.domain.model

import fi.riista.common.model.EntitiesByYearMonth
import fi.riista.common.model.YearMonth
import fi.riista.common.model.groupByYearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntitiesByYearMonthTest {

    @Test
    fun testGroupingByYearMonth() {
        val values = listOf(
            DatedInt(1, YearMonth(2022, 4)),
            DatedInt(2, YearMonth(2022, 1)),
            DatedInt(3, YearMonth(2022, 1)),
            DatedInt(4, YearMonth(2021, 5)),
            DatedInt(5, YearMonth(2022, 1)),
            DatedInt(6, YearMonth(2020, 12)),
            DatedInt(7, YearMonth(2022, 1)),
        )

        val intValues = values.map { it.value }
        val valueYearMonths = values.associate { it.value to it.yearMonth }

        assertEquals(
            listOf(
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2022, 4),
                    entities = listOf(1)
                ),
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2022, 1),
                    entities = listOf(2, 3, 5, 7)
                ),
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2021, 5),
                    entities = listOf(4)
                ),
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2020, 12),
                    entities = listOf(6)
                ),
            ),
            intValues.groupByYearMonth { valueYearMonths[it]!! }
        )
    }

    @Test
    fun testGroupingByYearMonthDifferentOrder() {
        val values = listOf(
            DatedInt(6, YearMonth(2020, 12)), // ^^ move all the way as first
            DatedInt(4, YearMonth(2021, 5)), // ^
            DatedInt(1, YearMonth(2022, 4)),
            DatedInt(3, YearMonth(2022, 1)), // ^
            DatedInt(2, YearMonth(2022, 1)),
            DatedInt(5, YearMonth(2022, 1)),
            DatedInt(7, YearMonth(2022, 1)),
        )

        val intValues = values.map { it.value }
        val valueYearMonths = values.associate { it.value to it.yearMonth }

        assertEquals(
            // result should still match the natural comparison order of YearMonth
            listOf(
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2022, 4),
                    entities = listOf(1),
                ),
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2022, 1),
                    entities = listOf(3, 2, 5, 7), // 3 is before 2 in raw data
                ),
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2021, 5),
                    entities = listOf(4),
                ),
                EntitiesByYearMonth(
                    yearMonth = YearMonth(2020, 12),
                    entities = listOf(6),
                ),
            ),
            intValues.groupByYearMonth { valueYearMonths[it]!! }
        )
    }
}

private data class DatedInt(
    val value: Int,
    val yearMonth: YearMonth
)
