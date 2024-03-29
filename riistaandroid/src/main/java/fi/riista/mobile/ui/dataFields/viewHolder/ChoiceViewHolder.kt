package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.ui.ClickableSpinner
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

interface ChoiceViewLauncher<FieldId : DataFieldId> {
    fun displayChoicesInSeparateView(
        fieldId: FieldId,
        mode: StringListField.Mode,
        choices: List<StringWithId>,
        selectedChoices: List<StringId>?,
        viewConfiguration: StringListField.ExternalViewConfiguration,
    )
}

class ChoiceViewHolder<FieldId : DataFieldId>(
    private val eventDispatcher: StringWithIdEventDispatcher<FieldId>,
    private val choiceViewLauncher: ChoiceViewLauncher<FieldId>?,
    view: View,
) : DataFieldViewHolder<FieldId, StringListField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val spinner: ClickableSpinner = view.findViewById(R.id.spinner_choice)
    private val spinnerAdapter = ChoicesAdapter(context)

    init {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val field = boundDataField
                // isBinding doesn't really help us here since onItemSelected is called
                // asynchronously after the selection has been made (i.e. field binding is
                // already over). But having it here doesn't hurt either so let's keep it.
                if (isBinding || field == null) {
                    return
                }

                // there may be extra items -> obtain the selected one from adapter
                // as adapter is the one containing all possible items
                val selectedChoice = spinnerAdapter.findChoice(id)

                // selecting (generated) empty item causes selectedChoice to be null and thus
                // it won't be dispatched as an event. This is fine since if empty selection
                // should be possible, the empty value should be part of the field values
                // and it should have a other id --> selectedChoice won't be null any more.
                // --> null check for selectedChoice prevents only generated empty items from
                //     being sent as events.
                val fieldSelected = getSelectedStringId(field)
                if (selectedChoice != null && selectedChoice.id != fieldSelected &&
                    !(field.settings.mode == StringListField.Mode.MULTI && selectedChoice.id == MULTI_MODE_SELECTION_ID)
                ) {
                    eventDispatcher.dispatchStringWithIdChanged(field.id, listOf(selectedChoice))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner.clickListener = {
            val handled = boundDataField?.letWith(choiceViewLauncher) { field, choiceViewLauncher ->
                val viewConfiguration = field.settings.externalViewConfiguration

                if ((field.settings.preferExternalViewForSelection || field.settings.mode == StringListField.Mode.MULTI)
                    && viewConfiguration != null
                ) {
                    choiceViewLauncher.displayChoicesInSeparateView(
                        fieldId = field.id,
                        mode = field.settings.mode,
                        choices = field.detailedValues.takeIf { it.isNotEmpty() } ?: field.values,
                        selectedChoices = field.selected,
                        viewConfiguration = viewConfiguration,
                    )
                    true
                } else {
                    false
                }
            } ?: false

            handled
        }
        spinner.adapter = spinnerAdapter
    }

    override fun onBeforeUpdateBoundData(dataField: StringListField<FieldId>) {
        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            labelView.visibility = View.VISIBLE
        } else {
            labelView.text = ""
            labelView.visibility = View.INVISIBLE
        }

        when (dataField.settings.mode) {
            StringListField.Mode.SINGLE -> {
                spinnerAdapter.setChoices(
                    choices = dataField.values,
                    allowEmptySelection = dataField.selected.isNullOrEmpty()
                )
            }
            StringListField.Mode.MULTI -> {
                spinnerAdapter.setChoices(
                    choices = listOf(
                        StringWithId(
                            id = MULTI_MODE_SELECTION_ID, string = dataField.settings.multiModeChooseText ?: ""
                        )
                    ),
                    allowEmptySelection = false,
                )
            }
        }

        val selectedPosition = getSelectedStringId(dataField)?.let { spinnerAdapter.getPosition(it) }
        if (spinnerAdapter.count >= 0 && selectedPosition != null) {
            spinner.setSelection(selectedPosition)
        }
    }

    private fun getSelectedStringId(dataField: StringListField<FieldId>): StringId? {
        return dataField.selected?.let { selectedIds ->
            if (!selectedIds.isNullOrEmpty()) {
                // Use get(0) as in this mode there should never be more than 1 selection.
                selectedIds[0]
            } else {
                null
            }
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: StringWithIdEventDispatcher<FieldId>,
        /**
         * A launcher that is able to display choices in external view. Will default
         * to spinner if no launcher is given.
         */
        private val choiceViewLauncher: ChoiceViewLauncher<FieldId>? = null,
    ) : DataFieldViewHolderFactory<FieldId, StringListField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.SELECTABLE_STRING
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, StringListField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_string_choiceview, container, attachToRoot)
            return ChoiceViewHolder(eventDispatcher, choiceViewLauncher, view)
        }
    }

    companion object {
        private const val MULTI_MODE_SELECTION_ID = -999L
    }
}
