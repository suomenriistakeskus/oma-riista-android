package fi.riista.mobile.utils

import android.text.InputFilter
import android.text.Spanned
import fi.riista.common.logging.getLogger

class DecimalValueFilter(
    var maxDecimals: Int?
): InputFilter {

    // not in companion object in case locale is changed
    val useCommas: Boolean by lazy {
        "%.1f".format(1.5f).contains(',')
            .also {
                logger.v { "Replacing with commas = $it" }
            }
    }

    val separator: Char by lazy {
        when (useCommas) {
            true -> ','
            false -> '.'
        }
    }

    val allowedDigits: String by lazy {
        when (useCommas) {
            true -> "1234567890.,"
            false -> "1234567890."
        }
    }

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source == null) {
            return null
        }

        val correctedInput = source.subSequence(startIndex = start, endIndex = end).toString()
            .let { input ->
                when (useCommas) {
                    true -> input.replace('.', ',')
                    false -> input.replace(',', '.')
                }
            }

        val result = dest
            ?.let { StringBuilder(dest) }
            ?.replace(dstart, dend, correctedInput)
            ?.toString()

        if (result != null && shouldPreventInput(result)) {
            return ""
        }

        return correctedInput
    }

    private fun shouldPreventInput(result: String): Boolean {
        val maxDecimalsReached = maxDecimals.let { decimals ->
            decimals != null && result.calculateAmountOfDecimals() > decimals
        }
        val noDecimalsAllowed = maxDecimals == 0

        return result.countSeparators() > 1 || // don't allow multiple separators
            result.firstOrNull() in SEPARATORS || // don't allow starting with separator
            maxDecimalsReached ||
            (noDecimalsAllowed && result.contains(separator))
    }

    private fun String.countSeparators(): Int {
        return count { char ->
            char in SEPARATORS
        }
    }

    private fun String.calculateAmountOfDecimals(): Int {
        return when (val separatorIndex = lastIndexOf(separator)) {
            -1 -> 0
            else -> lastIndex - separatorIndex
        }.also {
            logger.v { "decimal amount $it" }
        }
    }

    companion object {
        private val SEPARATORS = arrayOf('.', ',')

        private val logger by getLogger(DecimalValueFilter::class)
    }

}