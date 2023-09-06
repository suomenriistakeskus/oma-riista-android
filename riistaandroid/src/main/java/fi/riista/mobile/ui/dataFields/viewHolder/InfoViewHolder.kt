package fi.riista.mobile.ui.dataFields.viewHolder

import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class InfoViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, LabelField<FieldId>>(view) {

    private val textView: TextView = view.findViewById(R.id.tv_text)

    override fun onBeforeUpdateBoundData(dataField: LabelField<FieldId>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_FULL
        }

        textView.text = dataField.text
        textView.gravity = when (dataField.settings.textAlignment) {
            LabelField.TextAlignment.JUSTIFIED, // fallback to left alignment but include justification mode if supported
            LabelField.TextAlignment.LEFT -> Gravity.START
            LabelField.TextAlignment.CENTER -> Gravity.CENTER_HORIZONTAL
        }

        if (dataField.settings.textAlignment == LabelField.TextAlignment.JUSTIFIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textView.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            }
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, LabelField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.LABEL_INFO
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LabelField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_info, container, attachToRoot)
            return InfoViewHolder(view)
        }
    }
}
