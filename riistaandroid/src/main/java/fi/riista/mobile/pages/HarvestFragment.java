package fi.riista.mobile.pages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.button.MaterialButton;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.AppConfig;
import fi.riista.mobile.DiaryImageManager;
import fi.riista.mobile.DiaryImageManager.ImageManagerActivityAPI;
import fi.riista.mobile.EntryMapView;
import fi.riista.mobile.LocationClient;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.AntlerInstructionsActivity;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.activity.HarvestActivity;
import fi.riista.mobile.activity.HarvestPermitActivity;
import fi.riista.mobile.activity.HarvestSpecimensActivity;
import fi.riista.mobile.activity.MapViewerActivity;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.database.SpeciesResolver;
import fi.riista.mobile.gamelog.DeerHuntingFeatureAvailability;
import fi.riista.mobile.message.EventUpdateMessage;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.models.Permit;
import fi.riista.mobile.models.PermitSpeciesAmount;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.specimen.GameAge;
import fi.riista.mobile.models.specimen.Gender;
import fi.riista.mobile.models.specimen.MooseAntlersType;
import fi.riista.mobile.models.specimen.MooseFitnessClass;
import fi.riista.mobile.service.harvest.HarvestPersistenceService;
import fi.riista.mobile.service.harvest.HarvestRemoteUpdateResult;
import fi.riista.mobile.ui.ChoiceView;
import fi.riista.mobile.ui.FullScreenImageDialog;
import fi.riista.mobile.ui.HarvestSpecimenView;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.EditUtils;
import fi.riista.mobile.utils.HarvestValidator;
import fi.riista.mobile.utils.PermissionHelper;
import fi.riista.mobile.utils.RequiredHarvestFields;
import fi.riista.mobile.utils.ResourceProvider;
import fi.riista.mobile.utils.Utils;
import fi.riista.mobile.viewmodel.HarvestViewModel;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static fi.riista.mobile.utils.RequiredHarvestFields.Required.VOLUNTARY;
import static fi.riista.mobile.utils.RequiredHarvestFields.Required.YES;
import static fi.riista.mobile.utils.Utils.formatDouble;
import static fi.riista.mobile.utils.Utils.formatInt;
import static fi.riista.mobile.utils.Utils.parseDouble;
import static fi.riista.mobile.utils.Utils.parseInt;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class HarvestFragment extends DetailsPageFragment
        implements LocationListener, OnMapReadyCallback, ImageManagerActivityAPI
        , HarvestSpecimenView.SpecimenDetailsListener, LifecycleObserver {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    HarvestDatabase mHarvestDatabase;

    @Inject
    SpeciesResolver mSpeciesResolver;

    @Inject
    PermitManager mPermitManager;

    @Inject
    HarvestValidator mHarvestValidator;

    @Inject
    HarvestPersistenceService mHarvestPersistenceService;

    @Inject
    DeerHuntingFeatureAvailability mDeerHuntingFeatureAvailability;

    @ViewAnnotations.ViewId(R.id.mapView)
    private EntryMapView mMapView;
    @ViewAnnotations.ViewId(R.id.harvest_coordinates)
    private TextView mCoordinateText;

    @ViewAnnotations.ViewId(R.id.harvest_date)
    private Button mDateButton;
    @ViewAnnotations.ViewId(R.id.harvest_time)
    private Button mTimeButton;

    private MenuItem mEditItem;
    private MenuItem mDeleteItem;

    @ViewAnnotations.ViewId(R.id.btn_select_species)
    private MaterialButton mSpeciesButton;
    @ViewAnnotations.ViewId(R.id.harvest_amount_input)
    private EditText mAmountInput;
    @ViewAnnotations.ViewId(R.id.harvest_description)
    private EditText mDescriptionEdit;
    @ViewAnnotations.ViewId(R.id.harvest_specimens_button)
    private Button mSpecimenButton;
    @ViewAnnotations.ViewId(R.id.harvest_specimen_details)
    private HarvestSpecimenView mSpecimenDetails;
    @ViewAnnotations.ViewId(R.id.layout_moose_details)
    private LinearLayout mSpecimenExtensionFieldsLayout;
    @ViewAnnotations.ViewId(R.id.harvest_state)
    private TextView mHarvestStateLabel;
    @ViewAnnotations.ViewId(R.id.layout_species_extra)
    private LinearLayout mSpeciesExtraLayout;

    @ViewAnnotations.ViewId(R.id.harvest_permit_checkbox)
    private AppCompatCheckBox mPermitCheckbox;
    @ViewAnnotations.ViewId(R.id.harvest_permit_number)
    private Button mPermitNumber;
    @ViewAnnotations.ViewId(R.id.permit_required_indicator)
    private View mPermitRequiredNote;
    @ViewAnnotations.ViewId(R.id.permit_required_divider)
    private View mPermitRequiredDivider;

    @ViewAnnotations.ViewId(R.id.harvest_button_area)
    private ViewGroup mButtonView;
    @ViewAnnotations.ViewId(R.id.save_button)
    private Button mSubmitButton;
    @ViewAnnotations.ViewId(R.id.cancel_button)
    private Button mDismissButton;

    @ViewAnnotations.ViewId(R.id.harvest_busy_overlay)
    private View mBusyOverlay;

    private Group mAmountGroup;

    // Location service related
    private LocationClient mLocationClient;
    private DiaryImageManager mDiaryImageManager = null;

    private HarvestViewModel mModel;

    private boolean mImagesInitialized = false;
    private boolean mSendUpdateInProgress = false;

    private final ActivityResultLauncher<Intent> chooseSpeciesActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivitySpeciesResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> chooseSpecimenActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivitySpecimensResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> locationActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivityLocationResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> harvestPermitActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onActivityPermitResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> selectPhotoActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> mDiaryImageManager.handleSelectPhotoResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> captureImageActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> mDiaryImageManager.handleCaptureImageResult(result.getResultCode(), result.getData())
    );

    @ViewAnnotations.ViewOnClick(R.id.save_button)
    private void onSaveClick(final View view) {
        trySaveHarvest();
    }

    @ViewAnnotations.ViewOnClick(R.id.cancel_button)
    private void onCancelClick(final View view) {
        final Activity activity = requireActivity();

        activity.finish();

        if (mModel.isLocallyPersisted()) {
            // Reload activity and harvest entry from local database.
            activity.startActivity(activity.getIntent());
        }
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        requireActivity().getLifecycle().addObserver(this);
        super.onAttach(context);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreated(){
        requireActivity().getLifecycle().removeObserver(this);

        // HarvestActivity inits the model on its onCreate method, and we can't use model before that,
        // so next lines have to be on ON_CREATE lifecycle event
        mModel = new ViewModelProvider(requireActivity(), mViewModelFactory).get(HarvestViewModel.class);

        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        mModel.getDateTime().observe(lifecycleOwner, this::onDateTimeChanged);
        mModel.getSpecies().observe(lifecycleOwner, this::onSpeciesChanged);
        mModel.getAmount().observe(lifecycleOwner, this::onAmountChanged);
        mModel.getDescription().observe(lifecycleOwner, this::onDescriptionChanged);
        mModel.getLocation().observe(lifecycleOwner, this::onHarvestLocationChanged);
        mModel.getCoordinates().observe(lifecycleOwner, this::onCoordinatesChanged);

        mModel.getSpecimens().observe(lifecycleOwner, this::refreshSpecimenDetailsState);

        mModel.getDeerHuntingType().observe(lifecycleOwner, s -> refreshSpeciesExtraFields(mModel.getSpecies().getValue()));
        mModel.getGreySealHuntingMethod().observe(lifecycleOwner, s -> refreshSpeciesExtraFields(mModel.getSpecies().getValue()));
        mModel.getFeedingPlace().observe(lifecycleOwner, aBoolean -> refreshSpeciesExtraFields(mModel.getSpecies().getValue()));
        mModel.getTaigaBeanGoose().observe(lifecycleOwner, aBoolean -> refreshSpeciesExtraFields(mModel.getSpecies().getValue()));

        mModel.getPermitNumberAndType().observe(lifecycleOwner, this::onPermitNumberAndTypeChanged);

        mModel.getHarvestState().observe(lifecycleOwner, this::onHarvestStateChanged);

        mModel.getSpecimenMandatoryGender().observe(lifecycleOwner, s -> refreshRequiredIndicators());
        mModel.getSpecimenMandatoryAge().observe(lifecycleOwner, s -> refreshRequiredIndicators());
        mModel.getSpecimenMandatoryWeight().observe(lifecycleOwner, s -> refreshRequiredIndicators());

        mModel.getEditEnabled().observe(lifecycleOwner, this::onEditEnabledChanged);
        mModel.getHasAllRequiredFields().observe(lifecycleOwner, this::onHasAllRequiredFieldsChanged);

        mModel.getPermitNumberMandatory().observe(lifecycleOwner, this::onPermitNumberMandatoryChanged);
        mModel.getGreySealHuntingMethodRequired().observe(lifecycleOwner, this::onSpeciesExtraRequirementsChanged);
        mModel.getFeedingPlaceRequired().observe(lifecycleOwner, this::onSpeciesExtraRequirementsChanged);
        mModel.getTaigaBeanGooseRequired().observe(lifecycleOwner, this::onSpeciesExtraRequirementsChanged);

        setupDismissButton();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_harvest, container, false);

        ViewAnnotations.apply(this, view);

        mAmountGroup = view.findViewById(R.id.harvest_amount_group);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.setup(inflater.getContext(), false, true);
        mMapView.getMapAsync(this);

        if (!mImagesInitialized) {
            mDiaryImageManager = new DiaryImageManager(requireActivity(), this);
        }

        mBusyOverlay.setOnTouchListener((v, event) -> {
            // Just consume all events to prevent other controls form getting events
            v.performClick();
            return true;
        });

        setupView(view);

        mSpecimenDetails.setListener(this);

        return view;
    }


    private void setupView(final View view) {
        setupEditTools();
        setupDescriptionEdit();
        setupSpeciesButton();
        setupSpecimenButton();
        setupAmountInput();
        setupPermitNumber();
    }

    private void setupEditTools() {
        mDateButton.setOnClickListener(v -> {
            if (mModel.notLocked()) {
                final DateTime initial = new DateTime(mModel.getDateTime().getValue());

                EditUtils.showDateDialog(requireActivity(), initial, result -> {
                    final Calendar selected = Calendar.getInstance();
                    selected.set(result.getYear(), result.getMonthOfYear() - 1, result.getDayOfMonth(),
                            result.getHourOfDay(), result.getMinuteOfHour());

                    mModel.selectDateTime(selected);
                });
            }
        });
        mTimeButton.setOnClickListener(v -> {
            if (mModel.notLocked()) {
                final DateTime initial = new DateTime(mModel.getDateTime().getValue());

                EditUtils.showTimeDialog(requireActivity(), initial, result -> {
                    final Calendar selected = Calendar.getInstance();
                    selected.set(result.getYear(), result.getMonthOfYear() - 1, result.getDayOfMonth(),
                            result.getHourOfDay(), result.getMinuteOfHour());

                    mModel.selectDateTime(selected);
                });
            }
        });
    }

    private void setupDescriptionEdit() {
        mDescriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mModel.setDescription(mDescriptionEdit.getText().toString());
            }
        });
    }

    private void setupSpeciesButton() {
        mSpeciesButton.setOnClickListener(v -> {
            if (isEditEnabledAndHarvestNotLocked()) {
                EditUtils.showSpeciesCategoryDialog(this, chooseSpeciesActivityResultLaunch);
            }
        });
    }

    private void setupSpecimenButton() {
        mSpecimenButton.setOnClickListener(v -> {
            final Integer amount = mModel.getAmount().getValue();
            final Species species = mModel.getSpecies().getValue();
            final List<HarvestSpecimen> specimens = mModel.getSpecimens().getValue();

            final boolean isAmountValid = amount != null && amount >= 1 && amount <= GameLog.SPECIMEN_DETAILS_MAX;

            if (!isAmountValid || species == null || specimens == null) {
                Utils.LogMessage("Cannot invoke specimens activity because of missing data.");
                return;
            }

            final Intent intent = new Intent(getActivity(), HarvestSpecimensActivity.class);
            intent.putExtra(HarvestSpecimensActivity.EXTRA_SPECIES_ID, species.mId);
            intent.putExtra(HarvestSpecimensActivity.EXTRA_AMOUNT, amount);
            intent.putExtra(HarvestSpecimensActivity.EXTRA_SPECIMENS, HarvestSpecimen.withEmptyRemoved(specimens));
            intent.putExtra(HarvestSpecimensActivity.EXTRA_EDIT_MODE, isEditEnabledAndHarvestNotLocked());
            chooseSpecimenActivityResultLaunch.launch(intent);
        });
    }

    private void setupAmountInput() {
        mAmountInput.addTextChangedListener(new TextWatcher() {
            String previousValue = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                final String newValue = s.toString();

                if (newValue.equals(previousValue)) {
                    // Prevent infinite loops
                    return;
                }

                boolean illegalValue = false;

                if (s.length() > 0) {
                    try {
                        final int newValueAsInt = Integer.parseInt(newValue);

                        if (GameHarvest.isAmountWithinLegalRange(newValueAsInt)) {
                            mModel.setAmount(newValueAsInt);
                        } else {
                            illegalValue = true;
                        }
                    } catch (final IllegalArgumentException e) {
                        illegalValue = true;
                    }
                } else {
                    mModel.setAmount(null);
                }

                if (illegalValue) {
                    // Re-triggers afterTextChanged (setting null to view model).
                    mAmountInput.setText(previousValue);
                } else {
                    this.previousValue = newValue;
                }
            }
        });
        mAmountInput.setOnFocusChangeListener((v, hasFocus) -> {
            // Restore amount if empty on focus lost.
            if (!hasFocus) {
                if (mAmountInput.getText().length() == 0) {
                    final Integer amount = mModel.getAmount().getValue();
                    mAmountInput.setText(amount != null ? amount.toString() : "");
                }
            }
        });
        mAmountInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mAmountInput.clearFocus();
                hideKeyboard();

                return true;
            }
            return false;
        });
    }

    private String getPermitNumber() {
        final Pair<String, String> permitNumberAndType = mModel.getPermitNumberAndType().getValue();
        return permitNumberAndType == null ? null : permitNumberAndType.first;
    }

    private boolean isPermitNumberPresent() {
        return !TextUtils.isEmpty(getPermitNumber());
    }

    private void setupPermitNumber() {
        mPermitCheckbox.setOnClickListener(v -> {
            if (((AppCompatCheckBox) v).isChecked()) {
                mPermitCheckbox.setChecked(false);
                navigateToPermitList();
            } else {
                mModel.selectPermitNumber(null, null, null);
            }
        });

        mPermitNumber.setOnClickListener(v -> {
            if (Utils.isTrue(mModel.getEditEnabled())) {
                navigateToPermitList();
            }
        });
    }

    private void navigateToPermitList() {
        final Intent intent = new Intent(getActivity(), HarvestPermitActivity.class);
        final String permitNumber = getPermitNumber();

        if (!TextUtils.isEmpty(permitNumber)) {
            intent.putExtra(HarvestPermitActivity.EXTRA_PERMIT_NUMBER, permitNumber);
        }

        harvestPermitActivityResultLaunch.launch(intent);
    }

    private void onDateTimeChanged(@Nullable final Calendar calendar) {
        final DateTime dateTime = new DateTime(calendar);
        mDateButton.setText(dateTime.toString(EditUtils.DATE_FORMAT));
        mTimeButton.setText(dateTime.toString(EditUtils.TIME_FORMAT));

        refreshSpecimenDetailsState(mModel.getSpecimens().getValue());
    }

    private void onSpeciesChanged(@Nullable final Species species) {
        if (species != null) {
            mSpeciesButton.setText(species.mName);
            mSpeciesButton.setIcon(SpeciesInformation.getSpeciesImage(getContext(), species.mId));
            mSpeciesButton.setIconTint(null);

            refreshSpecimenDetailsState(mModel.getSpecimens().getValue());
            refreshSpeciesExtraState(Utils.isTrue(mModel.getEditEnabled()));
            refreshAmountInputState(Utils.isTrue(mModel.getEditEnabled()));
        }
    }

    private void onAmountChanged(@Nullable final Integer integer) {
        if (!TextUtils.equals("" + integer, mAmountInput.getText())) {
            mAmountInput.setText(integer != null ? integer.toString() : "");
        }
        refreshSpecimenDetailsState(mModel.getSpecimens().getValue());
    }

    private void onDescriptionChanged(@Nullable final String s) {
        if (!TextUtils.equals(s, mDescriptionEdit.getText())) {
            mDescriptionEdit.setText(s);
        }
    }

    private void onHarvestLocationChanged(@Nullable final Location location) {
        if (location != null && isAdded()) {
            mMapView.onLocationUpdated(location);
        }
    }

    private void onCoordinatesChanged(@Nullable final Pair<Long, Long> coordinates) {
        final String textFormat = getString(R.string.map_coordinates);
        final String coordinatesText;

        if (coordinates != null) {
            coordinatesText = format(textFormat,
                    coordinates.first != null ? coordinates.first.toString() : "",
                    coordinates.second != null ? coordinates.second.toString() : "");
        } else {
            coordinatesText = format(textFormat, "", "");
        }

        mCoordinateText.setText(coordinatesText);
    }

    private void onPermitNumberAndTypeChanged(@Nullable final Pair<String, String> permitNumberAndType) {
        final String permitNumber = permitNumberAndType.first;

        if (!TextUtils.isEmpty(permitNumber)) {
            mPermitCheckbox.setChecked(true);

            final String permitType = permitNumberAndType.second;
            mPermitNumber.setText(format("%s\n%s", permitType, permitNumber));
            mPermitNumber.setVisibility(View.VISIBLE);
        } else {
            mPermitCheckbox.setChecked(false);

            mPermitNumber.setVisibility(View.GONE);
            mPermitNumber.setText(null);
        }
    }

    private void refreshSpeciesExtraFields(@Nullable final Species species) {
        mSpeciesExtraLayout.removeAllViews();

        if (species != null) {
            final RequiredHarvestFields.Required deerHuntingMethodRequired =
                    mModel.getDeerHuntingTypeRequired().getValue();
            final RequiredHarvestFields.Required greySealHuntingMethodRequired =
                    mModel.getGreySealHuntingMethodRequired().getValue();
            final RequiredHarvestFields.Required feedingPlaceRequired = mModel.getFeedingPlaceRequired().getValue();
            final RequiredHarvestFields.Required taigaBeanGooseRequired = mModel.getTaigaBeanGooseRequired().getValue();

            if (YES == deerHuntingMethodRequired || VOLUNTARY == deerHuntingMethodRequired) {
                final DeerHuntingType deerHuntingType = mModel.getDeerHuntingType().getValue();

                createDeerHuntingTypeChoice(deerHuntingType);
                createDeerHuntingTypeDescriptionChoiceView(
                        deerHuntingType, mModel.getDeerHuntingTypeDescription().getValue());

            } else if (YES == greySealHuntingMethodRequired || VOLUNTARY == greySealHuntingMethodRequired) {
                createGreySealHuntingMethodChoice(mModel.getGreySealHuntingMethod().getValue());
            } else if (YES == feedingPlaceRequired || VOLUNTARY == feedingPlaceRequired) {
                createFeedingPlaceChoice(mModel.getFeedingPlace().getValue());
            } else if (YES == taigaBeanGooseRequired || VOLUNTARY == taigaBeanGooseRequired) {
                createTaigaBeanGooseChoice(mModel.getTaigaBeanGoose().getValue());
            }
        }

        mSpeciesExtraLayout.setEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private void onHarvestStateChanged(@Nullable final String state) {
        final ResourceProvider resourceProvider = new ResourceProvider(requireContext());

        int trafficLightColor = Color.TRANSPARENT;
        String stateText = null;

        if (GameHarvest.HARVEST_PROPOSED.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.harvest_proposed);
            stateText = getString(R.string.harvest_proposed);
        } else if (GameHarvest.HARVEST_SENT_FOR_APPROVAL.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.harvest_sent_for_approval);
            stateText = getString(R.string.harvest_sent_for_approval);
        } else if (GameHarvest.HARVEST_APPROVED.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.harvest_approved);
            stateText = getString(R.string.harvest_approved);
        } else if (GameHarvest.HARVEST_REJECTED.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.harvest_rejected);
            stateText = getString(R.string.harvest_rejected);
        } else if (GameHarvest.HARVEST_CREATEREPORT.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.harvest_create_report);
            stateText = getString(R.string.harvest_create_report);
        } else if (GameHarvest.PERMIT_PROPOSED.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.permit_proposed);
            stateText = getString(R.string.harvest_permit_proposed);
        } else if (GameHarvest.PERMIT_ACCEPTED.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.permit_accepted);
            stateText = getString(R.string.harvest_permit_accepted);
        } else if (GameHarvest.PERMIT_REJECTED.equals(state)) {
            trafficLightColor = resourceProvider.getColor(R.color.permit_rejected);
            stateText = getString(R.string.harvest_permit_rejected);
        }

        if (trafficLightColor != Color.TRANSPARENT && !TextUtils.isEmpty(stateText)) {
            final Drawable circle = resourceProvider.getDrawable(R.drawable.circle);
            circle.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);

            mHarvestStateLabel.setText(stateText);
            mHarvestStateLabel.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
            mHarvestStateLabel.setVisibility(View.VISIBLE);
        } else {
            mHarvestStateLabel.setVisibility(View.GONE);
        }
    }

    private void onEditEnabledChanged(@Nullable final Boolean aBoolean) {
        final boolean isEdit = Boolean.TRUE.equals(aBoolean);

        mButtonView.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        mSubmitButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        mDismissButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        mDescriptionEdit.setEnabled(isEdit);
        mDescriptionEdit.setHint(isEdit ? getString(R.string.add_description) : null);
        refreshAmountInputState(isEdit);
        refreshEditToolsState();

        if (mModel.notLocked()) {
            refreshExtensionFieldsInputState(isEdit);
            refreshSpeciesExtraState(isEdit);
            mSpeciesButton.setEnabled(isEdit);
            mSpecimenDetails.setEnabled(isEdit);
            mPermitCheckbox.setEnabled(isEdit);
            mPermitNumber.setEnabled(isEdit);
        } else {
            refreshExtensionFieldsInputState(false);
            refreshSpeciesExtraState(false);
            mSpeciesButton.setEnabled(false);
            mSpecimenDetails.setEnabled(false);
            mPermitCheckbox.setEnabled(false);
            mPermitNumber.setEnabled(false);
        }

        final boolean permitNumberPresent = isPermitNumberPresent();

        mPermitNumber.setVisibility(permitNumberPresent ? View.VISIBLE : View.GONE);
        mPermitCheckbox.setVisibility(isEdit || permitNumberPresent ? View.VISIBLE : View.GONE);
        mPermitRequiredDivider.setVisibility(isEdit || permitNumberPresent ? View.VISIBLE : View.GONE);

        mDiaryImageManager.setEditMode(isEdit);
    }

    private void onHasAllRequiredFieldsChanged(final Boolean aBoolean) {
        mSubmitButton.setEnabled(aBoolean && Utils.isTrue(mModel.getEditEnabled()));
    }

    private void onPermitNumberMandatoryChanged(final RequiredHarvestFields.Required required) {
        mPermitRequiredNote.setVisibility(YES == required && !isPermitNumberPresent() ? View.VISIBLE : View.GONE);
    }

    private void onSpeciesExtraRequirementsChanged(final RequiredHarvestFields.Required r) {
        refreshSpeciesExtraFields(mModel.getSpecies().getValue());
    }

    private void setupDismissButton() {
        if (mModel.isLocallyPersisted()) {
            mDismissButton.setText(R.string.cancel);
        }
    }

    private void trySaveHarvest() {
        if (mModel.getSpecies().getValue() != null && !mSendUpdateInProgress) {
            setSendStatus(true);

            final GameHarvest harvest = mModel.getResultHarvest();

            harvest.mHarvestSpecVersion = AppConfig.HARVEST_SPEC_VERSION;

            if (!mHarvestValidator.validate(harvest)) {
                Utils.LogMessage("Failed to validate harvest");

                setSendStatus(false);
                return;
            }

            harvest.mImages = mDiaryImageManager.getImages();

            final Calendar calendar = requireNonNull(mModel.getDateTime().getValue());
            final int huntingYear = DateTimeUtils.getHuntingYearForCalendar(calendar);

            if (!harvest.isPersistedLocally()) {

                // Persist new harvest locally.
                mHarvestPersistenceService.localPersistHarvest(harvest, () -> onHarvestSaved(harvest, huntingYear));

            } else if (harvest.mId <= 0) {

                // Update locally created harvest and persist it remotely by sending it to backend.
                mHarvestPersistenceService
                        .remotePersistHarvest(getWorkContext(), harvest, () -> onHarvestSaved(harvest, huntingYear));

            } else {
                mHarvestPersistenceService.remoteUpdateHarvest(getWorkContext(), harvest, new HarvestRemoteUpdateResult() {
                    @Override
                    protected void onSuccess() {
                        onHarvestSaved(harvest, huntingYear);
                    }

                    @Override
                    protected void onOutdated() {
                        setSendStatus(false);

                        if (getActivity() != null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.eventoutdated))
                                    .show();
                        }
                    }

                    @Override
                    protected void onFailure() {
                        setSendStatus(false);

                        if (getActivity() != null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.eventeditfailed))
                                    .show();
                        }
                    }
                });
            }
        }
    }

    private void onHarvestSaved(final GameHarvest harvest, final int huntingYear) {
        setSendStatus(false);

        // TODO This null-check might be a relic of the past.
        if (getActivity() != null) {
            getWorkContext().sendGlobalMessage(new EventUpdateMessage(GameLog.TYPE_HARVEST, harvest.mLocalId, huntingYear));

            finishEditAfterSave();
        }
    }

    private void finishEditAfterSave() {
        final Intent result = new Intent();
        result.putExtra(HarvestActivity.RESULT_DID_SAVE, true);

        final BaseActivity activity = (BaseActivity) requireActivity();
        activity.setResult(Activity.RESULT_OK, result);
        activity.finish();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        mMapView.onResume();
        mMapView.setMapTileType(AppPreferences.getMapTileSource(getContext()));
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    private boolean isEditEnabledAndHarvestNotLocked() {
        final boolean inEditMode = Utils.isTrue(mModel.getEditEnabled());
        return inEditMode && mModel.notLocked();
    }

    private void refreshEditToolsState() {
        final boolean enable = !mSendUpdateInProgress;

        final boolean inEditMode = Utils.isTrue(mModel.getEditEnabled());
        final boolean notLocked = mModel.notLocked();
        final boolean editEnabled = inEditMode && notLocked;

        mDateButton.setEnabled(editEnabled && enable);
        mTimeButton.setEnabled(editEnabled && enable);

        if (mEditItem != null) {
            mEditItem.setEnabled(enable);
            mEditItem.setVisible(!inEditMode);
        }

        if (mDeleteItem != null) {
            mDeleteItem.setEnabled(enable);
            mDeleteItem.setVisible(notLocked && mModel.isLocallyPersisted());
        }
    }

    private void showConfirmRemoveEvent() {
        EditUtils.showDeleteDialog(requireActivity(), () -> mHarvestPersistenceService
                .remoteDeleteHarvest(getWorkContext(), mModel.getResultHarvest(), new HarvestRemoteUpdateResult() {
                    @Override
                    protected void onSuccess() {
                        finishEditAfterSave();
                    }

                    @Override
                    protected void onFailure() {
                        if (getActivity() != null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.eventeditfailed))
                                    .show();
                        }
                    }
                }));
    }

    @Override
    public void onStart() {
        super.onStart();

        setViewTitle(getString(R.string.harvest));
        setSendStatus(false);

        final List<GameLogImage> harvestImages = mModel.getImages();

        if (mModel.isLocallyPersisted()) {
            // TODO Should pruning be done in case of a new harvest as well?
            mHarvestDatabase.pruneNonexistentLocalImages(getActivity(), mModel.requireLocalId(), harvestImages);
        }

        if (!mImagesInitialized) {
            mDiaryImageManager.setItems(harvestImages);
        }

        final View view = requireView();
        mDiaryImageManager.setup(
                view.findViewById(R.id.harvest_image),
                captureImageActivityResultLaunch,
                selectPhotoActivityResultLaunch
        );
        mImagesInitialized = true;

        if (mMapView != null) {
            mMapView.onStart();
        }
    }

    @Override
    public void onStop() {
        hideKeyboard();

        if (mLocationClient != null) {
            mLocationClient.removeListener(this);
            mLocationClient = null;
        }

        if (mMapView != null) {
            mMapView.onStop();
        }

        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit, menu);

        mEditItem = menu.findItem(R.id.item_edit);
        mDeleteItem = menu.findItem(R.id.item_delete);

        refreshEditToolsState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.item_edit:
                mModel.setEditEnabled(true);
                break;
            case R.id.item_delete:
                if (mModel.notLocked()) {
                    showConfirmRemoveEvent();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void hideKeyboard() {
        final Activity activity = requireActivity();
        final InputMethodManager inputManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        final View view = activity.getCurrentFocus();

        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Prevent UI interactions during network operations.
     *
     * @param isBusy Is operation ongoing
     */
    private void setSendStatus(final boolean isBusy) {
        mSendUpdateInProgress = isBusy;

        // Use overlay to hide UI controls
        mBusyOverlay.setVisibility(mSendUpdateInProgress ? View.VISIBLE : View.GONE);

        refreshEditToolsState();
    }

    //
    // Map and location related methods
    //

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(final GoogleMap map) {
        final String locationSource = mModel.getLocationSource().getValue();

        // New entry, track and save GPS location.
        if (!mModel.isLocallyPersisted()
                && (locationSource == null || locationSource.equals(GameLog.LOCATION_SOURCE_GPS))) {

            mMapView.setShowInfoWindow(true);
            mMapView.setShowAccuracy(true);

            if (getActivity() != null) {
                mLocationClient = ((BaseActivity) getActivity()).getLocationClient();
                mLocationClient.addListener(this);
            }
        }
        mMapView.setMapExternalId(
                AppPreferences.getSelectedClubAreaMapId(requireContext()),
                AppPreferences.getInvertMapColors(requireContext())
        );

        map.setOnMapClickListener(arg0 -> viewMap());
        map.setOnMarkerClickListener(marker -> {
            viewMap();
            return true;
        });
    }

    @Override
    public void onLocationChanged(final Location location) {
        // Don't update position with GPS position if pre-existing entry or location has already been set.
        if (getView() != null && !mModel.isLocallyPersisted()
                && (mModel.getLocationSource().getValue() == null || GameLog.LOCATION_SOURCE_GPS.equals(mModel.getLocationSource().getValue()))) {

            mModel.selectLocationGps(location);
        }
    }

    private void viewMap() {
        final Location location = mModel.getLocation().getValue();
        final boolean nonDefaultLocation =
                location != null && (location.getLatitude() != 0 || location.getLongitude() != 0);

        final Intent intent = new Intent(getActivity(), MapViewerActivity.class);
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, isEditEnabledAndHarvestNotLocked());
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, nonDefaultLocation ? location : null);
        intent.putExtra(MapViewerActivity.EXTRA_NEW, !mModel.isLocallyPersisted());
        intent.putExtra(MapViewerActivity.EXTRA_LOCATION_SOURCE, mModel.getLocationSource().getValue());
        locationActivityResultLaunch.launch(intent);
    }

    private void createDeerHuntingTypeChoice(@Nullable final DeerHuntingType huntingType) {
        final DeerHuntingType[] typeArr = DeerHuntingType.values();
        final ArrayList<String> allHuntingTypes = new ArrayList<>(typeArr.length);

        for (final DeerHuntingType type : typeArr) {
            allHuntingTypes.add(type.toString());
        }

        final String currentHuntingTypeAsString = DeerHuntingType.toString(huntingType);

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.deer_hunting_type));
        choiceView.setChoices(allHuntingTypes, currentHuntingTypeAsString, true, (position, value) -> {
            mModel.selectDeerHuntingType(DeerHuntingType.fromString(value));
            mModel.setDeerHuntingTypeDescription(null);
        });

        mSpeciesExtraLayout.addView(choiceView);
        choiceView.setChoiceEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private void createDeerHuntingTypeDescriptionChoiceView(@Nullable final DeerHuntingType huntingType,
                                                            @Nullable final String huntingTypeDescription) {

        if (huntingType != DeerHuntingType.OTHER) {
            return;
        }

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.deer_hunting_type_description));
        choiceView.setEditTextChoice(huntingTypeDescription, text -> {
            mModel.setDeerHuntingTypeDescription(text);
        });
        choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_TEXT, 1);
        choiceView.setEditTextMaxLength(255);

        mSpeciesExtraLayout.addView(choiceView);
        choiceView.setChoiceEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private void createGreySealHuntingMethodChoice(@Nullable final GreySealHuntingMethod huntingMethod) {
        final GreySealHuntingMethod[] methodArr = GreySealHuntingMethod.values();
        final ArrayList<String> allHuntingMethods = new ArrayList<>(methodArr.length);

        for (final GreySealHuntingMethod method : methodArr) {
            allHuntingMethods.add(method.toString());
        }

        final String currentHuntingMethodAsString = GreySealHuntingMethod.toString(huntingMethod);

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.harvest_hunting_type_title));
        choiceView.setChoices(allHuntingMethods, currentHuntingMethodAsString, true, (position, value) -> {
            mModel.selectGreySealHuntingMethod(GreySealHuntingMethod.fromString(value));
        });

        mSpeciesExtraLayout.addView(choiceView);
        choiceView.setChoiceEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private void createFeedingPlaceChoice(@Nullable final Boolean b) {
        final List<String> selectableValues = Arrays.asList(getString(R.string.no), getString(R.string.yes));
        final String selected = parseBooleanFieldToYesNoValue(b);
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.harvest_feeding_place_title));

        choiceView.setChoices(selectableValues,
                selected,
                true,
                (position, value) -> mModel.selectFeedingPlace(parseYesNoFieldToBoolean(value)));

        mSpeciesExtraLayout.addView(choiceView);
        choiceView.setChoiceEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private void createTaigaBeanGooseChoice(@Nullable final Boolean b) {
        final List<String> selectableValues = Arrays.asList(getString(R.string.no), getString(R.string.yes));
        final String selected = parseBooleanFieldToYesNoValue(b);
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.harvest_taiga_bean_goose_title));

        choiceView.setChoices(selectableValues,
                selected,
                true,
                (position, value) -> mModel.selectTaigaBeanGoose(parseYesNoFieldToBoolean(value)));

        mSpeciesExtraLayout.addView(choiceView);
        choiceView.setChoiceEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private String parseBooleanFieldToYesNoValue(final Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? getString(R.string.yes) : getString(R.string.no);
    }

    private Boolean parseYesNoFieldToBoolean(final String value) {
        if (value != null) {
            if (value.equals(getString(R.string.yes))) {
                return Boolean.TRUE;
            }
            if (value.equals(getString(R.string.no))) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    //
    // Moose/deer functionality
    //

    @Override
    public void onSpecimenChanged() {
        mModel.refreshSubmitReadyState();
        refreshSpecimenExtensionFields();
    }

    private void refreshSpecimenDetailsState(@NonNull final List<HarvestSpecimen> specimens) {
        requireNonNull(specimens);

        final Integer amount = mModel.getAmount().getValue();
        final Species selectedSpecies = mModel.getSpecies().getValue();
        final String permitNumber = getPermitNumber();

        if (selectedSpecies != null) {
            if (selectedSpecies.mMultipleSpecimenAllowedOnHarvests) {
                mSpecimenDetails.setVisibility(View.GONE);
                mSpecimenButton.setVisibility(amount != null && amount > 0 && amount <= GameLog.SPECIMEN_DETAILS_MAX ? View.VISIBLE : View.GONE);
            } else {
                mSpecimenDetails.setVisibility(View.VISIBLE);
                // Special case where no selection means 'unknown' value.
                mSpecimenDetails.setSelectionsClearable(selectedSpecies.mId == SpeciesInformation.GREY_SEAL_ID);
                mSpecimenButton.setVisibility(View.GONE);

                // HarvestViewModel guarantees that there always is at least one specimen.
                final HarvestSpecimen specimen1 = specimens.get(0);

                mSpecimenDetails.setupWithSpecimen(
                        specimen1, selectedSpecies.mId, isGenericWeightFieldVisible(selectedSpecies.mId));

                if (!TextUtils.isEmpty(permitNumber)) {
                    final Permit permit = mPermitManager.getPermit(permitNumber);
                    final PermitSpeciesAmount speciesAmount =
                            mPermitManager.getSpeciesAmountFromPermit(permit, selectedSpecies.mId);

                    if (speciesAmount != null) {
                        mSpecimenDetails.setRequiredFields(
                                speciesAmount.getAgeRequired(),
                                speciesAmount.getGenderRequired(),
                                speciesAmount.getWeightRequired());
                    }
                } else {
                    refreshRequiredIndicators();
                }
            }

        } else {
            mSpecimenDetails.setVisibility(View.GONE);
            mSpecimenButton.setVisibility(View.GONE);
        }

        refreshSpecimenExtensionFields();
    }

    private boolean isGenericWeightFieldVisible(final int speciesCode) {
        if (isAntlerFields2020Enabled()) {
            return speciesCode != SpeciesInformation.FALLOW_DEER_ID
                    && speciesCode != SpeciesInformation.MOOSE_ID
                    && speciesCode != SpeciesInformation.ROE_DEER_ID
                    && speciesCode != SpeciesInformation.WHITE_TAILED_DEER_ID
                    && speciesCode != SpeciesInformation.WILD_FOREST_DEER_ID
                    && speciesCode != SpeciesInformation.WILD_BOAR_ID;
        }

        return !SpeciesInformation.isPermitBasedMooselike(speciesCode);
    }

    private void refreshRequiredIndicators() {
        if (mSpecimenDetails != null) {
            mSpecimenDetails.setRequiredFields(
                    Utils.isTrue(mModel.mSpecimenMandatoryAge),
                    Utils.isTrue(mModel.mSpecimenMandatoryGender),
                    Utils.isTrue(mModel.mSpecimenMandatoryWeight));
        }
    }

    private void refreshSpecimenExtensionFields() {
        mSpecimenExtensionFieldsLayout.removeAllViews();

        final List<HarvestSpecimen> specimens = mModel.getSpecimens().getValue();
        final Species selectedSpecies = mModel.getSpecies().getValue();

        if (selectedSpecies != null && !specimens.isEmpty()) {
            final int speciesId = selectedSpecies.mId != null ? selectedSpecies.mId : -1;
            final boolean isAntlerFields2020Enabled = isAntlerFields2020Enabled();

            final HarvestSpecimen specimen1 = specimens.get(0);

            switch (speciesId) {
                case SpeciesInformation.MOOSE_ID:
                    addChoiceFieldsForMoose(specimen1, isAntlerFields2020Enabled);
                    break;
                case SpeciesInformation.ROE_DEER_ID:
                    if (isAntlerFields2020Enabled) {
                        addChoiceFieldsForRoeDeer2020(specimen1);
                    } else {
                        clearExtensionFields(specimens);
                    }
                    break;
                case SpeciesInformation.WHITE_TAILED_DEER_ID:
                    if (isAntlerFields2020Enabled) {
                        addChoiceFieldsForWhiteTailedDeer2020(specimen1);
                    } else {
                        addChoiceFieldsForPermitBasedDeer2016(specimen1);
                    }
                    break;
                case SpeciesInformation.FALLOW_DEER_ID:
                case SpeciesInformation.WILD_FOREST_DEER_ID:
                    if (isAntlerFields2020Enabled) {
                        addChoiceFieldsForPermitBasedDeer2020(specimen1);
                    } else {
                        addChoiceFieldsForPermitBasedDeer2016(specimen1);
                    }
                    break;
                case SpeciesInformation.WILD_BOAR_ID:
                    if (isAntlerFields2020Enabled) {
                        addChoiceFieldsForWildBoar2020(specimen1);
                    } else {
                        clearExtensionFields(specimens);
                    }
                    break;
                default:
                    clearExtensionFields(specimens);
                    break;
            }
        }
    }

    private void clearExtensionFields(final Collection<HarvestSpecimen> specimens) {
        // Clear all extension fields.
        for (final HarvestSpecimen specimen : specimens) {
            specimen.clearExtensionFields();
        }
    }

    private void addChoiceFieldsForMoose(final HarvestSpecimen specimen, final boolean isAntlerFields2020Enabled) {
        if (GameAge.YOUNG.toString().equals(specimen.getAge())) {
            createAloneJuvenileChoice(specimen);
        } else {
            specimen.setAlone(null);
        }

        createNotEdibleChoice(specimen);

        specimen.setWeight(null);
        createWeightEstimatedChoice(specimen, false);
        createWeightMeasuredChoice(specimen, false);

        createFitnessClassChoice(specimen);

        if (Gender.MALE.toString().equals(specimen.getGender()) && GameAge.ADULT.toString().equals(specimen.getAge())) {
            if (isAntlerFields2020Enabled) {
                addAntlerFields2020ForMoose(specimen);
            } else {
                addAntlerFields2015ForMoose(specimen);

                specimen.setAntlersLost(null);
                specimen.setAntlersGirth(null);
            }

            // Illegal for moose
            specimen.setAntlersLength(null);
            specimen.setAntlersInnerWidth(null);
            specimen.setAntlerShaftWidth(null);

        } else {
            specimen.clearAllAntlerFields();
        }

        createAdditionalInfoChoice(specimen);
        createAdditionalInfoInstructions(getString(R.string.additional_info_instructions));
    }

    private void createAntlerInstructionsButton(final int speciesId) {
        final View button = getLayoutInflater().inflate(R.layout.instructions_button, null);
        button.setOnClickListener(l -> {
            final Intent intent = new Intent(getActivity(), AntlerInstructionsActivity.class);
            intent.putExtra(AntlerInstructionsActivity.EXTRA_SPECIES, speciesId);
            startActivity(intent);
        });
        mSpecimenExtensionFieldsLayout.addView(button);
    }

    private void addAntlerFields2015ForMoose(final HarvestSpecimen specimen) {
        createAntlersTypeChoice(specimen);
        createAntlersWidthChoice(specimen);
        createAntlersPointsLeftChoice(specimen);
        createAntlersPointsRightChoice(specimen);
    }

    private void addAntlerFields2020ForMoose(final HarvestSpecimen specimen) {
        createAntlersLostChoice(specimen);

        if (!specimen.isAntlersLost()) {
            if (isEditEnabledAndHarvestNotLocked()) {
                createAntlerInstructionsButton(SpeciesInformation.MOOSE_ID);
            }
            createAntlersTypeChoice(specimen);
            createAntlersWidthChoice(specimen);
            createAntlersPointsLeftChoice(specimen);
            createAntlersPointsRightChoice(specimen);
            createAntlersGirthChoice(specimen);
        }
    }

    private void addChoiceFieldsForPermitBasedDeer2016(final HarvestSpecimen specimen) {
        createNotEdibleChoice(specimen);

        specimen.setWeight(null);
        createWeightEstimatedChoice(specimen, false);
        createWeightMeasuredChoice(specimen, false);

        if (Gender.MALE.toString().equals(specimen.getGender()) && GameAge.ADULT.toString().equals(specimen.getAge())) {
            createAntlersWidthChoice(specimen);
            createAntlersPointsLeftChoice(specimen);
            createAntlersPointsRightChoice(specimen);

            // Illegal for permit-based deer before 2020
            specimen.setAntlersLost(null);
            specimen.setAntlersType(null);
            specimen.setAntlersGirth(null);
            specimen.setAntlersLength(null);
            specimen.setAntlersInnerWidth(null);
            specimen.setAntlerShaftWidth(null);

        } else {
            specimen.clearAllAntlerFields();
        }

        createAdditionalInfoChoice(specimen);

        // Illegal for permit-based deer
        specimen.setFitnessClass(null);
        specimen.setAlone(null);
    }

    private void addChoiceFieldsForPermitBasedDeer2020(final HarvestSpecimen specimen) {
        createNotEdibleChoice(specimen);

        specimen.setWeight(null);
        createWeightEstimatedChoice(specimen, false);
        createWeightMeasuredChoice(specimen, false);

        if (Gender.MALE.toString().equals(specimen.getGender()) && GameAge.ADULT.toString().equals(specimen.getAge())) {
            createAntlersLostChoice(specimen);

            if (!specimen.isAntlersLost()) {
                createAntlersWidthChoice(specimen);
                createAntlersPointsLeftChoice(specimen);
                createAntlersPointsRightChoice(specimen);
            }

            // Illegal for permit-based deer starting from 2020
            specimen.setAntlersType(null);
            specimen.setAntlersGirth(null);
            specimen.setAntlersLength(null);
            specimen.setAntlersInnerWidth(null);
            specimen.setAntlerShaftWidth(null);

        } else {
            specimen.clearAllAntlerFields();
        }

        createAdditionalInfoChoice(specimen);
        createAdditionalInfoInstructions(getString(R.string.additional_info_instructions));

        // Illegal for permit-based deer
        specimen.setFitnessClass(null);
        specimen.setAlone(null);
    }

    private void addChoiceFieldsForRoeDeer2020(final HarvestSpecimen specimen) {
        specimen.setWeight(null);
        createWeightEstimatedChoice(specimen, false);
        createWeightMeasuredChoice(specimen, false);

        if (Gender.MALE.toString().equals(specimen.getGender()) && GameAge.ADULT.toString().equals(specimen.getAge())) {
            createAntlersLostChoice(specimen);

            if (!specimen.isAntlersLost()) {
                if (isEditEnabledAndHarvestNotLocked()) {
                    createAntlerInstructionsButton(SpeciesInformation.ROE_DEER_ID);
                }
                createAntlersPointsLeftChoice(specimen);
                createAntlersPointsRightChoice(specimen);
                createAntlersLengthChoice(specimen);
                createAntlerShaftWidthChoice(specimen);
            }

            // Illegal for roe deer
            specimen.setAntlersType(null);
            specimen.setAntlersWidth(null);
            specimen.setAntlersGirth(null);
            specimen.setAntlersInnerWidth(null);

        } else {
            specimen.clearAllAntlerFields();
        }

        // Illegal for roe deer
        specimen.setFitnessClass(null);
        specimen.setNotEdible(null);
        specimen.setAdditionalInfo(null);
        specimen.setAlone(null);
    }

    private void addChoiceFieldsForWhiteTailedDeer2020(final HarvestSpecimen specimen) {
        createNotEdibleChoice(specimen);

        specimen.setWeight(null);
        createWeightEstimatedChoice(specimen, false);
        createWeightMeasuredChoice(specimen, false);

        if (Gender.MALE.toString().equals(specimen.getGender()) && GameAge.ADULT.toString().equals(specimen.getAge())) {
            createAntlersLostChoice(specimen);

            if (!specimen.isAntlersLost()) {
                if (isEditEnabledAndHarvestNotLocked()) {
                    createAntlerInstructionsButton(SpeciesInformation.WHITE_TAILED_DEER_ID);
                }
                createAntlersPointsLeftChoice(specimen);
                createAntlersPointsRightChoice(specimen);
                createAntlersGirthChoice(specimen);
                createAntlersLengthChoice(specimen);
                createAntlersInnerWidthChoice(specimen);
            }

            // Illegal for white-tailed deer
            specimen.setAntlersType(null);
            specimen.setAntlerShaftWidth(null);

        } else {
            specimen.clearAllAntlerFields();
        }

        createAdditionalInfoChoice(specimen);
        createAdditionalInfoInstructions(getString(R.string.additional_info_instructions_white_tailed_deer));

        // Illegal for white-tailed deer
        specimen.setFitnessClass(null);
        specimen.setAlone(null);
    }

    private void addChoiceFieldsForWildBoar2020(final HarvestSpecimen specimen) {
        specimen.setWeight(null);
        createWeightEstimatedChoice(specimen, true);
        createWeightMeasuredChoice(specimen, true);

        // Clear illegal for wild board

        specimen.clearAllAntlerFields();

        specimen.setFitnessClass(null);
        specimen.setNotEdible(null);
        specimen.setAdditionalInfo(null);
        specimen.setAlone(null);
    }

    private void createNotEdibleChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.not_edible));
        choiceView.setChecked(specimen.isNotEdible(), specimen::setNotEdible);
        addChoiceView(choiceView, true);
    }

    private void createAloneJuvenileChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.mooselike_calf));
        choiceView.setChecked(specimen.isAlone(), specimen::setAlone);
        addChoiceView(choiceView, false);
    }

    private void createWeightEstimatedChoice(final HarvestSpecimen specimen, final boolean allowDecimals) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.weight_estimated));
        if (allowDecimals) {
            choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL, 1);
            choiceView.setEditTextChoice(
                    formatDouble(specimen.getWeightEstimated()),
                    text -> specimen.setWeightEstimated(parseDouble(text)));
        } else {
            choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_NUMBER, 1);
            choiceView.setEditTextChoice(
                    formatDouble(specimen.getWeightEstimated(), 0),
                    text -> specimen.setWeightEstimated(parseDouble(text)));
        }
        choiceView.setEditTextMaxValue(999.0f);

        addChoiceView(choiceView, true);
    }

    private void createWeightMeasuredChoice(final HarvestSpecimen specimen, final boolean allowDecimals) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.weight_measured));
        if (allowDecimals) {
            choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL, 1);
            choiceView.setEditTextChoice(
                    formatDouble(specimen.getWeightMeasured()),
                    text -> specimen.setWeightMeasured(parseDouble(text)));
        } else {
            choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_NUMBER, 1);
            choiceView.setEditTextChoice(
                    formatDouble(specimen.getWeightMeasured(), 0),
                    text -> specimen.setWeightMeasured(parseDouble(text)));
        }
        choiceView.setEditTextMaxValue(999.0f);

        addChoiceView(choiceView, true);
    }

    private void createFitnessClassChoice(final HarvestSpecimen specimen) {
        final ArrayList<String> classes = new ArrayList<>();
        for (final MooseFitnessClass fitness : MooseFitnessClass.values()) {
            classes.add(fitness.toString());
        }

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.fitness_class));
        choiceView.setChoices(classes, specimen.getFitnessClass(), true, (position, value) -> specimen.setFitnessClass(value));
        addChoiceView(choiceView, true);
    }

    private void createAntlersLostChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_lost));
        choiceView.setChecked(specimen.isAntlersLost(), checked -> {
            specimen.setAntlersLost(checked);

            if (checked) {
                specimen.clearAntlerDetailFields();
            }

            // Trigger redraw for antler fields.
            onSpecimenChanged();
        });
        addChoiceView(choiceView, false);
    }

    private void createAntlersTypeChoice(final HarvestSpecimen specimen) {
        final MooseAntlersType[] antlerTypeOptions = MooseAntlersType.values();
        final ArrayList<String> types = new ArrayList<>(antlerTypeOptions.length);

        for (final MooseAntlersType type : antlerTypeOptions) {
            types.add(type.toString());
        }

        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_type));
        choiceView.setChoices(types, specimen.getAntlersType(), true, (position, value) -> specimen.setAntlersType(value));
        addChoiceView(choiceView, true);
    }

    private void createAntlersWidthChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_width));
        choiceView.setEditTextMaxLength(3);
        choiceView.setEditTextMaxValue(200);
        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlersWidth()),
                text -> specimen.setAntlersWidth(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAntlersPointsLeftChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_points_left));
        choiceView.setEditTextMaxLength(2);
        choiceView.setEditTextMaxValue(30);
        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlerPointsLeft()),
                text -> specimen.setAntlerPointsLeft(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAntlersPointsRightChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_points_right));
        choiceView.setEditTextMaxLength(2);
        choiceView.setEditTextMaxValue(30);

        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlerPointsRight()),
                text -> specimen.setAntlerPointsRight(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAntlersGirthChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_girth));
        choiceView.setEditTextMaxLength(2);
        choiceView.setEditTextMaxValue(50);
        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlersGirth()),
                text -> specimen.setAntlersGirth(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAntlersLengthChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_length));
        choiceView.setEditTextMaxLength(3);
        choiceView.setEditTextMaxValue(100);
        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlersLength()),
                text -> specimen.setAntlersLength(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAntlersInnerWidthChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antlers_inner_width));
        choiceView.setEditTextMaxLength(3);
        choiceView.setEditTextMaxValue(100);
        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlersInnerWidth()),
                text -> specimen.setAntlersInnerWidth(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAntlerShaftWidthChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.antler_shaft_width));
        choiceView.setEditTextMaxLength(2);
        choiceView.setEditTextMaxValue(10);
        choiceView.setEditTextChoice(
                formatInt(specimen.getAntlerShaftWidth()),
                text -> specimen.setAntlerShaftWidth(parseInt(text)));

        addChoiceView(choiceView, true);
    }

    private void createAdditionalInfoChoice(final HarvestSpecimen specimen) {
        final ChoiceView<String> choiceView = new ChoiceView<>(getActivity(), getString(R.string.additional_info));
        choiceView.setEditTextChoice(specimen.getAdditionalInfo(), specimen::setAdditionalInfo);
        choiceView.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 4);
        addChoiceView(choiceView, true);
    }

    private void createAdditionalInfoInstructions(final String instructions) {
        final View view = getLayoutInflater().inflate(R.layout.view_info, null);
        final TextView textView = view.findViewById(R.id.tv_info);
        textView.setText(instructions);
        mSpecimenExtensionFieldsLayout.addView(view);
    }

    private void addChoiceView(final ChoiceView<String> choiceView, final boolean topMargin) {
        mSpecimenExtensionFieldsLayout.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(Utils.isTrue(mModel.getEditEnabled()));
    }

    private void refreshSpeciesExtraState(final boolean editMode) {
        for (int i = 0; i < mSpeciesExtraLayout.getChildCount(); ++i) {
            final ChoiceView choice = (ChoiceView) mSpeciesExtraLayout.getChildAt(i);
            choice.setChoiceEnabled(editMode);
        }
    }

    private void refreshExtensionFieldsInputState(final boolean editMode) {
        for (int i = 0; i < mSpecimenExtensionFieldsLayout.getChildCount(); ++i) {
            final View view = mSpecimenExtensionFieldsLayout.getChildAt(i);
            if (view instanceof  ChoiceView) {
                final ChoiceView choice = (ChoiceView) view;
                choice.setChoiceEnabled(editMode);
            }
        }
    }

    private void refreshAmountInputState(final boolean editMode) {
        final Species species = mModel.getSpecies().getValue();

        if (species != null && species.mMultipleSpecimenAllowedOnHarvests) {
            mAmountInput.setText(String.valueOf(mModel.getAmount().getValue()));
            mAmountInput.setEnabled(editMode && mModel.notLocked());
            mAmountGroup.setVisibility(View.VISIBLE);
        } else {
            // Reset value when hiding field
            mAmountInput.setText(String.valueOf(1));
            mAmountInput.setEnabled(false);
            mAmountGroup.setVisibility(View.GONE);
        }
    }

    private boolean isAntlerFields2020Enabled() {
        final boolean isDeerPilotActivated = mDeerHuntingFeatureAvailability.getEnabled();

        final Calendar pointOfTime = mModel.getDateTime().getValue();
        final boolean isHuntingYear2020OrLater =
                pointOfTime != null && DateTimeUtils.getHuntingYearForCalendar(pointOfTime) >= 2020;

        return isDeerPilotActivated && isHuntingYear2020OrLater;
    }

    private void onActivityLocationResult(final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final Location location = data.getParcelableExtra(MapViewerActivity.RESULT_LOCATION);
            final String source = data.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE);

            if (GameLog.LOCATION_SOURCE_GPS.equals(source)) {
                mModel.selectLocationGps(location);
            } else if (GameLog.LOCATION_SOURCE_MANUAL.equals(source)) {
                mModel.selectLocationManual(location);
            }
        }
    }

    private void onActivitySpeciesResult(final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final Species result = (Species) data.getSerializableExtra(ChooseSpeciesActivity.RESULT_SPECIES);
            mModel.selectSpecies(result.mId);
        }
    }

    private void onActivitySpecimensResult(final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final List<HarvestSpecimen> specimens =
                    (List<HarvestSpecimen>) data.getSerializableExtra(HarvestSpecimensActivity.RESULT_SPECIMENS);

            mModel.setAmount(specimens.size());
            mModel.setSpecimens(specimens);
        }
    }

    private void onActivityPermitResult(final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final String permitNumber = data.getStringExtra(HarvestPermitActivity.RESULT_PERMIT_NUMBER);
            final String permitType = data.getStringExtra(HarvestPermitActivity.RESULT_PERMIT_TYPE);
            final int speciesId = data.getIntExtra(HarvestPermitActivity.RESULT_PERMIT_SPECIES, -1);

            final Species species = mSpeciesResolver.findSpecies(speciesId);

            mModel.selectPermitNumber(permitNumber, permitType, species);
        }
    }

    // endregion DiaryImageManager methods

    @Override
    public void viewImage(final GameLogImage image) {
        final FragmentActivity activity = getActivity();

        if (activity != null) {
            final FullScreenImageDialog dialog = FullScreenImageDialog.newInstance(image);
            final FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            dialog.show(fragmentTransaction, FullScreenImageDialog.TAG);
        }
    }

    @Override
    public boolean hasPhotoPermissions() {
        final Context context = requireContext();
        return PermissionHelper.hasPhotoPermissions(context);
    }

    @Override
    public void requestPhotoPermissions() {
        final Activity activity = requireActivity();
        PermissionHelper.requestPhotoPermissions(activity, 111);
    }

    // endregion
}
