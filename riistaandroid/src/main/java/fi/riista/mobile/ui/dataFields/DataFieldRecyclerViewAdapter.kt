package fi.riista.mobile.ui.dataFields

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DataFields
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderFactory
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver

/**
 * [RecyclerView.Adapter] that can display [DataFields].
 *
 * Usage:
 * - pass ViewHolder type resolver with constructor parameter
 * - register ViewHolder factories using [registerViewHolderFactory]
 */
class DataFieldRecyclerViewAdapter<FieldId : DataFieldId>(
    private val viewHolderTypeResolver: DataFieldViewHolderTypeResolver<FieldId>,
) : RecyclerView.Adapter<DataFieldViewHolder<FieldId, out DataField<FieldId>>>() {

    var dataFields: DataFields<FieldId> = listOf()
        private set

    private val viewHolderFactories =
        mutableMapOf<DataFieldViewHolderType, DataFieldViewHolderFactory<FieldId, *>>()

    init {
        setHasStableIds(true)
    }

    fun setDataFields(dataFields: DataFields<FieldId>) {
        DiffUtil.calculateDiff(DataFieldDiffCallback(this.dataFields, dataFields))
            .dispatchUpdatesTo(this)

        this.dataFields = dataFields
    }

    fun registerViewHolderFactory(factory: DataFieldViewHolderFactory<FieldId, *>) {
        viewHolderFactories[factory.viewHolderType] = factory
    }

    override fun getItemViewType(position: Int): Int {
        val dataField = dataFields[position]
        return viewHolderTypeResolver.resolveViewHolderType(dataField).viewType
    }

    override fun getItemId(position: Int): Long = dataFields[position].id.toInt().toLong()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataFieldViewHolder<FieldId, out DataField<FieldId>> {
        val viewHolderFactory = viewHolderFactories[DataFieldViewHolderType.fromViewType(viewType)]

        @Suppress("FoldInitializerAndIfToElvis")
        if (viewHolderFactory == null) {
            // todo: only crash on debug and return dummy view on production?
            throw NoSuchElementException("Missing ViewHolder factory for viewType $viewType")
        }

        val layoutInflater = LayoutInflater.from(parent.context)
        return viewHolderFactory.createViewHolder(layoutInflater, parent, false)
    }

    override fun getItemCount(): Int = dataFields.size

    override fun onBindViewHolder(
        holder: DataFieldViewHolder<FieldId, out DataField<FieldId>>,
        position: Int
    ) {
        val dataField = dataFields[position]
        if (debug) {
            logger.v {
                val viewHolderType = DataFieldViewHolderType.fromViewType(holder.itemViewType)
                "Binding ${dataField.id} to viewholder of type: $viewHolderType " +
                        "at position $position"
            }
        }
        holder.bindDataField(dataField)
    }

    companion object {
        private const val debug = true
        private val logger by getLogger(DataFieldRecyclerViewAdapter::class)
    }
}

class DataFieldDiffCallback<FieldId : DataFieldId>(
    private val oldDataFields: DataFields<FieldId>,
    private val newDataFields: DataFields<FieldId>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldDataFields.size
    override fun getNewListSize(): Int = newDataFields.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldDataFields[oldItemPosition].id == newDataFields[newItemPosition].id).also {
            if (!debugLogDiffing) {
                return@also
            }

            if (it) {
                logger.v { "items same at position $oldItemPosition - $newItemPosition"}
            } else {
                logger.v { "different items at positions $oldItemPosition - $newItemPosition"}
            }
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldDataFields[oldItemPosition] == newDataFields[newItemPosition]).also {
            if (!debugLogDiffing) {
                return@also
            }

            if (it) {
                logger.v { "item contents same at position $oldItemPosition - $newItemPosition"}
            } else {
                logger.v { "different item contents at positions $oldItemPosition - $newItemPosition"}
            }
        }
    }

    companion object {
        private val debugLogDiffing = false
        private val logger by getLogger(DataFieldDiffCallback::class)
    }
}
