package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.HuntingDayAndTimeField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.utils.DateTimeUtils

class ReadOnlyHuntingDayAndTimeViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, HuntingDayAndTimeField<FieldId>>(view) {

    private val dateTextView: TextView = view.findViewById(R.id.tv_date)
    private val timeTextView: TextView = view.findViewById(R.id.tv_time)

    init {
        dateTextView.isEnabled = false
        timeTextView.isEnabled = false
    }

    override fun onBeforeUpdateBoundData(dataField: HuntingDayAndTimeField<FieldId>) {
        dataField.dateAndTime.toJodaDateTime().let { dateAndTime ->
            dateTextView.text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(dateAndTime.toLocalDate())
            timeTextView.text = DateTimeUtils.formatTime(dateAndTime)
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, HuntingDayAndTimeField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.READONLY_HUNTING_DAY_AND_TIME
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, HuntingDayAndTimeField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_date_and_time, container, attachToRoot)
            return ReadOnlyHuntingDayAndTimeViewHolder(view)
        }
    }
}