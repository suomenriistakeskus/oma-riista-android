package fi.riista.common.domain.model

import fi.riista.common.model.HoursAndMinutes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoursAndMinutesTest {
    @Test
    fun testConversionFromMinutes() {
        assertEquals(HoursAndMinutes(0, 0), HoursAndMinutes(0))
        assertEquals(HoursAndMinutes(0, 1), HoursAndMinutes(1))
        assertEquals(HoursAndMinutes(0, 59), HoursAndMinutes(59))
        assertEquals(HoursAndMinutes(1, 0), HoursAndMinutes(60))
        assertEquals(HoursAndMinutes(23, 59), HoursAndMinutes(23 * 60 + 59))
        assertEquals(HoursAndMinutes(24, 0), HoursAndMinutes(24 * 60))
        assertEquals(HoursAndMinutes(48, 0), HoursAndMinutes(48 * 60))
        assertEquals(HoursAndMinutes(72, 30), HoursAndMinutes(72 * 60 + 30))
    }

    @Test
    fun testConversionToTotalMinutes() {
        assertEquals(0, HoursAndMinutes(0, 0).toTotalMinutes())
        assertEquals(1, HoursAndMinutes(0, 1).toTotalMinutes())
        assertEquals(59, HoursAndMinutes(0, 59).toTotalMinutes())
        assertEquals(60, HoursAndMinutes(1, 0).toTotalMinutes())
        assertEquals(23 * 60 + 59, HoursAndMinutes(23, 59).toTotalMinutes())
        assertEquals(24 * 60, HoursAndMinutes(24, 0).toTotalMinutes())
        assertEquals(48 * 60, HoursAndMinutes(48, 0).toTotalMinutes())
        assertEquals(72 * 60 + 30, HoursAndMinutes(72, 30).toTotalMinutes())
    }

    @Test
    fun testComparison() {
        assertEquals(HoursAndMinutes(0, 0), HoursAndMinutes(0, 0))
        assertTrue(HoursAndMinutes(0, 0) < HoursAndMinutes(0, 1))
        assertTrue(HoursAndMinutes(0, 0) < HoursAndMinutes(1, 0))
        assertTrue(HoursAndMinutes(0, 1) < HoursAndMinutes(1, 0))
        assertTrue(HoursAndMinutes(0, 59) < HoursAndMinutes(1, 0))
    }
}
