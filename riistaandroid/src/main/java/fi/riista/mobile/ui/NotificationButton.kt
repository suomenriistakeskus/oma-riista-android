package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import fi.riista.mobile.R

class NotificationButton : FrameLayout {

    private var _buttonImage: Int? = null
    private var _buttonText: String? = null
    private var _notification: String? = null
    private var _notificationVisibility: Int = View.INVISIBLE

    private lateinit var buttonContent: View
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    var buttonImage: Int?
        get() = _buttonImage
        set(value) {
            _buttonImage = value
            value?.let {
                val drawable = AppCompatResources.getDrawable(context, value)
                imageView.setImageDrawable(drawable)
            } ?: run {
                imageView.setImageDrawable(null)
            }
        }

    var buttonText: String?
        get() = _buttonText
        set(value) {
            _buttonText = value
            textView.text = value
        }

    var notification: String?
        get() = _notification
        set(value) {
            val view = findViewById<TextView>(R.id.notification)
            _notification = value
            view.text = value
        }

    var notificationVisibility: Int
        get() = _notificationVisibility
        set(value) {
            _notificationVisibility = value
            findViewById<View>(R.id.notification).visibility = _notificationVisibility
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        buttonContent.isEnabled = enabled
        textView.isEnabled = enabled
        imageView.isEnabled = enabled
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.GroupHuntingButton, defStyle, 0
        )

        val iconId = a.getResourceId(R.styleable.GroupHuntingButton_buttonImage, 0)
        _buttonImage = iconId
        _buttonText = a.getString(R.styleable.GroupHuntingButton_buttonText)
        _notification = a.getString(R.styleable.GroupHuntingButton_notification)
        _notificationVisibility = a.getInt(R.styleable.GroupHuntingButton_notificationVisibility, 0)

        a.recycle()

        inflate(context, R.layout.group_hunting_button, this)
        buttonContent = findViewById(R.id.button_content)
        imageView = findViewById(R.id.buttonImage)
        textView = findViewById(R.id.buttonText)

        buttonImage = _buttonImage
        buttonText = _buttonText
        notification = _notification
        notificationVisibility = _notificationVisibility
    }
}
