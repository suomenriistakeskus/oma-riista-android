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
        fun onDurationSelected(dialogId: Int, duration: HoursAndMinutes)
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
        // prefer targetFragment over activity
        val listener = targetFragment as? Listener
                ?: activity as? Listener

        getDialogId(arguments)?.let { dialogId ->
            listener?.onDurationSelected(dialogId, duration)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val selectedIndex = picker.value
        possibleDurations.getOrNull(selectedIndex)?.let {
            putSelectedDuration(outState, it)
        }
    }



    companion object {
        private const val KEY_DIALOG_ID = "DurationPickerFragment_dialogId"
        private const val KEY_DIALOG_TITLE = "DurationPickerFragment_title"
        private const val KEY_POSSIBLE_DURATIONS = "DurationPickerFragment_possibleDurations"
        private const val KEY_SELECTED_DURATION = "DurationPickerFragment_selectedDuration"


        fun create(
            dialogId: Int,
            dialogTitle: String,
            possibleDurations: List<HoursAndMinutes>,
            selectedDuration: HoursAndMinutes
        ): DurationPickerFragment {
            require(possibleDurations.isNotEmpty()) {
                "At least one possible duration is required"
            }

            return DurationPickerFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_DIALOG_ID, dialogId)
                    putString(KEY_DIALOG_TITLE, dialogTitle)
                    putString(KEY_POSSIBLE_DURATIONS, possibleDurations.serializeToJson())
                    putSelectedDuration(this, selectedDuration)
                }
            }
        }

        private fun getDialogId(arguments: Bundle?): Int? {
            return arguments?.getInt(KEY_DIALOG_ID)
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

fun <T> T.showDurationPickerFragment(
    durationPickerFragment: DurationPickerFragment,
    requestCode: Int,
) where T : Fragment, T : DurationPickerFragment.Listener {
    requireNotNull(tag) {
        "Fragment needs to have a tag in order to launch a DurationPickerFragment"
    }

    durationPickerFragment.setTargetFragment(this, requestCode)
    durationPickerFragment.show(requireActivity().supportFragmentManager, durationPickerFragment.tag)
}