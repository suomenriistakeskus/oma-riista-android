package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.riista.mobile.DiaryImageManager;
import fi.riista.mobile.DiaryImageManager.DiaryImageManagerInterface;
import fi.riista.mobile.EntryMapView;
import fi.riista.mobile.LocationClient;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.ChooseSpeciesActivity;
import fi.riista.mobile.activity.HarvestActivity;
import fi.riista.mobile.activity.HarvestPermitActivity;
import fi.riista.mobile.activity.HarvestSpecimensActivity;
import fi.riista.mobile.activity.ImageViewerActivity;
import fi.riista.mobile.activity.MapViewerActivity;
import fi.riista.mobile.database.EditEventCompletion;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.GameDatabase.SyncMode;
import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.message.ChangeHarvestMessage;
import fi.riista.mobile.message.EventUpdateMessage;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.PermitSpeciesAmount;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.models.Specimen.MooseAntlersType;
import fi.riista.mobile.models.Specimen.MooseFitnessClass;
import fi.riista.mobile.models.Specimen.SpecimenAge;
import fi.riista.mobile.models.Specimen.SpecimenGender;
import fi.riista.mobile.ui.ChoiceView;
import fi.riista.mobile.ui.ChoiceView.OnCheckListener;
import fi.riista.mobile.ui.ChoiceView.OnChoiceListener;
import fi.riista.mobile.ui.ChoiceView.OnTextListener;
import fi.riista.mobile.ui.EditToolsView;
import fi.riista.mobile.ui.EditToolsView.OnDateTimeListener;
import fi.riista.mobile.ui.EditToolsView.OnDeleteListener;
import fi.riista.mobile.ui.HarvestSpecimenView;
import fi.riista.mobile.ui.HeaderTextView;
import fi.riista.mobile.ui.SelectSpeciesButton;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.HarvestValidator;
import fi.riista.mobile.utils.MapUtils;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.message.WorkMessageHandler;

