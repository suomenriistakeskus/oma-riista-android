package fi.riista.mobile.feature.specimens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderFactory
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType

interface SpecimenHeaderViewHolderListener<FieldId : DataFieldId> {
    fun onRemoveSpecimenClicked(fieldId: FieldId)
}

class SpecimenHeaderViewHolder<FieldId : DataFieldId>(
    private val listener: SpecimenHeaderViewHolderListener<FieldId>?,
    view: View,
) : DataFieldViewHolder<FieldId, LabelField<FieldId>>(view) {

    private val textView: TextView = view.findViewById(R.id.tv_text)
    private val removeButton: AppCompatImageButton = view.findViewById(R.id.btn_remove)

    init {
        if (listener != null) {
            removeButton.visibility = View.VISIBLE
            removeButton.setOnClickListener {
                boundDataField?.let {
                    listener.onRemoveSpecimenClicked(fieldId = it.id)
                }
            }
        } else {
            removeButton.visibility = View.GONE
        }
    }

    override fun onBeforeUpdateBoundData(dataField: LabelField<FieldId>) {
        textView.text = dataField.text
    }

    class Factory<FieldId : DataFieldId>(
        private val listener: SpecimenHeaderViewHolderListener<FieldId>?,
    ) : DataFieldViewHolderFactory<FieldId, LabelField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.LABEL_CAPTION
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LabelField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_specimen_header, container, attachToRoot)
            return SpecimenHeaderViewHolder(listener, view)
        }
    }
}
