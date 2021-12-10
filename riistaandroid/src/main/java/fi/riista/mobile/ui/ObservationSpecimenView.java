package fi.riista.mobile.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.observation.ObservationSpecimen;
import fi.riista.mobile.models.observation.metadata.ObservationContextSensitiveFieldSet;
import fi.riista.mobile.models.observation.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.models.specimen.Gender;

public class ObservationSpecimenView extends ConstraintLayout {

    private final GameObservation mObservation;
    private final ObservationSpecimen mSpecimen;

    private final ObservationSpecimenMetadata mObservationSpecimenMetadata;
    private final boolean mIsCarnivoreAuthority;

    private final int mPosition;
    private final boolean mEditMode;

    private final TextView mHeader;
    private final LinearLayout mDetailsContainer;
    private final RadioGroup mGenderSelect;
    private final RadioButtonImageText mMaleButton;
    private final RadioButtonImageText mFemaleButton;
    private final RadioButtonImageText mUnknownButton;

    private final ImageButton mRemoveButton;

    public ObservationSpecimenView(final Context context,
                                   final GameObservation observation,
                                   final ObservationSpecimen specimen,
                                   final ObservationSpecimenMetadata observationSpecimenMetadata,
                                   final boolean isCarnivoreAuthority,
                                   final int position,
                                   final boolean editMode) {
        super(context);

        mObservationSpecimenMetadata = observationSpecimenMetadata;

        mObservation = observation;
        mSpecimen = specimen;
        mIsCarnivoreAuthority = isCarnivoreAuthority;

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
        init();
    }

    private void init() {
        if (mObservationSpecimenMetadata != null) {
            final ObservationContextSensitiveFieldSet fields =
                    mObservationSpecimenMetadata.findFieldSetByType(mObservation.observationCategory, mObservation.observationType);

            if (fields != null) {
                initGenderButtons(fields);
                createContextSensitiveChoiceViews(mObservationSpecimenMetadata, fields);
            }
        }

        mRemoveButton.setVisibility(mEditMode ? VISIBLE : GONE);
    }

    private void initHeader() {
        final Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);

