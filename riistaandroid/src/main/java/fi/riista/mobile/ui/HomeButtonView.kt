package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import fi.riista.mobile.R

class HomeButtonView : FrameLayout {

    private var _mainBtnText: String? = null
    private var _mainBtnImage: Int? = null
    private var _subitem1Text: String? = null
    private var _subitem2Text: String? = null
    private var _subitem1Visibility: Int = View.VISIBLE
    private var _subitem2Visibility: Int = View.VISIBLE

    private lateinit var mainBtnTextView: TextView
    private lateinit var mainBtnImageView: ImageView
    private lateinit var subitem1View: TextView
    private lateinit var subitem2View: TextView

    private var mainBtnText: String?
        get() = _mainBtnText
        set(value) {
            _mainBtnText = value
            mainBtnTextView = findViewById(R.id.home_view_main_item_text)
            mainBtnTextView.text = _mainBtnText
        }

    private var mainBtnImage: Int?
        get() = _mainBtnImage
        set(value) {
            mainBtnImageView = findViewById(R.id.home_view_main_item_image)

            value?.let {
                mainBtnImageView.setImageResource(it)
            } ?: run {
                mainBtnImageView.setImageDrawable(null)
            }

            _mainBtnImage = value
        }

    private var subitem1Text: String?
        get() = _subitem1Text
        set(value) {
            _subitem1Text = value
            subitem1View = findViewById(R.id.home_view_subitem_1_button)
            subitem1View.text = _subitem1Text
        }

    private var subitem2Text: String?
        get() = _subitem2Text
        set(value) {
            _subitem2Text = value
            subitem2View = findViewById(R.id.home_view_subitem_2_button)
            subitem2View.text = subitem2Text
        }

    private var subitem1Visibility: Int
        get() = _subitem1Visibility
        set(value) {
            _subitem1Visibility = value
            findViewById<View>(R.id.home_view_subitem_1).visibility = _subitem1Visibility
        }

    private var subitem2Visibility: Int
        get() = _subitem2Visibility
        set(value) {
            _subitem2Visibility = value
            findViewById<View>(R.id.home_view_subitem_2).visibility = _subitem2Visibility
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
        val a = context.obtainStyledAttributes(attrs, R.styleable.HomeButtonView, defStyle, 0)

        _mainBtnText = a.getString(R.styleable.HomeButtonView_mainButtonText)
        _subitem1Text = a.getString(R.styleable.HomeButtonView_subitem1Text)
        _subitem2Text = a.getString(R.styleable.HomeButtonView_subitem2Text)
        _subitem1Visibility = a.getInt(R.styleable.HomeButtonView_subitem1Visibility, 0)
        _subitem2Visibility = a.getInt(R.styleable.HomeButtonView_subitem2Visibility, 0)

        val iconId = a.getResourceId(R.styleable.HomeButtonView_mainButtonSrc, 0)
        val drawable = AppCompatResources.getDrawable(context, iconId)

        a.recycle()

        inflate(context, R.layout.view_home_item, this)

        mainBtnTextView = findViewById(R.id.home_view_main_item_text)
        mainBtnImageView = findViewById(R.id.home_view_main_item_image)
        subitem1View = findViewById(R.id.home_view_subitem_1_button)
        subitem2View = findViewById(R.id.home_view_subitem_2_button)

        mainBtnTextView.text = mainBtnText
        mainBtnImageView.setImageDrawable(drawable)
        subitem1View.text = subitem1Text
        subitem2View.text = subitem2Text

        subitem1Visibility = _subitem1Visibility
        subitem2Visibility = _subitem2Visibility
    }
}
