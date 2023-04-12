package fi.riista.mobile.utils

import android.text.InputFilter
import android.text.Spanned

class MaxValueFilter(
    var enabled: Boolean,
    var maxValue: Float,
) : InputFilter {

    constructor(): this(enabled = true, maxValue = Float.MAX_VALUE)
    constructor(maxValue: Float): this(enabled = true, maxValue = maxValue)

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source == null || dest == null || !enabled) {
            return null // allow change
        }

        val result = StringBuilder(dest)
            .replace(dstart, dend, source.subSequence(start, end).toString())
            .toString()
            .replace(',', '.') // just in case we're using commas

        try {
            val value = result.toFloat()
            if (value < 0.0f || value > maxValue) {
                // Invalid value
                return ""
            }
        } catch (e: NumberFormatException) {
            // Allow
        }
        return null
    }
}