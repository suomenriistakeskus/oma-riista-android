package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.ui.controller.selectString.SelectableStringWithId
import fi.riista.common.model.StringWithId
import fi.riista.mobile.R

class SelectStringWithIdViewHolder(
    view: View,
    private val listener: SelectionListener,
) : RecyclerView.ViewHolder(view) {

    private val textViewValueText = view.findViewById<TextView>(R.id.tv_value_text)
    private var boundValue: StringWithId? = null

    interface SelectionListener {
        fun onValueClicked(value: StringWithId)
    }

    init {
        view.setOnClickListener {
            boundValue?.let {
                listener.onValueClicked(it)
            }
        }
    }

    fun bind(model: SelectableStringWithId) {
        boundValue = model.value
        textViewValueText.text = boundValue?.string ?: ""

        val resources = itemView.context.resources
        if (model.selected) {
            itemView.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
            )
            ResourcesCompat.getColor(resources, R.color.onPrimary, null).let { foregroundColor ->
                textViewValueText.setTextColor(foregroundColor)
            }
        } else {
            itemView.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.activityBackground, null)
            )

            ResourcesCompat.getColor(resources, R.color.colorText, null).let { textColor ->
                textViewValueText.setTextColor(textColor)
            }
        }
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
            listener: SelectionListener
        ): SelectStringWithIdViewHolder {
            val view = layoutInflater.inflate(R.layout.item_selectable_string_with_id, parent, attachToParent)
            return SelectStringWithIdViewHolder(view, listener)
        }
    }
}