public class HarvestFragment extends DetailsPageFragment implements LocationListener, OnMapReadyCallback,
        DiaryImageManagerInterface, HarvestSpecimenView.SpecimenDetailsListener {

    private static final int SPECIMENS_REQUEST_CODE = 155;

    // Edit data values
    private Species mSelectedSpecies = null;

    // UI components
    private EditToolsView mEditTools;
    private EntryMapView mMapView;
    private TextView mCoordinateText;

    private SelectSpeciesButton mSelectSpeciesButton;

    private HeaderTextView mAmountLabel;
    private EditText mAmountInput;
    private EditText mDescriptionEdit;
    private Button mSpecimenButton;
    private HarvestSpecimenView mSpecimenDetails;
    private LinearLayout mMooseDetailsLayout;

    private TextView mHarvestStateLabel;

    private AppCompatCheckBox mPermitCheckbox;
    private Button mPermitNumber;

    private ViewGroup mButtonView;
    private Button mSubmitButton;
    private Button mDismissButton;

    private View mBusyOverlay;

    // Location service related
    private LocationClient mLocationClient;

    private DiaryImageManager mDiaryImageManager = null;

    private GameHarvest mEvent = null;

    private boolean mImagesInitialized = false;
    private boolean mInitialized = false;
    private boolean mEditModeEnabled = true;
    private boolean mSendUpdateInProgress = false;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    public static HarvestFragment newInstance(GameHarvest harvest) {
        HarvestFragment fragment = new HarvestFragment();
        fragment.mEvent = harvest;

        Bundle bundle = new Bundle();
        bundle.putSerializable("harvest", (GameHarvest) Utils.cloneObject(harvest));
        fragment.setArguments(bundle);

        return fragment;
    }

    public void setTitle() {
        setViewTitle(getResources().getString(R.string.game));
        mEditTools.setDateTimeText(new DateTime(mEvent.mTime));
    }

    private void resetHarvest() {
        GameHarvest harvest = (GameHarvest) getArguments().getSerializable("harvest");
        mEvent = (GameHarvest) Utils.cloneObject(harvest);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mEvent.mLocalId > 0) {
            if (!mInitialized) {
                mEditModeEnabled = false;
                mInitialized = true;
            }
        } else {
            mEditModeEnabled = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_loggame, container, false);

        mEditTools = (EditToolsView) view.findViewById(R.id.view_edit_tools);

        AppPreferences.MapTileSource tileSource
                = AppPreferences.getMapTileSource(inflater.getContext()) == AppPreferences.MapTileSource.GOOGLE
                ? AppPreferences.MapTileSource.GOOGLE
                : AppPreferences.MapTileSource.MML_TOPOGRAPHIC;

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (EntryMapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);
        mMapView.setup(inflater.getContext(), false, true, tileSource);
        mMapView.getMapAsync(this);
        mCoordinateText = (TextView) view.findViewById(R.id.map_coordinates);

        mSelectSpeciesButton = (SelectSpeciesButton) view.findViewById(R.id.btn_select_species);

        mAmountLabel = (HeaderTextView) view.findViewById(R.id.amountLabel);
        mAmountLabel.setRightPadding(10);
        mAmountInput = mSelectSpeciesButton.getAmountInput();

        mHarvestStateLabel = (TextView) view.findViewById(R.id.harvestStateLabel);

        mSpecimenDetails = (HarvestSpecimenView) view.findViewById(R.id.specimenDetails);
        mMooseDetailsLayout = (LinearLayout) view.findViewById(R.id.layout_moose_details);
        mSpecimenButton = (Button) view.findViewById(R.id.specimenButton);

        mPermitCheckbox = (AppCompatCheckBox) view.findViewById(R.id.loggame_permit_checkbox);
        mPermitNumber = (Button) view.findViewById(R.id.loggame_permit_number);

        if (!mImagesInitialized) {
            mDiaryImageManager = new DiaryImageManager(getWorkContext(), this);
        }

        mDescriptionEdit = (EditText) view.findViewById(R.id.logDescription);

        mButtonView = (ViewGroup) view.findViewById(R.id.loggame_button_area);
        mSubmitButton = (Button) view.findViewById(R.id.newLogButton);
        mDismissButton = (Button) view.findViewById(R.id.dismissButton);

        mBusyOverlay = view.findViewById(R.id.loggame_busy_overlay);
        mBusyOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Just consume all events to prevent other controls form getting events
                return true;
            }
        });

        setupView(view);

        mSpecimenDetails.setListener(this);

        refreshEditToolsState();

        return view;
    }

    private void setupView(View view) {
        setupEditTools();
        setupDescriptionEdit();
        setupSpeciesButton();
        setupSpecimenButton();
        setupAmountInput();
        setupPermitNumber();
        setupDismissButton(view);
        setupSubmitButton();
    }

    private void setupEditTools() {
        mEditTools.getDateButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEvent.isEditable()) {
                    showDateTimePicker();
                }
            }
        });

        mEditTools.getEditButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditModeEnabled(true);
            }
        });

        mEditTools.getDeleteButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEvent.isEditable()) {
                    showConfirmRemoveEvent();
                }
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
                mEvent.mMessage = mDescriptionEdit.getText().toString();
            }
        });
    }

    private void setupSpeciesButton() {
        mSelectSpeciesButton.getSpeciesButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditModeEnabled && mEvent.isEditable()) {
                    UiUtils.startSpeciesSelection(getActivity(), HarvestFragment.this);
                }
            }
        });
    }

    private void setupSpecimenButton() {
        mSpecimenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEvent.mAmount < 1 || mEvent.mAmount >= GameHarvest.SPECIMEN_DETAILS_MAX) {
                    return;
                }

                Intent intent = new Intent(getActivity(), HarvestSpecimensActivity.class);
                intent.putExtra(HarvestSpecimensActivity.EXTRA_HARVEST, mEvent);
                intent.putExtra(HarvestSpecimensActivity.EXTRA_EDIT_MODE, mEditModeEnabled);
                startActivityForResult(intent, SPECIMENS_REQUEST_CODE);
            }
        });
    }

    private void setupAmountInput() {
        mAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1) {
                    mEvent.mAmount = 0;
                } else if (s.length() == 1 && mAmountInput.getText().toString().equals("0")) {
                    mAmountInput.setText("");
                } else if (s.length() > 0) {
                    mEvent.mAmount = validateAmountInput();
                }

                updateSubmitButton();
            }
        });
        mAmountInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mAmountInput.getText().length() == 0) {
                        mAmountInput.setText(String.valueOf(1));
                    }
                    mEvent.mAmount = validateAmountInput();

                    // Remove extra specimen information after amount decreases.
                    while (mEvent.mSpecimen.size() > mEvent.mAmount && mEvent.mSpecimen.size() > 0) {
                        mEvent.mSpecimen.remove(mEvent.mSpecimen.size() - 1);
                    }

                    refreshSpecimenDetailsState();
                }
            }
        });
        mAmountInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mAmountInput.clearFocus();
                    hideKeyboard();

                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Validate amount field value. Revert to previous value if not legal input.
     *
     * @return Amount value
     */
    private Integer validateAmountInput() {
        int amount = mEvent.mAmount;

        if (mSelectedSpecies == null) {
            amount = 1;
        } else if (mSelectedSpecies.mMultipleSpecimenAllowedOnHarvests) {
            try {
                amount = Integer.parseInt(mAmountInput.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();

                mAmountInput.setText(String.valueOf(amount));
            }
        } else if (amount != 1) {
            amount = 1;
            mAmountInput.setText(String.valueOf(1));
        }
        return amount;
    }

    /**
     * Remove empty items from specimen list. Ensure that amount and specimen information matches.
     *
     * @param event Log event containing specimen
     */
    private void validateSpecimenData(GameHarvest event) {
        if (event != null && event.mSpecimen != null) {
            for (Iterator<Specimen> iter = event.mSpecimen.listIterator(); iter.hasNext(); ) {
                Specimen specimen = iter.next();
                if (specimen.isEmpty()) {
                    iter.remove();
                }
            }

            // Get rid of any extra specimen items.
            if (event.mSpecimen.size() > event.mAmount) {
                event.mSpecimen.subList(event.mAmount, event.mSpecimen.size()).clear();
            }
        }
    }

    private void setupPermitNumber() {
        mPermitCheckbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((AppCompatCheckBox) v).isChecked()) {
                    navigateToPermitList();
                } else {
                    mPermitNumber.setVisibility(View.GONE);
                    mPermitNumber.setText(null);

                    mEvent.mPermitNumber = null;
                    mEvent.mPermitType = null;

                    // No longer constraints species and date from permit details
                    setEditModeEnabled(mEditModeEnabled);
                }
            }
        });

        mPermitNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditModeEnabled) {
                    navigateToPermitList();
                }
            }
        });
    }

    private void navigateToPermitList() {
        Intent intent = new Intent(getActivity(), HarvestPermitActivity.class);
        intent.putExtra(HarvestPermitActivity.EXTRA_HARVEST, mEvent);

        if (!TextUtils.isEmpty(mEvent.mPermitNumber)) {
            intent.putExtra(HarvestPermitActivity.EXTRA_PERMIT_NUMBER, mEvent.mPermitNumber);
        }

        startActivityForResult(intent, HarvestPermitActivity.PERMIT_REQUEST_CODE);
    }

    private void setupDismissButton(final View view) {
        if (mEvent.mLocalId > 0) {
            mDismissButton.setText(R.string.cancel);
        }

        mDismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEvent.mLocalId > 0) {
                    setEditModeEnabled(false);

                    resetHarvest();
                    setupWithItem(mEvent);

                    mDiaryImageManager.setItems(mEvent.mImages);
                    mDiaryImageManager.setup((ViewGroup) view.findViewById(R.id.diaryimages));
                } else {
                    getActivity().finish();
                }
            }
        });
    }

    private void setupSubmitButton() {
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedSpecies != null && !mSendUpdateInProgress) {
                    setSendStatus(true);

                    GameDatabase database = GameDatabase.getInstance();

                    // Validate and format updated data.
                    mEvent.mAmount = validateAmountInput();
                    validateSpecimenData(mEvent);

                    if (!HarvestValidator.validate(mEvent)) {
                        Utils.LogMessage("Failed to validate harvest");
                        return;
                    }

                    List<LogImage> images = mDiaryImageManager.getLogImages();
                    final WorkContext context = getWorkContext();
                    final int huntingYear = DateTimeUtils.getSeasonStartYearFromDate(mEvent.mTime);

                    if (mEvent.mLocalId <= 0) {
                        // Creating new
                        mEvent.mSpeciesID = mSelectedSpecies.mId;
                        mEvent.mImages = images;
                        database.addNewLocallyCreatedEvent(mEvent);

                        setSendStatus(false);
                        context.sendGlobalMessage(new EventUpdateMessage(mEvent.mType, mEvent.mLocalId, huntingYear));

                        finishEditAfterSave();
                    } else if (mEvent.mLocalId > 0 && mEvent.mId <= 0) {
                        // Edit locally created, not sent to backend
                        mEvent.mSpeciesID = mSelectedSpecies.mId;
                        mEvent.mImages = images;
                        mEvent.mSent = false;
                        database.editLocalEvent(mEvent, true);

                        SyncMode syncMode = database.getSyncMode(getActivity());
                        if (syncMode == SyncMode.SYNC_AUTOMATIC) {
                            database.sendLocalEvents(context, false);
                        }

                        setSendStatus(false);
                        context.sendGlobalMessage(new EventUpdateMessage(mEvent.mType, mEvent.mLocalId, huntingYear));

                        finishEditAfterSave();
                    } else {
                        mEvent.mSpeciesID = mSelectedSpecies.mId;
                        mEvent.mImages = images;
                        mEvent.mSent = false;
                        database.editEvent(context, mEvent, new EditEventCompletion() {
                            @Override
                            protected void editSuccessful() {
                                setSendStatus(false);
                                if (getActivity() != null) {
                                    context.sendGlobalMessage(new EventUpdateMessage(mEvent.mType, mEvent.mLocalId, huntingYear));

                                    finishEditAfterSave();
                                }
                            }

                            @Override
                            protected void editEventOutdated() {
                                setSendStatus(false);
                                if (getActivity() != null) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getResources().getString(R.string.error))
                                            .setMessage(getResources().getString(R.string.eventoutdated))
                                            .show();
                                }
                            }

                            @Override
                            protected void editFailed() {
                                setSendStatus(false);
                                if (getActivity() != null) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getResources().getString(R.string.error))
                                            .setMessage(getResources().getString(R.string.eventeditfailed))
                                            .show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void finishEditAfterSave() {
        Intent result = new Intent();
        result.putExtra(HarvestActivity.RESULT_DID_SAVE, true);

        BaseActivity activity = (BaseActivity) getActivity();
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

        updateLocationOnMap();
        refreshPermitNumber();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    private void refreshEditToolsState() {
        if (mEvent == null) {
            return;
        }
        boolean enable = !mSendUpdateInProgress;

        mEditTools.setButtonEnabled(mEditTools.getDateButton(), mEditModeEnabled && mEvent.isEditable() && enable);
        mEditTools.getDateButton().setVisibility(View.VISIBLE);

        mEditTools.getEditButton().setEnabled(enable);
        mEditTools.getEditButton().setVisibility(mEditModeEnabled && mEvent.isEditable() ? View.INVISIBLE : View.VISIBLE);

        mEditTools.getDeleteButton().setEnabled(enable);
        mEditTools.getDeleteButton().setVisibility(mEvent.isEditable() && mEvent.mLocalId > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    void showDateTimePicker() {
        DateTime dateTime = new DateTime(mEvent.mTime);
        mEditTools.showDateTimeDialog(dateTime, new OnDateTimeListener() {
            @Override
            public void onDateTime(DateTime dateTime) {
                mEvent.mTime.set(dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth(),
                        dateTime.getHourOfDay(), dateTime.getMinuteOfHour());

                setTitle();

                updateSubmitButton();
            }
        });
    }

    void showConfirmRemoveEvent() {
        mEditTools.showDeleteDialog(new OnDeleteListener() {
            @Override
            public void onDelete() {
                GameDatabase db = GameDatabase.getInstance();
                db.removeEvent(getWorkContext(), mEvent, new EditEventCompletion() {
                    @Override
                    protected void editSuccessful() {
                        finishEditAfterSave();
                    }

                    @Override
                    protected void editFailed() {
                        if (getActivity() != null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.error))
                                    .setMessage(getResources().getString(R.string.eventeditfailed))
                                    .show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        setSendStatus(false);

        GameDatabase.getInstance().PruneNonexistentLocalImages(getActivity(), mEvent);
        if (!mImagesInitialized) {
            mDiaryImageManager.setItems(mEvent.mImages);
        }

        mDiaryImageManager.setup((ViewGroup) getView().findViewById(R.id.diaryimages));
        mImagesInitialized = true;

        setTitle();
        setupWithItem(mEvent);
        setEditModeEnabled(mEditModeEnabled);
        updateSubmitButton();

        // Wait until event has been initialized
        setHasOptionsMenu(true);

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

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Prevent UI interactions during network operations.
     *
     * @param isBusy Is operation ongoing
     */
    private void setSendStatus(boolean isBusy) {
        mSendUpdateInProgress = isBusy;

        // Use overlay to hide UI controls
        if (mSendUpdateInProgress) {
            mBusyOverlay.setVisibility(View.VISIBLE);
        } else {
            mBusyOverlay.setVisibility(View.GONE);
        }

        refreshEditToolsState();
    }

    //
    // Map and location related methods
    //

    @Override
    public void onMapReady(GoogleMap map) {
        // New entry, track and save gps location.
        if (mEvent != null && mEvent.mLocalId <= 0
                && (mEvent.mLocationSource == null || mEvent.mLocationSource.equals(GameHarvest.LOCATION_SOURCE_GPS))) {
            mMapView.setShowInfoWindow(true);
            mMapView.setShowAccuracy(true);

            if (getActivity() != null) {
                mLocationClient = ((BaseActivity) getActivity()).getLocationClient();
                mLocationClient.addListener(this);
            }
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                viewMap();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        // Don't update position with gps position if pre-existing entry or location has already been set.
        if (getView() != null && mEvent.mLocalId <= 0
                && (mEvent.mLocationSource == null || mEvent.mLocationSource.equals(GameHarvest.LOCATION_SOURCE_GPS))) {
            mEvent.mLocation = location;
            mEvent.mAccuracy = location.getAccuracy();
            mEvent.mHasAltitude = location.hasAltitude();
            mEvent.mAltitude = location.getAltitude();
            mEvent.mCoordinates = MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude());
            mEvent.mLocationSource = GameHarvest.LOCATION_SOURCE_GPS;

            updateLocationOnMap();
            updateSubmitButton();
        }
    }

    @Override
    public void onSpecimenChanged() {
        refreshMooseDetails();

        updateSubmitButton();
    }

    private void refreshSpecimenDetailsState() {
        if (mSelectedSpecies != null && mSelectedSpecies.mMultipleSpecimenAllowedOnHarvests) {
            mSpecimenDetails.setVisibility(View.GONE);
            mSpecimenButton.setVisibility(mEvent.mAmount > 0 && mEvent.mAmount <= GameHarvest.SPECIMEN_DETAILS_MAX ? View.VISIBLE : View.GONE);
        } else if (mSelectedSpecies != null) {
            mSpecimenDetails.setVisibility(View.VISIBLE);
            // Special case where no selection means 'unknown' value
            mSpecimenDetails.setSelectionsClearable(mSelectedSpecies.mId == SpeciesInformation.SPECIES_GREY_SEAL);
            mSpecimenButton.setVisibility(View.GONE);

            if (mEvent.mSpecimen == null) {
                mEvent.mSpecimen = new ArrayList<>();
            }

            if (mEvent.mSpecimen.isEmpty()) {
                mEvent.mSpecimen.add(new Specimen());
            } else if (mEvent.mSpecimen.size() > 1) {
                // Remove all but the first item.
                mEvent.mSpecimen.subList(1, mEvent.mSpecimen.size()).clear();
            }

            Specimen specimen = mEvent.mSpecimen.get(0);
            mSpecimenDetails.setupWithSpecimen(specimen, mSelectedSpecies.mId);

            if (!TextUtils.isEmpty(mEvent.mPermitNumber)) {
                PermitManager manager = PermitManager.getInstance(getActivity());
                PermitSpeciesAmount speciesAmount = manager.getSpeciesAmountFromPermit(manager.getPermit(mEvent.mPermitNumber), mEvent.mSpeciesID);
                if (speciesAmount != null) {
                    mSpecimenDetails.setRequiredFields(speciesAmount.getAgeRequired(), speciesAmount.getGenderRequired(), speciesAmount.getWeightRequired());
                }
            } else {
                mSpecimenDetails.setRequiredFields(false, false, false);
            }

        } else {
            mSpecimenDetails.setVisibility(View.GONE);
            mSpecimenButton.setVisibility(View.GONE);
        }

        refreshMooseDetails();
    }

    private void refreshMooseDetails() {
        mMooseDetailsLayout.removeAllViews();

        if (mSelectedSpecies == null || mEvent.mSpecimen.isEmpty()) {
            return;
        }

        if (mSelectedSpecies.mId == Utils.MOOSE_ID) {
            createMooseChoiseFields(mEvent.mSpecimen.get(0));
        } else if (mSelectedSpecies.mId == Utils.FALLOW_DEER_ID ||
                mSelectedSpecies.mId == Utils.WHITE_TAILED_DEER ||
                mSelectedSpecies.mId == Utils.WILD_FOREST_DEER) {
            createDeerChoiseFields(mEvent.mSpecimen.get(0));
        } else {
            //Clear all moose fields
            for (Specimen specimen : mEvent.mSpecimen) {
                specimen.setNotEdible(null);
                specimen.setWeightEstimated(null);
                specimen.setWeightMeasured(null);
                specimen.setFitnessClass(null);
                specimen.setAntlersType(null);
                specimen.setAntlersWidth(null);
                specimen.setAntlerPointsLeft(null);
                specimen.setAntlerPointsRight(null);
                specimen.setAdditionalInfo(null);
            }
        }
    }

    private void createMooseChoiseFields(Specimen specimen) {
        specimen.setWeight(null);

        createMooseNotEdibleChoice(specimen);
        createMooseWeightEstimatedChoice(specimen);
        createMooseWeightMeasuredChoice(specimen);
        createMooseFitnessClassChoice(specimen);
        if (SpecimenGender.MALE.toString().equals(specimen.getGender()) &&
                SpecimenAge.ADULT.toString().equals(specimen.getAge())) {
            //Show only for male adult moose
            createMooseAntlersTypeChoice(specimen);
            createMooseAntlersWidthChoice(specimen);
            createMooseAntlersPointsLeftChoice(specimen);
            createMooseAntlersPointsRightChoice(specimen);
        } else {
            specimen.setAntlersType(null);
            specimen.setAntlersWidth(null);
            specimen.setAntlerPointsLeft(null);
            specimen.setAntlerPointsRight(null);
        }
        createMooseAdditionalInfoChoice(specimen);
    }

    private void createDeerChoiseFields(Specimen specimen) {
        specimen.setWeight(null);

        createMooseNotEdibleChoice(specimen);
        createMooseWeightEstimatedChoice(specimen);
        createMooseWeightMeasuredChoice(specimen);
        specimen.setFitnessClass(null);

        if (SpecimenGender.MALE.toString().equals(specimen.getGender()) &&
                SpecimenAge.ADULT.toString().equals(specimen.getAge())) {
            //Show only for male adult deer
            specimen.setAntlersType(null);
            createMooseAntlersWidthChoice(specimen);
            createMooseAntlersPointsLeftChoice(specimen);
            createMooseAntlersPointsRightChoice(specimen);
        } else {
            specimen.setAntlersType(null);
            specimen.setAntlersWidth(null);
            specimen.setAntlerPointsLeft(null);
            specimen.setAntlerPointsRight(null);
        }
        createMooseAdditionalInfoChoice(specimen);
    }

    private void createMooseNotEdibleChoice(final Specimen specimen) {
        boolean notEdible = specimen.getNotEdible() != null ? specimen.getNotEdible() : false;

        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_not_edible));
        choiceView.setChecked(notEdible, new OnCheckListener() {
            @Override
            public void onCheck(boolean check) {
                specimen.setNotEdible(check);
            }
        });
        addChoiceView(choiceView, false);
    }

    private void createMooseWeightEstimatedChoice(final Specimen specimen) {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_weight_estimated));
        choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL, 1);
        choiceView.setEditTextMaxValue(999.0f);
        choiceView.setEditTextChoice(Utils.formatDouble(specimen.getWeightEstimated()), new OnTextListener() {
            @Override
            public void onText(String text) {
                specimen.setWeightEstimated(Utils.parseDouble(text));
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseWeightMeasuredChoice(final Specimen specimen) {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_weight_weighted));
        choiceView.setEditTextMode(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL, 1);
        choiceView.setEditTextMaxValue(999.0f);
        choiceView.setEditTextChoice(Utils.formatDouble(specimen.getWeightMeasured()), new OnTextListener() {
            @Override
            public void onText(String text) {
                specimen.setWeightMeasured(Utils.parseDouble(text));
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseFitnessClassChoice(final Specimen specimen) {
        ArrayList<String> classes = new ArrayList<>();
        for (MooseFitnessClass fitness : MooseFitnessClass.values()) {
            classes.add(fitness.toString());
        }

        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_fitness_class));
        choiceView.setChoices(classes, specimen.getFitnessClass(), true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String value) {
                specimen.setFitnessClass(value);
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseAntlersTypeChoice(final Specimen specimen) {
        ArrayList<String> types = new ArrayList<>();
        for (MooseAntlersType type : MooseAntlersType.values()) {
            types.add(type.toString());
        }

        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_antlers_type));
        choiceView.setChoices(types, specimen.getAntlersType(), true, new OnChoiceListener() {
            @Override
            public void onChoice(int position, String value) {
                specimen.setAntlersType(value);
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseAntlersWidthChoice(final Specimen specimen) {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_antlers_width));
        choiceView.setEditTextMaxLength(3);
        choiceView.setEditTextMaxValue(999);
        choiceView.setEditTextChoice(Utils.formatInt(specimen.getAntlersWidth()), new OnTextListener() {
            @Override
            public void onText(String text) {
                specimen.setAntlersWidth(Utils.parseInt(text));
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseAntlersPointsLeftChoice(final Specimen specimen) {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_antlers_points_left));
        choiceView.setEditTextMaxLength(2);
        choiceView.setEditTextMaxValue(50);
        choiceView.setEditTextChoice(Utils.formatInt(specimen.getAntlerPointsLeft()), new OnTextListener() {
            @Override
            public void onText(String text) {
                specimen.setAntlerPointsLeft(Utils.parseInt(text));
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseAntlersPointsRightChoice(final Specimen specimen) {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_antlers_points_right));
        choiceView.setEditTextMaxLength(2);
        choiceView.setEditTextMaxValue(50);
        choiceView.setEditTextChoice(Utils.formatInt(specimen.getAntlerPointsRight()), new OnTextListener() {
            @Override
            public void onText(String text) {
                specimen.setAntlerPointsRight(Utils.parseInt(text));
            }
        });
        addChoiceView(choiceView, true);
    }

    private void createMooseAdditionalInfoChoice(final Specimen specimen) {
        ChoiceView choiceView = new ChoiceView(getActivity(), getString(R.string.moose_additional_info));
        choiceView.setEditTextChoice(specimen.getAdditionalInfo(), new OnTextListener() {
            @Override
            public void onText(String text) {
                specimen.setAdditionalInfo(text);
            }
        });
        choiceView.setEditTextMode(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 4);
        addChoiceView(choiceView, true);
    }

    private void addChoiceView(ChoiceView choiceView, boolean topMargin) {
        mMooseDetailsLayout.addView(choiceView);

        if (topMargin) {
            choiceView.setTopMargin(10);
        }
        choiceView.setChoiceEnabled(mEditModeEnabled);
    }

    private void updateLocationOnMap() {
        if (isAdded()) {
            String textFormat = getResources().getString(R.string.map_coordinates);

            if (mEvent.mCoordinates != null) {
                mCoordinateText.setText(String.format(textFormat, mEvent.mCoordinates.first.toString(), mEvent.mCoordinates.second.toString()));

                mMapView.onLocationUpdated(mEvent.mLocation);
            }
        }
    }

    private void refreshPermitNumber() {
        if (mEvent != null && !TextUtils.isEmpty(mEvent.mPermitNumber)) {
            mPermitCheckbox.setChecked(true);

            mPermitNumber.setVisibility(View.VISIBLE);
            mPermitNumber.setText(mEvent.mPermitNumber);
        } else {
            mPermitCheckbox.setChecked(false);

            mPermitNumber.setVisibility(View.GONE);
            mPermitNumber.setText(null);
        }
    }

    //
    // Species selection
    //

    public void refreshSpecies(Species species) {
        mSelectedSpecies = species;
        mSelectSpeciesButton.setSpecies(species);

        if (mSelectedSpecies == null || !mSelectedSpecies.mMultipleSpecimenAllowedOnHarvests) {
            mEvent.mAmount = 1;
        }

        refreshSpecimenDetailsState();
        refreshAmountInputState(mEditModeEnabled);
    }

    /**
     * Check if all required information for entry has been inserted and update submit button state accordingly.
     */
    void updateSubmitButton() {
        int amountInput = -1;
        try {
            amountInput = Integer.parseInt(mAmountInput.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean permitOk = PermitManager.getInstance(getActivity()).validateEntryPermitInformation(mEvent) &&
                HarvestValidator.validate(mEvent);

        if (mEvent.mLocalId <= 0) {
            if (mSelectedSpecies != null && mEvent.mLocation != null && amountInput > 0) {
                mSubmitButton.setEnabled(permitOk);
            } else {
                mSubmitButton.setEnabled(false);
            }
        } else {
            // Amount is required field
            mSubmitButton.setEnabled(permitOk && amountInput > 0);
        }
    }

    void setupWithItem(GameHarvest event) {
        mSelectedSpecies = SpeciesInformation.getSpecies(event.mSpeciesID);

        refreshSpecies(mSelectedSpecies);
        refreshAmountInputState(mEditModeEnabled);

        refreshHarvestStateIndicator(event);
        refreshSpecimenDetailsState();
        refreshPermitNumber();

        setTitle();
        updateLocationOnMap();

        mDescriptionEdit.setText(event.mMessage);
        mSubmitButton.setEnabled(true);
    }

    /**
     * Update traffic light color and state text
     *
     * @param event log entry containing state information
     */
    void refreshHarvestStateIndicator(GameHarvest event) {
        String permitState = event.mStateAcceptedToHarvestPermit;
        String reportState = event.mHarvestReportState;

        int trafficLightColor = Color.TRANSPARENT;
        String stateText = null;

        if (GameHarvest.PERMIT_PROPOSED.equals(permitState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.permit_proposed);
            stateText = getResources().getString(R.string.harvest_permit_proposed);
        } else if (GameHarvest.PERMIT_ACCEPTED.equals(permitState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.permit_accepted);
            stateText = getResources().getString(R.string.harvest_permit_accepted);
        } else if (GameHarvest.PERMIT_REJECTED.equals(permitState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.permit_rejected);
            stateText = getResources().getString(R.string.harvest_permit_rejected);
        } else if (GameHarvest.HARVEST_PROPOSED.equals(reportState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.harvest_proposed);
            stateText = getResources().getString(R.string.harvest_proposed);
        } else if (GameHarvest.HARVEST_SENT_FOR_APPROVAL.equals(reportState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.harvest_sent_for_approval);
            stateText = getResources().getString(R.string.harvest_sent_for_approval);
        } else if (GameHarvest.HARVEST_APPROVED.equals(reportState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.harvest_approved);
            stateText = getResources().getString(R.string.harvest_approved);
        } else if (GameHarvest.HARVEST_REJECTED.equals(reportState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.harvest_rejected);
            stateText = getResources().getString(R.string.harvest_rejected);
        } else if (GameHarvest.HARVEST_CREATEREPORT.equals(reportState)) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.harvest_create_report);
            stateText = getResources().getString(R.string.harvest_create_report);
        } else if (event.mHarvestReportRequired) {
            trafficLightColor = ContextCompat.getColor(getContext(), R.color.harvest_create_report);
            stateText = getResources().getString(R.string.harvest_create_report);
        }

        if (trafficLightColor != Color.TRANSPARENT && stateText != null) {
            Drawable circle = ContextCompat.getDrawable(getContext(), R.drawable.circle);
            circle.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);

            mHarvestStateLabel.setText(stateText);
            mHarvestStateLabel.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
            mHarvestStateLabel.setVisibility(View.VISIBLE);
        } else {
            mHarvestStateLabel.setVisibility(View.GONE);
        }
    }

    void setEditModeEnabled(boolean enabled) {
        mEditModeEnabled = enabled;

        mButtonView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mSubmitButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDismissButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDescriptionEdit.setEnabled(enabled);
        mDescriptionEdit.setHint(enabled ? getString(R.string.add_description) : null);
        refreshMooseInputState(enabled);
        refreshAmountInputState(enabled);
        refreshEditToolsState();

        Button speciesButton = mSelectSpeciesButton.getSpeciesButton();

        if (mEvent != null) {
            if (mEvent.isEditable()) {
                speciesButton.setEnabled(enabled);
                mSpecimenDetails.setEnabled(enabled);
                mPermitCheckbox.setEnabled(enabled);
                mPermitNumber.setEnabled(enabled);
            } else {
                speciesButton.setEnabled(false);
                mSpecimenDetails.setEnabled(false);
                mPermitCheckbox.setEnabled(false);
                mPermitNumber.setEnabled(false);
            }
        }
        speciesButton.setVisibility(enabled && TextUtils.isEmpty(mEvent.mPermitNumber) ? View.VISIBLE : View.GONE);
        mPermitCheckbox.setVisibility(enabled || !TextUtils.isEmpty(mEvent.mPermitNumber) ? View.VISIBLE : View.GONE);
        mPermitNumber.setVisibility(enabled && !TextUtils.isEmpty(mEvent.mPermitNumber) ? View.VISIBLE : View.GONE);
        mDiaryImageManager.setEditMode(enabled);
    }

    void refreshMooseInputState(boolean editMode) {
        for (int i = 0; i < mMooseDetailsLayout.getChildCount(); ++i) {
            ChoiceView choice = (ChoiceView) mMooseDetailsLayout.getChildAt(i);
            choice.setChoiceEnabled(editMode);
        }
    }

    void refreshAmountInputState(boolean editMode) {
        if (mSelectedSpecies != null && mSelectedSpecies.mMultipleSpecimenAllowedOnHarvests) {
            mAmountInput.setText(String.valueOf(mEvent.mAmount));
            mAmountInput.setEnabled(editMode && mEvent.isEditable());
            mAmountInput.setVisibility(View.VISIBLE);
            mAmountLabel.setVisibility(View.VISIBLE);
        } else {
            // Reset value when hiding field
            mAmountInput.setText(String.valueOf(1));
            mAmountInput.setEnabled(false);
            mAmountInput.setVisibility(View.GONE);
            mAmountLabel.setVisibility(View.GONE);
        }
    }

    public void viewMap() {
        Intent intent = new Intent(getActivity(), MapViewerActivity.class);
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, mEditModeEnabled && mEvent.isEditable());
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, mEvent.hasNonDefaultLocation() ? mEvent.mLocation : null);
        intent.putExtra(MapViewerActivity.EXTRA_NEW, mEvent.mLocalId <= 0);
        intent.putExtra(MapViewerActivity.EXTRA_LOCATION_SOURCE, mEvent.mLocationSource);
        startActivityForResult(intent, MapViewerActivity.LOCATION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MapViewerActivity.LOCATION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Location location = data.getParcelableExtra(MapViewerActivity.RESULT_LOCATION);

                if (mEvent != null) {
                    mEvent.mLocation = location;
                    mEvent.mAccuracy = location.getAccuracy();
                    mEvent.mHasAltitude = location.hasAltitude();
                    mEvent.mAltitude = location.getAltitude();
                    mEvent.mCoordinates = MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude());
                    mEvent.mLocationSource = data.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE);
                }
            }
        } else if (requestCode == ChooseSpeciesActivity.SPECIES_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectedSpecies = (Species) data.getSerializableExtra(ChooseSpeciesActivity.RESULT_SPECIES);
                mEvent.mSpeciesID = mSelectedSpecies.mId;
            }
        } else if (requestCode == SPECIMENS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<Specimen> specimens = (List<Specimen>) data.getSerializableExtra(HarvestSpecimensActivity.RESULT_SPECIMENS);

                for (Iterator<Specimen> iter = specimens.listIterator(); iter.hasNext(); ) {
                    Specimen specimen = iter.next();
                    if (specimen.isEmpty()) {
                        iter.remove();
                    }
                }

                mEvent.mSpecimen.clear();
                mEvent.mSpecimen.addAll(specimens);
            }
        } else if (requestCode == HarvestPermitActivity.PERMIT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String permitNumber = data.getStringExtra(HarvestPermitActivity.RESULT_PERMIT_NUMBER);
                String permitType = data.getStringExtra(HarvestPermitActivity.RESULT_PERMIT_TYPE);
                int species = data.getIntExtra(HarvestPermitActivity.RESULT_PERMIT_SPECIES, -1);

                mEvent.mPermitNumber = permitNumber;
                mEvent.mPermitType = permitType;
                mEvent.mSpeciesID = species;

                mSelectedSpecies = SpeciesInformation.getSpecies(species);
                mPermitNumber.setText(permitNumber);
            }
        } else {
            mDiaryImageManager.handleImageResult(requestCode, resultCode, data);
        }
    }

    //
    // DiaryImageManager methods
    //

    @Override
    public void viewImage(final String uuid) {
        List<LogImage> images = GameDatabase.getInstance().getAllLogImages();

        Intent intent = ImageViewerActivity.createIntent(getActivity(), images, uuid);
        intent.putExtra(ImageViewerActivity.EXTRA_ISHARVEST, true);
        startActivity(intent);
    }

    @WorkMessageHandler(ChangeHarvestMessage.class)
    public void onChangeHarvestMessage(ChangeHarvestMessage message) {
        if (message.localId == mEvent.mLocalId) {
            return;
        }

        final Activity activity = getActivity();

        GameHarvest harvest = GameDatabase.getInstance().getEventByLocalId(message.localId);

        if (harvest != null) {
            activity.getIntent().putExtra(HarvestActivity.EXTRA_HARVEST, harvest);
            activity.getIntent().putExtra(HarvestActivity.EXTRA_NEW, false);

            activity.recreate();
        }
    }
}
