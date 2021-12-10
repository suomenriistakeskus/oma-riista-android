package fi.riista.mobile.feature.poi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.R

class SeparatorViewHolder(
    val view: View,
) : RecyclerView.ViewHolder(view) {

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): SeparatorViewHolder {
            val view = layoutInflater.inflate(R.layout.item_poi_list_separator, parent, attachToParent)
            return SeparatorViewHolder(view)
        }
    }
}
