package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.model.GameAge
import fi.riista.common.ui.dataField.AgeEventDispatcher
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText

class EditableAgeViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: AgeEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, AgeField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val ageSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val adultRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val youngRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        labelView.setText(R.string.age_title)
        ageSelect.isEnabled = true
        ageSelect.setOnCheckedChangeListener { group, checkedId ->
            if (!isBinding) {
                val radioButton: View = group.findViewById(checkedId)
                val index: Int = group.indexOfChild(radioButton)

                boundDataField?.let {
                    when (index) {
                        0 -> dataFieldEventDispatcher.dispatchAgeChanged(it.id, GameAge.ADULT)
                        1 -> dataFieldEventDispatcher.dispatchAgeChanged(it.id, GameAge.YOUNG)
                    }
                }
            }
        }

        adultRadioButton.setText(R.string.age_adult)
        adultRadioButton.isEnabled = true

        youngRadioButton.setText(R.string.age_young)
        youngRadioButton.isEnabled = true
    }

    override fun onBeforeUpdateBoundData(dataField: AgeField<FieldId>) {
        labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()

        ageSelect.clearCheck()
        when (dataField.age) {
            GameAge.ADULT -> ageSelect.check(adultRadioButton.id)
            GameAge.YOUNG -> ageSelect.check(youngRadioButton.id)
            GameAge.UNKNOWN -> {
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
            val view = layoutInflater.inflate(R.layout.item_two_radio_button, container, attachToRoot)
            return EditableAgeViewHolder(eventDispatcher, view)
        }
    }

}
