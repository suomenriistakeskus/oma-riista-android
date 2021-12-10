package fi.riista.mobile.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.models.specimen.GameAge;
import fi.riista.mobile.models.specimen.Gender;

public class HarvestSpecimenView extends LinearLayout {

    private final String WEIGHT_INPUT_PATTERN = "[0-9]{0,3}(([.,][0-9])|([.,]))?";

    private SpecimenDetailsListener mCallback;

    // HarvestSpecimen reference containing displayed values.
    private HarvestSpecimen mSpecimen;
    private int mSpeciesId = -1;

    private boolean mAgeRequired;
    private boolean mGenderRequired;
    private boolean mWeightRequired;

    private RadioGroup mGenderSelect;
    private RadioButtonToggle mGenderButtonFemale;
    private RadioButtonToggle mGenderButtonMale;
    private View mGenderRequiredIndicator;

    private RadioGroup mAgeSelect;
    private RadioButtonToggle mAgeButtonAdult;
    private RadioButtonToggle mAgeButtonYoung;
    private View mAgeRequiredIndicator;

    private TextInputLayout mWeightLayout;
    private TextInputEditText mWeightEdit;

    // If true and no selection for gender or age is set then value of field is 'unknown' instead of null
    private boolean mNotSelectedIsUnknown;

    private Context mContext;

    public HarvestSpecimenView(final Context context) {
        super(context);
        init(context);
    }

