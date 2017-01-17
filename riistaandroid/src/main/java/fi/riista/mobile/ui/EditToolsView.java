package fi.riista.mobile.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;

import java.util.Calendar;

import fi.riista.mobile.R;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;

public class EditToolsView extends LinearLayout {

    private static final String DATETIME_FORMAT = "dd.MM.yyyy  HH:mm";

    public interface OnDateTimeListener {
        void onDateTime(DateTime dateTime);
    }

    public interface OnDeleteListener {
        void onDelete();
    }

    @ViewId(R.id.btn_edit_date)
    private ImageButton mEditDateButton;

    @ViewId(R.id.txt_edit_date)
    private TextView mDateTimeText;

    @ViewId(R.id.btn_edit_start)
    private ImageButton mEditStartButton;

    @ViewId(R.id.btn_edit_delete)
    private ImageButton mEditDeleteButton;

    public EditToolsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_edit_tools, this);

        ViewAnnotations.apply(this);

        int buttonColor = getButtonColor();
        setImageColor(mEditDateButton, buttonColor);
        setImageColor(mEditStartButton, buttonColor);
        setImageColor(mEditDeleteButton, buttonColor);
    }

    private int getButtonColor() {
        return getContext().getResources().getColor(R.color.button_background_color);
    }

    private void setImageColor(ImageView view, int color) {
        view.setColorFilter(color, Mode.MULTIPLY);
    }

    public void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
        button.setColorFilter(enabled ? getButtonColor() : Color.GRAY, Mode.MULTIPLY);
    }

    public ImageButton getDateButton() {
        return mEditDateButton;
    }

    public ImageButton getEditButton() {
        return mEditStartButton;
    }

    public ImageButton getDeleteButton() {
        return mEditDeleteButton;
    }

    public void setDateTimeText(DateTime dateTime) {
        String timeText = "";
        if (dateTime != null) {
            timeText = dateTime.toString(DATETIME_FORMAT);
        }
        mDateTimeText.setText(timeText);
    }

    public void showDateTimeDialog(final DateTime date, final OnDateTimeListener listener) {
        //DatePicker months are 0-based
        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                // Bug in most Android 4.x versions: onDateSet is called for both button click and dialog dismiss
                // With this check only button clicks are handled
                if (view.isShown()) {
                    TimePickerDialog timeDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if (view.isShown()) {
                                DateTime newDate = new DateTime(year, monthOfYear + 1, dayOfMonth, hourOfDay, minute);
                                listener.onDateTime(newDate);
                            }
                        }
                    }, date.hourOfDay().get(), date.minuteOfHour().get(), true);
                    timeDialog.show();
                }
            }
        }, date.year().get(), date.monthOfYear().get() - 1, date.dayOfMonth().get());
        dateDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        dateDialog.show();
    }

    public void showDeleteDialog(final OnDeleteListener listener) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_entry_caption)
                .setMessage(R.string.deleta_entry_text)
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDelete();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

}
