package fi.riista.mobile.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.utils.Utils;

/**
 * Generic specimen details view for species with no custom information
 */
public class HarvestSpecimenView extends LinearLayout {

    private final String WEIGHT_INPUT_PATTERN = "[0-9]{0,3}(([\\.,][0-9])||([\\.,]))?";

    private SpecimenDetailsListener mCallback;

    // Specimen reference containing displayed values.
    private Specimen mSpecimen;
    private int mSpeciesId = -1;

    private boolean mAgeRequired;
    private boolean mGenderRequired;
    private boolean mWeightRequired;

    private RadioGroup mGenderSelect;
    private ToggleRadioButton mGenderButtonFemale;
    private ToggleRadioButton mGenderButtonMale;
    private View mGenderRequiredIndicator;

    private RadioGroup mAgeSelect;
    private ToggleRadioButton mAgeButtonAdult;
    private ToggleRadioButton mAgeButtonYoung;
    private View mAgeRequiredIndicator;

    private EditText mWeightEdit;
    private View mWeightRequiredIndicator;

    // If true and no selection for gender or age is set then value of field is 'unknown' instead of null
    private boolean mNotSelectedIsUnknown;

    private Context mContext;

    public HarvestSpecimenView(Context context) {
        super(context);
        init(context);
    }

