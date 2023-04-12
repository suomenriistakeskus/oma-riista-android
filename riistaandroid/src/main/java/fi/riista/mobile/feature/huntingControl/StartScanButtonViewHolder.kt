package fi.riista.mobile.feature.huntingControl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.ui.dataField.CustomUserInterfaceField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderFactory
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType

class StartScanButtonViewHolder<FieldId : DataFieldId>(
    private val buttonClickedListener: () -> Unit,
    view: View,
) : DataFieldViewHolder<FieldId, CustomUserInterfaceField<FieldId>>(view) {

    private val button: View = view.findViewById(R.id.button_content)

    override fun onBeforeUpdateBoundData(dataField: CustomUserInterfaceField<FieldId>) {
        button.setOnClickListener {
            buttonClickedListener()
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val buttonClickedListener: () -> Unit,
    ) : DataFieldViewHolderFactory<FieldId, CustomUserInterfaceField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.CUSTOM
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, CustomUserInterfaceField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.scan_button, container, attachToRoot)
            return StartScanButtonViewHolder(buttonClickedListener, view)
        }
    }
}
