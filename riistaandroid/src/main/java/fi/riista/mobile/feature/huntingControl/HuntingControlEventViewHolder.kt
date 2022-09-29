package fi.riista.mobile.feature.huntingControl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.huntingControl.model.HuntingControlEventId
import fi.riista.common.domain.huntingControl.ui.eventSelection.SelectHuntingControlEvent
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.utils.DATE_FORMAT_FINNISH_SHORT
import fi.riista.mobile.utils.toVisibility

class HuntingControlEventViewHolder(
    private val view: View,
    private val listener: SelectHuntingControlEventListener,
) : RecyclerView.ViewHolder(view) {

    interface SelectHuntingControlEventListener {
        fun huntingControlEventSelected(eventId: HuntingControlEventId)
    }

    private val dateTimeText: TextView = view.findViewById(R.id.tv_datetime)
    private val titleText: TextView = view.findViewById(R.id.tv_title)
    private val modifiedImage: AppCompatImageView = view.findViewById(R.id.iv_modified)

    fun bind(event: SelectHuntingControlEvent) {
        dateTimeText.text =  dateFormat.print(event.date.toJodaLocalDate())
        titleText.text = event.title

        view.setOnClickListener {
            listener.huntingControlEventSelected(event.id)
        }
        modifiedImage.visibility = event.modified.toVisibility()
    }

    companion object {
        private val dateFormat by lazy {
            org.joda.time.format.DateTimeFormat.forPattern(DATE_FORMAT_FINNISH_SHORT)
        }

        fun create(
            listener: SelectHuntingControlEventListener,
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): HuntingControlEventViewHolder {
            val view = layoutInflater.inflate(R.layout.item_hunting_control_event, parent, attachToParent)
            return HuntingControlEventViewHolder(view, listener)
        }
    }
}
