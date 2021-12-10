package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.model.Gender
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.GenderEventDispatcher
import fi.riista.common.ui.dataField.GenderField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText

class EditableGenderViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: GenderEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, GenderField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val genderSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val femaleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val maleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        labelView.setText(R.string.gender_title)
        genderSelect.isEnabled = true
        genderSelect.setOnCheckedChangeListener { group, checkedId ->
            if (!isBinding) {
                val radioButton: View = group.findViewById(checkedId)
                val index: Int = group.indexOfChild(radioButton)

                boundDataField?.let {
                    when (index) {
                        0 -> dataFieldEventDispatcher.dispatchGenderChanged(it.id, Gender.FEMALE)
                        1 -> dataFieldEventDispatcher.dispatchGenderChanged(it.id, Gender.MALE)
                    }
                }
            }
        }

        femaleRadioButton.setText(R.string.gender_female)
        femaleRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_female, 0, 0, 0)
        femaleRadioButton.isEnabled = true

        maleRadioButton.setText(R.string.gender_male)
        maleRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_male, 0, 0, 0)
        maleRadioButton.isEnabled = true
    }

    override fun onBeforeUpdateBoundData(dataField: GenderField<FieldId>) {
        labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()

        genderSelect.clearCheck()
        when (dataField.gender) {
            Gender.FEMALE -> genderSelect.check(femaleRadioButton.id)
            Gender.MALE -> genderSelect.check(maleRadioButton.id)
            Gender.UNKNOWN -> {
                // nop
            }
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: GenderEventDispatcher<FieldId>
    ) : DataFieldViewHolderFactory<FieldId, GenderField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.EDITABLE_GENDER
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, GenderField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_two_radio_button, container, attachToRoot)
            return EditableGenderViewHolder(eventDispatcher, view)
        }
    }

}
