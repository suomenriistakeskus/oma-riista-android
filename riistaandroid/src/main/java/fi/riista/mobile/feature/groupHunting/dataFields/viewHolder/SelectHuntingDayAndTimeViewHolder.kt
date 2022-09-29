package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.HuntingDayAndTimeField
import fi.riista.common.ui.dataField.LocalTimeEventDispatcher
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderFactory
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.EditUtils
import org.joda.time.DateTime


interface SelectHuntingDayLauncher<FieldId : DataFieldId> {
    fun launchHuntingDaySelection(fieldId: FieldId,
                                  selectedHuntingDayId: GroupHuntingDayId?,
                                  preferredHuntingDayDate: LocalDate?,)
}

class SelectHuntingDayAndTimeViewHolder<FieldId : DataFieldId>(
    private val eventDispatcher: LocalTimeEventDispatcher<FieldId>,
    private val selectHuntingDayLauncher: SelectHuntingDayLauncher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, HuntingDayAndTimeField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val dateTextView: TextView = view.findViewById(R.id.tv_date)
    private val timeTextView: TextView = view.findViewById(R.id.tv_time)

    init {
        dateTextView.setOnClickListener {
            boundDataField?.let { field ->
                selectHuntingDayLauncher.launchHuntingDaySelection(
                        fieldId = field.id,
                        selectedHuntingDayId = field.huntingDayId,
                        preferredHuntingDayDate = field.dateAndTime.date
                )
            }
        }

        timeTextView.setOnClickListener {
            boundDataField?.dateAndTime?.toJodaDateTime()?.let {
                EditUtils.showTimeDialog(view.context, it, object : EditUtils.OnDateTimeListener {
                    override fun onDateTime(dateTime: DateTime) {
                        val localDateTime = LocalDateTime.fromJodaDateTime(dateTime)
                        boundDataField?.let { field ->
                            eventDispatcher.dispatchLocalTimeChanged(field.id, localDateTime.time)
                        }
                    }
                })
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: HuntingDayAndTimeField<FieldId>) {
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

        dataField.dateAndTime.toJodaDateTime().also { dateAndTime ->
            dateTextView.text = if (dataField.huntingDayId != null) {
                DateTimeUtils.formatLocalDateUsingShortFinnishFormat(dateAndTime.toLocalDate())
            } else {
                context.getString(R.string.select)
            }
            timeTextView.text = DateTimeUtils.formatTime(dateAndTime)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: LocalTimeEventDispatcher<FieldId>,
        private val selectHuntingDayLauncher: SelectHuntingDayLauncher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, HuntingDayAndTimeField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.SELECT_HUNTING_DAY_AND_TIME
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, HuntingDayAndTimeField<FieldId>> {
            // re-use same layout as in date and time selection but just don't use typical
            // date selection. Instead select one of already existing hunting days.
            val view = layoutInflater.inflate(R.layout.item_date_and_time, container, attachToRoot)
            return SelectHuntingDayAndTimeViewHolder(eventDispatcher, selectHuntingDayLauncher, view)
        }
    }
}
