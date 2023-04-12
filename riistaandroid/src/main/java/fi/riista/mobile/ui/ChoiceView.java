package fi.riista.mobile.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.observation.ObservationStrings;
import fi.riista.mobile.utils.MaxValueFilter;
import fi.riista.mobile.utils.UiUtils;

public class ChoiceView<T> extends LinearLayout {

    public interface OnChoiceListener<T> {
        void onChoice(int position, T value);
    }

    public interface OnCheckListener {
        void onCheck(boolean check);
    }

    public interface OnTextListener {
        void onText(String text);
    }

    private final TextView mTitle;
    private final AppCompatSpinner mSpinner;
    private final AppCompatCheckBox mCheckBox;
    private final EditText mEditText;
    private final AppCompatTextView mTextReadonly;
    private final LinearLayout mCheckBoxes;

    private final HashMap<String, Integer> mLocalizationAliases = new HashMap<>();

    private boolean mFirstChoice;

    public ChoiceView(final Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater.from(context).inflate(R.layout.view_choice, this);

        mTitle = findViewById(R.id.txt_choice_title);
        mCheckBox = findViewById(R.id.check_choice);
        mSpinner = findViewById(R.id.spinner_choice);
        mEditText = findViewById(R.id.edit_text_choice);
        mTextReadonly = findViewById(R.id.text_readonly_choice);
        mCheckBoxes = findViewById(R.id.container_check_boxes);
    }

    public ChoiceView(final Context context) {
        this(context, (AttributeSet) null);
    }

    public ChoiceView(final Context context, final String titleText) {
        this(context, (AttributeSet) null);
        mTitle.setText(titleText);
        mCheckBox.setText(titleText);
    }

    public void setTitle(final String title) {
        mTitle.setText(title);
    }

    public void setChoiceEnabled(final boolean enabled) {
        mSpinner.setEnabled(enabled);
        mCheckBox.setEnabled(enabled);
        mEditText.setEnabled(enabled);

        for (int i = 0; i < mCheckBoxes.getChildCount(); ++i) {
            mCheckBoxes.getChildAt(i).setEnabled(enabled);
        }
    }

    public void setTopMargin(final int dip) {
        UiUtils.setTopMargin(this, dip);
    }

    private String localize(final String value) {
        final Context context = getContext();
        final Integer alias = mLocalizationAliases.get(value);

        // Need to remap the localization string
        final String mappedValue = alias != null ? context.getString(alias) : value;

        final String localized = ObservationStrings.get(context, mappedValue);

        return localized != null ? localized : mappedValue;
    }

    public void setChoices(final List<T> choices,
                           final T selectedChoice,
                           final boolean nullable,
                           final OnChoiceListener<T> listener) {
        setChoices(choices, selectedChoice, nullable, true, listener);
    }

    public void setChoices(final List<T> choices,
                           final T selectedChoice,
                           final boolean nullable,
                           final boolean localize,
                           final OnChoiceListener<T> listener) {

        mFirstChoice = true;

        hideAll();
        mSpinner.setVisibility(View.VISIBLE);

        final ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final List<T> choicesCopy = new ArrayList<>(choices);
        if (nullable || selectedChoice == null) {
            choicesCopy.add(0, null);
        }

        int selectedPosition = 0;
        for (final T choice : choicesCopy) {
            if (choice == null) {
                adapter.add("");
            } else {
                if (localize) {
                    adapter.add(localize(choice.toString()));
                } else {
                    adapter.add(choice.toString());
                }
            }

            if ((choice == null && selectedChoice == null) || (choice != null && choice.equals(selectedChoice))) {
                selectedPosition = adapter.getCount() - 1; // -1 for empty first item
            }
        }

        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(selectedPosition);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (mFirstChoice) {
                    mFirstChoice = false;
                } else {
                    final T value = choicesCopy.get(position);
                    final T selected = "".equals(value) ? null : value;

                    listener.onChoice(position, selected);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    public void setChecked(final boolean checked, final OnCheckListener listener) {
        hideAll(true);

        mCheckBox.setVisibility(View.VISIBLE);
        mCheckBox.setChecked(checked);
        mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onCheck(isChecked));
    }

    public void setEditTextChoice(final String text, final OnTextListener listener) {
        hideAll();

        mEditText.setVisibility(View.VISIBLE);
        mEditText.setText(text != null ? text : "");
        mEditText.setSelectAllOnFocus(true);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                listener.onText(s.toString());
            }
        });
    }

    public void setTextReadonlyChoice(final String text) {
        hideAll();

        mTextReadonly.setVisibility(View.VISIBLE);
        mTextReadonly.setText(text != null ? text : "");
    }

    public void startMultipleChoices() {
        hideAll();

        mCheckBoxes.setVisibility(View.VISIBLE);
        mCheckBoxes.removeAllViews();
    }

    public void addMultipleChoice(final String text, final boolean checked, final OnCheckListener listener) {
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final AppCompatCheckBox box = (AppCompatCheckBox) inflater.inflate(R.layout.view_right_checkbox, null);

        box.setChecked(checked);
        box.setText(localize(text));
        box.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onCheck(isChecked));

        mCheckBoxes.addView(box);
    }

    private void hideAll() {
        hideAll(false);
    }

    private void hideAll(final boolean hideTitle) {
        mTitle.setVisibility(hideTitle ? GONE : VISIBLE);
        mSpinner.setVisibility(View.GONE);
        mCheckBox.setVisibility(View.GONE);
        mEditText.setVisibility(View.GONE);
        mCheckBoxes.setVisibility(View.GONE);
        mTextReadonly.setVisibility(View.GONE);
    }

    public void setEditTextMode(final int inputType, final int lines) {
        mEditText.setInputType(inputType);
        mEditText.setLines(lines);
        mEditText.setMaxLines(lines);
    }

    public void setEditTextMaxLength(final int maxLength) {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    public void setEditTextMaxValue(final float value) {
        mEditText.setFilters(new InputFilter[]{new MaxValueFilter(value)});
    }

    public void setLocalizationAlias(final String value, final int stringId) {
        mLocalizationAliases.put(value, stringId);
    }
}
