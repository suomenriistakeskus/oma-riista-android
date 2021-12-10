package fi.riista.mobile.feature.poi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.poi.ui.PoiListItem
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.localized

class PoiGroupItemViewHolder(
    private val context: Context,
    private val listener: Listener,
    private val view: View,
) : RecyclerView.ViewHolder(view) {

    interface Listener {
        fun poiGroupItemSelected(poiGroupItem: PoiListItem.PoiGroupItem)
    }

    private val iconView: ImageView = view.findViewById(R.id.iv_row_status)
    private val idView: TextView = view.findViewById(R.id.item_number)
    private val typeView: TextView = view.findViewById(R.id.type)

    fun bind(poiGroupItem: PoiListItem.PoiGroupItem) {
        idView.text = poiGroupItem.text
        typeView.text = poiGroupItem.type.localized(context)
        if (poiGroupItem.expanded) {
            iconView.setImageResource(R.drawable.ic_chevron_down)
        } else {
            iconView.setImageResource(R.drawable.ic_chevron_right)
        }
        view.setOnClickListener {
            listener.poiGroupItemSelected(poiGroupItem)
        }
    }

    companion object {
        fun create(
            context: Context,
            listener: Listener,
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): PoiGroupItemViewHolder {
            val view = layoutInflater.inflate(R.layout.item_poi_list_poi_group_item, parent, attachToParent)
            return PoiGroupItemViewHolder(context, listener, view)
        }
    }
}
