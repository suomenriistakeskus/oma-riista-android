package fi.riista.mobile.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

/**
 * A dialog fragment that can be used for either picking a date or time.
 */
class DateTimePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    enum class PickMode {
        DATE,
        TIME,
    }

    /**
     * An interface for the listener. The instantiating activity is expected to implement this.
     */
    interface Listener {
        fun onDateTimeSelected(dialogId: Int, dateTime: DateTime)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val pickMode = PickMode.valueOf(args.getString(KEY_PICK_MODE, "DATE"))
        val selectedDateTime = getDateTimeFromArguments()
        val minDateTime = args.getSerializable(KEY_MIN_DATE_TIME) as? DateTime
        val maxDateTime = args.getSerializable(KEY_MAX_DATE_TIME) as? DateTime

        return when (pickMode) {
            PickMode.DATE -> createDatePickerDialog(selectedDateTime, minDateTime, maxDateTime)
            PickMode.TIME -> createTimePickerDialog(selectedDateTime)
        }

    }

    private fun createDatePickerDialog(
        selectedDateTime: DateTime,
        minDateTime: DateTime?,
        maxDateTime: DateTime?
    ): Dialog {
        return DatePickerDialog(
                requireActivity(),
                this,
                selectedDateTime.year,
                selectedDateTime.monthOfYear - 1,
                selectedDateTime.dayOfMonth
        ).also { datePickerDialog ->
            minDateTime?.let {
                datePickerDialog.datePicker.minDate = it.toDate().time
            }
            maxDateTime?.let {
                datePickerDialog.datePicker.maxDate = it.toDate().time
            }
        }
    }

    private fun createTimePickerDialog(selectedDateTime: DateTime): Dialog {
        return TimePickerDialog(
                requireActivity(),
                this,
                selectedDateTime.hourOfDay,
                selectedDateTime.minuteOfHour,
                true
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val originalDateTime = getDateTimeFromArguments()
        val newDateTime = DateTime(
                year, month + 1, dayOfMonth,
                originalDateTime.hourOfDay, originalDateTime.minuteOfHour, originalDateTime.secondOfMinute
        )

        getListener()?.onDateTimeSelected(
                dialogId = requireArguments().getInt(KEY_DIALOG_ID),
                dateTime = newDateTime
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val originalDateTime = getDateTimeFromArguments()

        val newDateTime = getDateTimeWithinLimits(originalDateTime.toLocalDate(), hourOfDay, minute)

        getListener()?.onDateTimeSelected(
                dialogId = requireArguments().getInt(KEY_DIALOG_ID),
                dateTime = newDateTime
        )
    }

    private fun getDateTimeWithinLimits(date: LocalDate, hourOfDay: Int, minute: Int): DateTime {
        val minDateTime = requireArguments().getSerializable(KEY_MIN_DATE_TIME) as? DateTime
        val maxDateTime = requireArguments().getSerializable(KEY_MAX_DATE_TIME) as? DateTime

        val isMinDate = date == minDateTime?.toLocalDate()
        val isMaxDate = date == maxDateTime?.toLocalDate()

        val minutesFromDayStart = minutesFromDayStart(hourOfDay, minute)

        var hourOfDayWithinLimits = hourOfDay
        var minuteWithinLimits = minute
        if (isMinDate && minDateTime != null) {
            if (minutesFromDayStart < minutesFromDayStart(minDateTime.hourOfDay, minDateTime.minuteOfHour)) {
                hourOfDayWithinLimits = minDateTime.hourOfDay
                minuteWithinLimits = minDateTime.minuteOfHour
            }
        } else if (isMaxDate && maxDateTime != null) {
            if (minutesFromDayStart > minutesFromDayStart(maxDateTime.hourOfDay, maxDateTime.minuteOfHour)) {
                hourOfDayWithinLimits = maxDateTime.hourOfDay
                minuteWithinLimits = maxDateTime.minuteOfHour
            }
        }

        return DateTime(
                date.year, date.monthOfYear, date.dayOfMonth,
                hourOfDayWithinLimits, minuteWithinLimits
        )
    }

    private fun minutesFromDayStart(hourOfDay: Int, minute: Int): Int {
        return hourOfDay * 60 + minute
    }

    private fun getListener(): Listener? {
        // prefer targetFragment over activity
        return targetFragment as? Listener
                ?: activity as? Listener
    }

    private fun getDateTimeFromArguments(): DateTime {
        return requireArguments().getSerializable(KEY_SELECTED_DATE_TIME) as DateTime
    }

    companion object {
        private const val KEY_DIALOG_ID = "DTPF_dialogId"
        private const val KEY_PICK_MODE = "DTPF_pickMode"
        private const val KEY_SELECTED_DATE_TIME = "DTPF_selectedDateTime"
        private const val KEY_MIN_DATE_TIME = "DTPF_minDateTime"
        private const val KEY_MAX_DATE_TIME = "DTPF_maxDateTime"

        fun create(dialogId: Int,
                   selectedDate: org.joda.time.LocalDate,
                   minDate: org.joda.time.LocalDate? = null,
                   maxDate: org.joda.time.LocalDate? = null
        ): DateTimePickerFragment {
            return create(
                    dialogId = dialogId,
                    pickMode = PickMode.DATE,
                    selectedDateTime = selectedDate.toDateTime(LocalTime(12, 0, 0)),
                    minDateTime = minDate?.toDateTime(LocalTime(0, 0, 0)),
                    maxDateTime = maxDate?.toDateTime(LocalTime(23, 59, 59))
            )
        }

        fun create(dialogId: Int,
                   pickMode: PickMode,
                   selectedDateTime: DateTime,
                   minDateTime: DateTime?,
                   maxDateTime: DateTime?
        ): DateTimePickerFragment {
            return DateTimePickerFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_DIALOG_ID, dialogId)
                    putString(KEY_PICK_MODE, pickMode.toString())
                    putSerializable(KEY_SELECTED_DATE_TIME, selectedDateTime)
                    minDateTime?.let {
                        putSerializable(KEY_MIN_DATE_TIME, it)
                    }
                    maxDateTime?.let {
                        putSerializable(KEY_MAX_DATE_TIME, it)
                    }
                }
            }
        }
    }
}


fun <T> T.showDatePickerFragment(
    datePickerFragment: DateTimePickerFragment,
    requestCode: Int,
) where T : Fragment, T : DateTimePickerFragment.Listener {
    requireNotNull(tag) {
        "Fragment needs to have a tag in order to launch a DurationPickerFragment"
    }

    datePickerFragment.setTargetFragment(this, requestCode)
    datePickerFragment.show(requireActivity().supportFragmentManager, datePickerFragment.tag)
}