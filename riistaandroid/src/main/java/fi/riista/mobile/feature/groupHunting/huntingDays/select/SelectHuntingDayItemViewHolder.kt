package fi.riista.mobile.feature.groupHunting.huntingDays.select

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.groupHunting.ui.huntingDays.select.SelectableHuntingDayViewModel
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime


class SelectHuntingDayItemViewHolder(
    private val selectionListener: SelectionListener,
    view: View,
): RecyclerView.ViewHolder(view) {

    interface SelectionListener {
        fun onHuntingDaySelected(huntingDayId: GroupHuntingDayId)
    }

    private val startDateTimeTextView = view.findViewById<TextView>(R.id.tv_start_date_and_time)
    private val endDateTimeTextView = view.findViewById<TextView>(R.id.tv_end_date_and_time)
    private val lineBetweenDateTimes = view.findViewById<View>(R.id.view_line)

    private var huntingDayId: GroupHuntingDayId? = null

    init {
        view.setOnClickListener {
            huntingDayId?.let { dayId ->
                selectionListener.onHuntingDaySelected(dayId)
            }
        }
    }

    fun bind(huntingDayViewModel: SelectableHuntingDayViewModel) {
        huntingDayId = huntingDayViewModel.huntingDayId

        startDateTimeTextView.text = dateTimeFormat.print(
                huntingDayViewModel.startDateTime.toJodaDateTime()
        )
        endDateTimeTextView.text = dateTimeFormat.print(
                huntingDayViewModel.endDateTime.toJodaDateTime()
        )

        val resources = itemView.context.resources
        if (huntingDayViewModel.selected) {
            itemView.setBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
            )

            ResourcesCompat.getColor(resources, R.color.onPrimary, null).let { foregroundColor ->
                startDateTimeTextView.setTextColor(foregroundColor)
                lineBetweenDateTimes.setBackgroundColor(foregroundColor)
                endDateTimeTextView.setTextColor(foregroundColor)
            }
        } else {
            itemView.setBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.activityBackground, null)
            )

            ResourcesCompat.getColor(resources, R.color.colorText, null).let { textColor ->
                startDateTimeTextView.setTextColor(textColor)
                endDateTimeTextView.setTextColor(textColor)
            }

            lineBetweenDateTimes.setBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.colorDarkGrey, null)
            )
        }
    }

    companion object {
        private val dateTimeFormat by lazy {
            org.joda.time.format.DateTimeFormat.forPattern("d.M.yyyy  H:mm")
        }

        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
            selectionListener: SelectionListener,
        ): SelectHuntingDayItemViewHolder {
            val view = layoutInflater.inflate(R.layout.item_select_hunting_day, parent, attachToParent)
            return SelectHuntingDayItemViewHolder(selectionListener, view)
        }
    }
}