    public HarvestSpecimenView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HarvestSpecimenView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.view_specimen_details, this);
        mContext = context;

        mGenderSelect = view.findViewById(R.id.gender_select);
        mGenderButtonFemale = view.findViewById(R.id.gender_select_female);
        mGenderButtonMale = view.findViewById(R.id.gender_select_male);
        mGenderRequiredIndicator = view.findViewById(R.id.gender_select_required);
        mGenderSelect.setOnCheckedChangeListener((group, checkedId) -> {
            if (mSpecimen != null) {
                final Gender selectedGender = getSelectedGender();
                mSpecimen.setGender(selectedGender != null ? selectedGender.name() : null);
                refreshGenderRequiredIndicator();

                if (mCallback != null) {
                    mCallback.onSpecimenChanged();
                }
            }
        });

        mAgeSelect = view.findViewById(R.id.age_select);
        mAgeButtonAdult = view.findViewById(R.id.age_select_adult);
        mAgeButtonYoung = view.findViewById(R.id.age_select_young);
        mAgeRequiredIndicator = view.findViewById(R.id.age_select_required);
        mAgeSelect.setOnCheckedChangeListener((group, checkedId) -> {
            if (mSpecimen != null) {
                final GameAge selectedAge = getSelectedAge();
                mSpecimen.setAge(selectedAge != null ? selectedAge.name() : null);
                refreshAgeRequiredIndicator();

                if (mCallback != null) {
                    mCallback.onSpecimenChanged();
                }
            }
        });

        mWeightLayout = view.findViewById(R.id.weight_layout);
        mWeightEdit = view.findViewById(R.id.weight_input);
        mWeightEdit.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            final String src = source.toString();
            final String dst = dest.toString();
            final String res = dst.substring(0, dstart) + src.substring(start, end) + dst.substring(dend);

            return res.matches(WEIGHT_INPUT_PATTERN) ? null : "";
        }});
        mWeightEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mWeightEdit.clearFocus();
                hideKeyboard();

                return true;
            }
            return false;
        });
        mWeightEdit.addTextChangedListener(new DecimalSeparatorSwitcher(mWeightEdit));
    }

    public void setListener(final SpecimenDetailsListener callback) {
        mCallback = callback;
    }

    /**
     * Populate view with display information
     *
     * @param specimen HarvestSpecimen object containing display information
     * @param speciesId Official game species code for selected species
     * @param isWeightVisible Indicates whether weight field should be displayed or not
     */
    public void setupWithSpecimen(final HarvestSpecimen specimen,
                                  final int speciesId,
                                  final boolean isWeightVisible) {
        mSpeciesId = speciesId;

        setupWithSpecimen(specimen, speciesId == SpeciesInformation.GREY_SEAL_ID);

        mWeightLayout.setVisibility(isWeightVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Populate view with display information
     *
     * @param specimen         HarvestSpecimen object containing display information
     * @param allowClearValues Enable clearing selected value by clicking button again
     */
    public void setupWithSpecimen(final HarvestSpecimen specimen, final boolean allowClearValues) {
        mSpecimen = specimen;

        setGender(specimen.getGender());
        setAge(specimen.getAge());
        setWeight(specimen.getWeight());

        setSelectionsClearable(allowClearValues);
    }

    public void setRequiredFields(final boolean ageRequired, final boolean genderRequired, final boolean weightRequired) {
        mAgeRequired = ageRequired;
        mGenderRequired = genderRequired;
        mWeightRequired = weightRequired;

        mAgeRequiredIndicator.setVisibility(mAgeRequired && getSelectedAge() == null ? VISIBLE : GONE);
        mGenderRequiredIndicator.setVisibility(mGenderRequired && getSelectedGender() == null ? VISIBLE : GONE);
        mWeightLayout.setErrorEnabled(mWeightRequired && getWeight() == null);
        mWeightLayout.setError(mWeightRequired && getWeight() == null ? getResources().getString(R.string.specimen_value_required) : null);
    }

    protected void refreshGenderRequiredIndicator() {
        mGenderRequiredIndicator.setVisibility(mGenderRequired && mGenderSelect.getCheckedRadioButtonId() < 0 ? VISIBLE : GONE);
    }

    protected void refreshAgeRequiredIndicator() {
        mAgeRequiredIndicator.setVisibility(mAgeRequired && mAgeSelect.getCheckedRadioButtonId() < 0 ? VISIBLE : GONE);
    }

    protected void refreshWeightRequiredIndicator() {
        mWeightLayout.setErrorEnabled(mWeightRequired && getWeight() == null);
        mWeightLayout.setError(mWeightRequired && getWeight() == null ? getResources().getString(R.string.specimen_value_required) : null);
    }

    void validateWeightInput(final Editable s) {
        if (mWeightRequired && getWeight() == null) {
            mWeightLayout.setErrorEnabled(true);
            mWeightLayout.setError(getResources().getString(R.string.specimen_value_required));
        } else {
            mWeightLayout.setErrorEnabled(false);
            mWeightLayout.setError(null);
        }
    }

    public void setSelectionsClearable(final boolean allowValueClear) {
        mNotSelectedIsUnknown = allowValueClear;

        mGenderButtonFemale.setToggleEnabled(allowValueClear);
        mGenderButtonMale.setToggleEnabled(allowValueClear);
        mAgeButtonAdult.setToggleEnabled(allowValueClear);
        mAgeButtonYoung.setToggleEnabled(allowValueClear);
    }

    private Gender getSelectedGender() {
        final int selected = mGenderSelect.getCheckedRadioButtonId();

        if (selected == mGenderButtonFemale.getId()) {
            return Gender.FEMALE;
        } else if (selected == mGenderButtonMale.getId()) {
            return Gender.MALE;
        } else if (selected == -1 && mNotSelectedIsUnknown) {
            return Gender.UNKNOWN;
        }

        return null;
    }

    private void setGender(final String gender) {
        if (gender == null || gender.isEmpty()) {
            mGenderSelect.clearCheck();
        } else if (gender.equals(Gender.FEMALE.toString())) {
            mGenderSelect.check(mGenderButtonFemale.getId());
        } else if (gender.equals(Gender.MALE.toString())) {
            mGenderSelect.check(mGenderButtonMale.getId());
        } else {
            mGenderSelect.clearCheck();
        }

        refreshGenderRequiredIndicator();
    }

    private GameAge getSelectedAge() {
        int selected = mAgeSelect.getCheckedRadioButtonId();

        if (selected == mAgeButtonAdult.getId()) {
            return GameAge.ADULT;
        } else if (selected == mAgeButtonYoung.getId()) {
            return GameAge.YOUNG;
        } else if (selected == -1 && mNotSelectedIsUnknown) {
            return GameAge.UNKNOWN;
        }

        return null;
    }

    private void setAge(final String age) {
        if (age == null || age.isEmpty()) {
            mAgeSelect.clearCheck();
        } else if (age.equals(GameAge.ADULT.toString())) {
            mAgeSelect.check(mAgeButtonAdult.getId());
        } else if (age.equals(GameAge.YOUNG.toString())) {
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

    void setWeight(final Double weight) {
        final String weightText = weight != null && weight >= 0 ? String.valueOf(weight) : null;
        mWeightEdit.setText(weightText);

        refreshWeightRequiredIndicator();
    }

    @Override
    public void setEnabled(final boolean enabled) {
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
        final InputMethodManager inputManager =
                (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public interface SpecimenDetailsListener {
        void onSpecimenChanged();
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
        public void afterTextChanged(final Editable s) {
            final int selection = mTextInput.getSelectionEnd();

            if (s != null && s.toString().contains(".")) {
                mTextInput.setText(s.toString().replace('.', ','));
                mTextInput.setSelection(selection);
            }

            if (mSpecimen != null &&
                    mSpeciesId != SpeciesInformation.MOOSE_ID &&
                    mSpeciesId != SpeciesInformation.FALLOW_DEER_ID &&
                    mSpeciesId != SpeciesInformation.WHITE_TAILED_DEER_ID &&
                    mSpeciesId != SpeciesInformation.WILD_FOREST_DEER_ID) {

                mSpecimen.setWeight(getWeight());
                refreshWeightRequiredIndicator();

                if (mCallback != null) {
                    mCallback.onSpecimenChanged();
                }
            }

            validateWeightInput(mTextInput.getText());
        }
    }
}
