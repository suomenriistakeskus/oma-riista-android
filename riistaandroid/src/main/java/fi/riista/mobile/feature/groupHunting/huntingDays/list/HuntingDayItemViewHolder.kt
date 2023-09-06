package fi.riista.mobile.feature.groupHunting.huntingDays.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayViewModel
import fi.riista.common.model.LocalDate
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.toVisibility

class HuntingDayItemViewHolder(view: View, listener: Listener): RecyclerView.ViewHolder(view) {
    interface Listener {
        fun onViewHuntingDay(huntingDayId: GroupHuntingDayId)
        fun onEditHuntingDay(huntingDayId: GroupHuntingDayId)
        fun onCreateHuntingDay(preferredHuntingDayDate: LocalDate?)
    }

    private val dateTextView = view.findViewById<TextView>(R.id.tv_date)
    private val proposedEntriesIndicator = view.findViewById<View>(R.id.tv_proposed_entries_indicator)
    private val harvestCountTextView = view.findViewById<TextView>(R.id.tv_harvest_count)
    private val observationCountTextView = view.findViewById<TextView>(R.id.tv_observation_count)
    private val dayActionContainer = view.findViewById<View>(R.id.fl_day_action_container)
    private val editDayImageView = dayActionContainer.findViewById<View>(R.id.iv_edit_day)
    private val createDayImageView = dayActionContainer.findViewById<View>(R.id.iv_create_day)

    enum class HuntingDayAction {
        EDIT,
        CREATE,
    }

    private var huntingDayId: GroupHuntingDayId? = null
    private var huntingDayAction: HuntingDayAction? = null

    init {
        view.setOnClickListener {
            huntingDayId?.let {
                listener.onViewHuntingDay(it)
            }
        }
        dayActionContainer.setOnClickListener {
            huntingDayId?.letWith(huntingDayAction) { dayId, action ->
                when (action) {
                    HuntingDayAction.EDIT -> listener.onEditHuntingDay(dayId)
                    HuntingDayAction.CREATE -> listener.onCreateHuntingDay(dayId.date)
                }
            }
        }
    }


    fun bind(huntingDayViewModel: HuntingDayViewModel) {
        huntingDayId = huntingDayViewModel.huntingDay.id

        dateTextView.text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(
                huntingDayViewModel.huntingDay.startDateTime.date.toJodaLocalDate()
        )
        harvestCountTextView.text = huntingDayViewModel.harvestCount.toString()
        observationCountTextView.text = huntingDayViewModel.observationCount.toString()

        proposedEntriesIndicator.visibility = when (huntingDayViewModel.hasProposedEntries) {
            true -> View.VISIBLE
            false -> View.INVISIBLE // invisible --> proper alignment for other elements
        }

        huntingDayAction = when {
            huntingDayViewModel.canEditHuntingDay -> HuntingDayAction.EDIT
            huntingDayViewModel.canCreateHuntingDay -> HuntingDayAction.CREATE
            else -> null
        }.also { action ->
            when (action) {
                HuntingDayAction.EDIT -> {
                    editDayImageView.visibility = View.VISIBLE
                    createDayImageView.visibility = View.GONE
                }
                HuntingDayAction.CREATE -> {
                    editDayImageView.visibility = View.GONE
                    createDayImageView.visibility = View.VISIBLE
                }
                null -> {
                    // nothing to do
                }
            }

            dayActionContainer.visibility = (action != null).toVisibility()
        }
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
            listener: Listener
        ): HuntingDayItemViewHolder {
            val view = layoutInflater.inflate(R.layout.item_hunting_day, parent, attachToParent)
            return HuntingDayItemViewHolder(view, listener)
        }
    }
}