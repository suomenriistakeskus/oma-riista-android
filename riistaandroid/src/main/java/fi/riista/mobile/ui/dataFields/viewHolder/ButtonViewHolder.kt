package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import fi.riista.common.ui.dataField.ButtonField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class ButtonViewHolder<FieldId : DataFieldId>(
    private val buttonClickedListener: (FieldId) -> Unit,
    view: View
) : DataFieldViewHolder<FieldId, ButtonField<FieldId>>(view) {

    private val primaryButton: AppCompatButton = view.findViewById(R.id.button_primary)

    override fun onBeforeUpdateBoundData(dataField: ButtonField<FieldId>) {
        primaryButton.text = dataField.text

        primaryButton.setOnClickListener {
            buttonClickedListener(dataField.id)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val buttonClickedListener: (FieldId) -> Unit,
    ) : DataFieldViewHolderFactory<FieldId, ButtonField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.BUTTON
    ) {
        override fun createViewHolder(

            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, ButtonField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_button, container, attachToRoot)
            return ButtonViewHolder(buttonClickedListener, view)
        }
    }
}
