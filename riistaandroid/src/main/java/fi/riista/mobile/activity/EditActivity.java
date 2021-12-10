package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.joda.time.DateTime;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import fi.riista.mobile.EntryMapView;
import fi.riista.mobile.LocationClient;
import fi.riista.mobile.R;
import fi.riista.mobile.message.EventUpdateMessage;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.pages.ObservationEditFragment;
import fi.riista.mobile.pages.SrvaEditFragment;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.EditUtils;
import fi.riista.mobile.utils.MapUtils;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewOnClick;

public class EditActivity extends BaseActivity implements OnMapReadyCallback, LocationListener {

    public static final String RESULT_DID_SAVE = "result_entry_saved";

    public static final String EXTRA_OBSERVATION = "extra_observation";
    public static final String EXTRA_SRVA_EVENT = "extra_srva_event";
    public static final String EXTRA_NEW = "extra_new";

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Inject
    ObservationMetadataHelper mObservationMetadataHelper;

    private LocationClient mLocationClient;
    private EditListener mEditListener;
    private EditBridge mEditBridge;
    private boolean mEditMode = false;
    private boolean mNew = false;
    private boolean mLocationSelectedManually = false;

    @ViewId(R.id.mapView)
    private EntryMapView mMapView;
    @ViewId(R.id.txt_edit_location)
    private TextView mLocationText;

    @ViewId(R.id.logDescription)
    private EditText mDescriptionEdit;

    private MenuItem mEditItem;
    private MenuItem mDeleteItem;

    // Images
    @ViewId(R.id.layout_edit_state_buttons)
    private View mEditStateButtonsLayout;
    @ViewId(R.id.btn_edit_cancel)
    private Button mCancelEditButton;

    // Cancel and save
    @ViewId(R.id.btn_edit_save)
    private Button mSaveEditButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(null); // Don't restore state, it is done manually

        if (!mObservationMetadataHelper.hasMetadata() || !SrvaParametersHelper.getInstance().hasParameters()) {
            // Can't do anything until the client downloads the metadata.
            finish();
            return;
        }

        setContentView(R.layout.activity_edit);

        ViewAnnotations.apply(this);

        final Intent intent = getIntent();

        mNew = intent.getBooleanExtra(EXTRA_NEW, false);
        mEditMode = mNew;

        final GameObservation observation = (GameObservation) intent.getSerializableExtra(EXTRA_OBSERVATION);
        final SrvaEvent srvaEvent = (SrvaEvent) intent.getSerializableExtra(EXTRA_SRVA_EVENT);

        if (observation != null) {
            setCustomTitle(getString(R.string.observation));

            final ObservationEditFragment fragment = ObservationEditFragment.newInstance(observation);
            getSupportFragmentManager().beginTransaction().add(R.id.layout_edit_fragment_container, fragment).commit();

        } else if (srvaEvent != null) {
            setCustomTitle(getString(R.string.srva));

            final SrvaEditFragment fragment = SrvaEditFragment.newInstance(srvaEvent);
            getSupportFragmentManager().beginTransaction().add(R.id.layout_edit_fragment_container, fragment).commit();

        } else {
            finish();
        }

        initButtons();
        initMapView(savedInstanceState);
        initDescriptionTextWatcher();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        // Don't restore state
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        mEditItem = menu.findItem(R.id.item_edit);
        mEditItem.setVisible(mEditBridge.isEditable() && !mEditMode);

