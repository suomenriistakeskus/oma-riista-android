package fi.riista.mobile.ui

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatSpinner
import fi.riista.common.logging.getLogger

/**
 * A spinner that allows custom click handling.
 */
class ClickableSpinner(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    mode: Int,
    popupTheme: Resources.Theme?
) : AppCompatSpinner(context, attrs, defStyleAttr, mode, popupTheme) {

    /**
     * A custom click listener.
     *
     * Should return true if click was handled, false otherwise.
     */
    var clickListener: (() -> Boolean)? = null

    constructor(context: Context):
            this(context, null)
    constructor(context: Context, mode: Int):
            this(context, null, R.attr.spinnerStyle, mode)
    constructor(context: Context, attrs: AttributeSet?):
            this(context, attrs, R.attr.spinnerStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
            this(context, attrs, defStyleAttr, MODE_THEME)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, mode: Int):
            this(context, attrs, defStyleAttr, mode, null)


    override fun performClick(): Boolean {
        val clickHandled = clickListener?.let { it() } ?: false
        return if (clickHandled) {
            true
        } else {
            super.performClick()
        }
    }

    companion object {
        // same as in AppCompatSpinner
        private const val MODE_THEME = -1
    }
}