    public HarvestSpecimenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HarvestSpecimenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_specimen_details, this);
        mContext = context;

        mGenderSelect = (RadioGroup) view.findViewById(R.id.gender_select);
        mGenderButtonFemale = (ToggleRadioButton) view.findViewById(R.id.gender_select_female);
        mGenderButtonMale = (ToggleRadioButton) view.findViewById(R.id.gender_select_male);
        mGenderRequiredIndicator = view.findViewById(R.id.gender_select_required);
        mGenderSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mSpecimen != null) {
                    mSpecimen.setGender(getGender());
                    refreshGenderRequiredIndicator();

                    if (mCallback != null) {
                        mCallback.onSpecimenChanged();
                    }
                }
            }
        });

        mAgeSelect = (RadioGroup) view.findViewById(R.id.age_select);
        mAgeButtonAdult = (ToggleRadioButton) view.findViewById(R.id.age_select_adult);
        mAgeButtonYoung = (ToggleRadioButton) view.findViewById(R.id.age_select_young);
        mAgeRequiredIndicator = view.findViewById(R.id.age_select_required);
        mAgeSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mSpecimen != null) {
                    mSpecimen.setAge(getAge());
                    refreshAgeRequiredIndicator();

                    if (mCallback != null) {
                        mCallback.onSpecimenChanged();
                    }
                }
            }
        });

        mWeightEdit = (EditText) view.findViewById(R.id.weight_input);
        mWeightRequiredIndicator = view.findViewById(R.id.weight_select_required);
        mWeightEdit.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String src = source.toString();
                String dst = dest.toString();
                String res = dst.substring(0, dstart) + src.substring(start, end) + dst.substring(dend);

                if (res.matches(WEIGHT_INPUT_PATTERN)) {
                    return null;
                }
                return "";
            }
        }});
        mWeightEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mWeightEdit.clearFocus();
                    hideKeyboard();

                    return true;
                }
                return false;
            }
        });
        mWeightEdit.addTextChangedListener(new DecimalSeparatorSwitcher(mWeightEdit));
    }

    public void setListener(SpecimenDetailsListener callback) {
        mCallback = callback;
    }

    /**
     * Populate view with display information
     *
     * @param specimen Specimen object containing display information
     */
    public void setupWithSpecimen(Specimen specimen, int speciesId) {
        mSpeciesId = speciesId;

        setupWithSpecimen(specimen, speciesId == SpeciesInformation.SPECIES_GREY_SEAL);

        findViewById(R.id.layout_specimen_weight)
                .setVisibility(mSpeciesId == Utils.MOOSE_ID ||
                        mSpeciesId == Utils.FALLOW_DEER_ID ||
                        mSpeciesId == Utils.WHITE_TAILED_DEER ||
                        mSpeciesId == Utils.WILD_FOREST_DEER ? View.GONE : View.VISIBLE);
    }

    /**
     * Populate view with display information
     *
     * @param specimen         Specimen object containing display information
     * @param allowClearValues Enable clearing selected value by clicking button again
     */
    public void setupWithSpecimen(Specimen specimen, boolean allowClearValues) {
        mSpecimen = specimen;

        setGender(specimen.getGender());
        setAge(specimen.getAge());
        setWeight(specimen.getWeight());

        setSelectionsClearable(allowClearValues);
    }

    /**
     * Required indicator is shown for required fields with no value selected
     *
     * @param genderRequired Is field value required
     * @param ageRequired    Is field value required
     * @param weightRequired Is field value required
     */
    public void setRequiredFields(boolean ageRequired, boolean genderRequired, boolean weightRequired) {
        mAgeRequired = ageRequired;
        mGenderRequired = genderRequired;
        mWeightRequired = weightRequired;

        mAgeRequiredIndicator.setVisibility(mAgeRequired && getAge() == null ? VISIBLE : INVISIBLE);
        mGenderRequiredIndicator.setVisibility(mGenderRequired && getGender() == null ? VISIBLE : INVISIBLE);
        mWeightRequiredIndicator.setVisibility(mWeightRequired && getWeight() == null ? VISIBLE : INVISIBLE);
    }

    protected void refreshGenderRequiredIndicator() {
        mGenderRequiredIndicator.setVisibility(mGenderRequired && mGenderSelect.getCheckedRadioButtonId() < 0 ? VISIBLE : INVISIBLE);
    }

    protected void refreshAgeRequiredIndicator() {
        mAgeRequiredIndicator.setVisibility(mAgeRequired && mAgeSelect.getCheckedRadioButtonId() < 0 ? VISIBLE : INVISIBLE);
    }

    protected void refreshWeightRequiredIndicator() {
        mWeightRequiredIndicator.setVisibility(mWeightRequired && getWeight() == null ? VISIBLE : INVISIBLE);
    }

    /**
     * Enable or disable clearing radio button selections by clicking selected item again.
     *
     * @param allowValueClear Clearing values enabled.
     */
    public void setSelectionsClearable(boolean allowValueClear) {
        mNotSelectedIsUnknown = allowValueClear;

        mGenderButtonFemale.setUncheckable(allowValueClear);
        mGenderButtonMale.setUncheckable(allowValueClear);
        mAgeButtonAdult.setUncheckable(allowValueClear);
        mAgeButtonYoung.setUncheckable(allowValueClear);
    }

    String getGender() {
        int selected = mGenderSelect.getCheckedRadioButtonId();

        if (selected == mGenderButtonFemale.getId()) {
            return Specimen.SpecimenGender.FEMALE.toString();
        } else if (selected == mGenderButtonMale.getId()) {
            return Specimen.SpecimenGender.MALE.toString();
        } else if (selected == -1 && mNotSelectedIsUnknown) {
            return Specimen.SpecimenGender.UNKNOWN.toString();
        } else {
            return null;
        }
    }

    void setGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            mGenderSelect.clearCheck();
        } else if (gender.equals(Specimen.SpecimenGender.FEMALE.toString())) {
            mGenderSelect.check(mGenderButtonFemale.getId());
        } else if (gender.equals(Specimen.SpecimenGender.MALE.toString())) {
            mGenderSelect.check(mGenderButtonMale.getId());
        } else {
            mGenderSelect.clearCheck();
        }

        refreshGenderRequiredIndicator();
    }

    String getAge() {
        int selected = mAgeSelect.getCheckedRadioButtonId();

        if (selected == mAgeButtonAdult.getId()) {
            return Specimen.SpecimenAge.ADULT.toString();
        } else if (selected == mAgeButtonYoung.getId()) {
            return Specimen.SpecimenAge.YOUNG.toString();
        } else if (selected == -1 && mNotSelectedIsUnknown) {
            return Specimen.SpecimenAge.UNKNOWN.toString();
        } else {
            return null;
        }
    }

    void setAge(String age) {
        if (age == null || age.isEmpty()) {
            mAgeSelect.clearCheck();
        } else if (age.equals(Specimen.SpecimenAge.ADULT.toString())) {
            mAgeSelect.check(mAgeButtonAdult.getId());
        } else if (age.equals(Specimen.SpecimenAge.YOUNG.toString())) {
            mAgeSelect.check(mAgeButtonYoung.getId());
        } else {
            mAgeSelect.clearCheck();
        }

        refreshAgeRequiredIndicator();
    }

    Double getWeight() {
        try {
            String input = mWeightEdit.getText().toString();
            input = input.replace(",", ".");

            return Double.valueOf(input);
        } catch (NumberFormatException e) {
            Log.d(getClass().getSimpleName(), "Failed to parse weight input");
        }

        return null;
    }

    void setWeight(Double weight) {
        if (weight != null && weight >= 0) {
            mWeightEdit.setText(String.valueOf(weight));
        } else {
            mWeightEdit.setText(null);
        }

        refreshWeightRequiredIndicator();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mGenderSelect.setEnabled(enabled);
        mGenderButtonFemale.setEnabled(enabled);
        mGenderButtonMale.setEnabled(enabled);

        mAgeSelect.setEnabled(enabled);
        mAgeButtonAdult.setEnabled(enabled);
        mAgeButtonYoung.setEnabled(enabled);

        mWeightEdit.setEnabled(enabled);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class DecimalSeparatorSwitcher implements TextWatcher {

        private EditText mTextInput;

        public DecimalSeparatorSwitcher(EditText textInput) {
            mTextInput = textInput;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int selection = mTextInput.getSelectionEnd();

            if (s != null && s.toString().contains(".")) {
                mTextInput.setText(s.toString().replace('.', ','));
                mTextInput.setSelection(selection);
            }

            if (mSpecimen != null &&
                    mSpeciesId != Utils.MOOSE_ID &&
                    mSpeciesId != Utils.FALLOW_DEER_ID &&
                    mSpeciesId != Utils.WHITE_TAILED_DEER &&
                    mSpeciesId != Utils.WILD_FOREST_DEER) {
                mSpecimen.setWeight(getWeight());
                refreshWeightRequiredIndicator();

                if (mCallback != null) {
                    mCallback.onSpecimenChanged();
                }
            }
        }
    }

    public interface SpecimenDetailsListener {
        void onSpecimenChanged();
    }
}
