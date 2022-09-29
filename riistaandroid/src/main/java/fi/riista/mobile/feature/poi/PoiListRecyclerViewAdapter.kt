package fi.riista.mobile.feature.poi

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.poi.ui.PoiListItem

/**
 * [RecyclerView.Adapter] that can display a POI.
 */
class PoiListRecyclerViewAdapter(
    private val poiGroupItemListener: PoiGroupItemViewHolder.Listener,
    private val poiItemListener: PoiItemViewHolder.Listener,
    private val layoutInflater: LayoutInflater,
    private val context: Context,
): ListAdapter<PoiListItem, RecyclerView.ViewHolder>(PoiListDiffCallback()) {

    fun setItems(model: List<PoiListItem>) {
        submitList(model)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PoiListItem.PoiGroupItem -> ITEM_VIEW_TYPE_POI_GROUP
            is PoiListItem.PoiItem -> ITEM_VIEW_TYPE_POI
            is PoiListItem.Separator -> ITEM_VIEW_TYPE_SEPARATOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_POI_GROUP -> PoiGroupItemViewHolder.create(
                context = context,
                listener = poiGroupItemListener,
                layoutInflater = layoutInflater,
                parent = parent,
                attachToParent = false,
            )
            ITEM_VIEW_TYPE_POI -> PoiItemViewHolder.create(
                listener = poiItemListener,
                layoutInflater = layoutInflater,
                parent = parent,
                attachToParent = false,
            )
            ITEM_VIEW_TYPE_SEPARATOR -> SeparatorViewHolder.create(
                layoutInflater = layoutInflater,
                parent = parent,
                attachToParent = false,
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PoiGroupItemViewHolder -> {
                val poiGroupItem = getItem(position) as PoiListItem.PoiGroupItem
                holder.bind(poiGroupItem)
            }
            is PoiItemViewHolder -> {
                val poiItem = getItem(position) as PoiListItem.PoiItem
                holder.bind(poiItem)
            }
            is SeparatorViewHolder -> {
                // No need to bind
            }
        }
    }

    companion object {
        private const val ITEM_VIEW_TYPE_POI_GROUP = 0
        private const val ITEM_VIEW_TYPE_POI = 1
        private const val ITEM_VIEW_TYPE_SEPARATOR = 2
    }
}

private class PoiListDiffCallback : DiffUtil.ItemCallback<PoiListItem>() {
    override fun areItemsTheSame(oldItem: PoiListItem, newItem: PoiListItem): Boolean {
        return if (oldItem is PoiListItem.PoiItem && newItem is PoiListItem.PoiItem) {
            oldItem.id == newItem.id && oldItem.groupId == newItem.groupId
        } else if (oldItem is PoiListItem.PoiGroupItem && newItem is PoiListItem.PoiGroupItem) {
            oldItem.id == newItem.id
        } else if (oldItem is PoiListItem.Separator && newItem is PoiListItem.Separator) {
            oldItem.id == newItem.id
        } else {
            false
        }
    }

    override fun areContentsTheSame(oldItem: PoiListItem, newItem: PoiListItem): Boolean {
        return oldItem == newItem
    }
}
