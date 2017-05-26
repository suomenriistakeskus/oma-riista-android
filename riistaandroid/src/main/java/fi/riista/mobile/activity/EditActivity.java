package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.joda.time.DateTime;

import java.util.List;

import fi.riista.mobile.DiaryImageManager;
import fi.riista.mobile.DiaryImageManager.DiaryImageManagerInterface;
import fi.riista.mobile.EntryMapView;
import fi.riista.mobile.LocationClient;
import fi.riista.mobile.R;
import fi.riista.mobile.message.EventUpdateMessage;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.LogEventBase;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.pages.ObservationEditFragment;
import fi.riista.mobile.pages.SrvaEditFragment;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.riista.mobile.ui.EditToolsView;
import fi.riista.mobile.ui.EditToolsView.OnDateTimeListener;
import fi.riista.mobile.ui.EditToolsView.OnDeleteListener;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.MapUtils;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewOnClick;

public class EditActivity extends BaseActivity implements OnMapReadyCallback, DiaryImageManagerInterface, LocationListener {

    public static final String RESULT_DID_SAVE = "result_entry_saved";

    public static final int NEW_OBSERVATION_REQUEST_CODE = 301;
    public static final int EDIT_OBSERVATION_REQUEST_CODE = 302;
    public static final int NEW_SRVA_REQUEST_CODE = 401;
    public static final int EDIT_SRVA_REQUEST_CODE = 402;

    public static final String EXTRA_OBSERVATION = "extra_observation";
    public static final String EXTRA_SRVA_EVENT = "extra_srva_event";
    public static final String EXTRA_NEW = "extra_new";

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    //Events passed to edit fragments (observation, SRVA etc.)
    public interface EditListener {
        void onEditStart();

        void onEditCancel();

        void onEditSave();

        void onDelete();

        void onDateChanged(DateTime date);

        void onLocationChanged(Location location, String source);

        void onDescriptionChanged(String description);

        void onImagesChanged(List<LogImage> images);

        void onViewImage(String uuid);
    }

    //Data queried from edit fragments
    public interface EditBridge {
        DateTime getDate();

        Location getLocation();

        String getLocationSource();

        String getDescription();

        boolean isEditable();

        List<LogImage> getImages();
    }

    private LocationClient mLocationClient;
    private EditListener mEditListener;
    private EditBridge mEditBridge;
    private DiaryImageManager mDiaryImageManager;
    private boolean mEditMode = false;
    private boolean mNew = false;
    private boolean mLocationSelectedManually = false;
    private long mCurrentObservationId = -1;

    @ViewId(R.id.view_edit_tools)
    private EditToolsView mEditTools;

    @ViewId(R.id.mapView)
    private EntryMapView mMapView;

    @ViewId(R.id.txt_edit_location)
    private TextView mLocationText;

    //Images

    @ViewId(R.id.diaryimages)
    private LinearLayout mImagesLayout;

    @ViewId(R.id.logDescription)
    private EditText mDescriptionEdit;

    //Cancel and save

    @ViewId(R.id.layout_edit_state_buttons)
    private View mEditStateButtonsLayout;

    @ViewId(R.id.btn_edit_cancel)
    private Button mCancelEditButton;

