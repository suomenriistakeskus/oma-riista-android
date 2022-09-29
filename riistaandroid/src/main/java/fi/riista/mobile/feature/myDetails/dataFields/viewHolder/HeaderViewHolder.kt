package fi.riista.mobile.feature.myDetails.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.huntingclub.ui.HuntingClubViewModel
import fi.riista.mobile.R

class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val headerTextView: TextView = view.findViewById(R.id.tv_header)

    fun bind(header: HuntingClubViewModel.Header) {
        headerTextView.text = header.text
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): HeaderViewHolder {
            val view = layoutInflater.inflate(R.layout.item_hunting_club_header, parent, attachToParent)
            return HeaderViewHolder(view)
        }
    }
}
