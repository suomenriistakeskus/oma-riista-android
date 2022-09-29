package fi.riista.mobile.ui.dataFields.viewHolder

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
        textView.text = dataField.text
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
