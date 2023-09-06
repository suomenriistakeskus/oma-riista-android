package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import fi.riista.common.ui.dataField.BooleanEventDispatcher
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.toVisibility

class BooleanAsSwitchViewHolder<FieldId : DataFieldId>(
    private val eventDispatcher: BooleanEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, BooleanField<FieldId>>(view) {

    private val switchBox: SwitchCompat = view.findViewById(R.id.switchbox)
    private val explanationTextView: TextView = view.findViewById(R.id.tv_explanation)

    init {
        switchBox.setOnClickListener {
            boundDataField?.let { field ->
                val currentlyChecked = field.value == true
                eventDispatcher.dispatchBooleanChanged(field.id, !currentlyChecked)
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: BooleanField<FieldId>) {
        switchBox.text = dataField.settings.label
        switchBox.isEnabled = !dataField.settings.readOnly
        switchBox.isChecked = dataField.value == true

        explanationTextView.visibility = (dataField.settings.text != null).toVisibility()
        explanationTextView.text = dataField.settings.text
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: BooleanEventDispatcher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, BooleanField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.BOOLEAN_AS_SWITCH
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, BooleanField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_switch, container, attachToRoot)
            return BooleanAsSwitchViewHolder(eventDispatcher, view)
        }
    }
}
