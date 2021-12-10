package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

/**
 * TextView will throw an exception if next view is not focusable.
 * This listener will try to move the focus, but wont throw an exception if it is not possible.
 * It will skip over components that are not focusable.
 */
class FocusingOnEditorActionListener: TextView.OnEditorActionListener {
    override fun onEditorAction(textView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            var view = textView?.focusSearch(View.FOCUS_DOWN)
            while (view != null) {
                if (view.requestFocus(View.FOCUS_FORWARD)) {
                    return true
                }
                view = textView?.focusSearch(View.FOCUS_DOWN)
            }
        }
        return false
    }
}
