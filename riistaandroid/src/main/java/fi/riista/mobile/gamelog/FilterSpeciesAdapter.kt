package fi.riista.mobile.gamelog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.models.Species

class FilterSpeciesAdapter(private var context: Context?, private var listener: SpeciesSelectionListener) : RecyclerView.Adapter<FilterSpeciesAdapter.SpeciesViewHolder>() {

    private var dataSet = ArrayList<SpeciesItem>()

    class SpeciesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val image: AppCompatImageView = view.findViewById(R.id.filter_species_image)
        val title: TextView = view.findViewById(R.id.filter_species_name)
        val radioBtn: RadioButton = view.findViewById(R.id.filter_item_selected)

        fun bind(adapter: FilterSpeciesAdapter, item: SpeciesItem) {
            view.setOnClickListener {
                if (radioBtn.isChecked) {
                    item.selected = false
                    radioBtn.isChecked = false
                } else {
                    item.selected = true
                    radioBtn.isChecked = true
                }
                adapter.listener.onSelectionsChanged(adapter.getSelectedIds())
            }
        }
    }

    class SpeciesItem(val species: Species) {
        var selected: Boolean = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeciesViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_filter_species_item, parent, false)

        return SpeciesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpeciesViewHolder, position: Int) {
        val item = this.dataSet[position]

        holder.image.setImageDrawable(SpeciesInformation.getSpeciesImage(context, item.species.mId))
        holder.title.text = item.species.mName
        holder.radioBtn.isChecked = item.selected

        holder.bind(this, item)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setDataSet(@NonNull list: ArrayList<Species>) {
        dataSet.clear()

        for (item in list) {
            dataSet.add(SpeciesItem(item))
        }

        notifyDataSetChanged()
    }

    fun clearAll() {
        for (item in dataSet) {
            item.selected = false
        }
        notifyDataSetChanged()
        listener.onSelectionsChanged(getSelectedIds())
    }

    fun selectAll() {
        for (item in dataSet) {
            item.selected = true
        }
        notifyDataSetChanged()
        listener.onSelectionsChanged(getSelectedIds())
    }

    fun getSelectedIds(): List<Int> {
        val result = ArrayList<Int>()
        for (item in dataSet) {
            if (item.selected) {
                result.add(item.species.mId)
            }
        }
        return result
    }

    companion object {
        interface SpeciesSelectionListener {
            fun onSelectionsChanged(selectedCodes: List<Int>)
        }
    }
}
