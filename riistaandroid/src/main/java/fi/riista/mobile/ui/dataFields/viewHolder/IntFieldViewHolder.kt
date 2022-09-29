package fi.riista.mobile.ui.dataFields.viewHolder

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.IntEventDispatcher
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import fi.riista.mobile.utils.TextValueFilter

class IntFieldViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: IntEventDispatcher<FieldId>?,
    view: View,
) : DataFieldViewHolder<FieldId, IntField<FieldId>>(view) {

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
                    var value: Int? = null
                    try {
                        value = editable.toString().toInt()
                    } catch (e: NumberFormatException) {
                        // Empty
                    }
                    dataFieldEventDispatcher?.dispatchIntChanged(dataField.id, value)
                }
            }
        })
        editText.setOnEditorActionListener(FocusingOnEditorActionListener())
        editText.inputType = InputType.TYPE_CLASS_NUMBER
    }

    override fun onBeforeUpdateBoundData(dataField: IntField<FieldId>) {
        val currentText: String = editText.text.toString()
        val updatedText = dataField.value?.toString() ?: ""

        val label = dataField.settings.label
        if (label != null) {
            labelView.text = label
            labelView.required = dataField.settings.requirementStatus.isVisiblyRequired()
            labelView.visibility = View.VISIBLE
        } else {
            labelView.text = ""
            labelView.visibility = View.INVISIBLE
        }

        editText.isEnabled = !dataField.settings.readOnly && dataFieldEventDispatcher != null

        val maxInt = dataField.settings.maxValue
        if (maxInt != null) {
            editText.filters = arrayOf<InputFilter>(TextValueFilter(maxInt.toFloat()))
        } else {
            editText.filters = arrayOf<InputFilter>()
        }

        logger.v { "Binding data field. Current '$currentText', updated '$updatedText'" }
        // guard against updating the value when we just dispatched the changes by ourselves
        if (currentText != updatedText) {
            logger.v { "Updating text" }
            editText.setText(updatedText)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: IntEventDispatcher<FieldId>?,
    ) : DataFieldViewHolderFactory<FieldId, IntField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.INT
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, IntField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_editable_text, container, attachToRoot)
            return IntFieldViewHolder(eventDispatcher, view)
        }
    }

    companion object {
        private val logger by getLogger(IntFieldViewHolder::class)
    }
}
