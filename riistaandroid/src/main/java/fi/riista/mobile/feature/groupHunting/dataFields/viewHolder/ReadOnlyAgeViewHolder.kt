package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import fi.riista.common.model.GameAge
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.RadioButtonImageText

class ReadOnlyAgeViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, AgeField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val ageSelect: RadioGroup = view.findViewById(R.id.rg_group)
    private val adultRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_left)
    private val youngRadioButton: RadioButtonImageText = view.findViewById(R.id.rb_right)

    init {
        labelView.setText(R.string.age_title)
        ageSelect.isEnabled = false

        adultRadioButton.setText(R.string.age_adult)
        adultRadioButton.isEnabled = false

        youngRadioButton.setText(R.string.age_young)
        youngRadioButton.isEnabled = false
    }

    override fun onBeforeUpdateBoundData(dataField: AgeField<FieldId>) {
        ageSelect.clearCheck()
        when (dataField.age) {
            GameAge.ADULT -> ageSelect.check(adultRadioButton.id)
            GameAge.YOUNG -> ageSelect.check(youngRadioButton.id)
            GameAge.UNKNOWN -> {
                // nop
            }
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, AgeField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.AGE
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, AgeField<FieldId>> {
            // intentionally utilize same layout as other "read only toggles"
            val view = layoutInflater.inflate(R.layout.item_two_radio_button, container, attachToRoot)
            return ReadOnlyAgeViewHolder(view)
        }
    }
}