        if (species != null) {
            mHeader.setText(String.format(Locale.getDefault(), "%s %d", species.mName, mPosition + 1));
        }
    }

    private void initGenderButtons(final ObservationContextSensitiveFieldSet fields) {
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

        final String genderField = fields.specimenFields.get("gender");
        if (genderField != null) {
            mGenderSelect.setVisibility(View.VISIBLE);

            mMaleButton.setToggleEnabled(true);
            mFemaleButton.setToggleEnabled(true);
            mUnknownButton.setToggleEnabled(true);
        } else {
            mGenderSelect.setVisibility(View.GONE);
        }
    }

    public void setOnRemoveListener(final Integer tag, final View.OnClickListener listener) {
        if (mEditMode) {
            setTag(tag);
            mRemoveButton.setOnClickListener(listener);
        }
    }

    private Gender getSelectedGender() {
        final int selectedId = mGenderSelect.getCheckedRadioButtonId();

        if (mMaleButton.getId() == selectedId) {
            return Gender.MALE;
        } else if (mFemaleButton.getId() == selectedId) {
            return Gender.FEMALE;
        } else if (mUnknownButton.getId() == selectedId) {
            return Gender.UNKNOWN;
        }

        return null;
    }

    private void setSelectedGender(final String value) {
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

    private void createContextSensitiveChoiceViews(final ObservationSpecimenMetadata metadata,
                                                   final ObservationContextSensitiveFieldSet fields) {

        if (mObservation.specimens == null) {
            return;
        }

        final Context context = getContext();

        final String ageField = fields.specimenFields.get("age");
        if (ageField != null) {
            final ChoiceView<String> age = createAgeChoice(mSpecimen, fields.allowedAges, context.getString(R.string.age_title));
            addChoiceView(age);
        }

        if (fields.hasSpecimenField("lengthOfPaw")
                || (mIsCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthoritySpecimenField("lengthOfPaw"))) {

            final ChoiceView<String> pawLength = createPawLengthChoice(mSpecimen, pawDimensionRange(metadata.minLengthOfPaw, metadata.maxLengthOfPaw), context.getString(R.string.tassu_paw_length));
            addChoiceView(pawLength);
        }

        if (fields.hasSpecimenField("widthOfPaw")
                || (mIsCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthoritySpecimenField("widthOfPaw"))) {

            final ChoiceView<String> pawWidth = createPawWidthChoice(mSpecimen, pawDimensionRange(metadata.minWidthOfPaw, metadata.maxWidthOfPaw), context.getString(R.string.tassu_paw_width));
            addChoiceView(pawWidth);
        }

        final String stateField = fields.specimenFields.get("state");
        if (stateField != null) {
            final ChoiceView<String> state = createStateChoice(mSpecimen, fields.allowedStates, context.getString(R.string.observation_state));
            addChoiceView(state);
        }

        final String markingField = fields.specimenFields.get("marking");
        if (markingField != null) {
            final ChoiceView<String> marking = createMarkingChoice(mSpecimen, fields.allowedMarkings, context.getString(R.string.observation_marked));
            addChoiceView(marking);
        }
    }

    private ChoiceView<String> createAgeChoice(final ObservationSpecimen specimen, List<String> ages, String header) {
        final ChoiceView<String> choiceView = new ChoiceView<String>(getContext(), header);

        if (mObservation.gameSpeciesCode == SpeciesInformation.BEAR_ID) {
            // Use a different age term for bears of a certain age
            choiceView.setLocalizationAlias("_1TO2Y", R.string.age_eraus);
        }

        choiceView.setChoices(ages, specimen.age, true, (position, age) -> specimen.age = age);
        return choiceView;
    }

    private ChoiceView<String> createPawLengthChoice(final ObservationSpecimen specimen,
                                             final List<String> values,
                                             final String header) {

        final ChoiceView<String> choiceView = new ChoiceView<String>(getContext(), header);
        choiceView.setChoices(values, specimen.lengthOfPaw, true, (position, selected) -> specimen.lengthOfPaw = selected);
        return choiceView;
    }

    private ChoiceView<String> createPawWidthChoice(final ObservationSpecimen specimen,
                                            final List<String> values,
                                            final String header) {

        final ChoiceView<String> choiceView = new ChoiceView<String>(getContext(), header);
        choiceView.setChoices(values, specimen.widthOfPaw, true, (position, selected) -> specimen.widthOfPaw = selected);
        return choiceView;
    }

    private ChoiceView<String> createStateChoice(final ObservationSpecimen specimen,
                                         final List<String> states,
                                         final String header) {

        final ChoiceView<String> choiceView = new ChoiceView<String>(getContext(), header);
        choiceView.setChoices(states, specimen.state, true, (position, state) -> specimen.state = state);
        return choiceView;
    }

    private ChoiceView<String> createMarkingChoice(final ObservationSpecimen specimen,
                                           final List<String> markings,
                                           final String header) {

        final ChoiceView<String> choiceView = new ChoiceView<>(getContext(), header);
        choiceView.setChoices(markings, specimen.marking, true, (position, marking) -> specimen.marking = marking);
        return choiceView;
    }

    private void addChoiceView(final ChoiceView<String> choiceView) {
        mDetailsContainer.addView(choiceView);
        choiceView.setChoiceEnabled(mEditMode);
    }

    private List<String> pawDimensionRange(final int minValue, final int maxValue) {
        final BigDecimal minDecimal = new BigDecimal(minValue);
        final BigDecimal increment = new BigDecimal(0.5);

        final ArrayList<String> list = new ArrayList<>();
        for (int i = minValue; minValue + i * increment.doubleValue() <= maxValue; i++) {
            list.add(minDecimal.add(increment.multiply(new BigDecimal(i))) + "");
        }
        return list;
    }
}
