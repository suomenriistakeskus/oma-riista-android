package fi.riista.mobile.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.Specimen.SpecimenAge;
import fi.riista.mobile.models.Specimen.SpecimenGender;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaSpecimen;
import fi.riista.mobile.ui.ChoiceView.OnChoiceListener;

public class SrvaSpecimenView extends LinearLayout {

    private SrvaEvent mEvent;
    private SrvaSpecimen mSpecimen;
    private int mPosition;
    private boolean mEditMode = true;
    private TextView mHeader;
    private LinearLayout mDetailsContainer;
    private ImageButton mMaleButton;
    private ImageButton mFemaleButton;
    private ImageButton mUnknownButton;

    public SrvaSpecimenView(Context context, SrvaEvent event, SrvaSpecimen specimen, int position, boolean editMode) {
        super(context);

        mEvent = event;
        mSpecimen = specimen;
        mPosition = position;
        mEditMode = editMode;

        LayoutInflater.from(context).inflate(R.layout.view_observation_specimen, this);

        mHeader = (TextView) findViewById(R.id.txt_specimen_header);
        mDetailsContainer = (LinearLayout) findViewById(R.id.layout_specimen_details);

        initHeader();
        initGenderButtons();
        initAgeChoice();
    }

    private void initHeader() {
        Species species = SpeciesInformation.getSpecies(mEvent.gameSpeciesCode);
        if (species != null) {
            mHeader.setText(species.mName + " " + (mPosition + 1));
        }
    }

    private void initGenderButtons() {
        mMaleButton = initGenderButton(SpecimenGender.MALE.toString(), R.id.btn_specimen_male);
        mFemaleButton = initGenderButton(SpecimenGender.FEMALE.toString(), R.id.btn_specimen_female);
        mUnknownButton = initGenderButton(SpecimenGender.UNKNOWN.toString(), R.id.btn_specimen_unknown);
    }

    private ImageButton initGenderButton(final String gender, int buttonId) {
        boolean selected = gender.equals(mSpecimen.gender);

        ImageButton button = (ImageButton) findViewById(buttonId);
        button.setEnabled(mEditMode);
        button.setSelected(selected);
        updateGenderIconFilter(button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    //Deselect existing
                    deselectAllGenderButtons();

                    mSpecimen.gender = null;
                } else {
                    //Select this one
                    deselectAllGenderButtons();
                    v.setSelected(true);

                    mSpecimen.gender = gender;
                }
                updateGenderIconFilter((ImageButton) v);
            }
        });
        return button;
    }

    private void updateGenderIconFilter(ImageButton button) {
        if (button.isSelected()) {
            button.setColorFilter(null);
        } else {
            button.setColorFilter(Color.GRAY, Mode.MULTIPLY);
        }
    }

    private void deselectAllGenderButtons() {
        mMaleButton.setSelected(false);
        updateGenderIconFilter(mMaleButton);

        mFemaleButton.setSelected(false);
        updateGenderIconFilter(mFemaleButton);

        mUnknownButton.setSelected(false);
        updateGenderIconFilter(mUnknownButton);
    }

    private void initAgeChoice() {
        ChoiceView choiceView = new ChoiceView(getContext(), getResources().getString(R.string.age_input_header));

        choiceView.setLocalizationAlias(SpecimenAge.YOUNG.toString(), R.string.age_value_young);

        ArrayList<String> ages = new ArrayList<String>();
        for (SpecimenAge age : SpecimenAge.values()) {
            ages.add(age.toString());
        }

        choiceView.setChoices(ages, mSpecimen.age, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String age) {
                mSpecimen.age = age;
            }
        });
        addChoiceView(choiceView);
    }

    private void addChoiceView(ChoiceView choiceView) {
        mDetailsContainer.addView(choiceView);
        choiceView.setChoiceEnabled(mEditMode);
    }
}