        mDeleteItem = menu.findItem(R.id.item_delete);
        mDeleteItem.setVisible(mEditBridge.isEditable() && !mNew);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.item_edit:
                startEdit(true);
                break;
            case R.id.item_delete:
                EditUtils.showDeleteDialog(this, () -> {
                    if (mEditListener != null) {
                        mEditListener.onDelete();
                    } else {
                        finish();
                    }
                });
        }

        return super.onOptionsItemSelected(item);
    }

    public void finishEdit(final String type, final DateTime time, final long localId) {
        final int huntingYear = DateTimeUtils.getHuntingYearForCalendar(time.toCalendar(null));
        final EventUpdateMessage message = new EventUpdateMessage(type, localId, huntingYear);

        getWorkContext().sendGlobalMessage(message);

        final Intent result = new Intent();
        result.putExtra(EditActivity.RESULT_DID_SAVE, true);
        this.setResult(Activity.RESULT_OK, result);
        this.finish();
    }

    private void initDescriptionTextWatcher() {
        mDescriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                mEditListener.onDescriptionChanged(s.toString());
            }
        });
    }

    public void setEditValid(final boolean valid) {
        mSaveEditButton.setEnabled(valid);
    }

    public boolean isEditModeOn() {
        return mEditMode;
    }

    public void connectEditFragment(final EditListener listener, final EditBridge bridge) {
        mEditListener = listener;
        mEditBridge = bridge;

        updateViews();

        startEdit(mEditMode);
    }

    private void updateViews() {
        updateLocationText();
        updateDescriptionText();
    }

    private void updateLocationText() {
        final Location loc = mEditBridge.getLocation();
        final String text;

        if (loc != null) {
            final Pair<Long, Long> location = MapUtils.WGS84toETRSTM35FIN(loc.getLatitude(), loc.getLongitude());
            final String textFormat = getResources().getString(R.string.map_coordinates);

            text = String.format(textFormat, location.first.toString(), location.second.toString());
        } else {
            text = "";
        }

        mLocationText.setText(text);
    }

    public void updateDescriptionText() {
        final String description = mEditBridge.getDescription();
        mDescriptionEdit.setText(description != null ? description : "");
    }

    @ViewOnClick(R.id.btn_edit_cancel)
    protected void onCancelEditButtonClicked(final View view) {
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
    protected void onSaveButtonClicked(final View view) {
        mEditListener.onEditSave();
    }

    private void startEdit(final boolean start) {
        mEditMode = start && mEditBridge.isEditable();

        if (mEditItem != null) {
            mEditItem.setVisible(mEditBridge.isEditable() && !mEditMode);
        }
        if (mDeleteItem != null) {
            mDeleteItem.setVisible(mEditBridge.isEditable() && !mNew);
        }

        mEditStateButtonsLayout.setVisibility(mEditMode ? View.VISIBLE : View.GONE);

        mDescriptionEdit.setEnabled(mEditMode);

        if (mEditListener != null) {
            if (mEditMode) {
                mEditListener.onEditStart();
            } else {
                mEditListener.onEditCancel();
            }
        }
    }

    private void initButtons() {
        mEditStateButtonsLayout.setVisibility(View.GONE);
    }

    private void initMapView(final Bundle savedInstanceState) {
        final Bundle mapViewBundle = savedInstanceState != null
                ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
                : null;
        mMapView.onCreate(mapViewBundle);

        mMapView.setup(this, false, true);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mLocationClient = getLocationClient();
        mLocationClient.addListener(this);

        updateMapPosition();

        if (mNew) {
            mMapView.setShowInfoWindow(true);
            mMapView.setShowAccuracy(true);
        }

        map.setOnMapClickListener(location -> viewMap());
        map.setOnMarkerClickListener(marker -> {
            viewMap();
            return true;
        });
    }

    private void viewMap() {
        final Intent intent = new Intent(this, MapViewerActivity.class);
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
        final Location location = currentLocation();

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
            mMapView.setMapTileType(AppPreferences.getMapTileSource(this));
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
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapView != null) {
            Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
            if (mapViewBundle == null) {
                mapViewBundle = new Bundle();
                outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
            }
            mMapView.onSaveInstanceState(mapViewBundle);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == MapViewerActivity.LOCATION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mLocationSelectedManually = true;

                final Location location = data.getParcelableExtra(MapViewerActivity.RESULT_LOCATION);
                final String source = data.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE);

                setLocation(location, source);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setLocation(final Location location, final String source) {
        mEditListener.onLocationChanged(location, source);
        updateMapPosition();
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (!mLocationSelectedManually) {
            if (mNew && (mEditBridge.getLocation() == null
                    || GameLog.LOCATION_SOURCE_GPS.equals(mEditBridge.getLocationSource()))) {

                setLocation(location, GameLog.LOCATION_SOURCE_GPS);
            }
            updateMapPosition();
        }
    }

    // Events passed to edit fragments (observation, SRVA etc.)
    public interface EditListener {
        void onEditStart();

        void onEditCancel();

        void onEditSave();

        void onDelete();

        void onLocationChanged(Location location, String source);

        void onDescriptionChanged(String description);
    }

    // Data queried from edit fragments
    public interface EditBridge {
        Location getLocation();

        String getLocationSource();

        String getDescription();

        boolean isEditable();
    }
}
