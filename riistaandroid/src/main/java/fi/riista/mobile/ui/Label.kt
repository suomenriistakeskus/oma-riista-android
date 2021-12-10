package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import fi.riista.mobile.R

class Label : FrameLayout {

    private var _text: String? = null
    var text: String?
        get() = _text
        set(value) {
            _text = value
            labelText.text = value
        }

    private var _required: Boolean = false
    var required: Boolean
        get() = _required
        set(value) {
            _required = value

            labelRequiredIndicator.visibility = when (value) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }

    private val labelText: TextView
    private val labelRequiredIndicator: TextView

    init {
        val view = inflate(context, R.layout.view_label, this)
        labelText = view.findViewById(R.id.tv_label_text)
        labelRequiredIndicator = view.findViewById(R.id.tv_label_required_indicator)
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
            context,
            attrs,
            defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Label, defStyle, 0)

        _text = a.getString(R.styleable.Label_text)
        _required = a.getBoolean(R.styleable.Label_required, false)

        a.recycle()
    }

    fun setText(@StringRes textRes: Int) {
        this.text = context.getString(textRes)
    }
}