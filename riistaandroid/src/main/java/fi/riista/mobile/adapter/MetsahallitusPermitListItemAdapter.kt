package fi.riista.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.R
import fi.riista.mobile.models.MetsahallitusPermit

class MetsahallitusPermitListItemAdapter(private val permitList: List<MetsahallitusPermit>,
                                         private val languageCode: String) :
        RecyclerView.Adapter<MetsahallitusPermitListItemAdapter.ViewHolder>() {

    private var clickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_mh_permit_card, parent, false)

        return ViewHolder(view, clickListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val permit = permitList[position]

        val permitType: String? = permit.getPermitType(languageCode)

        permitType?.let {
            viewHolder.cardTitle.text = it
        } ?: run {
            viewHolder.cardTitle.setText(R.string.my_details_mh_card_title)
        }
        viewHolder.cardTitle.append(", ${permit.permitIdentifier}")

        viewHolder.areaName.text = permit.getAreaNumberAndName(languageCode)
        viewHolder.permitName.text = permit.getPermitName(languageCode)
        viewHolder.permitPeriod.text = permit.period
    }

    // Invoked by the layout manager
    override fun getItemCount() = permitList.size

    fun setClickListener(itemClickListener: View.OnClickListener?) {
        clickListener = itemClickListener
    }

    class ViewHolder(@NonNull itemView: View, private val clickListener: View.OnClickListener?)
        : RecyclerView.ViewHolder(itemView) {

        val cardTitle: TextView = itemView.findViewById(R.id.mh_permit_card_title)
        val permitName: TextView = itemView.findViewById(R.id.mh_permit_card_name)
        val areaName: TextView = itemView.findViewById(R.id.mh_permit_card_area_name)
        val permitPeriod: TextView = itemView.findViewById(R.id.mh_permit_card_period)

        init {
            itemView.tag = this
            itemView.setOnClickListener(clickListener)
        }
    }
}
