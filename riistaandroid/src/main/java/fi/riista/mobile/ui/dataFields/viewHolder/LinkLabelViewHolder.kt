package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import fi.riista.common.ui.dataField.ActionEventDispatcher
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class LinkLabelViewHolder<FieldId : DataFieldId>(
    private val eventDispatcher: ActionEventDispatcher<FieldId>?,
    view: View,
) : DataFieldViewHolder<FieldId, LabelField<FieldId>>(view) {

    private val linkButton: MaterialButton = view.findViewById(R.id.btn_link)

    init {
        linkButton.isEnabled = eventDispatcher != null

        linkButton.setOnClickListener {
            boundDataField?.letWith(eventDispatcher) { field, dispatcher ->
                dispatcher.dispatchEvent(field.id)
            }
        }
    }
    override fun onBeforeUpdateBoundData(dataField: LabelField<FieldId>) {
        linkButton.text = dataField.text
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: ActionEventDispatcher<FieldId>?
    ) : DataFieldViewHolderFactory<FieldId, LabelField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.LABEL_LINK
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LabelField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_link_label, container, attachToRoot)
            return LinkLabelViewHolder(eventDispatcher, view)
        }
    }
}
