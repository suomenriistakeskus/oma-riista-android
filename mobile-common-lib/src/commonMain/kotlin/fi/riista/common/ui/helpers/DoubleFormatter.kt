package fi.riista.common.ui.helpers

import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider

class DoubleFormatter(
    private val stringProvider: StringProvider
) {
    internal fun formatWithZeroDecimals(value: Double): String {
        return stringProvider.getFormattedDouble(RR.stringFormat.double_format_zero_decimals, value)
    }

    internal fun formatWithOneDecimal(value: Double): String {
        return stringProvider.getFormattedDouble(RR.stringFormat.double_format_one_decimal, value)
    }
}

internal fun Double.formatWithZeroDecimals(formatter: DoubleFormatter): String {
    return formatter.formatWithZeroDecimals(this)
}

internal fun Double.formatWithOneDecimal(formatter: DoubleFormatter): String {
    return formatter.formatWithOneDecimal(this)
}