package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder

class ReadOnlySingleLineTextViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, StringField<FieldId>>(view) {

    private val labelTextView: TextView = view.findViewById(R.id.tv_label)
    private val valueTextView: TextView = view.findViewById(R.id.tv_value)

    override fun onBeforeUpdateBoundData(dataField: StringField<FieldId>) {
        val valueGravity = if (dataField.settings.label != null) {
            labelTextView.visibility = View.VISIBLE
            labelTextView.text = dataField.settings.label

            // label exists, align value to the end
            Gravity.END
        } else {
            labelTextView.visibility = View.GONE

            // label doesn't exist, align to the start
            Gravity.START
        }
        valueTextView.text = dataField.value
        valueTextView.gravity = valueGravity
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