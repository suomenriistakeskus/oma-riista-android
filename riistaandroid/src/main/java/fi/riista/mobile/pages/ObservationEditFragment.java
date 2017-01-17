package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.EditActivity.EditBridge;
import fi.riista.mobile.activity.EditActivity.EditListener;
import fi.riista.mobile.activity.ImageViewerActivity;
import fi.riista.mobile.activity.ObservationSpecimensActivity;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.message.ChangeObservationMessage;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.ObservationSpecimen;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.metadata.ObservationContextSensitiveFieldSet;
import fi.riista.mobile.models.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.observation.ObservationDatabase.ObservationListener;
import fi.riista.mobile.observation.ObservationDatabase.ObservationsListener;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.observation.ObservationValidator;
import fi.riista.mobile.ui.ChoiceView;
import fi.riista.mobile.ui.ChoiceView.OnCheckListener;
import fi.riista.mobile.ui.ChoiceView.OnChoiceListener;
import fi.riista.mobile.ui.ChoiceView.OnTextListener;
import fi.riista.mobile.ui.SelectSpeciesButton;
import fi.riista.mobile.utils.BaseDatabase.DeleteListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.message.WorkMessageHandler;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewOnClick;

public class ObservationEditFragment extends Fragment implements EditListener, EditBridge {

    private static final int SPECIES_REQUEST_CODE = 150;

    public static ObservationEditFragment newInstance(GameObservation observation) {
        ObservationEditFragment fragment = new ObservationEditFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("observation", observation);
        fragment.setArguments(bundle);

        return fragment;
    }

    @ViewId(R.id.btn_select_species)
    private SelectSpeciesButton mSelectSpeciesButton;

    @ViewId(R.id.layout_details_container)
    private LinearLayout mDetailsContainer;

    @ViewId(R.id.btn_observation_species)
    private Button mSpecimensButton;

