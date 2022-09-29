package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.ui.controller.selectString.SelectableStringWithId

class SelectStringWithIdAdapter(
    private val layoutInflater: LayoutInflater,
    private val selectionListener: SelectStringWithIdViewHolder.SelectionListener,
) : RecyclerView.Adapter<SelectStringWithIdViewHolder>() {

    var values: List<SelectableStringWithId> = listOf()
        set(value) {
            DiffUtil.calculateDiff(SelectableGroupMemberDiffCallback(field, value))
                .dispatchUpdatesTo(this)
            field = value
        }

    override fun getItemId(position: Int): Long = values[position].value.id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectStringWithIdViewHolder {
        return SelectStringWithIdViewHolder.create(
            layoutInflater, parent, false, selectionListener
        )
    }

    override fun onBindViewHolder(holder: SelectStringWithIdViewHolder, position: Int) {
        holder.bind(values[position])
    }

    override fun getItemCount(): Int = values.size
}

private class SelectableGroupMemberDiffCallback(
    private val oldValues: List<SelectableStringWithId>,
    private val newValues: List<SelectableStringWithId>,
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldValues.size
    override fun getNewListSize(): Int = newValues.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldValues[oldItemPosition].value.id == newValues[newItemPosition].value.id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldValues[oldItemPosition]
        val new = newValues[newItemPosition]

        return old.value.id == new.value.id && old.selected == new.selected
    }
}
