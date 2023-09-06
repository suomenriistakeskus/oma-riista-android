package fi.riista.mobile.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
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
        fun onDateTimeSelected(fieldId: Int, dateTime: DateTime)
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
        setFragmentResult(newDateTime)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val originalDateTime = getDateTimeFromArguments()

        val newDateTime = getDateTimeWithinLimits(originalDateTime.toLocalDate(), hourOfDay, minute)

        setFragmentResult(newDateTime)
    }

    private fun setFragmentResult(dateTime: DateTime) {
        val requestCode = requireNotNull(requireArguments().getString(KEY_REQUEST_CODE))
        val fieldId = requireArguments().getInt(KEY_FIELD_ID)
        val bundle = Bundle().also {
            it.putInt(KEY_DIALOG_RESULT_FIELD, fieldId)
            it.putLong(KEY_DIALOG_RESULT_DATE_TIME_MILLIS, dateTime.millis)
        }
        requireActivity().supportFragmentManager.setFragmentResult(requestCode, bundle)
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

    private fun getDateTimeFromArguments(): DateTime {
        return requireArguments().getSerializable(KEY_SELECTED_DATE_TIME) as DateTime
    }

    companion object {
        const val KEY_DIALOG_RESULT_FIELD = "DateTimePickerFragmentField"
        const val KEY_DIALOG_RESULT_DATE_TIME_MILLIS = "DateTimePickerFragmentDateTime"

        private const val KEY_REQUEST_CODE = "DTPF_requestCode"
        private const val KEY_FIELD_ID = "DTPF_fieldId"
        private const val KEY_PICK_MODE = "DTPF_pickMode"
        private const val KEY_SELECTED_DATE_TIME = "DTPF_selectedDateTime"
        private const val KEY_MIN_DATE_TIME = "DTPF_minDateTime"
        private const val KEY_MAX_DATE_TIME = "DTPF_maxDateTime"

        fun create(
            requestCode: String,
            fieldId: Int,
            selectedDate: LocalDate,
            minDate: LocalDate? = null,
            maxDate: LocalDate? = null
        ): DateTimePickerFragment {
            return create(
                requestCode = requestCode,
                fieldId = fieldId,
                pickMode = PickMode.DATE,
                selectedDateTime = selectedDate.toDateTime(LocalTime(12, 0, 0)),
                minDateTime = minDate?.toDateTime(LocalTime(0, 0, 0)),
                maxDateTime = maxDate?.toDateTime(LocalTime(23, 59, 59))
            )
        }

        fun create(
            requestCode: String,
            fieldId: Int,
            pickMode: PickMode,
            selectedDateTime: DateTime,
            minDateTime: DateTime?,
            maxDateTime: DateTime?
        ): DateTimePickerFragment {
            return DateTimePickerFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_REQUEST_CODE, requestCode)
                    putInt(KEY_FIELD_ID, fieldId)
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

fun <T> T.registerDatePickerFragmentResultListener(
    requestCode: String,
) where T : Fragment, T : DateTimePickerFragment.Listener  {
    requireActivity().supportFragmentManager.setFragmentResultListener(
        requestCode,
        viewLifecycleOwner,
    ) { _, result ->
        val fieldId = result.getInt(DateTimePickerFragment.KEY_DIALOG_RESULT_FIELD)
        val dateTime = result.getLong(DateTimePickerFragment.KEY_DIALOG_RESULT_DATE_TIME_MILLIS)
        onDateTimeSelected(fieldId, DateTime(dateTime))
    }
}

fun <T> T.registerDatePickerFragmentResultListener(
    requestCode: String,
) where T : AppCompatActivity, T : DateTimePickerFragment.Listener {
    supportFragmentManager.setFragmentResultListener(
        requestCode,
        this,
    ) { _, result ->
        val fieldId = result.getInt(DateTimePickerFragment.KEY_DIALOG_RESULT_FIELD)
        val dateTime = result.getLong(DateTimePickerFragment.KEY_DIALOG_RESULT_DATE_TIME_MILLIS)
        onDateTimeSelected(fieldId, DateTime(dateTime))
    }
}

fun <T> T.showDatePickerFragment(
    datePickerFragment: DateTimePickerFragment,
) where T : Fragment {
    requireNotNull(tag) {
        "Fragment needs to have a tag in order to launch a DateTimePickerFragment"
    }
    datePickerFragment.show(requireActivity().supportFragmentManager, datePickerFragment.tag)
}
