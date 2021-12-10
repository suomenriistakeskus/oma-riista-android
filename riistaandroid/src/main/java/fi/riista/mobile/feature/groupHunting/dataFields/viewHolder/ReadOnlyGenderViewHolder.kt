package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.model.Gender
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.GenderField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText

class ReadOnlyGenderViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, GenderField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val genderSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val femaleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val maleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        labelView.setText(R.string.gender_title)
        genderSelect.isEnabled = false

        femaleRadioButton.setText(R.string.gender_female)
        femaleRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_female, 0, 0, 0)
        femaleRadioButton.isEnabled = false

        maleRadioButton.setText(R.string.gender_male)
        maleRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_male, 0, 0, 0)
        maleRadioButton.isEnabled = false
    }

    override fun onBeforeUpdateBoundData(dataField: GenderField<FieldId>) {
        genderSelect.clearCheck()
        when (dataField.gender) {
            Gender.FEMALE -> genderSelect.check(femaleRadioButton.id)
            Gender.MALE -> genderSelect.check(maleRadioButton.id)
            Gender.UNKNOWN -> {
                // nop
            }
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, GenderField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.GENDER
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, GenderField<FieldId>> {
            // intentionally utilize same layout as other toggles
            val view = layoutInflater.inflate(R.layout.item_two_radio_button, container, attachToRoot)
            return ReadOnlyGenderViewHolder(view)
        }
    }
}
