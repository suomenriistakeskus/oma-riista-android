package fi.riista.mobile.feature.moreView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.feature.moreView.dataFields.viewHolder.MoreItemClickedListener
import fi.riista.mobile.feature.moreView.dataFields.viewHolder.MoreViewHolder

class MoreRecyclerViewAdapter(
    private val layoutInflater: LayoutInflater,
    private val moreItemClickListener: MoreItemClickedListener,
) : ListAdapter<MoreItem, RecyclerView.ViewHolder>(MoreItemDiffCallback()) {

    fun setItems(model: List<MoreItem>) {
        submitList(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MoreViewHolder.create(
            layoutInflater = layoutInflater,
            parent = parent,
            attachToParent = false,
            listener = moreItemClickListener,
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) as MoreItem
        when (holder) {
            is MoreViewHolder -> holder.bind(item)
        }
    }
}

private class MoreItemDiffCallback : DiffUtil.ItemCallback<MoreItem>() {
    override fun areItemsTheSame(oldItem: MoreItem, newItem: MoreItem): Boolean {
        return oldItem.type == newItem.type
    }

    override fun areContentsTheSame(oldItem: MoreItem, newItem: MoreItem): Boolean {
        return oldItem == newItem
    }
}
