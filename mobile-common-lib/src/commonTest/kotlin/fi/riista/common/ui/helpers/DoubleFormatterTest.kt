package fi.riista.common.ui.helpers

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.Species
import fi.riista.common.helpers.TestStringProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleFormatterTest {

    private val doubleFormatter = DoubleFormatter(stringProvider = TestStringProvider.INSTANCE)

    @Test
    fun testOneDecimalFormatting() {
        assertEquals("0.0", doubleFormatter.formatWithOneDecimal(0.0))
        assertEquals("1.0", doubleFormatter.formatWithOneDecimal(1.0))
        assertEquals("1.1", doubleFormatter.formatWithOneDecimal(1.1))
        assertEquals("20.1", doubleFormatter.formatWithOneDecimal(20.1), "20.1")
        assertEquals("20.1", doubleFormatter.formatWithOneDecimal(20.12), "20.12")
        assertEquals("20.1", doubleFormatter.formatWithOneDecimal(20.18), "20.18") // not-rounded
        assertEquals("20.1", doubleFormatter.formatWithOneDecimal(20.182), "20.182") // not-rounded
    }

    @Test
    fun testZeroDecimalFormatting() {
        assertEquals("0", doubleFormatter.formatWithZeroDecimals(0.0))
        assertEquals("1", doubleFormatter.formatWithZeroDecimals(1.0))
        assertEquals("1", doubleFormatter.formatWithZeroDecimals(1.1))
        assertEquals("20", doubleFormatter.formatWithZeroDecimals(20.1), "20.1")
        assertEquals("20", doubleFormatter.formatWithZeroDecimals(20.2), "20.2")
        assertEquals("20", doubleFormatter.formatWithZeroDecimals(20.8), "20.8") // not-rounded
        assertEquals("20", doubleFormatter.formatWithZeroDecimals(20.82), "20.82") // not-rounded
    }
}