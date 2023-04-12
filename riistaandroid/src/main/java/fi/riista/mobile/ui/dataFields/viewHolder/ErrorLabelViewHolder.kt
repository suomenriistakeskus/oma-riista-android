package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class ErrorLabelViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, LabelField<FieldId>>(view) {

    private val textView: TextView = view.findViewById(R.id.tv_text)

    override fun onBeforeUpdateBoundData(dataField: LabelField<FieldId>) {
        textView.text = dataField.text

        if (boundDataField?.settings?.highlightBackground != dataField.settings.highlightBackground) {
            updateBackgroundHighlight(dataField.settings.highlightBackground)
        }
    }

    private fun updateBackgroundHighlight(highlightBackground: Boolean) {
        if (highlightBackground) {
            textView.setBackgroundColor(
                ResourcesCompat.getColor(context.resources, R.color.color_background_error, null)
            )
            val padding = context.resources.getDimensionPixelSize(R.dimen.datafield_padding_medium)
            textView.setPadding(padding)
        } else {
            textView.setBackgroundColor(
                ResourcesCompat.getColor(context.resources, R.color.activityBackground, null)
            )
            textView.setPadding(0)
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, LabelField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.LABEL_ERROR
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LabelField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_error_label, container, attachToRoot)
            return ErrorLabelViewHolder(view)
        }
    }
}