    private GameObservation mObservation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetObservation();
    }

    private void resetObservation() {
        GameObservation observation = (GameObservation) getArguments().getSerializable("observation");
        mObservation = (GameObservation) Utils.cloneObject(observation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_observation_edit, container, false);
        ViewAnnotations.apply(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EditActivity editActivity = (EditActivity) getActivity();
        editActivity.connectEditFragment(this, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChooseSpeciesActivity.SPECIES_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Species species = (Species) data.getSerializableExtra(ChooseSpeciesActivity.RESULT_SPECIES);
                mObservation.gameSpeciesCode = species.mId;

                changeObservationType(null);
            }
        } else if (requestCode == SPECIES_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<ObservationSpecimen> specimens = (List<ObservationSpecimen>) data.getSerializableExtra(ObservationSpecimensActivity.RESULT_SPECIMENS);
                mObservation.specimens.clear();
                mObservation.specimens.addAll(specimens);
            }
            updateSelectedSpecies();
        }
    }

    @ViewOnClick(R.id.btn_observation_species)
    protected void onShowSpeciesClicked(View view) {
        Intent intent = new Intent(getActivity(), ObservationSpecimensActivity.class);
        intent.putExtra(ObservationSpecimensActivity.EXTRA_OBSERVATION, mObservation);
        intent.putExtra(ObservationSpecimensActivity.EXTRA_EDIT_MODE, isEditModeOn());
        startActivityForResult(intent, SPECIES_REQUEST_CODE);
    }

    @ViewOnClick(R.id.speciesButton)
    protected void onSelectSpeciesButtonClicked(View view) {
        if (isEditModeOn()) {
            UiUtils.startSpeciesSelection(getActivity(), this);
        }
    }

    private void updateSelectedSpecies() {
        updateSpecimensButton();

        mSelectSpeciesButton.getSpeciesButton().setEnabled(isEditModeOn());
        mSelectSpeciesButton.getAmountInput().setVisibility(View.GONE);

        ObservationSpecimenMetadata metadata = ObservationMetadataHelper.getInstance().getMetadataForSpecies(mObservation.gameSpeciesCode);
        Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);
        if (metadata == null || species == null) {
            //No metadata for this species, can't really do anything useful.
            return;
        }

        mSelectSpeciesButton.setSpecies(species);

        resetDetailViews(metadata);
    }

    private void updateSpecimensButton() {
        boolean hasSpecimens = mObservation.observationType != null && mObservation.specimens != null && mObservation.specimens.size() > 0;
        mSpecimensButton.setVisibility(hasSpecimens ? View.VISIBLE : View.GONE);
    }

    private void removeSpecimens() {
        if (mObservation.totalSpecimenAmount != null) {
            mObservation.totalSpecimenAmount = null;
            mObservation.specimens = null;
        }
        updateSpecimensButton();

        validate();
    }

    private void resetSpecimens() {
        if (mObservation.specimens != null) {
            mObservation.specimens = new ArrayList<>();
            if (mObservation.totalSpecimenAmount != null) {
                for (int i = 0; i < mObservation.totalSpecimenAmount; ++i) {
                    mObservation.specimens.add(new ObservationSpecimen());
                }
            }
            mObservation.totalSpecimenAmount = mObservation.specimens.size();
        }
        updateSelectedSpecies();
    }

    private void changeSpecimentAmount(int amount) {
        if (mObservation.specimens == null) {
            mObservation.specimens = new ArrayList<>();
        }

        //Add missing
        for (int i = mObservation.specimens.size(); i < amount; ++i) {
            mObservation.specimens.add(new ObservationSpecimen());
        }

        //Remove extras
        while (mObservation.specimens.size() > amount) {
            mObservation.specimens.remove(mObservation.specimens.size() - 1);
        }

        mObservation.totalSpecimenAmount = mObservation.specimens.size();

        updateSpecimensButton();

        validate();
    }

    private void changeObservationType(String type) {
        mObservation.observationType = type;

        ObservationSpecimenMetadata metadata = ObservationMetadataHelper.getInstance().getMetadataForSpecies(mObservation.gameSpeciesCode);
        if (metadata != null && metadata.hasBaseFieldSet("withinMooseHunting")) {
            mObservation.withinMooseHunting = (mObservation.withinMooseHunting != null && mObservation.withinMooseHunting);
        } else {
            mObservation.withinMooseHunting = null;
        }

        mObservation.mooselikeMaleAmount = null;
        mObservation.mooselikeFemaleAmount = null;
        mObservation.mooselikeFemale1CalfAmount = null;
        mObservation.mooselikeFemale2CalfsAmount = null;
        mObservation.mooselikeFemale3CalfsAmount = null;
        mObservation.mooselikeFemale4CalfsAmount = null;
        mObservation.mooselikeUnknownSpecimenAmount = null;

        resetSpecimens();
    }

    private void resetDetailViews(ObservationSpecimenMetadata metadata) {
        mDetailsContainer.removeAllViews();

        if (metadata.hasBaseFieldSet("withinMooseHunting")) {
            createWithinMooseHuntingChoice();
        }
        createObservationTypeChoiceView(metadata);

        createAmountChoiceView(metadata);

        createMooselikeChoiceViews(metadata);

        updateSpecimensButton();

        validate();
    }

    private void createWithinMooseHuntingChoice() {
        boolean withinMooseHunting = false;
        if (mObservation.withinMooseHunting != null) {
            withinMooseHunting = mObservation.withinMooseHunting;
        }

        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.within_moose_hunting));
        choiceView.setChecked(withinMooseHunting, new OnCheckListener() {
            @Override
            public void onCheck(boolean check) {
                mObservation.withinMooseHunting = check;
                mObservation.observationType = null;
                resetSpecimens();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createObservationTypeChoiceView(final ObservationSpecimenMetadata metadata) {
        ArrayList<String> types = new ArrayList<>();
        for (ObservationContextSensitiveFieldSet field : metadata.contextSensitiveFieldSets) {
            boolean withinMooseHunting = (mObservation.withinMooseHunting != null) && mObservation.withinMooseHunting;
            if (field.withinMooseHunting == withinMooseHunting) {
                types.add(field.type);
            }
        }

        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.observation_type));
        choiceView.setChoices(types, mObservation.observationType, true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String type) {
                changeObservationType(type);
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createAmountChoiceView(ObservationSpecimenMetadata metadata) {
        ObservationContextSensitiveFieldSet fields = metadata.findFieldSetByType(mObservation.observationType, mObservation.withinMooseHunting);
        if (fields != null) {
            if (fields.hasField(fields.baseFields, "amount")) {
                int amount = 0;
                if (mObservation.totalSpecimenAmount != null) {
                    amount = mObservation.totalSpecimenAmount;
                }
                amount = Math.max(amount, 1);

                changeSpecimentAmount(amount);

                ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.harvest_amount));
                choiceView.setEditTextMaxLength(3);
                choiceView.setEditTextChoice(Utils.formatInt(amount), new OnTextListener() {
                    @Override
                    public void onText(String text) {
                        Integer amount = Utils.parseInt(text);
                        if (amount != null) {
                            changeSpecimentAmount(amount);
                        }
                    }
                });
                addChoiceView(choiceView, true);
            } else {
                removeSpecimens();
            }
        }
    }

    private void createMooselikeChoiceViews(ObservationSpecimenMetadata metadata) {
        ObservationContextSensitiveFieldSet fields = metadata.findFieldSetByType(mObservation.observationType, mObservation.withinMooseHunting);
        if (fields == null) {
            return;
        }

        if (fields.hasFieldSet(fields.baseFields, "mooselikeMaleAmount")) {
            if (mObservation.mooselikeMaleAmount == null) {
                mObservation.mooselikeMaleAmount = 0;
            }
            createMooselikeMaleChoice();
        }
        if (fields.hasFieldSet(fields.baseFields, "mooselikeFemaleAmount")) {
            if (mObservation.mooselikeFemaleAmount == null) {
                mObservation.mooselikeFemaleAmount = 0;
            }
            createMooselikeFemaleChoice();
        }
        if (fields.hasFieldSet(fields.baseFields, "mooselikeFemale1CalfAmount")) {
            if (mObservation.mooselikeFemale1CalfAmount == null) {
                mObservation.mooselikeFemale1CalfAmount = 0;
            }
            createMooselikeFemale1CalfChoice();
        }
        if (fields.hasFieldSet(fields.baseFields, "mooselikeFemale2CalfsAmount")) {
            if (mObservation.mooselikeFemale2CalfsAmount == null) {
                mObservation.mooselikeFemale2CalfsAmount = 0;
            }
            createMooselikeFemale2CalfsChoice();
        }
        if (fields.hasFieldSet(fields.baseFields, "mooselikeFemale3CalfsAmount")) {
            if (mObservation.mooselikeFemale3CalfsAmount == null) {
                mObservation.mooselikeFemale3CalfsAmount = 0;
            }
            createMooselikeFemale3CalfsChoice();
        }
        if (fields.hasFieldSet(fields.baseFields, "mooselikeFemale4CalfsAmount")) {
            if (mObservation.mooselikeFemale4CalfsAmount == null) {
                mObservation.mooselikeFemale4CalfsAmount = 0;
            }
            createMooselikeFemale4CalfsChoice();
        }
        if (fields.hasFieldSet(fields.baseFields, "mooselikeUnknownSpecimenAmount")) {
            if (mObservation.mooselikeUnknownSpecimenAmount == null) {
                mObservation.mooselikeUnknownSpecimenAmount = 0;
            }
            createMooselikeUnknownsChoice();
        }
    }

    private void createMooselikeMaleChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_male));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeMaleAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeMaleAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemaleChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_female));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemaleAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeFemaleAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale1CalfChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_female_calf));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale1CalfAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeFemale1CalfAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale2CalfsChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_female_calfs2));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale2CalfsAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeFemale2CalfsAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale3CalfsChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_female_calfs3));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale3CalfsAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeFemale3CalfsAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale4CalfsChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_female_calfs4));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale4CalfsAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeFemale4CalfsAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeUnknownsChoice() {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.mooselike_unknown));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeUnknownSpecimenAmount), false, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String amount) {
                mObservation.mooselikeUnknownSpecimenAmount = Utils.parseInt(amount);
                validate();
            }
        });
        addChoiceView(choiceView, true);
    }

    private String mooselikeValue(Integer value) {
        return "" + value;
    }

    private List<String> mooselikeRange() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 50; ++i) {
            list.add(i + "");
        }
        return list;
    }

    private void addChoiceView(ChoiceView choiceView, boolean topMargin) {
        mDetailsContainer.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(isEditModeOn());
    }

    private boolean validate() {
        boolean valid = ObservationValidator.validate(mObservation);

        ((EditActivity) getActivity()).setEditValid(valid);

        return valid;
    }

    @Override
    public void onEditStart() {
        updateSelectedSpecies();
    }

    @Override
    public void onEditCancel() {
        resetObservation();

        updateSelectedSpecies();
    }

    @Override
    public void onEditSave() {
        if (validate()) {
            mObservation.modified = true;
            mObservation.observationSpecVersion = AppConfig.OBSERVATION_SPEC_VERSION;

            ObservationDatabase.getInstance().saveObservation(mObservation, new SaveListener() {
                @Override
                public void onSaved(long localId) {
                    ((EditActivity) getActivity()).finishEdit(mObservation.type, mObservation.toDateTime(), localId);
                }

                @Override
                public void onError() {
                }
            });
        }
    }

    @Override
    public void onDelete() {
        ObservationDatabase.getInstance().deleteObservation(mObservation, false, new DeleteListener() {
            @Override
            public void onDelete() {
                ((EditActivity) getActivity()).finishEdit(mObservation.type, mObservation.toDateTime(), -1);
            }

            @Override
            public void onError() {
            }
        });
    }

    @Override
    public void onDateChanged(DateTime date) {
        mObservation.setPointOfTime(date);
        validate();
    }

    @Override
    public void onLocationChanged(Location location, String source) {
        mObservation.geoLocation = GeoLocation.fromLocation(location);
        mObservation.geoLocation.source = source;
        validate();
    }

    @Override
    public void onDescriptionChanged(String description) {
        mObservation.description = description;
        validate();
    }

    @Override
    public DateTime getDate() {
        return mObservation.toDateTime();
    }

    @Override
    public Location getLocation() {
        return mObservation.toLocation();
    }

    @Override
    public String getLocationSource() {
        if (mObservation.geoLocation != null) {
            return mObservation.geoLocation.source;
        }
        return null;
    }

    @Override
    public String getDescription() {
        return mObservation.description;
    }

    @Override
    public boolean isEditable() {
        return mObservation.canEdit;
    }

    private boolean isEditModeOn() {
        return isEditable() && ((EditActivity) getActivity()).isEditModeOn();
    }

    @Override
    public List<LogImage> getImages() {
        return mObservation.getAllImages();
    }

    @Override
    public void onImagesChanged(List<LogImage> images) {
        mObservation.setLocalImages(images);
    }

    @Override
    public void onViewImage(final String uuid) {
        ObservationDatabase.getInstance().loadObservationsWithAnyImages(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                ArrayList<LogImage> images = new ArrayList<>();
                for (GameObservation observation : observations) {
                    images.addAll(observation.getAllImages());
                }

                Intent intent = ImageViewerActivity.createIntent(getActivity(), images, uuid);
                startActivity(intent);
            }
        });
    }

    @WorkMessageHandler(ChangeObservationMessage.class)
    public void onChangeObservationMessage(ChangeObservationMessage message) {
        if (message.localId == mObservation.localId) {
            return;
        }

        final Activity activity = getActivity();

        ObservationDatabase.getInstance().loadObservation(message.localId, new ObservationListener() {
            @Override
            public void onObservation(GameObservation observation) {
                if (observation != null) {
                    activity.getIntent().putExtra(EditActivity.EXTRA_OBSERVATION, observation);
                    activity.getIntent().putExtra(EditActivity.EXTRA_NEW, false);

                    activity.recreate();
                }
            }
        });
    }
}
