package fi.riista.mobile.utils

import android.content.Context
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

class SoftInputService(private val context: Context?, private val targetView: View?) : Runnable {

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun run() {
        if (context == null || targetView == null || !targetView.isFocusable || !targetView.isFocusableInTouchMode) {
            return
        }
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!targetView.requestFocus()) {
            Log.d(TAG, "Cannot focus on view")
            post()
        } else if (!imm.showSoftInput(targetView, InputMethodManager.SHOW_IMPLICIT)) {
            Log.d(TAG, "Unable to show keyboard")
            post()
        }
    }

    fun show() {
        handler.post(this)
    }

    private fun post() {
        handler.postDelayed(this, INTERVAL_MS.toLong())
    }

    companion object {
        private val TAG = SoftInputService::class.java.simpleName
        private const val INTERVAL_MS = 100

        fun hide(context: Context, windowToken: IBinder?) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}
