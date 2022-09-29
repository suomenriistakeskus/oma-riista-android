package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.GenderField
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText
import fi.riista.mobile.utils.toVisibility

class ReadOnlyGenderViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, GenderField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val genderSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val femaleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val maleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_middle)
    private val unknownRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

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

        unknownRadioButton.setText(R.string.gender_unknown)
        unknownRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ic_question_mark, 0, 0, 0)
        unknownRadioButton.isEnabled = false
    }

    override fun onBeforeUpdateBoundData(dataField: GenderField<FieldId>) {
        unknownRadioButton.visibility = dataField.settings.showUnknown.toVisibility()

        genderSelect.clearCheck()
        when (dataField.gender) {
            Gender.FEMALE -> genderSelect.check(femaleRadioButton.id)
            Gender.MALE -> genderSelect.check(maleRadioButton.id)
            Gender.UNKNOWN -> genderSelect.check(unknownRadioButton.id)
            null -> {
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
            val view = layoutInflater.inflate(R.layout.item_three_radio_button, container, attachToRoot)
            return ReadOnlyGenderViewHolder(view)
        }
    }
}
