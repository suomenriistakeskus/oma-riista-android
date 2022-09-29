package fi.riista.mobile.feature.poi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.poi.ui.PoiListItem
import fi.riista.mobile.R

class PoiItemViewHolder(
    private val listener: Listener,
    private val view: View,
) : RecyclerView.ViewHolder(view) {

    interface Listener {
        fun poiItemSelected(poiItem: PoiListItem.PoiItem)
    }

    private val poiTextView: TextView = view.findViewById(R.id.tv_content)

    fun bind(poiItem: PoiListItem.PoiItem) {
        poiTextView.text = poiItem.text
        view.setOnClickListener {
            listener.poiItemSelected(poiItem)
        }
    }

    companion object {
        fun create(
            listener: Listener,
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): PoiItemViewHolder {
            val view = layoutInflater.inflate(R.layout.item_poi_list_poi_item, parent, attachToParent)
            return PoiItemViewHolder(listener, view)
        }
    }
}
