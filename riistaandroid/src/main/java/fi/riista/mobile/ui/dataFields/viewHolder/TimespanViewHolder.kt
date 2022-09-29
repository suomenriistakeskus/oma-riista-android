package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.model.LocalTime
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.TimespanField
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalTime
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.DateTimeUtils

interface TimePickerFragmentLauncher<FieldId : DataFieldId> {
    fun pickTime(
        fieldId: FieldId,
        currentTime: LocalTime?,
    )
}

class TimespanViewHolder<FieldId : DataFieldId>(
    private val pickerLauncher: TimePickerFragmentLauncher<FieldId>?,
    view: View,
) : DataFieldViewHolder<FieldId, TimespanField<FieldId>>(view) {

    private val unselectedTime = "--:--"
    private val startLabelView: Label = view.findViewById(R.id.v_label_start_time)
    private val endLabelView: Label = view.findViewById(R.id.v_label_end_time)
    private val startTimeTextView: TextView = view.findViewById(R.id.tv_start_time)
    private val endTimeTextView: TextView = view.findViewById(R.id.tv_end_time)

    init {
        startTimeTextView.setOnClickListener {
            boundDataField?.let { field ->
                pickerLauncher?.pickTime(
                    fieldId = field.startFieldId,
                    currentTime = field.startTime,
                )
            }
        }
        endTimeTextView.setOnClickListener {
            boundDataField?.let { field ->
                pickerLauncher?.pickTime(
                    fieldId = field.endFieldId,
                    currentTime = field.endTime,
                )
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: TimespanField<FieldId>) {
        val startLabel = dataField.settings.startLabel
        if (startLabel != null) {
            startLabelView.text = startLabel
            startLabelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            startLabelView.visibility = View.VISIBLE
        } else {
            startLabelView.visibility = View.GONE
        }

        val endLabel = dataField.settings.endLabel
        if (endLabel != null) {
            endLabelView.text = endLabel
            endLabelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            endLabelView.visibility = View.VISIBLE
        } else {
            endLabelView.visibility = View.GONE
        }

        startTimeTextView.isEnabled = !dataField.settings.readOnly
        endTimeTextView.isEnabled = !dataField.settings.readOnly

        val startTimeText = dataField.startTime?.let { time ->
            DateTimeUtils.formatTime(time.toJodaLocalTime())
        } ?:  unselectedTime
        startTimeTextView.text = startTimeText

        val endTimeText = dataField.endTime?.let { time ->
             DateTimeUtils.formatTime(time.toJodaLocalTime())
        } ?: unselectedTime
        endTimeTextView.text = endTimeText
    }

    class Factory<FieldId : DataFieldId>(
        private val pickerLauncher: TimePickerFragmentLauncher<FieldId>?,
    ) : DataFieldViewHolderFactory<FieldId, TimespanField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.TIMESPAN,
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, TimespanField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_timespan, container, attachToRoot)
            return TimespanViewHolder(pickerLauncher, view)
        }
    }
}
