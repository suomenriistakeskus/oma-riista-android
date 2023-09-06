package fi.riista.mobile.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.window.layout.WindowMetricsCalculator
import fi.riista.mobile.models.user.UserInfo

object UiUtils {
    enum class IconPosition {
        LEFT,
        RIGHT,
    }

    @JvmStatic
    fun dipToPixels(context: Context, dip: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip.toFloat(), context.resources.displayMetrics)
            .toInt()
    }

    @JvmStatic
    fun setTopMargin(view: View, dip: Int) {
        val params = view.layoutParams as LinearLayout.LayoutParams
        params.topMargin = dipToPixels(view.context, dip)
        view.layoutParams = params
    }

    @JvmStatic
    fun isSrvaVisible(userInfo: UserInfo?): Boolean {
        return userInfo != null && userInfo.enableSrva
    }

    @JvmStatic
    fun scrollToListviewBottom(listView: ListView) {
        listView.post { listView.setSelection(listView.count - 1) }
    }

    @JvmStatic
    fun addIconWithTint(button: AppCompatButton, @DrawableRes icon: Int, @ColorRes color: Int, position: IconPosition) {
        val buttonIcon: Drawable? = AppCompatResources.getDrawable(button.context, icon)
        buttonIcon?.let { drawable ->
            DrawableCompat.setTint(
                drawable,
                ResourcesCompat.getColor(button.resources, color, null)
            )
        }
        when (position) {
            IconPosition.LEFT -> button.setCompoundDrawablesWithIntrinsicBounds(buttonIcon, null, null, null)
            IconPosition.RIGHT -> button.setCompoundDrawablesWithIntrinsicBounds(null, null, buttonIcon, null)
        }
    }

    @JvmStatic
    fun getViewHeightInPixels(view: View): Int {
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(view.context)
        val deviceWidth = windowMetrics.bounds.width()

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        return view.measuredHeight
    }
}

fun String.removeSoftHyphens(): String {
    return this.replace("\u00AD", "")
}
