package fi.riista.mobile.feature.myDetails.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.huntingclub.ui.HuntingClubViewModel
import fi.riista.mobile.R

class HuntingClubViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val officialCodeTextView: TextView = view.findViewById(R.id.tv_official_code)
    private val huntingClubTextView: TextView = view.findViewById(R.id.tv_hunting_club)

    fun bind(huntingClub: HuntingClubViewModel.HuntingClub) {
        officialCodeTextView.text = itemView.context.getString(
            R.string.my_details_customer_number,
            huntingClub.officialCode
        )
        huntingClubTextView.text = huntingClub.name
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): HuntingClubViewHolder {
            val view = layoutInflater.inflate(R.layout.item_hunting_club, parent, attachToParent)
            return HuntingClubViewHolder(view)
        }
    }
}
