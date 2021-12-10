package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId

internal class ChoicesAdapter(context: Context): BaseAdapter() {

    private val layoutInflater = LayoutInflater.from(context)

    private var choices: List<StringWithId> = listOf()
    private var allowEmptySelection: Boolean = false

    fun setChoices(choices: List<StringWithId>,
                   allowEmptySelection: Boolean) {
        this.choices = if (allowEmptySelection) {
            listOf(StringWithId("", EMPTY_ITEM_ID))
        } else {
            emptyList()
        } + choices
        this.allowEmptySelection = allowEmptySelection

        notifyDataSetChanged()
    }

    fun findChoice(id: StringId): StringWithId? {
        return choices.find { it.id == id }
            ?.takeIf { it.id != EMPTY_ITEM_ID }
    }

    /**
     * Gets the position of the choice which id equals [choiceId]. Returns null
     * if no such choice is found.
     */
    fun getPosition(choiceId: StringId?): Int? {
        return if (choiceId == null && allowEmptySelection) {
            0 // empty item is the first one
        } else {
            choices.indexOfFirst { it.id == choiceId }
                .takeIf { it >= 0 }
        }
    }

    override fun getCount(): Int {
        return choices.size
    }

    override fun getItem(position: Int): Any {
        return choices[position]
    }

    override fun getItemId(position: Int): Long = getChoice(position).id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = convertView as? TextView
            ?: layoutInflater.inflate(
                android.R.layout.simple_spinner_item,
                parent,
                false
            ) as TextView

        textView.text = getChoice(position).string

        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = convertView as? TextView
            ?: layoutInflater.inflate(
                android.R.layout.simple_spinner_dropdown_item,
                parent,
                false
            ) as TextView

        textView.text = getChoice(position).string

        return textView
    }

    private fun getChoice(position: Int): StringWithId {
        return choices[position]
    }

    companion object {
        private const val EMPTY_ITEM_ID: StringId = -99999L
    }
}
