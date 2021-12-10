package fi.riista.mobile.feature.groupHunting.huntingDays.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.groupHunting.ui.huntingDays.HuntingDayViewModel

class ListHuntingDaysRecyclerViewAdapter(
    private val layoutInflater: LayoutInflater,
    private val itemListener: HuntingDayItemViewHolder.Listener
) : RecyclerView.Adapter<HuntingDayItemViewHolder>() {

    init {
        setHasStableIds(true)
    }

    var huntingDays: List<HuntingDayViewModel> = listOf()
        set(value) {
            DiffUtil.calculateDiff(HuntingDayDiffCallback(field, value))
                .dispatchUpdatesTo(this)

            field = value
        }

    override fun getItemId(position: Int): Long = huntingDays[position].huntingDay.id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HuntingDayItemViewHolder {
        return HuntingDayItemViewHolder.create(layoutInflater, parent, false, itemListener)
    }

    override fun onBindViewHolder(holder: HuntingDayItemViewHolder, position: Int) {
        holder.bind(huntingDays[position])
    }

    override fun getItemCount(): Int {
        return huntingDays.size
    }
}

private class HuntingDayDiffCallback(
    private val oldHuntingDays: List<HuntingDayViewModel>,
    private val newHuntingDays: List<HuntingDayViewModel>,
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldHuntingDays.size
    override fun getNewListSize(): Int = newHuntingDays.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldHuntingDays[oldItemPosition].huntingDay.id ==
                newHuntingDays[newItemPosition].huntingDay.id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // don't compare actual harvests or observations, just the content that is probably
        // visible in the listing
        val oldDay = oldHuntingDays[oldItemPosition]
        val newDay = newHuntingDays[newItemPosition]

        return oldDay.huntingDay == newDay.huntingDay &&
                oldDay.harvestCount == newDay.harvestCount &&
                oldDay.observationCount == newDay.observationCount &&
                oldDay.hasProposedEntries == newDay.hasProposedEntries
    }
}