package fi.riista.mobile.pages;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.AppConfig;
import fi.riista.mobile.DiaryImageManager;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.EditActivity.EditBridge;
import fi.riista.mobile.activity.EditActivity.EditListener;
import fi.riista.mobile.activity.ObservationSpecimensActivity;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.message.ChangeObservationMessage;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.observation.ObservationCategory;
import fi.riista.mobile.models.observation.ObservationSpecimen;
import fi.riista.mobile.models.observation.ObservationType;
import fi.riista.mobile.models.observation.metadata.ObservationContextSensitiveFieldSet;
import fi.riista.mobile.models.observation.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.models.observation.metadata.ObservationWithinHuntingCapability;
import fi.riista.mobile.models.user.UserInfo;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.observation.ObservationValidator;
import fi.riista.mobile.ui.ChoiceView;
import fi.riista.mobile.ui.FullScreenImageDialog;
import fi.riista.mobile.utils.BaseDatabase.DeleteListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.EditUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.message.WorkMessageHandler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class ObservationEditFragment extends Fragment
        implements EditListener, EditBridge, DiaryImageManager.ImageManagerActivityAPI {

    @Inject
    UserInfoStore mUserInfoStore;

    @Inject
    ObservationDatabase mObservationDatabase;

    @Inject
    ObservationMetadataHelper mObservationMetadataHelper;

    @Inject
    ObservationValidator mObservationValidator;

    private GameObservation mObservation;
    private UserInfo mUserInfo;

    private DiaryImageManager mImageManager;

    private Button mDateButton;
    private Button mTimeButton;
    private MaterialButton mSpeciesButton;
    private LinearLayout mDetailsContainer;
    private LinearLayout mDetailsContainer2;
    private Button mSpecimensButton;

    private final ActivityResultLauncher<Intent> chooseSpeciesActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivitySpeciesResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> chooseSpecimenActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivitySpecimenResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> selectPhotoActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                mImageManager.handleSelectPhotoResult(result.getResultCode(), result.getData());
                onImagesChanged(mImageManager.getImages());
            }
    );

    private final ActivityResultLauncher<Intent> captureImageActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                mImageManager.handleCaptureImageResult(result.getResultCode(), result.getData());
                onImagesChanged(mImageManager.getImages());
            }
    );

    public static ObservationEditFragment newInstance(final GameObservation observation) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable("observation", observation);

        final ObservationEditFragment fragment = new ObservationEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserInfo = mUserInfoStore.getUserInfo();
        mImageManager = new DiaryImageManager(requireActivity(), this);

        resetObservation();
    }

    private void resetObservation() {
        final GameObservation observation = (GameObservation) getArguments().getSerializable("observation");
        mObservation = (GameObservation) Utils.cloneObject(observation);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_observation_edit, container, false);

        mDateButton = view.findViewById(R.id.btn_edit_date);
        mTimeButton = view.findViewById(R.id.btn_edit_time);
        mSpeciesButton = view.findViewById(R.id.btn_select_species);
        LinearLayout mImagesLayout = view.findViewById(R.id.edit_image_view);

        mDetailsContainer = view.findViewById(R.id.layout_details_container);
        mDetailsContainer2 = view.findViewById(R.id.layout_details_container2);
        mSpecimensButton = view.findViewById(R.id.btn_observation_specimens);

        mSpecimensButton.setOnClickListener(v -> onShowSpecimensClicked());

        mDateButton.setText(mObservation.toDateTime().toString(EditUtils.DATE_FORMAT));
        mDateButton.setOnClickListener(v -> {
            final Context context = getContext();
            if (context != null) {
                EditUtils.showDateDialog(getContext(), mObservation.toDateTime(), dateTime -> {
                    mObservation.setPointOfTime(dateTime);
                    mDateButton.setText(dateTime.toString(EditUtils.DATE_FORMAT));
                    mTimeButton.setText(dateTime.toString(EditUtils.TIME_FORMAT));

                    validate();
                });
            }
        });

        mTimeButton.setText(mObservation.toDateTime().toString(EditUtils.TIME_FORMAT));
        mTimeButton.setOnClickListener(v -> {
            final Context context = getContext();
            if (context != null) {
                EditUtils.showTimeDialog(getContext(), mObservation.toDateTime(), dateTime -> {
                    mObservation.setPointOfTime(dateTime);
                    mDateButton.setText(dateTime.toString(EditUtils.DATE_FORMAT));
                    mTimeButton.setText(dateTime.toString(EditUtils.TIME_FORMAT));

                    validate();
                });
            }
        });

        final Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);
        if (species != null) {
            mSpeciesButton.setText(species.mName);
            mSpeciesButton.setIcon(SpeciesInformation.getSpeciesImage(getContext(), species.mId));
            mSpeciesButton.setIconTint(null);
        }
        mSpeciesButton.setOnClickListener(v -> {
            if (isEditModeOn()) {
                EditUtils.showSpeciesCategoryDialog(this, chooseSpeciesActivityResultLaunch);
            }
        });

        mImageManager.setItems(mObservation.getImages());
        mImageManager.setup(mImagesLayout, captureImageActivityResultLaunch, selectPhotoActivityResultLaunch);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final EditActivity activity = (EditActivity) requireActivity();
        activity.connectEditFragment(this, this);
    }

    private void onActivitySpeciesResult(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final Species species = (Species) data.getSerializableExtra(
                    ChooseSpeciesActivity.RESULT_SPECIES
            );
            mObservation.gameSpeciesCode = species.mId;
            mObservation.observationCategorySelected = false;

            onObservationTypeChanged(null);
        }
    }

    private void onActivitySpecimenResult(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final List<ObservationSpecimen> specimens = (List<ObservationSpecimen>) data.getSerializableExtra(
                    ObservationSpecimensActivity.RESULT_SPECIMENS
            );
            mObservation.specimens.clear();
            mObservation.specimens.addAll(specimens);
            onSpecimenAmountChanged(specimens.size());
        }
        updateSelectedSpecies();
    }

    private void onShowSpecimensClicked() {
        final Intent intent = new Intent(getActivity(), ObservationSpecimensActivity.class);
        intent.putExtra(ObservationSpecimensActivity.EXTRA_OBSERVATION, mObservation);
        intent.putExtra(ObservationSpecimensActivity.EXTRA_EDIT_MODE, isEditModeOn());
        chooseSpecimenActivityResultLaunch.launch(intent);
    }

    private void updateSelectedSpecies() {
        updateSpecimensButton();

        final ObservationSpecimenMetadata metadata =
                mObservationMetadataHelper.getMetadataForSpecies(mObservation.gameSpeciesCode);
        final Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);

        if (metadata == null || species == null) {
            // No metadata for this species, can't really do anything useful.
            return;
        }

        mSpeciesButton.setEnabled(isEditModeOn());
        mSpeciesButton.setText(species.mName);
        mSpeciesButton.setIcon(SpeciesInformation.getSpeciesImage(getContext(), species.mId));
        mSpeciesButton.setIconTint(null);

        if (isEditModeOn()) {
            resetDetailViews(metadata);
        } else {
            resetDetailViewsForReadOnlyMode(metadata);
        }
    }

    private void updateSpecimensButton() {
        final boolean hasSpecimens = mObservation.observationType != null
                && mObservation.specimens != null
                && mObservation.specimens.size() > 0;

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

    private void resetSpecimensAndUpdatedSelectedSpecies() {
        if (mObservation.specimens != null) {
            final int newAmount = mObservation.totalSpecimenAmount != null ? mObservation.totalSpecimenAmount : 0;

            mObservation.specimens.clear();
            for (int i = 0; i < newAmount; ++i) {
                mObservation.specimens.add(new ObservationSpecimen());
            }

            mObservation.totalSpecimenAmount = newAmount;
        }

        updateSelectedSpecies();
    }

    private void onSpecimenAmountChanged(final int amount) {
        if (mObservation.specimens == null) {
            mObservation.specimens = new ArrayList<>();
        }

        // Add missing
        for (int i = mObservation.specimens.size(); i < amount; ++i) {
            mObservation.specimens.add(new ObservationSpecimen());
        }

        // Remove extras
        while (mObservation.specimens.size() > amount) {
            mObservation.specimens.remove(mObservation.specimens.size() - 1);
        }

        mObservation.totalSpecimenAmount = mObservation.specimens.size();

        updateSpecimensButton();

        validate();
    }

    private void onObservationTypeChanged(final ObservationType type) {
        mObservation.observationType = type;
        clearFieldsOnObservationTypeChanged();
        resetSpecimensAndUpdatedSelectedSpecies();
    }

    private void clearFieldsOnObservationTypeChanged() {
        mObservation.mooselikeMaleAmount = null;
        mObservation.mooselikeFemaleAmount = null;
        mObservation.mooselikeFemale1CalfAmount = null;
        mObservation.mooselikeFemale2CalfsAmount = null;
        mObservation.mooselikeFemale3CalfsAmount = null;
        mObservation.mooselikeFemale4CalfsAmount = null;
        mObservation.mooselikeCalfAmount = null;
        mObservation.mooselikeUnknownSpecimenAmount = null;

        mObservation.deerHuntingType = null;
        mObservation.deerHuntingTypeDescription = null;

        mObservation.observerName = null;
        mObservation.observerPhoneNumber = null;
        mObservation.officialAdditionalInfo = null;
        mObservation.verifiedByCarnivoreAuthority = null;

        // TODO: Null somewhere else?
        mObservation.inYardDistanceToResidence = null;
        mObservation.litter = null;
        mObservation.pack = null;
    }

    private void resetDetailViews(final ObservationSpecimenMetadata metadata) {
        mDetailsContainer.removeAllViews();
        mDetailsContainer2.removeAllViews();

        final ObservationWithinHuntingCapability deerHuntingCapability = metadata.getDeerHuntingCapability();

        if (metadata.getMooseHuntingCapability() == ObservationWithinHuntingCapability.YES) {
            createWithinMooseHuntingChoice();
            mObservation.mooseOrDeerHuntingCapability = true;
        } else if (deerHuntingCapability == ObservationWithinHuntingCapability.YES
                || mUserInfo.isDeerPilotUser() && deerHuntingCapability == ObservationWithinHuntingCapability.DEER_PILOT) {

            createWithinDeerHuntingChoice();
            mObservation.mooseOrDeerHuntingCapability = true;
        } else {
            mObservation.mooseOrDeerHuntingCapability = false;
        }

        createObservationTypeChoiceView(metadata);

        createDeerHuntingTypeChoiceView(metadata);
        createDeerHuntingTypeDescriptionChoiceView(metadata);

        createAmountChoiceView(metadata);
        createMooselikeChoiceViews(metadata);

        createCarnivoreAuthorityFields(metadata);

        updateSpecimensButton();

        validate();
    }

    private void resetDetailViewsForReadOnlyMode(final ObservationSpecimenMetadata metadata) {
        mDetailsContainer.removeAllViews();
        mDetailsContainer2.removeAllViews();

        if (mObservation.observationCategory == ObservationCategory.MOOSE_HUNTING) {
            createWithinMooseHuntingChoice();
        } else if (mObservation.observationCategory == ObservationCategory.DEER_HUNTING) {
            createWithinDeerHuntingChoice();
        }

        addNonEditableChoiceView(mObservation.observationType, R.string.observation_type, true);

        if (mObservation.observationCategory == ObservationCategory.NORMAL) {
            if (mObservation.totalSpecimenAmount != null) {
                addNonEditableChoiceView(mObservation.totalSpecimenAmount, R.string.harvest_amount, true);
            }
        } else {
            if (mObservation.deerHuntingType != null) {
                addNonEditableChoiceView(mObservation.deerHuntingType, R.string.deer_hunting_type, true);

                if (mObservation.deerHuntingTypeDescription != null) {
                    addNonEditableTextView(
                            mObservation.deerHuntingTypeDescription, R.string.deer_hunting_type_description, true);
                }
            }

            final boolean withinDeerHunting =
                    mObservation.observationCategory == ObservationCategory.DEER_HUNTING;
            createReadOnlyMooselikeChoiceViews(withinDeerHunting);
        }

        // Unlike other fields, metadata is still used to handle carnivore authority fields because of their
        // sensitive nature.
        createCarnivoreAuthorityFields(metadata);

        updateSpecimensButton();
    }

    private void createWithinMooseHuntingChoice() {
        createWithinHuntingChoice(getString(R.string.within_moose_hunting), ObservationCategory.MOOSE_HUNTING);
    }

    private void createWithinDeerHuntingChoice() {
        createWithinHuntingChoice(getString(R.string.within_deer_hunting), ObservationCategory.DEER_HUNTING);
    }

    private void createWithinHuntingChoice(String label, ObservationCategory yesCategory) {
        final String observationCategory = getObservationCategoryChoice();
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), label);
        final List<String> choices = Arrays.asList(getString(R.string.yes), getString(R.string.no));
        choiceView.setChoices(choices, observationCategory, false, (position, type) -> {
            mObservation.observationCategory = (type.equals(getString(R.string.yes))) ? yesCategory : ObservationCategory.NORMAL;
            mObservation.observationType = null;
            mObservation.observationCategorySelected = true;
            resetSpecimensAndUpdatedSelectedSpecies();
        });
        addChoiceView(choiceView, true);
    }

    private String getObservationCategoryChoice() {
        if (mObservation.observationCategorySelected) {
            switch (mObservation.observationCategory) {
                case NORMAL:
                    return getString(R.string.no);
                case DEER_HUNTING: // fall through
                case MOOSE_HUNTING:
                    return getString(R.string.yes);
            }
        }
        return null;
    }

    private void createObservationTypeChoiceView(final ObservationSpecimenMetadata metadata) {
        List<ObservationType> observationTypes = metadata.getObservationTypes(mObservation.observationCategory);

        // Reset observation category to normal if no observation types are found. This may happen when observation
        // category is deprecated for the selected species.
        final ObservationCategory defaultCategory = ObservationCategory.NORMAL;
        if (observationTypes.isEmpty() && mObservation.observationCategory != defaultCategory) {
            mObservation.observationCategory = defaultCategory;
            observationTypes = metadata.getObservationTypes(defaultCategory);

            // Set observation type to null if newly-set category does not match with it.
            if (mObservation.observationType != null && !observationTypes.contains(mObservation.observationType)) {
                mObservation.observationType = null;
            }
        }

        if (mObservation.observationType == null && observationTypes.size() == 1) {
            // Auto-select the only available observation type.
            mObservation.observationType = observationTypes.get(0);
            clearFieldsOnObservationTypeChanged();
        }

        final ArrayList<String> observationTypeStrings = new ArrayList<>(observationTypes.size());
        for (final ObservationType obsType : observationTypes) {
            observationTypeStrings.add(obsType.name());
        }

        final String currentObservationTypeAsString = ObservationType.toString(mObservation.observationType);
        final boolean nullable = observationTypes.isEmpty();

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.observation_type));
        choiceView.setChoices(observationTypeStrings, currentObservationTypeAsString, nullable, (position, type) -> {
            onObservationTypeChanged(ObservationType.fromString(type));
        });
        addChoiceView(choiceView, true);
    }

    private void createDeerHuntingTypeChoiceView(final ObservationSpecimenMetadata metadata) {
        final ObservationContextSensitiveFieldSet fields =
                metadata.findFieldSetByType(mObservation.observationCategory, mObservation.observationType);

        if (fields == null) {
            return;
        }

        if (fields.hasBaseField("deerHuntingType")
                || mUserInfo.isDeerPilotUser() && fields.hasDeerPilotBaseField("deerHuntingType")) {

            final List<String> deerHuntingTypes = new ArrayList<>();
            for (final DeerHuntingType deerHuntingType : DeerHuntingType.values()) {
                deerHuntingTypes.add(deerHuntingType.name());
            }

            final String currentDeerHuntingTypeAsString = DeerHuntingType.toString(mObservation.deerHuntingType);

            final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.deer_hunting_type));
            choiceView.setChoices(deerHuntingTypes, currentDeerHuntingTypeAsString, false, (position, type) -> {
                mObservation.deerHuntingType = DeerHuntingType.fromString(type);
                mObservation.deerHuntingTypeDescription = null;
                resetDetailViews(metadata);
            });

            addChoiceView(choiceView, true);
        }
    }

    private void createDeerHuntingTypeDescriptionChoiceView(final ObservationSpecimenMetadata metadata) {
        final ObservationContextSensitiveFieldSet fields =
                metadata.findFieldSetByType(mObservation.observationCategory, mObservation.observationType);

        if (fields == null || mObservation.deerHuntingType != DeerHuntingType.OTHER) {
            return;
        }

        if (fields.hasBaseField("deerHuntingTypeDescription")
                || mUserInfo.isDeerPilotUser() && fields.hasDeerPilotBaseField("deerHuntingTypeDescription")) {

            final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.deer_hunting_type_description));
            choiceView.setEditTextChoice(mObservation.deerHuntingTypeDescription, text -> {
                mObservation.deerHuntingTypeDescription = text;
                validate();
            });
            choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_TEXT, 1);
            choiceView.setEditTextMaxLength(255);

            addChoiceView(choiceView, true);
        }
    }

    private void createAmountChoiceView(final ObservationSpecimenMetadata metadata) {
        final ObservationContextSensitiveFieldSet fields =
                metadata.findFieldSetByType(mObservation.observationCategory, mObservation.observationType);

        if (fields != null) {
            final boolean isCarnivoreAuthority = mUserInfo.isCarnivoreAuthority();

            if (fields.hasBaseField("amount")
                    || isCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthorityBaseField("amount")) {

                final int existingAmount =
                        mObservation.totalSpecimenAmount != null ? mObservation.totalSpecimenAmount : 0;
                final int validAmount = Math.max(existingAmount, 1);

                onSpecimenAmountChanged(validAmount);

                final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.harvest_amount));
                choiceView.setEditTextMaxLength(3);
                choiceView.setEditTextChoice(Utils.formatInt(validAmount), text -> {
                    final Integer changedAmount = Utils.parseInt(text);
                    if (changedAmount != null) {
                        onSpecimenAmountChanged(changedAmount);
                    }
                });

                addChoiceView(choiceView, true);
            } else {
                removeSpecimens();
            }
        }
    }

    private void createCarnivoreAuthorityFields(final ObservationSpecimenMetadata metadata) {
        final ObservationContextSensitiveFieldSet fields =
                metadata.findFieldSetByType(mObservation.observationCategory, mObservation.observationType);

        if (fields == null) {
            return;
        }

        final boolean isCarnivoreAuthority = mUserInfo.isCarnivoreAuthority();

        if (fields.hasRequiredBaseField("verifiedByCarnivoreAuthority")
                || isCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthorityBaseField(
                        "verifiedByCarnivoreAuthority")) {

            if (mObservation.verifiedByCarnivoreAuthority == null) {
                mObservation.verifiedByCarnivoreAuthority = false;
            }
            createVerifiedByCarnivoreAuthorityField();
        }

        if (fields.hasRequiredBaseField("observerName")
                || isCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthorityBaseField("observerName")) {

            if (mObservation.observerName == null) {
                mObservation.observerName = "";
            }
            createObserverNameField();
        }

        if (fields.hasRequiredBaseField("observerPhoneNumber")
                || isCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthorityBaseField("observerPhoneNumber")) {

            if (mObservation.observerPhoneNumber == null) {
                mObservation.observerPhoneNumber = "";
            }
            createObserverPhoneNumberField();
        }

        if (fields.hasRequiredBaseField("officialAdditionalInfo")
                || isCarnivoreAuthority && fields.hasVoluntaryCarnivoreAuthorityBaseField("officialAdditionalInfo")) {

            if (mObservation.officialAdditionalInfo == null) {
                mObservation.officialAdditionalInfo = "";
            }
            createOfficialAdditionalInfoField();
        }

        createDistanceToResidenceField();
        createLitterField();
        createPackField();
    }

    private void createObserverNameField() {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_observer_name));
        choiceView.setEditTextChoice(mObservation.observerName, text -> {
            mObservation.observerName = text;
            validate();
        });
        choiceView.setEditTextMode(EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME, 1);
        choiceView.setEditTextMaxLength(255);

        addChoiceView(choiceView, true);
    }

    private void createObserverPhoneNumberField() {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_observer_phone_number));
        choiceView.setEditTextChoice(mObservation.observerPhoneNumber, text -> {
            mObservation.observerPhoneNumber = text;
            validate();
        });
        choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_PHONE, 1);
        choiceView.setEditTextMaxLength(255);

        addChoiceView(choiceView, true);
    }

    private void createOfficialAdditionalInfoField() {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_official_additional_info));
        choiceView.setEditTextChoice(mObservation.officialAdditionalInfo, text -> {
            mObservation.officialAdditionalInfo = text;
            validate();
        });
        choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_TEXT, 1);
        choiceView.setEditTextMaxLength(255);

        addChoiceView(choiceView, true);
    }

    private void createVerifiedByCarnivoreAuthorityField() {
        final boolean verified = Boolean.TRUE.equals(mObservation.verifiedByCarnivoreAuthority);

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_verified_by_carnivore_authority));
        choiceView.setChecked(verified, check -> mObservation.verifiedByCarnivoreAuthority = check);

        addChoiceView(choiceView, true);
    }

    private void createDistanceToResidenceField() {
        if (mObservation.inYardDistanceToResidence != null) {
            final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_distance_to_residence));
            choiceView.setTextReadonlyChoice(mObservation.inYardDistanceToResidence.toString() + " m");
            addChoiceView(choiceView, true);
        }
    }

    private void createLitterField() {
        if (Boolean.TRUE.equals(mObservation.litter)) {
            final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_litter));
            choiceView.setTextReadonlyChoice(mObservation.litter ? getString(R.string.yes) : getString(R.string.no));
            addChoiceViewBottom(choiceView, true);
        }
    }

    private void createPackField() {
        if (Boolean.TRUE.equals(mObservation.pack)) {
            final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.tassu_pack));
            choiceView.setTextReadonlyChoice(mObservation.pack ? getString(R.string.yes) : getString(R.string.no));
            addChoiceViewBottom(choiceView, true);
        }
    }

    private void createMooselikeChoiceViews(final ObservationSpecimenMetadata metadata) {
        final ObservationContextSensitiveFieldSet fields =
                metadata.findFieldSetByType(mObservation.observationCategory, mObservation.observationType);

        if (fields == null) {
            return;
        }

        // TODO Repeating code for mooselike fields, could use reflection in here (test with Proguard?)
        final boolean withinDeerHunting =
                mObservation.observationCategory == ObservationCategory.DEER_HUNTING;

        if (fields.hasRequiredBaseField("mooselikeMaleAmount")) {
            if (mObservation.mooselikeMaleAmount == null) {
                mObservation.mooselikeMaleAmount = 0;
            }
            createMooselikeMaleChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeFemaleAmount")) {
            if (mObservation.mooselikeFemaleAmount == null) {
                mObservation.mooselikeFemaleAmount = 0;
            }
            createMooselikeFemaleChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeFemale1CalfAmount")) {
            if (mObservation.mooselikeFemale1CalfAmount == null) {
                mObservation.mooselikeFemale1CalfAmount = 0;
            }
            createMooselikeFemale1CalfChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeFemale2CalfsAmount")) {
            if (mObservation.mooselikeFemale2CalfsAmount == null) {
                mObservation.mooselikeFemale2CalfsAmount = 0;
            }
            createMooselikeFemale2CalfsChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeFemale3CalfsAmount")) {
            if (mObservation.mooselikeFemale3CalfsAmount == null) {
                mObservation.mooselikeFemale3CalfsAmount = 0;
            }
            createMooselikeFemale3CalfsChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeFemale4CalfsAmount")) {
            if (mObservation.mooselikeFemale4CalfsAmount == null) {
                mObservation.mooselikeFemale4CalfsAmount = 0;
            }
            createMooselikeFemale4CalfsChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeCalfAmount")) {
            if (mObservation.mooselikeCalfAmount == null) {
                mObservation.mooselikeCalfAmount = 0;
            }
            createMooselikeCalfChoice(withinDeerHunting);
        }
        if (fields.hasRequiredBaseField("mooselikeUnknownSpecimenAmount")) {
            if (mObservation.mooselikeUnknownSpecimenAmount == null) {
                mObservation.mooselikeUnknownSpecimenAmount = 0;
            }
            createMooselikeUnknownsChoice(withinDeerHunting);
        }
    }

    private void createReadOnlyMooselikeChoiceViews(final boolean withinDeerHunting) {
        if (mObservation.mooselikeMaleAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_male_within_deer_hunting
                    : R.string.mooselike_male;
            addNonEditableChoiceView(mObservation.mooselikeMaleAmount, localizationKey, true);
        }
        if (mObservation.mooselikeFemaleAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_female_within_deer_hunting
                    : R.string.mooselike_female;
            addNonEditableChoiceView(mObservation.mooselikeFemaleAmount, localizationKey, true);
        }
        if (mObservation.mooselikeFemale1CalfAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_female_calf_within_deer_hunting
                    : R.string.mooselike_female_calf;
            addNonEditableChoiceView(mObservation.mooselikeFemale1CalfAmount, localizationKey, true);
        }
        if (mObservation.mooselikeFemale2CalfsAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_female_calfs2_within_deer_hunting
                    : R.string.mooselike_female_calfs2;
            addNonEditableChoiceView(mObservation.mooselikeFemale2CalfsAmount, localizationKey, true);
        }
        if (mObservation.mooselikeFemale3CalfsAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_female_calfs3_within_deer_hunting
                    : R.string.mooselike_female_calfs3;
            addNonEditableChoiceView(mObservation.mooselikeFemale3CalfsAmount, localizationKey, true);
        }
        if (mObservation.mooselikeFemale4CalfsAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_female_calfs4_within_deer_hunting
                    : R.string.mooselike_female_calfs4;
            addNonEditableChoiceView(mObservation.mooselikeFemale4CalfsAmount, localizationKey, true);
        }
        if (mObservation.mooselikeCalfAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_calf_within_deer_hunting
                    : R.string.mooselike_calf;
            addNonEditableChoiceView(mObservation.mooselikeCalfAmount, localizationKey, true);
        }
        if (mObservation.mooselikeUnknownSpecimenAmount != null) {
            @StringRes final int localizationKey = withinDeerHunting
                    ? R.string.mooselike_unknown_within_deer_hunting
                    : R.string.mooselike_unknown;
            addNonEditableChoiceView(mObservation.mooselikeUnknownSpecimenAmount, localizationKey, true);
        }
    }

    private void createMooselikeMaleChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_male_within_deer_hunting
                : R.string.mooselike_male;

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeMaleAmount), false, (position, amount) -> {
            mObservation.mooselikeMaleAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemaleChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_female_within_deer_hunting
                : R.string.mooselike_female;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemaleAmount), false, (position, amount) -> {
            mObservation.mooselikeFemaleAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale1CalfChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_female_calf_within_deer_hunting
                : R.string.mooselike_female_calf;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale1CalfAmount), false, (position, amount) -> {
            mObservation.mooselikeFemale1CalfAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale2CalfsChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_female_calfs2_within_deer_hunting
                : R.string.mooselike_female_calfs2;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale2CalfsAmount), false, (position, amount) -> {
            mObservation.mooselikeFemale2CalfsAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale3CalfsChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_female_calfs3_within_deer_hunting
                : R.string.mooselike_female_calfs3;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale3CalfsAmount), false, (position, amount) -> {
            mObservation.mooselikeFemale3CalfsAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeFemale4CalfsChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_female_calfs4_within_deer_hunting
                : R.string.mooselike_female_calfs4;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeFemale4CalfsAmount), false, (position, amount) -> {
            mObservation.mooselikeFemale4CalfsAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeCalfChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_calf_within_deer_hunting
                : R.string.mooselike_calf;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeCalfAmount), false, (position, amount) -> {
            mObservation.mooselikeCalfAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private void createMooselikeUnknownsChoice(final boolean withinDeerHunting) {
        @StringRes final int localizationKey = withinDeerHunting
                ? R.string.mooselike_unknown_within_deer_hunting
                : R.string.mooselike_unknown;
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(localizationKey));
        choiceView.setChoices(mooselikeRange(), mooselikeValue(mObservation.mooselikeUnknownSpecimenAmount), false, (position, amount) -> {
            mObservation.mooselikeUnknownSpecimenAmount = Utils.parseInt(amount);
            validate();
        });
        addChoiceView(choiceView, true);
    }

    private String mooselikeValue(final Integer value) {
        return "" + value;
    }

    private List<String> mooselikeRange() {
        final ArrayList<String> list = new ArrayList<>(51);
        for (int i = 0; i <= 50; ++i) {
            list.add(i + "");
        }
        return list;
    }

    private void addNonEditableChoiceView(final Object readOnlyValue,
                                          @StringRes final int resId,
                                          final boolean topMargin) {

        final String valueAsString = readOnlyValue != null ? readOnlyValue.toString() : null;
        final List<String> choices = readOnlyValue != null ? singletonList(valueAsString) : emptyList();

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(resId));
        choiceView.setChoices(choices, valueAsString, true, (position, type) -> {});

        addChoiceView(choiceView, topMargin);
    }

    private void addNonEditableTextView(final String readOnlyValue,
                                        @StringRes final int resId,
                                        final boolean topMargin) {

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(resId));
        choiceView.setEditTextChoice(readOnlyValue, text -> {});
        choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_TEXT, 1);
        choiceView.setEditTextMaxLength(255);

        addChoiceView(choiceView, topMargin);
    }

    private void addChoiceView(final ChoiceView<String> choiceView, final boolean topMargin) {
        mDetailsContainer.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(isEditModeOn());
    }

    private void addChoiceViewBottom(final ChoiceView<String> choiceView, final boolean topMargin) {
        mDetailsContainer2.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(isEditModeOn());
    }

    private boolean validate() {
        final boolean valid = mObservationValidator.validate(mObservation);

        final EditActivity activity = (EditActivity) requireActivity();
        activity.setEditValid(valid);

        return valid;
    }

    private boolean isEditModeOn() {
        final EditActivity activity = (EditActivity) requireActivity();
        return isEditable() && activity.isEditModeOn();
    }

    private void onImagesChanged(final List<GameLogImage> images) {
        mObservation.setLocalImages(images);
    }

    @Override
    public void onEditStart() {
        mDateButton.setEnabled(isEditable());
        mTimeButton.setEnabled(isEditable());

        mImageManager.setEditMode(isEditModeOn());

        updateSelectedSpecies();
    }

    @Override
    public void onEditCancel() {
        mDateButton.setEnabled(false);
        mTimeButton.setEnabled(false);

        mImageManager.setEditMode(false);

        resetObservation();

        updateSelectedSpecies();

        mImageManager.updateItems(mObservation.getImages());
    }

    @Override
    public void onEditSave() {
        if (validate()) {
            mObservation.modified = true;
            mObservation.observationSpecVersion = AppConfig.OBSERVATION_SPEC_VERSION;

            // TODO Add progress dialog etc.
            mObservationDatabase.saveObservation(mObservation, new SaveListener() {
                @Override
                public void onSaved(final long localId) {
                    final EditActivity activity = (EditActivity) requireActivity();
                    activity.finishEdit(mObservation.type, mObservation.toDateTime(), localId);
                }

                @Override
                public void onError() {
                    // TODO Report error
                }
            });
        }
    }

    @Override
    public void onDelete() {
        mObservationDatabase.deleteObservation(mObservation, false, new DeleteListener() {
            @Override
            public void onDelete() {
                final EditActivity activity = (EditActivity) requireActivity();
                activity.finishEdit(mObservation.type, mObservation.toDateTime(), -1);
            }

            @Override
            public void onError() {
                // TODO
            }
        });
    }

    @Override
    public void onLocationChanged(final Location location, final String source) {
        mObservation.geoLocation = GeoLocation.fromLocation(location);
        mObservation.geoLocation.source = source;
        validate();
    }

    @Override
    public void onDescriptionChanged(final String description) {
        mObservation.description = description;
        validate();
    }

    @Override
    public Location getLocation() {
        return mObservation.toLocation();
    }

    @Override
    public String getLocationSource() {
        return mObservation.geoLocation != null ? mObservation.geoLocation.source : null;
    }

    @Override
    public String getDescription() {
        return mObservation.description;
    }

    @Override
    public boolean isEditable() {
        return mObservation.canEdit;
    }

    @WorkMessageHandler(ChangeObservationMessage.class)
    public void onChangeObservationMessage(final ChangeObservationMessage message) {
        if (message.localId != mObservation.localId) {
            final Activity activity = requireActivity();

            mObservationDatabase.loadObservation(message.localId, observation -> {
                if (observation != null) {
                    activity.getIntent().putExtra(EditActivity.EXTRA_OBSERVATION, observation);
                    activity.getIntent().putExtra(EditActivity.EXTRA_NEW, false);

                    activity.recreate();
                }
            });
        }
    }

    @Override
    public void viewImage(final GameLogImage image) {
        final FragmentActivity activity = getActivity();

        if (activity != null) {
            final FragmentTransaction fragmentTransaction =
                    getActivity().getSupportFragmentManager().beginTransaction();

            final FullScreenImageDialog dialog = FullScreenImageDialog.newInstance(image);
            dialog.show(fragmentTransaction, FullScreenImageDialog.TAG);
        }
    }

    @Override
    public boolean hasPhotoPermissions() {
        final Context context = requireContext();

        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPhotoPermissions() {
        if (!hasPhotoPermissions()) {
            // Ignore result, user has to tap item again to use granted permission or request it again.
            final Activity activity = requireActivity();
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        }
    }
}
