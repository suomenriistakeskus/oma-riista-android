package fi.riista.mobile.feature.groupHunting

import androidx.fragment.app.Fragment
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.pages.PageFragment

/**
 * A [Fragment] that will contain a list of [DataField]
 */
open class DataFieldFragment<FieldId : DataFieldId> : Fragment() {

    protected fun createAdapter(viewHolderTypeResolver: DataFieldViewHolderTypeResolver<FieldId>):
            DataFieldRecyclerViewAdapter<FieldId> {
        return DataFieldRecyclerViewAdapter(viewHolderTypeResolver)
    }
}

/**
 * A [PageFragment] that will contain a list of [DataField]
 */
open class DataFieldPageFragment<FieldId : DataFieldId> : PageFragment() {

    protected fun createAdapter(viewHolderTypeResolver: DataFieldViewHolderTypeResolver<FieldId>):
            DataFieldRecyclerViewAdapter<FieldId> {
        return DataFieldRecyclerViewAdapter(viewHolderTypeResolver)
    }
}
