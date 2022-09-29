package fi.riista.mobile.feature.groupHunting.huntingDays.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.groupHunting.ui.huntingDays.select.SelectableHuntingDayViewModel

class SelectHuntingDayRecyclerViewAdapter(
    private val layoutInflater: LayoutInflater,
    private val selectionListener: SelectHuntingDayItemViewHolder.SelectionListener
) : RecyclerView.Adapter<SelectHuntingDayItemViewHolder>() {

    init {
        setHasStableIds(true)
    }

    var huntingDays: List<SelectableHuntingDayViewModel> = listOf()
        set(value) {
            DiffUtil.calculateDiff(SelectableHuntingDayDiffCallback(field, value))
                .dispatchUpdatesTo(this)

            field = value
        }

    override fun getItemId(position: Int): Long = huntingDays[position].huntingDayId.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectHuntingDayItemViewHolder {
        return SelectHuntingDayItemViewHolder.create(
                layoutInflater, parent, false, selectionListener)
    }

    override fun onBindViewHolder(holder: SelectHuntingDayItemViewHolder, position: Int) {
        holder.bind(huntingDays[position])
    }

    override fun getItemCount(): Int {
        return huntingDays.size
    }
}

private class SelectableHuntingDayDiffCallback(
    private val oldHuntingDays: List<SelectableHuntingDayViewModel>,
    private val newHuntingDays: List<SelectableHuntingDayViewModel>,
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldHuntingDays.size
    override fun getNewListSize(): Int = newHuntingDays.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldHuntingDays[oldItemPosition].huntingDayId ==
                newHuntingDays[newItemPosition].huntingDayId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // don't compare actual harvests or observations, just the content that is probably
        // visible in the listing
        val oldDay = oldHuntingDays[oldItemPosition]
        val newDay = newHuntingDays[newItemPosition]

        return oldDay.huntingDayId == newDay.huntingDayId &&
                oldDay.selected == newDay.selected
    }
}