package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.ui.dataField.BooleanEventDispatcher
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.ClickableCheckbox
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class BooleanAsCheckboxViewHolder<FieldId : DataFieldId>(
    private val eventDispatcher: BooleanEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, BooleanField<FieldId>>(view) {

    private val checkbox: ClickableCheckbox = view.findViewById(R.id.checkbox)

    init {
        checkbox.clickListener = {
            boundDataField?.let { field ->
                val currentlyChecked = field.value == true
                eventDispatcher.dispatchBooleanChanged(field.id, !currentlyChecked)
            }
            true
        }
    }

    override fun onBeforeUpdateBoundData(dataField: BooleanField<FieldId>) {
        checkbox.text = dataField.settings.label
        checkbox.isEnabled = !dataField.settings.readOnly

        checkbox.isChecked = dataField.value == true
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: BooleanEventDispatcher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, BooleanField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.BOOLEAN_AS_CHECKBOX
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, BooleanField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_checkbox, container, attachToRoot)
            return BooleanAsCheckboxViewHolder(eventDispatcher, view)
        }
    }
}
