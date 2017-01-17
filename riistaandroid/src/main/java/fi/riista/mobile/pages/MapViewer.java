package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import fi.riista.mobile.EntryMapView;
import fi.riista.mobile.LocationClient;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.MapViewerActivity;
import fi.riista.mobile.models.LogEventBase;
import fi.riista.mobile.utils.AppPreferences;

public class MapViewer extends DetailsPageFragment implements LocationListener, OnMapReadyCallback {

    private boolean mEditMode = false;
    private Location mStartLocation;
    private boolean mNewItem = false;
    private String mLocationSource;

    private EntryMapView mMapView;
    private LocationClient mLocationClient;
    private Location mCurrentGpsLocation;

    private ImageView mCrosshair;
    private Button mGpsPositionButton;
    private Button mSetPositionButton;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mEditMode = arguments.getBoolean(MapViewerActivity.EXTRA_EDIT_MODE, false);
        mStartLocation = arguments.getParcelable(MapViewerActivity.EXTRA_START_LOCATION);
        mNewItem = arguments.getBoolean(MapViewerActivity.EXTRA_NEW, false);
        mLocationSource = arguments.getString(MapViewerActivity.EXTRA_LOCATION_SOURCE, LogEventBase.LOCATION_SOURCE_MANUAL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapviewer, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (EntryMapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);
        mMapView.setup(inflater.getContext(), false, true, AppPreferences.getMapTileSource(inflater.getContext()));
        mMapView.getMapAsync(this);

        mCrosshair = (ImageView) view.findViewById(R.id.map_crosshair);
        mGpsPositionButton = (Button) view.findViewById(R.id.goToGpsPosButton);
        mSetPositionButton = (Button) view.findViewById(R.id.moveMarkerButton);

        setupActionButtons();
        setHasOptionsMenu(true);

        return view;
    }

    private void setupActionButtons() {
        mGpsPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentGpsLocation != null) {
                    mMapView.animateCameraTo(mCurrentGpsLocation);
                }
            }
        });

        mSetPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = mMapView.getCameraLocation();

                updateLocation(location, mLocationSource.equals(LogEventBase.LOCATION_SOURCE_MANUAL));

                Intent result = new Intent();
                result.putExtra(MapViewerActivity.RESULT_LOCATION, location);
                result.putExtra(MapViewerActivity.RESULT_LOCATION_SOURCE, mLocationSource);

                BaseActivity activity = (BaseActivity) getActivity();
                activity.setResult(Activity.RESULT_OK, result);
                activity.finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        setEditMode(mEditMode);

        mMapView.onResume();

        if (canEditLocation()) {
            mLocationClient = ((BaseActivity) getActivity()).getLocationClient();
            mLocationClient.addListener(this);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mLocationClient != null) {
            mLocationClient.removeListener(this);
        }
        mMapView.onPause();
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
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.map_viewer, menu);
        MenuItem addItem = menu.findItem(R.id.select_map_layer);
        addItem.setVisible(AppPreferences.getMapTileSource(getActivity()) != AppPreferences.MapTileSource.GOOGLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_map_layer:
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.setting_spinner_dropdown_item);
                arrayAdapter.add(getString(R.string.map_type_topographic));
                arrayAdapter.add(getString(R.string.map_type_background));
                arrayAdapter.add(getString(R.string.map_type_aerial));

                AppPreferences.MapTileSource tileSource = AppPreferences.getMapTileSource(getActivity());

                // Defaults to MML topographic
                int selectedIndex = 0;
                if (tileSource == AppPreferences.MapTileSource.MML_BACKGROUND) {
                    selectedIndex = 1;
                } else if (tileSource == AppPreferences.MapTileSource.MML_AERIAL) {
                    selectedIndex = 2;
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.map_type_select))
                        .setSingleChoiceItems(arrayAdapter, selectedIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                switch (which) {
                                    case 0:
                                        AppPreferences.setMapTileSource(getActivity(), AppPreferences.MapTileSource.MML_TOPOGRAPHIC);
                                        if (mMapView != null) {
                                            mMapView.setMapTileType(AppPreferences.MapTileSource.MML_TOPOGRAPHIC);
                                        }
                                        break;
                                    case 1:
                                        AppPreferences.setMapTileSource(getActivity(), AppPreferences.MapTileSource.MML_BACKGROUND);
                                        if (mMapView != null) {
                                            mMapView.setMapTileType(AppPreferences.MapTileSource.MML_BACKGROUND);
                                        }
                                        break;
                                    case 2:
                                        AppPreferences.setMapTileSource(getActivity(), AppPreferences.MapTileSource.MML_AERIAL);
                                        if (mMapView != null) {
                                            mMapView.setMapTileType(AppPreferences.MapTileSource.MML_AERIAL);
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mStartLocation != null) {
            setInitialLocation(mStartLocation);
        }

        // Only show gps state indicator when creating new entry.
        if (mNewItem) {
            mMapView.setShowInfoWindow(true);
            mMapView.setShowAccuracy(true);
        } else {
            mMapView.setShowInfoWindow(false);
            mMapView.setShowAccuracy(true);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentGpsLocation = location;
        mGpsPositionButton.setEnabled(mCurrentGpsLocation != null);

        // Always show and save latest gps position when when creating entry unless manually set already.
        if (mNewItem && mLocationSource.equals(LogEventBase.LOCATION_SOURCE_GPS)) {
            mMapView.refreshLocationIndicators(location);

            mLocationSource = LogEventBase.LOCATION_SOURCE_MANUAL;
        }
    }

    private void updateLocation(Location location, boolean isSetManually) {
        // Accuracy and altitude data is invalid when setting manual location.
        if (isSetManually) {
            location.setAccuracy(0);

            if (location.hasAltitude()) {
                location.setAltitude(0);
            }
        }
    }

    public void setInitialLocation(Location location) {
        if (location != null) {
            mMapView.moveCameraTo(location);
            mMapView.refreshLocationIndicators(location);
        }
    }

    private boolean canEditLocation() {
        return mEditMode;
    }

    private void setEditMode(boolean enabled) {
        mEditMode = enabled;

        mCrosshair.setVisibility(canEditLocation() ? View.VISIBLE : View.GONE);

        mGpsPositionButton.setEnabled(canEditLocation() && mCurrentGpsLocation != null);
        mGpsPositionButton.setVisibility(canEditLocation() ? View.VISIBLE : View.GONE);

        mSetPositionButton.setEnabled(canEditLocation());
        mSetPositionButton.setVisibility(canEditLocation() ? View.VISIBLE : View.GONE);
    }
}
