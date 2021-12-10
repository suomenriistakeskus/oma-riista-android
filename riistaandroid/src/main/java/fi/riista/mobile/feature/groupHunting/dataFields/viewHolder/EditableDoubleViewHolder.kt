package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DoubleEventDispatcher
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.ui.Label
import java.lang.Math.abs

class EditableDoubleViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: DoubleEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, DoubleField<FieldId>>(view) {

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
                    dataFieldEventDispatcher.dispatchDoubleChanged(dataField.id, stringToDouble(editable.toString()))
                }
            }
        })
        editText.setOnEditorActionListener(FocusingOnEditorActionListener())
        editText.inputType = InputType.TYPE_CLASS_NUMBER
    }

    override fun onBeforeUpdateBoundData(dataField: DoubleField<FieldId>) {
        val currentText: String = editText.text.toString()
        val updatedText = doubleToString(dataField.value, dataField.settings.decimals)

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
        if (!equalsDelta(stringToDouble(currentText), dataField.value)) {
            logger.v { "Updating text" }
            editText.setText(updatedText)
        }
    }

    private fun stringToDouble(value: String?): Double? {
        return try {
            value?.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun doubleToString(value: Double?, decimals: Int?): String {
        if (value == null) {
            return ""
        }
        if (decimals != null) {
            return "%.${decimals}f".format(value)
        }
        return value.toString()
    }

    private fun equalsDelta(first: Double?, second: Double?): Boolean {
        if (first != null && second != null) {
            return abs(first - second) < 0.000001
        } else if (first == null && second == null) {
            return true
        }
        return false
    }

    class Factory<FieldId : DataFieldId>(
        private val eventDispatcher: DoubleEventDispatcher<FieldId>,
    ) : DataFieldViewHolderFactory<FieldId, DoubleField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.EDITABLE_DOUBLE
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean,
        ): DataFieldViewHolder<FieldId, DoubleField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_editable_text, container, attachToRoot)
            return EditableDoubleViewHolder(eventDispatcher, view)
        }
    }

    companion object {
        private val logger by getLogger(EditableDoubleViewHolder::class)
    }
}
