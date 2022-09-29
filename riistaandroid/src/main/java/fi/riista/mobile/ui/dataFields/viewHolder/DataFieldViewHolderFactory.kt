package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

/**
 * A base class for factories that are able to create [DataFieldViewHolder] that use
 * [DataField]s of type [DataFieldType]
 */
abstract class DataFieldViewHolderFactory<FieldId : DataFieldId, DataFieldType : DataField<FieldId>>(
    val viewHolderType: DataFieldViewHolderType,
) {

    abstract fun createViewHolder(layoutInflater: LayoutInflater,
                                  container: ViewGroup,
                                  attachToRoot: Boolean): DataFieldViewHolder<FieldId, DataFieldType>
}
