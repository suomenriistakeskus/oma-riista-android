package fi.riista.mobile.ui.dataFields.viewHolder

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.DoubleEventDispatcher
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.mobile.R
import fi.riista.mobile.ui.Label
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.DecimalValueFilter
import fi.riista.mobile.utils.MaxValueFilter

class EditableDoubleViewHolder<FieldId : DataFieldId>(
    private val dataFieldEventDispatcher: DoubleEventDispatcher<FieldId>,
    view: View,
) : DataFieldViewHolder<FieldId, DoubleField<FieldId>>(view) {

    private val labelView: Label = view.findViewById(R.id.v_label)
    private val editText: AppCompatEditText = view.findViewById(R.id.et_editable_text)
    private val decimalValueFilter = DecimalValueFilter(maxDecimals = null)
    private val maxValueFilter = MaxValueFilter()

    init {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // nop
            }

            override fun afterTextChanged(editable: Editable?) {
                if (isBinding) {
                    // prevent dispatching change events when we're actually binding
                    // the new value
                    return
                } else if (editable == null) {
                    return
                }

                boundDataField?.let { dataField ->
                    dataFieldEventDispatcher.dispatchDoubleChanged(
                        fieldId = dataField.id,
                        value = stringToDouble(editable.toString())
                    )
                }
            }
        })
        editText.setOnEditorActionListener(FocusingOnEditorActionListener())

        editText.filters = arrayOf(decimalValueFilter, maxValueFilter)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.keyListener = DigitsKeyListener.getInstance(decimalValueFilter.allowedDigits)
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

        decimalValueFilter.maxDecimals = dataField.settings.decimals

        val maxValue = dataField.settings.maxValue
        maxValueFilter.enabled = if (maxValue != null) {
            maxValueFilter.maxValue = maxValue.toFloat()
            true
        } else {
            false
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
            // replace in case we're displaying value using commas
            value?.replace(',', '.')?.toDouble()
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
            return kotlin.math.abs(first - second) < 0.000001
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
