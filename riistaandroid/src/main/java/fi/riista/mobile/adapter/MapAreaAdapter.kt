package fi.riista.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import fi.riista.mobile.R
import fi.riista.mobile.utils.toVisibility

typealias AreaMapItemClickListener = (MapAreaAdapter.AreaListItem) -> Unit
typealias AreaRemoveListener = (String) -> Unit

class MapAreaAdapter(
    dataSet: Array<AreaListItem>,
    clickListener: AreaMapItemClickListener,
    val areaRemoveListener: AreaRemoveListener
) : RecyclerView.Adapter<MapAreaAdapter.ViewHolder>() {

    data class AreaListItem(
        val areaId: String,
        val titleText: String,
        val nameText: String,
        val idText: String?,
        val manuallyAdded: Boolean,
    )

    private var dataset: Array<AreaListItem>
    private var onClickListener: AreaMapItemClickListener

    init {
        this.dataset = dataSet
        this.onClickListener = clickListener
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val itemTitle: TextView = view.findViewById(R.id.item_title_text)
        val itemName: TextView = view.findViewById(R.id.item_name_text)
        val itemId: TextView = view.findViewById(R.id.item_id_text)
        val removeButton: MaterialButton = view.findViewById(R.id.btn_remove_area)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_area_map_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = this.dataset[position]

        holder.itemTitle.text = item.titleText
        holder.itemName.text = item.nameText
        if (!item.idText.isNullOrEmpty()) {
            holder.itemId.text = item.idText
            holder.itemId.visibility = View.VISIBLE
        } else {
            holder.itemId.visibility = View.GONE
        }
        holder.removeButton.visibility = item.manuallyAdded.toVisibility()

        holder.itemView.setOnClickListener { _ ->
            onClickListener.invoke(item)
        }
        holder.removeButton.setOnClickListener { _ ->
            areaRemoveListener(item.areaId)
        }
    }

    override fun getItemCount(): Int {
        return this.dataset.size
    }

    fun setDataSet(dataSet: Array<AreaListItem>) {
        this.dataset = dataSet
        this.dataset.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nameText })

        notifyDataSetChanged()
    }
}
