package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringEventDispatcher
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label

class EditableTextViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: StringEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, StringField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val editText: AppCompatEditText = view.findViewById(R.id.et_editable_text)

    init {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // nop
            }

            override fun afterTextChanged(s: Editable?) {
                if (isBinding) {
                    // prevent dispatching change events when we're actually binding
                    // the new value
                    return
                }

                boundDataField?.letWith(s) { dataField, editable ->
                    dataFieldEventDispatcher.dispatchStringChanged(dataField.id, editable.toString())
                }
            }
        })
        editText.setOnEditorActionListener(FocusingOnEditorActionListener())
    }

    override fun onBeforeUpdateBoundData(dataField: StringField<FieldId>) {
        val currentText: String = editText.text.toString()
        val updatedText = dataField.value

        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            labelView.visibility = View.VISIBLE
        } else {
            labelView.text = ""
            labelView.visibility = View.INVISIBLE
        }

        logger.v { "Binding data field. Current '$currentText', updated '$updatedText'" }
        // guard against updating the value when we just dispatched the changes by ourselves
        if (currentText != updatedText) {
            logger.v { "Updating text" }
            editText.setText(updatedText)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: StringEventDispatcher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, StringField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.EDITABLE_TEXT
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, StringField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_editable_text, container, attachToRoot)
            return EditableTextViewHolder(eventDispatcher, view)
        }
    }

    companion object {
        private val logger by getLogger(EditableTextViewHolder::class)
    }
}
