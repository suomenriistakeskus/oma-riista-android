package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder

class ReadOnlyTextViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, StringField<FieldId>>(view) {

    private val textView: TextView = view.findViewById(R.id.tv_text)

    override fun onBeforeUpdateBoundData(dataField: StringField<FieldId>) {
        textView.text = dataField.value
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, StringField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.READONLY_TEXT
    ) {

        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, StringField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_text_read_only, container, attachToRoot)
            return ReadOnlyTextViewHolder(view)
        }
    }
}