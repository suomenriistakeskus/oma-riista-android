package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringField
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class ReadOnlySingleLineTextViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, StringField<FieldId>>(view) {

    private val labelTextView: TextView = view.findViewById(R.id.tv_label)
    private val valueTextView: TextView = view.findViewById(R.id.tv_value)

    override fun onBeforeUpdateBoundData(dataField: StringField<FieldId>) {
        if (dataField.settings.label != null) {
            labelTextView.visibility = View.VISIBLE
            labelTextView.text = dataField.settings.label
        } else {
            labelTextView.visibility = View.GONE
        }
        valueTextView.text = dataField.value
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, StringField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
    ) {

        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, StringField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_text_read_only_single_line, container, attachToRoot)
            return ReadOnlySingleLineTextViewHolder(view)
        }
    }
}
