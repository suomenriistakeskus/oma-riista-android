package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.utils.DateTimeUtils

class ReadOnlyDateAndTimeViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, DateAndTimeField<FieldId>>(view) {

    private val dateTextView: TextView = view.findViewById(R.id.tv_date)
    private val timeTextView: TextView = view.findViewById(R.id.tv_time)

    init {
        dateTextView.isEnabled = false
        timeTextView.isEnabled = false
    }

    override fun onBeforeUpdateBoundData(dataField: DateAndTimeField<FieldId>) {
        dataField.dateAndTime.toJodaDateTime().let { dateAndTime ->
            dateTextView.text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(dateAndTime.toLocalDate())
            timeTextView.text = DateTimeUtils.formatTime(dateAndTime)
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, DateAndTimeField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.READONLY_DATE_AND_TIME
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, DateAndTimeField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_date_and_time, container, attachToRoot)
            return ReadOnlyDateAndTimeViewHolder(view)
        }
    }
}