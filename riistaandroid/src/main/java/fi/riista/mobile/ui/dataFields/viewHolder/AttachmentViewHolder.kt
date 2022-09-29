package fi.riista.mobile.ui.dataFields.viewHolder

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import fi.riista.common.ui.dataField.AttachmentField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class AttachmentViewHolder<FieldId : DataFieldId>(
    private val view: View,
    private val attachmentClickedListener: (FieldId, String) -> Unit,
    private val deleteEventClickedListener: ((FieldId, String) -> Unit)?,
) : DataFieldViewHolder<FieldId, AttachmentField<FieldId>>(view) {

    private val imageView: ImageView = view.findViewById(R.id.image)
    private val filenameView: TextView = view.findViewById(R.id.tv_filename)
    private val deleteButton: Button = view.findViewById(R.id.btn_delete_attachment)

    override fun onBeforeUpdateBoundData(dataField: AttachmentField<FieldId>) {
        filenameView.text = dataField.filename
        val bitmapData = dataField.thumbnailAsByteArray
        if (bitmapData != null) {
            val bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.baseline_text_snippet_24)
        }

        if (dataField.settings.readOnly) {
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
            deleteEventClickedListener?.let { listener ->
                deleteButton.setOnClickListener {
                    listener(dataField.id, dataField.filename)
                }
            }
        }

        view.setOnClickListener {
            attachmentClickedListener(dataField.id, dataField.filename)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val attachmentClickedListener: (FieldId, String) -> Unit,
        private val deleteEventClickedListener: ((FieldId, String) -> Unit)? = null,
    ) : DataFieldViewHolderFactory<FieldId, AttachmentField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.ATTACHMENT
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, AttachmentField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_attachment, container, attachToRoot)
            return AttachmentViewHolder(view, attachmentClickedListener, deleteEventClickedListener)
        }
    }
}
