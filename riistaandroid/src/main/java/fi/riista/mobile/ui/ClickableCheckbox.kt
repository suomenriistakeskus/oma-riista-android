package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatCheckBox

/**
 * A checkbox that allows custom click handling.
 */
class ClickableCheckbox(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : AppCompatCheckBox(context, attrs, defStyleAttr) {

    /**
     * A custom click listener.
     *
     * Should return true if click was handled, false otherwise.
     */
    var clickListener: (() -> Boolean)? = null

    constructor(context: Context):
            this(context, null)
    constructor(context: Context, attrs: AttributeSet?):
            this(context, attrs, R.attr.checkboxStyle)

    override fun performClick(): Boolean {
        val clickHandled = clickListener?.let { it() } ?: false
        return if (clickHandled) {
            true
        } else {
            super.performClick()
        }
    }
}