package fi.riista.mobile.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Locale;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.specimen.GameAge;
import fi.riista.mobile.models.specimen.Gender;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaSpecimen;

public class SrvaSpecimenView extends ConstraintLayout {

    private SrvaEvent mEvent;
    private SrvaSpecimen mSpecimen;
    private int mPosition;
    private boolean mEditMode = true;
    private TextView mHeader;
    private LinearLayout mDetailsContainer;
    private RadioGroup mGenderSelect;
    private RadioButtonImageText mMaleButton;
    private RadioButtonImageText mFemaleButton;
    private RadioButtonImageText mUnknownButton;

    private ImageButton mRemoveButton;

    public SrvaSpecimenView(Context context, SrvaEvent event, SrvaSpecimen specimen, int position, boolean editMode) {
        super(context);

        mEvent = event;
        mSpecimen = specimen;
        mPosition = position;
        mEditMode = editMode;

        LayoutInflater.from(context).inflate(R.layout.view_observation_specimen, this);

        mHeader = findViewById(R.id.txt_specimen_header);
        mDetailsContainer = findViewById(R.id.layout_specimen_details);

        mGenderSelect = findViewById(R.id.gender_select);
        mMaleButton = findViewById(R.id.gender_select_male);
        mFemaleButton = findViewById(R.id.gender_select_female);
        mUnknownButton = findViewById(R.id.gender_select_unknown);

        mRemoveButton = findViewById(R.id.btn_specimen_item_remove);

        initHeader();
        initGenderButtons();
        initAgeChoice();

        mRemoveButton.setVisibility(mEditMode ? VISIBLE : GONE);
    }

    private void initHeader() {
        Species species = SpeciesInformation.getSpecies(mEvent.gameSpeciesCode);
        if (species != null) {
            mHeader.setText(String.format(Locale.getDefault(), "%s %d", species.mName, mPosition + 1));
        }
    }

    private void initGenderButtons() {
        mGenderSelect.setOnCheckedChangeListener((group, checkedId) -> {
            if (mSpecimen != null) {
                final Gender selectedGender = getSelectedGender();
                mSpecimen.gender = selectedGender != null ? selectedGender.name() : null;
            }
        });

        mGenderSelect.setEnabled(mEditMode);
        mFemaleButton.setEnabled(mEditMode);
        mMaleButton.setEnabled(mEditMode);
        mUnknownButton.setEnabled(mEditMode);
        setSelectedGender(mSpecimen.gender);

        mGenderSelect.setVisibility(View.VISIBLE);

        mMaleButton.setToggleEnabled(true);
        mFemaleButton.setToggleEnabled(true);
        mUnknownButton.setToggleEnabled(true);
    }

    public void setOnRemoveListener(Integer tag, View.OnClickListener listener) {
        if (mEditMode) {
            setTag(tag);
            mRemoveButton.setOnClickListener(listener);
        }
    }

    private Gender getSelectedGender() {
        int selectedId = mGenderSelect.getCheckedRadioButtonId();

        if (mMaleButton.getId() == selectedId) {
            return Gender.MALE;
        } else if (mFemaleButton.getId() == selectedId) {
            return Gender.FEMALE;
        } else if (mUnknownButton.getId() == selectedId) {
            return Gender.UNKNOWN;
        }

        return null;
    }

    private void setSelectedGender(String value) {
        if (Gender.MALE.toString().equals(value)) {
            mGenderSelect.check(mMaleButton.getId());
        } else if (Gender.FEMALE.toString().equals(value)) {
            mGenderSelect.check(mFemaleButton.getId());
        } else if (Gender.UNKNOWN.toString().equals(value)) {
            mGenderSelect.check(mUnknownButton.getId());
        } else {
            mGenderSelect.clearCheck();
        }
    }

    private void initAgeChoice() {
        ChoiceView<String> choiceView = new ChoiceView<>(getContext(), getResources().getString(R.string.age_title));

        choiceView.setLocalizationAlias(GameAge.YOUNG.toString(), R.string.age_young);

        ArrayList<String> ages = new ArrayList<>();
        for (final GameAge age : GameAge.values()) {
            ages.add(age.toString());
        }

        choiceView.setChoices(ages, mSpecimen.age, true, (position, age) -> mSpecimen.age = age);
        addChoiceView(choiceView);
    }

    private void addChoiceView(ChoiceView<String> choiceView) {
        mDetailsContainer.addView(choiceView);
        choiceView.setChoiceEnabled(mEditMode);
    }
}
