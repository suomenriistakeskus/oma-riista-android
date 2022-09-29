package fi.riista.common.domain.srva.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class SrvaEventFieldTest {
    @Test
    fun testAllowedIndicesRange() {
        val minAllowedIndex = 0
        val maxAllowedIndex = 999
        assertFailsWith<IllegalArgumentException> {
            SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = minAllowedIndex - 1)
        }
        for (index in minAllowedIndex..maxAllowedIndex) {
            val field = SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = index)
            assertEquals(SrvaEventField.Type.METHOD_ITEM, field.type)
            assertEquals(index, field.index)
        }
        assertFailsWith<IllegalArgumentException> {
            SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = maxAllowedIndex + 1)
        }
    }

    @Test
    fun testConversionToInt() {
        assertEquals(1000, SrvaEventField(type = SrvaEventField.Type.LOCATION, index = 0).toInt())
        assertEquals(1001, SrvaEventField(type = SrvaEventField.Type.LOCATION, index = 1).toInt())
        assertEquals(1002, SrvaEventField(type = SrvaEventField.Type.LOCATION, index = 2).toInt())

        assertEquals(17000, SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = 0).toInt())
        assertEquals(17001, SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = 1).toInt())
    }

    @Test
    fun testConversionFromInt() {
        assertEquals(SrvaEventField(type = SrvaEventField.Type.LOCATION, index = 0), SrvaEventField.fromInt(1000))
        assertEquals(SrvaEventField(type = SrvaEventField.Type.LOCATION, index = 1), SrvaEventField.fromInt(1001))
        assertEquals(SrvaEventField(type = SrvaEventField.Type.LOCATION, index = 2), SrvaEventField.fromInt(1002))

        assertEquals(SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = 0), SrvaEventField.fromInt(17000))
        assertEquals(SrvaEventField(type = SrvaEventField.Type.METHOD_ITEM, index = 1), SrvaEventField.fromInt(17001))
    }
}
