package fi.riista.mobile.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.observation.ObservationStrings;
import fi.riista.mobile.utils.TextValueFilter;
import fi.riista.mobile.utils.UiUtils;

public class ChoiceView extends LinearLayout {

    public interface OnChoiceListener {
        void onChoice(int position, String value);
    }

    public interface OnCheckListener {
        void onCheck(boolean check);
    }

    public interface OnTextListener {
        void onText(String text);
    }

    private AppCompatSpinner mSpinner;
    private AppCompatCheckBox mCheckBox;
    private EditText mEditText;
    private LinearLayout mCheckBoxes;
    private View mBottomSeparator;
    private boolean mFirstChoice;
    private HashMap<String, Integer> mLocalizationAliases = new HashMap<String, Integer>();

    public ChoiceView(Context context, String titleText) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.view_choice, this);

        ((TextView) findViewById(R.id.txt_choice_title)).setText(titleText);
        mSpinner = (AppCompatSpinner) findViewById(R.id.spinner_choice);
        mCheckBox = (AppCompatCheckBox) findViewById(R.id.check_choice);
        mEditText = (EditText) findViewById(R.id.edit_text_choice);
        mCheckBoxes = (LinearLayout) findViewById(R.id.container_check_boxes);
        mBottomSeparator = findViewById(R.id.separator_choice_bottom);
    }

    public void setChoiceEnabled(boolean enabled) {
        mSpinner.setEnabled(enabled);
        mCheckBox.setEnabled(enabled);
        mEditText.setEnabled(enabled);
        for (int i = 0; i < mCheckBoxes.getChildCount(); ++i) {
            mCheckBoxes.getChildAt(i).setEnabled(enabled);
        }
    }

    public void setTopMargin(int dip) {
        UiUtils.setTopMargin(this, dip);
    }

    private String localize(String value) {
        Integer alias = mLocalizationAliases.get(value);
        if (alias != null) {
            //Need to remap the localization string
            value = getContext().getString(alias);
        }

        String localized = ObservationStrings.get(getContext(), value);
        if (localized == null) {
            localized = value;
        }
        return localized;
    }

    public void setChoices(List<String> choices, String selectedChoice, boolean nullable, final OnChoiceListener listener) {
        mFirstChoice = true;

        hideAll();
        mSpinner.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final List<String> choicesCopy = new ArrayList<>(choices);
        if (nullable) {
            choicesCopy.add(0, "");
        }

        int selectedPosition = 0;
        for (String choice : choicesCopy) {
            adapter.add(localize(choice));

            if (choice.equals(selectedChoice)) {
                selectedPosition = adapter.getCount() - 1; // -1 For empty first item
            }
        }
        mSpinner.setAdapter(adapter);

        mSpinner.setSelection(selectedPosition);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mFirstChoice) {
                    mFirstChoice = false;
                    return;
                }

                String selected = choicesCopy.get(position);
                if (selected.equals("")) {
                    selected = null;
                }
                listener.onChoice(position, selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setChecked(boolean checked, final OnCheckListener listener) {
        hideAll();
        mCheckBox.setVisibility(View.VISIBLE);

        mCheckBox.setChecked(checked);
        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onCheck(isChecked);
            }
        });
    }

    public void setEditTextChoice(String text, final OnTextListener listener) {
        hideAll();
        mEditText.setVisibility(View.VISIBLE);

        if (text == null) {
            text = "";
        }
        mEditText.setText(text);
        mEditText.setSelectAllOnFocus(true);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.onText(s.toString());
            }
        });
    }

    public void startMultipleChoices() {
        hideAll();
        mCheckBoxes.setVisibility(View.VISIBLE);
        mCheckBoxes.removeAllViews();
    }

    public void addMultipleChoice(String text, boolean checked, final OnCheckListener listener) {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled}, //disabled
                        new int[]{android.R.attr.state_enabled}, //enabled
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        R.color.checkbox_disabled_color, //disabled
                        R.color.checkbox_enabled_color, //enabled
                        R.color.checkbox_checked_color
                }
        );

        AppCompatCheckBox box = new AppCompatCheckBox(getContext());
        box.setChecked(checked);
        box.setSupportButtonTintList(colorStateList);
        box.setText(localize(text));
        box.setTextColor(getResources().getColor(R.color.text_dark));
        box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onCheck(isChecked);
            }
        });
        mCheckBoxes.addView(box);
    }

    private void hideAll() {
        mSpinner.setVisibility(View.GONE);
        mCheckBox.setVisibility(View.GONE);
        mEditText.setVisibility(View.GONE);
        mCheckBoxes.setVisibility(View.GONE);
    }

    public void setEditTextMode(int inputType, int lines) {
        mEditText.setInputType(inputType);
        mEditText.setLines(lines);
        mEditText.setMaxLines(lines);
    }

    public void setEditTextMaxLength(int maxLength) {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    public void setEditTextMaxValue(float value) {
        mEditText.setFilters(new InputFilter[]{new TextValueFilter(value)});
    }

    public void setShowBottomSeparator(boolean show) {
        mBottomSeparator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setLocalizationAlias(String value, int stringId) {
        mLocalizationAliases.put(value, stringId);
    }
}
