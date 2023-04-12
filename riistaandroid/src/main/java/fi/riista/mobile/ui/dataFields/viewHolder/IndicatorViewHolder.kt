package fi.riista.mobile.ui.dataFields.viewHolder

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.IndicatorColor
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class IndicatorViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, LabelField<FieldId>>(view) {

    private val textView: TextView = view.findViewById(R.id.tv_text)

    override fun onBeforeUpdateBoundData(dataField: LabelField<FieldId>) {
        textView.text = dataField.text
        when (dataField.settings.indicatorColor) {
            IndicatorColor.GREEN -> R.color.traffic_light_green
            IndicatorColor.YELLOW -> R.color.traffic_light_yellow
            IndicatorColor.RED -> R.color.traffic_light_red
            IndicatorColor.INVISIBLE -> null
        }
            ?.let { indicatorColorRes ->
                ContextCompat.getColor(context, indicatorColorRes)
            }
            ?.letWith(ContextCompat.getDrawable(context, R.drawable.circle)) { color, circle ->
                circle.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                textView.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null)
            }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, LabelField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.LABEL_INDICATOR
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LabelField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_indicator_label, container, attachToRoot)
            return IndicatorViewHolder(view)
        }
    }
}
