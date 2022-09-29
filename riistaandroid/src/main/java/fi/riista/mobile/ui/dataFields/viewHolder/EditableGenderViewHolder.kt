package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.view.updatePadding
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.GenderEventDispatcher
import fi.riista.common.ui.dataField.GenderField
import fi.riista.mobile.R
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.toVisibility

class EditableGenderViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: GenderEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, GenderField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val genderSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val femaleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val maleRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_middle)
    private val unknownRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        labelView.setText(R.string.gender_title)
        genderSelect.isEnabled = true
        genderSelect.setOnCheckedChangeListener { group, checkedId ->
            if (!isBinding) {
                val field = boundDataField ?: kotlin.run {
                    return@setOnCheckedChangeListener
                }

                val radioButton: View? = group.findViewById(checkedId)
                if (radioButton == null) {
                    if (field.settings.requirementStatus.isRequired().not()) {
                        dataFieldEventDispatcher.dispatchGenderChanged(
                            fieldId = field.id,
                            value = null
                        )
                    }
                    return@setOnCheckedChangeListener
                }

                when (group.indexOfChild(radioButton)) {
                    0 -> dataFieldEventDispatcher.dispatchGenderChanged(field.id, Gender.FEMALE)
                    1 -> dataFieldEventDispatcher.dispatchGenderChanged(field.id, Gender.MALE)
                    2 -> dataFieldEventDispatcher.dispatchGenderChanged(field.id, Gender.UNKNOWN)
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

        unknownRadioButton.setText(R.string.gender_unknown)
        unknownRadioButton.updatePadding(left = 0, right = 0) // more space for "unknown"
        unknownRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ic_question_mark, 0, 0, 0)
        unknownRadioButton.isEnabled = true
    }

    override fun onBeforeUpdateBoundData(dataField: GenderField<FieldId>) {
        labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
        unknownRadioButton.visibility = dataField.settings.showUnknown.toVisibility()
        listOf(femaleRadioButton, maleRadioButton, unknownRadioButton).forEach { btn ->
            btn.setToggleEnabled(dataField.settings.requirementStatus.isRequired().not())
        }

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
            val view = layoutInflater.inflate(R.layout.item_three_radio_button, container, attachToRoot)
            return EditableGenderViewHolder(eventDispatcher, view)
        }
    }

}
