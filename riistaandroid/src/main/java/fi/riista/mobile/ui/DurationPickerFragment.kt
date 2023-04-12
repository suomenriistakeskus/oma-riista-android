package fi.riista.mobile.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.formatToHoursAndMinutesString


class DurationPickerFragment: DialogFragment() {

    /**
     * An interface for the listener. The instantiating activity is expected to implement this.
     */
    interface Listener {
        fun onDurationSelected(fieldId: Int, duration: HoursAndMinutes)
    }

    private lateinit var picker: NumberPicker
    private lateinit var possibleDurations: List<HoursAndMinutes>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_duration_picker, null)

        possibleDurations = getPossibleDurations(arguments)

        val selectedDurationFromSavedState = getSelectedDuration(savedInstanceState)

        picker = view.findViewById(R.id.np_number_picker)
        with (picker) {
            wrapSelectorWheel = false
            minValue = 0
            maxValue = possibleDurations.lastIndex
            displayedValues = possibleDurations.map {
                it.formatToHoursAndMinutesString(context)
            }.toTypedArray()

            // prefer saved state over args
            val selectedIndex = (selectedDurationFromSavedState ?: getSelectedDuration(arguments))
                ?.let {
                    possibleDurations.indexOf(it)
                } ?: 0

            value = selectedIndex
        }

        return AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_AppCompat_Dialog)
            .setTitle(getDialogTitle(arguments))
            .setView(view)
            .setPositiveButton(R.string.select) { _, _ ->
                val selectedIndex = picker.value
                possibleDurations.getOrNull(selectedIndex)?.let {
                    onDurationSelected(it)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun onDurationSelected(duration: HoursAndMinutes) {
        val requestCode = requireNotNull(requireArguments().getString(KEY_REQUEST_CODE))
        val fieldId = requireArguments().getInt(KEY_FIELD_ID)
        val bundle = Bundle().also {
            it.putInt(KEY_DIALOG_RESULT_FIELD, fieldId)
            it.putInt(KEY_DIALOG_RESULT_HOURS, duration.hours)
            it.putInt(KEY_DIALOG_RESULT_MINUTES, duration.minutes)
        }
        requireActivity().supportFragmentManager.setFragmentResult(requestCode, bundle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val selectedIndex = picker.value
        possibleDurations.getOrNull(selectedIndex)?.let {
            putSelectedDuration(outState, it)
        }
    }



    companion object {
        const val KEY_DIALOG_RESULT_FIELD = "DurationPickerFragmentField"
        const val KEY_DIALOG_RESULT_HOURS = "DurationPickerFragmentHours"
        const val KEY_DIALOG_RESULT_MINUTES = "DurationPickerFragmentMinutes"

        private const val KEY_REQUEST_CODE = "DurationPickerFragment_requestCode"
        private const val KEY_FIELD_ID = "DurationPickerFragment_fieldId"
        private const val KEY_DIALOG_TITLE = "DurationPickerFragment_title"
        private const val KEY_POSSIBLE_DURATIONS = "DurationPickerFragment_possibleDurations"
        private const val KEY_SELECTED_DURATION = "DurationPickerFragment_selectedDuration"

        fun create(
            requestCode: String,
            fieldId: Int,
            dialogTitle: String,
            possibleDurations: List<HoursAndMinutes>,
            selectedDuration: HoursAndMinutes
        ): DurationPickerFragment {
            require(possibleDurations.isNotEmpty()) {
                "At least one possible duration is required"
            }

            return DurationPickerFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_REQUEST_CODE, requestCode)
                    putInt(KEY_FIELD_ID, fieldId)
                    putString(KEY_DIALOG_TITLE, dialogTitle)
                    putString(KEY_POSSIBLE_DURATIONS, possibleDurations.serializeToJson())
                    putSelectedDuration(this, selectedDuration)
                }
            }
        }

        private fun getDialogTitle(arguments: Bundle?): String? {
            return arguments?.getString(KEY_DIALOG_TITLE)
        }

        private fun getPossibleDurations(arguments: Bundle?): List<HoursAndMinutes> {
            return arguments?.getString(KEY_POSSIBLE_DURATIONS)?.deserializeFromJson()
                    ?: listOf()
        }

        private fun getSelectedDuration(arguments: Bundle?): HoursAndMinutes? {
            return arguments?.getString(KEY_SELECTED_DURATION)?.deserializeFromJson()
        }

        private fun putSelectedDuration(bundle: Bundle, selectedDuration: HoursAndMinutes) {
            bundle.putString(KEY_SELECTED_DURATION, selectedDuration.serializeToJson())
        }
    }
}

fun <T> T.registerDurationPickerFragmentResultListener(
    requestCode: String,
) where T : Fragment, T : DurationPickerFragment.Listener  {
    requireActivity().supportFragmentManager.setFragmentResultListener(
        requestCode,
        viewLifecycleOwner,
    ) { _, result ->
        val fieldId = result.getInt(DurationPickerFragment.KEY_DIALOG_RESULT_FIELD)
        val hours = result.getInt(DurationPickerFragment.KEY_DIALOG_RESULT_HOURS)
        val minutes = result.getInt(DurationPickerFragment.KEY_DIALOG_RESULT_MINUTES)
        onDurationSelected(fieldId, HoursAndMinutes(hours = hours, minutes = minutes))
    }
}

fun <T> T.showDurationPickerFragment(
    durationPickerFragment: DurationPickerFragment,
) where T : Fragment, T : DurationPickerFragment.Listener {
    requireNotNull(tag) {
        "Fragment needs to have a tag in order to launch a DurationPickerFragment"
    }

    durationPickerFragment.show(requireActivity().supportFragmentManager, durationPickerFragment.tag)
}
