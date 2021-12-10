package fi.riista.mobile.feature.groupHunting.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.groupHunting.ui.diary.GroupDiaryEntryViewModel
import fi.riista.mobile.database.SpeciesResolver

class ListGroupDiaryEntriesAdapter(
    private val layoutInflater: LayoutInflater,
    private val speciesResolver: SpeciesResolver,
    private val itemListener: GroupDiaryEntryViewHolder.Listener
) : RecyclerView.Adapter<GroupDiaryEntryViewHolder>() {

    var diaryEntries: List<GroupDiaryEntryViewModel> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupDiaryEntryViewHolder {
        return GroupDiaryEntryViewHolder.create(
                layoutInflater, parent, false, speciesResolver, itemListener
        )
    }

    override fun onBindViewHolder(holder: GroupDiaryEntryViewHolder, position: Int) {
        holder.bind(diaryEntries[position])
    }

    override fun getItemCount(): Int {
        return diaryEntries.size
    }
}