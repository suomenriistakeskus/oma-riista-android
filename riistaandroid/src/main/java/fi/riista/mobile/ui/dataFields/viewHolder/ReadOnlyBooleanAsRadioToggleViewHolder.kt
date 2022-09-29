package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText

class ReadOnlyBooleanAsRadioToggleViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, BooleanField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val yesNoSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val noRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val yesRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        yesNoSelect.isEnabled = false

        noRadioButton.setText(R.string.no)
        noRadioButton.isEnabled = false

        yesRadioButton.setText(R.string.yes)
        yesRadioButton.isEnabled = false
    }

    override fun onBeforeUpdateBoundData(dataField: BooleanField<FieldId>) {
        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.visibility = View.VISIBLE
        } else {
            labelView.visibility = View.GONE
        }

        yesNoSelect.clearCheck()
        when (dataField.value) {
            true -> yesNoSelect.check(yesRadioButton.id)
            false -> yesNoSelect.check(noRadioButton.id)
            null -> {
                // nop
            }
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, BooleanField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.READONLY_BOOLEAN_AS_RADIO_TOGGLE
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, BooleanField<FieldId>> {
            // intentionally utilize same layout as other toggles
            val view = layoutInflater.inflate(R.layout.item_two_radio_button, container, attachToRoot)
            return ReadOnlyBooleanAsRadioToggleViewHolder(view)
        }
    }
}