    @ViewId(R.id.btn_edit_save)
    private Button mSaveEditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null); //Don't restore state, it is done manually

        if (!ObservationMetadataHelper.getInstance().hasMetadata() ||
                !SrvaParametersHelper.getInstance().hasParameters()) {
            //Can't do anything until the client downloads the metadata
            finish();
            return;
        }

        setContentView(R.layout.activity_edit);

        ViewAnnotations.apply(this);

        Intent intent = getIntent();

        mNew = intent.getBooleanExtra(EXTRA_NEW, false);
        mEditMode = mNew;

        GameObservation observation = (GameObservation) intent.getSerializableExtra(EXTRA_OBSERVATION);
        SrvaEvent srvaEvent = (SrvaEvent) intent.getSerializableExtra(EXTRA_SRVA_EVENT);

        if (observation != null) {
            if (observation.localId != null) {
                mCurrentObservationId = observation.localId;
            }
            setCustomTitle(getString(R.string.observation));

            ObservationEditFragment fragment = ObservationEditFragment.newInstance(observation);
            getSupportFragmentManager().beginTransaction().add(R.id.layout_edit_fragment_container, fragment).commit();
        } else if (srvaEvent != null) {
            setCustomTitle(getString(R.string.srva));

            SrvaEditFragment fragment = SrvaEditFragment.newInstance(srvaEvent);
            getSupportFragmentManager().beginTransaction().add(R.id.layout_edit_fragment_container, fragment).commit();
        } else {
            finish();
        }

        initButtons();
        initMapView(savedInstanceState);
        initDescriptionTextWatcher();

        mDiaryImageManager = new DiaryImageManager(getWorkContext(), this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //Don't restore state
    }

    public void finishEdit(String type, DateTime time, long localId) {
        int huntingYear = DateTimeUtils.getSeasonStartYearFromDate(time.toCalendar(null));
        EventUpdateMessage message = new EventUpdateMessage(type, localId, huntingYear);

        getWorkContext().sendGlobalMessage(message);

        Intent result = new Intent();
        result.putExtra(EditActivity.RESULT_DID_SAVE, true);
        this.setResult(Activity.RESULT_OK, result);
        this.finish();
    }

    private void initDescriptionTextWatcher() {
        mDescriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mEditListener.onDescriptionChanged(s.toString());
            }
        });
    }

    public void setEditValid(boolean valid) {
        mSaveEditButton.setEnabled(valid);
    }

    public boolean isEditModeOn() {
        return mEditMode;
    }

    public void connectEditFragment(EditListener listener, EditBridge bridge) {
        mEditListener = listener;
        mEditBridge = bridge;

        updateViews();

        startEdit(mEditMode);
    }

    private void updateViews() {
        updateTimeText();
        updateLocationText();
        updateDescriptionText();
        updateImages();
    }

    @ViewOnClick(R.id.btn_edit_date)
    protected void onEditDateButtonClicked(View view) {
        showDateTimeDialog();
    }

    private void showDateTimeDialog() {
        DateTime date = mEditBridge.getDate();
        mEditTools.showDateTimeDialog(date, new OnDateTimeListener() {
            @Override
            public void onDateTime(DateTime dateTime) {
                mEditListener.onDateChanged(dateTime);
                updateTimeText();
            }
        });
    }

    private void updateTimeText() {
        mEditTools.setDateTimeText(mEditBridge.getDate());
    }

    private void updateLocationText() {
        Location loc = mEditBridge.getLocation();
        if (loc != null) {
            Pair<Long, Long> location = MapUtils.WGS84toETRSTM35FIN(loc.getLatitude(), loc.getLongitude());

            String textFormat = getResources().getString(R.string.map_coordinates);
            mLocationText.setText(String.format(textFormat, location.first.toString(), location.second.toString()));
        } else {
            mLocationText.setText("");
        }
    }

    public void updateDescriptionText() {
        String description = mEditBridge.getDescription();
        if (description != null) {
            mDescriptionEdit.setText(description);
        } else {
            mDescriptionEdit.setText("");
        }
    }

    private void updateImages() {
        List<LogImage> images = mEditBridge.getImages();
        mDiaryImageManager.setItems(images);
        mDiaryImageManager.setup(mImagesLayout);
    }

    @ViewOnClick(R.id.btn_edit_cancel)
    protected void onCancelEditButtonClicked(View view) {
        if (mNew) {
            finish();
        } else {
            startEdit(false);

            if (mEditListener != null) {
                mEditListener.onEditCancel();

                updateViews();
            }
        }
    }

    @ViewOnClick(R.id.btn_edit_save)
    protected void onSaveButtonClicked(View view) {
        mEditListener.onEditSave();
    }

    @ViewOnClick(R.id.btn_edit_delete)
    protected void onDeleteButtonClicked(View view) {
        mEditTools.showDeleteDialog(new OnDeleteListener() {
            @Override
            public void onDelete() {
                if (mEditListener != null) {
                    mEditListener.onDelete();
                } else {
                    finish();
                }
            }
        });
    }

    @ViewOnClick(R.id.btn_edit_start)
    protected void onStartEditButtonClicked(View view) {
        startEdit(true);
    }

    private void startEdit(boolean start) {
        mEditMode = start && mEditBridge.isEditable();
        mDiaryImageManager.setEditMode(mEditMode);

        mEditTools.setButtonEnabled(mEditTools.getDateButton(), mEditMode);

        mEditTools.getEditButton().setVisibility((mEditBridge.isEditable() && !mEditMode) ? View.VISIBLE : View.INVISIBLE);

        mEditTools.getDeleteButton().setVisibility((mEditBridge.isEditable() && !mNew) ? View.VISIBLE : View.INVISIBLE);

        mEditStateButtonsLayout.setVisibility(mEditMode ? View.VISIBLE : View.GONE);

        mDescriptionEdit.setEnabled(mEditMode);

        if (mEditListener != null) {
            mEditListener.onEditStart();
        }
    }

    private void initButtons() {
        mEditStateButtonsLayout.setVisibility(View.GONE);
    }

    private void initMapView(Bundle savedInstanceState) {
        AppPreferences.MapTileSource tileSource
                = AppPreferences.getMapTileSource(this) == AppPreferences.MapTileSource.GOOGLE
                ? AppPreferences.MapTileSource.GOOGLE
                : AppPreferences.MapTileSource.MML_TOPOGRAPHIC;

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);

        mMapView.setup(this, false, true, tileSource);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mLocationClient = getLocationClient();
        mLocationClient.addListener(this);

        updateMapPosition();

        if (mNew) {
            mMapView.setShowInfoWindow(true);
            mMapView.setShowAccuracy(true);
        }

        map.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loaction) {
                viewMap();
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                viewMap();
                return true;
            }
        });
    }

    private void viewMap() {
        Intent intent = new Intent(EditActivity.this, MapViewerActivity.class);
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, mEditMode);
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, currentLocation());
        intent.putExtra(MapViewerActivity.EXTRA_NEW, mNew);
        intent.putExtra(MapViewerActivity.EXTRA_LOCATION_SOURCE, mEditBridge.getLocationSource());
        startActivityForResult(intent, MapViewerActivity.LOCATION_REQUEST_CODE);
    }

    private Location currentLocation() {
        return mEditBridge.getLocation();
    }

    private void updateMapPosition() {
        Location location = currentLocation();
        if (location != null) {
            mMapView.moveCameraTo(location);
            mMapView.refreshLocationIndicators(location);
        }

        updateLocationText();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mMapView != null) {
            mMapView.onStart();
        }
    }

    @Override
    public void onStop() {
        if (mMapView != null) {
            mMapView.onStop();
        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMapView != null) {
            mMapView.onDestroy();
        }

        if (mLocationClient != null) {
            mLocationClient.removeListener(this);
            mLocationClient = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapView != null) {
            Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
            if (mapViewBundle == null) {
                mapViewBundle = new Bundle();
                outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
            }
            mMapView.onSaveInstanceState(mapViewBundle);
        }
    }

    @Override
    public void viewImage(String uuid) {
        if (mEditListener != null) {
            mEditListener.onViewImage(uuid);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MapViewerActivity.LOCATION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mLocationSelectedManually = true;

                Location location = data.getParcelableExtra(MapViewerActivity.RESULT_LOCATION);
                String source = data.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE);
                setLocation(location, source);
            }
        } else if (requestCode == DiaryImageManager.REQUEST_IMAGE_CAPTURE ||
                requestCode == DiaryImageManager.REQUEST_SELECT_PHOTO) {
            mDiaryImageManager.handleImageResult(requestCode, resultCode, data);
            mEditListener.onImagesChanged(mDiaryImageManager.getLogImages());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setLocation(Location location, String source) {
        mEditListener.onLocationChanged(location, source);
        updateMapPosition();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLocationSelectedManually) {
            return;
        }

        if (mNew && (mEditBridge.getLocation() == null || LogEventBase.LOCATION_SOURCE_GPS.equals(mEditBridge.getLocationSource()))) {
            setLocation(location, LogEventBase.LOCATION_SOURCE_GPS);
        }
        updateMapPosition();
    }
}
