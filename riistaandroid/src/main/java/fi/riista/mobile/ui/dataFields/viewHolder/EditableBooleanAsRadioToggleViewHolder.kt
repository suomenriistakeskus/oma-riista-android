package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.ui.dataField.BooleanEventDispatcher
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText

class EditableBooleanAsRadioToggleViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: BooleanEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, BooleanField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val yesNoSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val noRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val yesRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        yesNoSelect.isEnabled = true
        yesNoSelect.setOnCheckedChangeListener { group, checkedId ->
            if (!isBinding) {
                val radioButton: View = group.findViewById(checkedId)
                val index: Int = group.indexOfChild(radioButton)

                boundDataField?.let {
                    when (index) {
                        0 -> dataFieldEventDispatcher.dispatchBooleanChanged(it.id, false)
                        1 -> dataFieldEventDispatcher.dispatchBooleanChanged(it.id, true)
                    }
                }
            }
        }


        noRadioButton.setText(R.string.no)
        noRadioButton.isEnabled = true

        yesRadioButton.setText(R.string.yes)
        yesRadioButton.isEnabled = true
    }

    override fun onBeforeUpdateBoundData(dataField: BooleanField<FieldId>) {
        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
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

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: BooleanEventDispatcher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, BooleanField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.EDITABLE_BOOLEAN_AS_RADIO_TOGGLE
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, BooleanField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_two_radio_button, container, attachToRoot)
            return EditableBooleanAsRadioToggleViewHolder(eventDispatcher, view)
        }
    }
}
