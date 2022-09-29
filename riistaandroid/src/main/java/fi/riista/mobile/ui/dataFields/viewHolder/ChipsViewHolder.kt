package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.dataField.ChipField
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringWithIdClickEventDispatcher
import fi.riista.mobile.R
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class ChipsViewHolder<FieldId : DataFieldId>(
    view: View,
    private val layoutInflater: LayoutInflater,
    private val eventDispatcher: StringWithIdClickEventDispatcher<FieldId>?,
) : DataFieldViewHolder<FieldId, ChipField<FieldId>>(view) {

    private val chipGroup: ChipGroup = view.findViewById(R.id.chip_group)
    private val labelView: Label = view.findViewById(R.id.tv_label)

    override fun onBeforeUpdateBoundData(dataField: ChipField<FieldId>) {
        chipGroup.removeAllViews()
        dataField.chips.forEach { chipField ->
            val chip = when (dataField.settings.mode) {
                ChipField.Mode.VIEW -> viewChip()
                ChipField.Mode.DELETE -> deleteChip(dataField, chipField)
                ChipField.Mode.TOGGLE -> toggleChip(dataField, chipField)
            }
            if (dataField.settings.label == null) {
                labelView.visibility = View.GONE
            } else {
                labelView.visibility = View.VISIBLE
                labelView.text = dataField.settings.label
                labelView.required = dataField.settings.requirementStatus.isRequired()
                labelView.allCaps = dataField.settings.readOnly
            }
            chip.text = chipField.string
            chipGroup.addView(chip)
        }
    }

    private fun viewChip(): Chip {
        return layoutInflater.inflate(R.layout.item_chip_view, chipGroup, false) as Chip
    }

    private fun deleteChip(dataField: ChipField<FieldId>, chipField: StringWithId): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_delete, chipGroup, false) as Chip
        chip.setOnClickListener {
            eventDispatcher?.dispatchStringWithIdClicked(dataField.id, chipField)
        }
        return chip
    }

    private fun toggleChip(dataField: ChipField<FieldId>, chipField: StringWithId): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_toggle, chipGroup, false) as Chip
        chip.setOnClickListener {
            eventDispatcher?.dispatchStringWithIdClicked(dataField.id, chipField)
        }
        chip.isChecked = dataField.selectedIds?.contains(chipField.id) ?: false
        return chip
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: StringWithIdClickEventDispatcher<FieldId>? = null,
    ) : DataFieldViewHolderFactory<FieldId, ChipField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.CHIPS
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, ChipField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_chips, container, attachToRoot)
            return ChipsViewHolder(view, layoutInflater, eventDispatcher)
        }
    }
}
