package fi.riista.common.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HunterNumberTest {
    @Test
    fun testHunterNumberValid() {
        val hunterNumber1 = "27272727"
        assertTrue(hunterNumber1.isHunterNumberValid())
        val hunterNumber2 = "88888888"
        assertTrue(hunterNumber2.isHunterNumberValid())
        val hunterNumber3 = "99999999"
        assertTrue(hunterNumber3.isHunterNumberValid())
    }

    @Test
    fun testHunterNumberInvalid() {
        val hunterNumber4 = "27272729"
        assertFalse(hunterNumber4.isHunterNumberValid())
        val hunterNumber5 = "12345678"
        assertFalse(hunterNumber5.isHunterNumberValid())
        val hunterNumber6 = "87654321"
        assertFalse(hunterNumber6.isHunterNumberValid())
    }

    @Test
    fun testChecksum() {
        assertEquals(6, calculateChecksum("1616161"))
        assertEquals(2, calculateChecksum("2222222"))
        assertEquals(5, calculateChecksum("5555555"))
        assertEquals(0, calculateChecksum("1324354"))
        assertEquals(9, calculateChecksum("8275982"))
        assertEquals(9, calculateChecksum("1129110"))
    }
}
