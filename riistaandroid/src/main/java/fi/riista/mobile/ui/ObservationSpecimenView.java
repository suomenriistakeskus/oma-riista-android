package fi.riista.mobile.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.ObservationSpecimen;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.metadata.ObservationContextSensitiveFieldSet;
import fi.riista.mobile.models.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.ui.ChoiceView.OnChoiceListener;
import fi.riista.mobile.utils.Utils;

public class ObservationSpecimenView extends LinearLayout {

    private GameObservation mObservation;
    private ObservationSpecimen mSpecimen;
    private int mPosition;
    private boolean mEditMode = true;
    private TextView mHeader;
    private LinearLayout mDetailsContainer;
    private LinearLayout mGenderLayout;
    private ImageButton mMaleButton;
    private ImageButton mFemaleButton;
    private ImageButton mUnknownButton;

    public ObservationSpecimenView(Context context, GameObservation observation, ObservationSpecimen specimen, int position, boolean editMode) {
        super(context);

        mObservation = observation;
        mSpecimen = specimen;
        mPosition = position;
        mEditMode = editMode;

        LayoutInflater.from(context).inflate(R.layout.view_observation_specimen, this);

        mHeader = (TextView) findViewById(R.id.txt_specimen_header);
        mDetailsContainer = (LinearLayout) findViewById(R.id.layout_specimen_details);
        mGenderLayout = (LinearLayout) findViewById(R.id.layout_specimen_gender);

        initHeader();
        init();
    }

    private void init() {
        ObservationSpecimenMetadata metadata = ObservationMetadataHelper.getInstance().getMetadataForSpecies(mObservation.gameSpeciesCode);
        if (metadata != null) {
            ObservationContextSensitiveFieldSet fields = metadata.findFieldSetByType(mObservation.observationType, mObservation.withinMooseHunting);
            if (fields != null) {
                initGenderButtons(fields);
                createContextSensitiveChoiceViews(fields);
            }
        }
    }

    private void initHeader() {
        Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);
        if (species != null) {
            mHeader.setText(species.mName + " " + (mPosition + 1));
        }
    }

    private void initGenderButtons(ObservationContextSensitiveFieldSet fields) {
        String genderField = fields.specimenFields.get("gender");
        if (genderField != null) {
            mGenderLayout.setVisibility(View.VISIBLE);

            mMaleButton = initGenderButton("MALE", R.id.btn_specimen_male);
            mFemaleButton = initGenderButton("FEMALE", R.id.btn_specimen_female);
            mUnknownButton = initGenderButton("UNKNOWN", R.id.btn_specimen_unknown);
        } else {
            mGenderLayout.setVisibility(View.GONE);
        }
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

    private void createContextSensitiveChoiceViews(ObservationContextSensitiveFieldSet fields) {
        if (mObservation.specimens == null) {
            return;
        }
        Context context = getContext();

        String ageField = fields.specimenFields.get("age");
        if (ageField != null) {
            ChoiceView age = createAgeChoice(mSpecimen, fields.allowedAges, context.getString(R.string.age_input_header));
            addChoiceView(age);
        }

        String stateField = fields.specimenFields.get("state");
        if (stateField != null) {
            ChoiceView state = createStateChoice(mSpecimen, fields.allowedStates, context.getString(R.string.observation_state));
            addChoiceView(state);
        }

        String markingField = fields.specimenFields.get("marking");
        if (markingField != null) {
            ChoiceView marking = createMarkingChoice(mSpecimen, fields.allowedMarkings, context.getString(R.string.observation_marked));
            addChoiceView(marking);
        }
    }

    private ChoiceView createAgeChoice(final ObservationSpecimen specimen, List<String> ages, String header) {
        ChoiceView choiceView = new ChoiceView(getContext(), header);

        if (mObservation.gameSpeciesCode == Utils.BEAR_ID) {
            //Use a different age term for bears of a certain age
            choiceView.setLocalizationAlias("_1TO2Y", R.string.age_eraus);
        }

        choiceView.setChoices(ages, specimen.age, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String age) {
                specimen.age = age;
            }
        });
        return choiceView;
    }

    private ChoiceView createStateChoice(final ObservationSpecimen specimen, List<String> states, String header) {
        ChoiceView choiceView = new ChoiceView(getContext(), header);
        choiceView.setChoices(states, specimen.state, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String state) {
                specimen.state = state;
            }
        });
        return choiceView;
    }

    private ChoiceView createMarkingChoice(final ObservationSpecimen specimen, List<String> markings, String header) {
        ChoiceView choiceView = new ChoiceView(getContext(), header);
        choiceView.setChoices(markings, specimen.marking, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String marking) {
                specimen.marking = marking;
            }
        });
        return choiceView;
    }

    private void addChoiceView(ChoiceView choiceView) {
        mDetailsContainer.addView(choiceView);
        choiceView.setChoiceEnabled(mEditMode);
    }
}
