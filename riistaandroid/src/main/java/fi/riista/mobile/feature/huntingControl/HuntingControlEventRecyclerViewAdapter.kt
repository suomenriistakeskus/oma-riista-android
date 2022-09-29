package fi.riista.mobile.feature.huntingControl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.huntingControl.ui.eventSelection.SelectHuntingControlEvent

class HuntingControlEventRecyclerViewAdapter(
    private val layoutInflater: LayoutInflater,
    private val listener: HuntingControlEventViewHolder.SelectHuntingControlEventListener,
) : ListAdapter<SelectHuntingControlEvent, RecyclerView.ViewHolder>(HuntingControlEventDiffCallback()) {

    fun setItems(model: List<SelectHuntingControlEvent>) {
        submitList(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HuntingControlEventViewHolder.create(
            listener = listener,
            layoutInflater = layoutInflater,
            parent = parent,
            attachToParent = false,
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HuntingControlEventViewHolder) {
            holder.bind(getItem(position))
        }
    }
}

private class HuntingControlEventDiffCallback : DiffUtil.ItemCallback<SelectHuntingControlEvent>() {
    override fun areItemsTheSame(oldItem: SelectHuntingControlEvent, newItem: SelectHuntingControlEvent): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SelectHuntingControlEvent, newItem: SelectHuntingControlEvent): Boolean {
        return oldItem == newItem
    }
}
