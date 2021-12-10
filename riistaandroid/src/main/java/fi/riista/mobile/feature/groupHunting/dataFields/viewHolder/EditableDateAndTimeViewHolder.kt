package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.model.LocalDateTime
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.Label
import fi.riista.mobile.utils.DateTimeUtils

interface DateTimePickerFragmentLauncher<FieldId : DataFieldId> {
    fun pickDateOrTime(fieldId: FieldId,
                       pickMode: DateTimePickerFragment.PickMode,
                       currentDateTime: LocalDateTime,
                       minDateTime: LocalDateTime?,
                       maxDateTime: LocalDateTime?)
}

class EditableDateAndTimeViewHolder<FieldId : DataFieldId>(
    private val pickerLauncher: DateTimePickerFragmentLauncher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, DateAndTimeField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val dateTextView: TextView = view.findViewById(R.id.tv_date)
    private val timeTextView: TextView = view.findViewById(R.id.tv_time)

    init {
        dateTextView.setOnClickListener {
            boundDataField?.let { field ->
                pickerLauncher.pickDateOrTime(
                        fieldId = field.id,
                        pickMode = DateTimePickerFragment.PickMode.DATE,
                        currentDateTime = field.dateAndTime,
                        minDateTime = field.settings.minDateTime,
                        maxDateTime = field.settings.maxDateTime
                )
            }
        }

        timeTextView.setOnClickListener {
            boundDataField?.let { field ->
                pickerLauncher.pickDateOrTime(
                        fieldId = field.id,
                        pickMode = DateTimePickerFragment.PickMode.TIME,
                        currentDateTime = field.dateAndTime,
                        minDateTime = field.settings.minDateTime,
                        maxDateTime = field.settings.maxDateTime
                )
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: DateAndTimeField<FieldId>) {
        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            labelView.visibility = View.VISIBLE
        } else {
            labelView.visibility = View.GONE
        }

        dateTextView.isEnabled = !dataField.settings.readOnlyDate
        timeTextView.isEnabled = !dataField.settings.readOnlyTime

        dataField.dateAndTime.toJodaDateTime().let { dateAndTime ->
            dateTextView.text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(dateAndTime.toLocalDate())
            timeTextView.text = DateTimeUtils.formatTime(dateAndTime)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val pickerLauncher: DateTimePickerFragmentLauncher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, DateAndTimeField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.EDITABLE_DATE_AND_TIME
    ) {
        override fun createViewHolder(
                layoutInflater: LayoutInflater,
                container: ViewGroup,
                attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, DateAndTimeField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_date_and_time, container, attachToRoot)
            return EditableDateAndTimeViewHolder(pickerLauncher, view)
        }
    }
}
