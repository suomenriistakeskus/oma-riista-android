package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.toVisibility

class CaptionViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, LabelField<FieldId>>(view) {

    private val textView: TextView = view.findViewById(R.id.tv_text)
    private val capsTextView: TextView = view.findViewById(R.id.tv_text_caps)
    private val image: AppCompatImageView = view.findViewById(R.id.image)

    override fun onBeforeUpdateBoundData(dataField: LabelField<FieldId>) {
        textView.text = dataField.text
        capsTextView.text = dataField.text
        textView.visibility = (!dataField.settings.allCaps).toVisibility()
        capsTextView.visibility = dataField.settings.allCaps.toVisibility()
        image.visibility = (dataField.settings.captionIcon != null).toVisibility()
        getImage(dataField.settings.captionIcon)?.let { icon ->
            image.setImageResource(icon)
        }
    }

    private fun getImage(image: LabelField.Icon?): Int? {
        return when (image) {
            LabelField.Icon.VERIFIED -> R.drawable.outline_verified_20
            else -> null
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, LabelField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.LABEL_CAPTION
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LabelField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_caption, container, attachToRoot)
            return CaptionViewHolder(view)
        }
    }
}
