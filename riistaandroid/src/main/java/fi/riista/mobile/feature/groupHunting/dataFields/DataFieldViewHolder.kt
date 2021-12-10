package fi.riista.mobile.feature.groupHunting.dataFields

import android.content.Context
import android.view.View
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.Padding
import fi.riista.mobile.R

/**
 * A base class for [RecyclerView.ViewHolder]s that display the data encapsulated in
 * [DataField]. The concrete classes need to specify the exact type of the [DataField]
 * using [DataFieldType]
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class DataFieldViewHolder<FieldId : DataFieldId, DataFieldType : DataField<FieldId>>(
    view: View,
) : RecyclerView.ViewHolder(view) {

    /**
     * Is the data field currently being bound (binding is in process)?
     *
     * Subclasses should prevent dispatching change events when [isBinding] is true.
     */
    protected var isBinding: Boolean = false
        private set

    protected val context: Context
        get() = itemView.context

    /**
     * The currently bound [DataField]. The value is updated after [onBeforeUpdateBoundData]
     * has been called.
     */
    protected var boundDataField: DataFieldType? = null

    fun bindDataField(dataField: DataField<FieldId>) {
        isBinding = true

        @Suppress("UNCHECKED_CAST")
        val newDataField = (dataField as? DataFieldType)
                ?: throw IllegalStateException("Failed to cast data field (id = ${dataField.id} to correct type")

        updatePaddings(dataField.settings)

        onBeforeUpdateBoundData(newDataField)
        boundDataField = newDataField

        isBinding = false
    }

    private fun updatePaddings(settings: DataField.Settings) {
        itemView.updatePadding(
                top = getPaddingInPixels(settings.paddingTop),
                bottom = getPaddingInPixels(settings.paddingBottom)
        )
    }

    abstract fun onBeforeUpdateBoundData(dataField: DataFieldType)

    private fun getPaddingInPixels(padding: Padding): Int {
        return when (padding) {
            Padding.NONE -> 0
            Padding.SMALL -> context.resources.getDimensionPixelSize(R.dimen.datafield_padding_small)
            Padding.SMALL_MEDIUM -> context.resources.getDimensionPixelSize(R.dimen.datafield_padding_small_medium)
            Padding.MEDIUM -> context.resources.getDimensionPixelSize(R.dimen.datafield_padding_medium)
            Padding.MEDIUM_LARGE -> context.resources.getDimensionPixelSize(R.dimen.datafield_padding_medium_large)
            Padding.LARGE -> context.resources.getDimensionPixelSize(R.dimen.datafield_padding_large)
        }
    }
}