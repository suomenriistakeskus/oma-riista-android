package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import fi.riista.mobile.R
import org.joda.time.DateTime

typealias HideListener = () -> Unit

class BusyIndicatorView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {


    private enum class State {
        HIDDEN,
        SHOWN,
        HIDING
    }

    private var currentState = State.HIDDEN
    private var displayTime = DateTime(0) // epoch

    private var hideListeners = mutableListOf<HideListener>()

    init {
        inflate(context, R.layout.view_busy_indicator, this)

        // hidden initially
        visibility = View.GONE

        // prevent all clicks to underlying UI elements
        isClickable = true
        isFocusable = true
    }

    fun show() {
        setState(State.SHOWN)
    }

    fun hide(hideCompletedListener: HideListener = {}) {
        when (currentState) {
            State.HIDDEN -> {
                // make sure we're really hidden
                setState(State.HIDDEN)
                hideCompletedListener()
            }
            State.SHOWN -> {
                setState(State.HIDING)
                hideListeners.add(hideCompletedListener)

                val remainingTimeToDisplay = remainingTimeToDisplayInMillis()
                if (remainingTimeToDisplay > 0) {
                    handler.postDelayed(
                            {
                                setState(State.HIDDEN, State.HIDING)
                            }, remainingTimeToDisplay
                    )
                } else {
                    setState(State.HIDDEN)
                }
            }
            State.HIDING -> {
                hideListeners.add(hideCompletedListener)
            }
        }
    }

    private fun remainingTimeToDisplayInMillis(): Long {
        val timeDisplayed = DateTime.now().millis - displayTime.millis
        return (MINIMUM_DISPLAY_TIME_MILLIS - timeDisplayed).coerceAtLeast(0)
    }

    @Suppress("SameParameterValue")
    private fun setState(state: State, expectedCurrentState: State): Boolean {
        return if (currentState == expectedCurrentState) {
            setState(state)
            true
        } else {
            false
        }
    }

    private fun setState(state: State) {
        // NOTE: remember to update the visibility with doSetVisibility()
        when (state) {
            State.HIDDEN -> {
                doSetVisibility(View.GONE)
                hideListeners.forEach { listener ->
                    listener()
                }
                hideListeners.clear()
            }
            State.SHOWN -> {
                hideListeners.clear()
                doSetVisibility(View.VISIBLE)
                displayTime = DateTime.now()
            }
            State.HIDING -> {
                // nop
            }
        }
        currentState = state
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            show()
        } else {
            hide()
        }
    }

    private fun doSetVisibility(visibility: Int) {
        super.setVisibility(visibility)
    }

    companion object {
        private const val MINIMUM_DISPLAY_TIME_MILLIS = 500
    }
}