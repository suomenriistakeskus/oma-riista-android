package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.domain.model.GameAge
import fi.riista.common.ui.dataField.AgeEventDispatcher
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.toVisibility

class EditableAgeViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: AgeEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, AgeField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val ageSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val adultRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val youngRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_middle)
    private val unknownRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        labelView.setText(R.string.age_title)
        ageSelect.isEnabled = true
        ageSelect.setOnCheckedChangeListener { group, checkedId ->
            if (!isBinding) {
                val field = boundDataField ?: kotlin.run {
                    return@setOnCheckedChangeListener
                }

                val radioButton: View? = group.findViewById(checkedId)
                if (radioButton == null) {
                    if (field.settings.requirementStatus.isRequired().not()) {
                        dataFieldEventDispatcher.dispatchAgeChanged(
                            fieldId = field.id,
                            value = null
                        )
                    }
                    return@setOnCheckedChangeListener
                }

                when (group.indexOfChild(radioButton)) {
                    0 -> dataFieldEventDispatcher.dispatchAgeChanged(field.id, GameAge.ADULT)
                    1 -> dataFieldEventDispatcher.dispatchAgeChanged(field.id, GameAge.YOUNG)
                    2 -> dataFieldEventDispatcher.dispatchAgeChanged(field.id, GameAge.UNKNOWN)
                }
            }
        }

        adultRadioButton.setText(R.string.age_adult)
        adultRadioButton.isEnabled = true

        youngRadioButton.setText(R.string.age_young)
        youngRadioButton.isEnabled = true

        unknownRadioButton.setText(R.string.age_unknown)
        unknownRadioButton.isEnabled = true
    }

    override fun onBeforeUpdateBoundData(dataField: AgeField<FieldId>) {
        labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
        unknownRadioButton.visibility = dataField.settings.showUnknown.toVisibility()
        listOf(adultRadioButton, youngRadioButton, unknownRadioButton).forEach { btn ->
            btn.setToggleEnabled(dataField.settings.requirementStatus.isRequired().not())
        }

        ageSelect.clearCheck()
        when (dataField.age) {
            GameAge.ADULT -> ageSelect.check(adultRadioButton.id)
            GameAge.YOUNG -> ageSelect.check(youngRadioButton.id)
            GameAge.UNKNOWN -> ageSelect.check(unknownRadioButton.id)
            GameAge.LESS_THAN_ONE_YEAR,
            GameAge.BETWEEN_ONE_AND_TWO_YEARS,
            null -> {
                // nop
            }
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: AgeEventDispatcher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, AgeField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.EDITABLE_AGE
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, AgeField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_three_radio_button, container, attachToRoot)
            return EditableAgeViewHolder(eventDispatcher, view)
        }
    }

}
