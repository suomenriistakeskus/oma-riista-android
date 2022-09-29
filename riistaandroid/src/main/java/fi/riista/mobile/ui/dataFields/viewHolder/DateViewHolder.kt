package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.model.LocalDate
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DateField
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.DateTimeUtils

interface DatePickerFragmentLauncher<FieldId : DataFieldId> {
    fun pickDate(
        fieldId: FieldId,
        currentDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
    )
}

class DateViewHolder<FieldId : DataFieldId>(
    private val pickerLauncher: DatePickerFragmentLauncher<FieldId>?,
    view: View,
) : DataFieldViewHolder<FieldId, DateField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val dateTextView: TextView = view.findViewById(R.id.tv_date)

    init {
        dateTextView.setOnClickListener {
            boundDataField?.let { field ->
                pickerLauncher?.pickDate(
                    fieldId = field.id,
                    currentDate = field.date,
                    minDate = field.settings.minDate,
                    maxDate = field.settings.maxDate,
                )
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: DateField<FieldId>) {
        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            labelView.visibility = View.VISIBLE
        } else {
            labelView.visibility = View.GONE
        }

        dateTextView.isEnabled = !dataField.settings.readOnly

        dataField.date.toJodaLocalDate().let { date ->
            dateTextView.text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(date)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val pickerLauncher: DatePickerFragmentLauncher<FieldId>?,
    ) : DataFieldViewHolderFactory<FieldId, DateField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.DATE
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, DateField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_date, container, attachToRoot)
            return DateViewHolder(pickerLauncher, view)
        }
    }
}
