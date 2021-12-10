package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.SelectDurationField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.riistaSdkHelpers.formatToHoursAndMinutesString
import fi.riista.mobile.ui.Label


interface DurationPickerFragmentLauncher<FieldId : DataFieldId> {
    fun showDurationPickerFragment(fieldId: FieldId,
                                   possibleDurations: List<HoursAndMinutes>,
                                   selectedDuration: HoursAndMinutes)
}


class SelectDurationViewHolder<FieldId : DataFieldId>(
    pickerLauncher: DurationPickerFragmentLauncher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, SelectDurationField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val valueTextView: TextView = view.findViewById(R.id.tv_value)

    private val lineBelowText: View = view.findViewById(R.id.view_underline)
    private val dropdownArrow: View = view.findViewById(R.id.iv_drop_down_arrow)

    init {
        view.setOnClickListener {
            boundDataField?.let { field ->
                pickerLauncher.showDurationPickerFragment(
                        fieldId = field.id,
                        possibleDurations = field.possibleValues,
                        selectedDuration = field.value
                )
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: SelectDurationField<FieldId>) {
        // update also line + drowdownArrow isEnabled status in order to update their styles
        listOf(itemView, lineBelowText, dropdownArrow).forEach {
            it.isEnabled = !dataField.settings.readOnly
        }

        labelView.visibility = if (dataField.settings.label != null) {
            View.VISIBLE
        } else {
            View.GONE
        }
        labelView.text = dataField.settings.label ?: ""
        valueTextView.text = dataField.value.formatToHoursAndMinutesString(context)
    }

    class Factory<FieldId : DataFieldId>(
        private val pickerLauncher: DurationPickerFragmentLauncher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, SelectDurationField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.SELECTABLE_DURATION
    ) {
        override fun createViewHolder(
                layoutInflater: LayoutInflater,
                container: ViewGroup,
                attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, SelectDurationField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_select_duration, container, attachToRoot)
            return SelectDurationViewHolder(pickerLauncher, view)
        }
    }
}
