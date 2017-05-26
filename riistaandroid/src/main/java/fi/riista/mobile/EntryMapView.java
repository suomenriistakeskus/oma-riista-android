package fi.riista.mobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import fi.riista.mobile.ui.MyInfoWindowAdapter;
import fi.riista.mobile.utils.AppPreferences;

/**
 * MapView wrapper
 * Call {@link #setup(android.content.Context, boolean, boolean, fi.riista.mobile.utils.AppPreferences.MapTileSource)} before other usage.
 */
public class EntryMapView extends MapView implements OnMapReadyCallback {
    private static final float MAP_ZOOM_LEVEL_MIN = 4;
    private static final float MAP_ZOOM_LEVEL_MAX = 16;
    private static final float MAP_ZOOM_LEVEL_DEFAULT = 15;

    private GoogleMap mMap;
    private Marker mLocationMarker;
    private Circle mAccuracyCircle;

    private boolean mShowInfoWindow;
    private boolean mShowAccuracy;
    private AppPreferences.MapTileSource mTiletype;

    private MyInfoWindowAdapter mMyInfoWindowAdapter;
    private MmlTileProvider mTileProvider;
    private TileOverlay mMmlOverlay;

    private TextView mCopyrightText;

    // Stores location in case map is not yet ready. Once map loading is finished this location will be displayed.
    private Location mUpdatedLocation;

    public EntryMapView(Context context) {
        this(context, null);
    }

    public EntryMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(Context context, boolean showGpsLevel, boolean showAccuracy, AppPreferences.MapTileSource tileType) {
        mShowInfoWindow = showGpsLevel;
        mShowAccuracy = showAccuracy;
        mTiletype = tileType;

        getMapAsync(this);
        MapsInitializer.initialize(context);

        mMyInfoWindowAdapter = new MyInfoWindowAdapter(context);
    }

    public void setShowInfoWindow(boolean show) {
        if (show && !mShowInfoWindow) {
            mMap.setInfoWindowAdapter(mMyInfoWindowAdapter);
        } else if (!show && mShowInfoWindow) {
            mMap.setInfoWindowAdapter(null);
        }
        mShowInfoWindow = show;
    }

    public void setShowAccuracy(boolean show) {
        mShowAccuracy = show;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setContentDescription(null);

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        if (mShowInfoWindow) {
            mMap.setInfoWindowAdapter(mMyInfoWindowAdapter);
        }
        setUpMap();
    }

    @SuppressLint("RtlHardcoded")
    private void setUpMap() {
        if (mMap == null) {
            return;
        }

        if (mTiletype != AppPreferences.MapTileSource.GOOGLE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            mMap.setMyLocationEnabled(false);
            mMap.setMaxZoomPreference(MAP_ZOOM_LEVEL_MAX);
            mMap.setMinZoomPreference(MAP_ZOOM_LEVEL_MIN);

            mTileProvider = new MmlTileProvider(256, 256);
            mTileProvider.setMapType(mTiletype);

            mMmlOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider).zIndex(1f));

            if (mCopyrightText == null) {
                mCopyrightText = setupCopyright();
                addView(mCopyrightText);
            } else {
                mCopyrightText.setText(R.string.map_copyright_mml);
            }
        }

        if (mUpdatedLocation != null) {
            onLocationUpdated(mUpdatedLocation);
            mUpdatedLocation = null;
        }
    }

    private TextView setupCopyright() {
        // Display copyright text in bottom right corner
        TextView copyrightText = new TextView(getContext());
        copyrightText.setEnabled(false);
        copyrightText.setText(R.string.map_copyright_mml);
        copyrightText.setPadding(0, 0, 5, 0);
        copyrightText.setGravity(Gravity.BOTTOM | Gravity.RIGHT);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, copyrightText.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, copyrightText.getId());
        copyrightText.setLayoutParams(params);
        return copyrightText;
    }

    public void setMapTileType(AppPreferences.MapTileSource tileType) {
        mTiletype = tileType;

        if (mTileProvider != null) {
            mTileProvider.setMapType(tileType);
        }
        if (mMmlOverlay != null) {
            mMmlOverlay.clearTileCache();
        }
    }

    public void onLocationUpdated(Location location) {
        moveCameraTo(location);
        refreshGpsInfoWindow(location);
        refreshLocationIndicators(location);
    }

    public void refreshLocationIndicators(Location location) {
        // Location pinpoint marker
        if (mLocationMarker != null) {
            mLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        } else if (mMap != null) {
            MarkerOptions options = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));
            mLocationMarker = mMap.addMarker(options);
            mLocationMarker.showInfoWindow();
        }

        refreshGpsInfoWindow(location);
        refreshAccuraryIndicator(location);
    }

    private void refreshGpsInfoWindow(Location location) {
        if (mShowInfoWindow && mMyInfoWindowAdapter != null) {
            mMyInfoWindowAdapter.newLocation(location);
            if (mLocationMarker != null) {
                mLocationMarker.showInfoWindow();
            }
        } else if (mMap != null) {
            mMap.setInfoWindowAdapter(null);
        }
    }

    private void refreshAccuraryIndicator(Location location) {
        if (mShowAccuracy) {
            // Accuracy circle marker
            if (mAccuracyCircle != null) {
                mAccuracyCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
                mAccuracyCircle.setRadius(location.getAccuracy());
            } else if (mMap != null) {
                CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()));
                circleOptions.fillColor(getResources().getColor(R.color.accuracy_circle_fill_color));
                circleOptions.strokeColor(android.R.color.black);
                circleOptions.strokeWidth(1);
                circleOptions.radius(location.getAccuracy());
                circleOptions.zIndex(2f);
                mAccuracyCircle = mMap.addCircle(circleOptions);
            }
        } else {
            if (mAccuracyCircle != null) {
                mAccuracyCircle.remove();
                mAccuracyCircle = null;
            }
        }
    }

    public void moveCameraTo(Location location) {
        if (mMap == null) {
            // Map not yet ready, store location for later
            mUpdatedLocation = location;
            return;
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAP_ZOOM_LEVEL_DEFAULT));
    }

    public void animateCameraTo(Location location) {
        if (mMap == null) {
            // Map not yet ready, store location for later
            mUpdatedLocation = location;
            return;
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAP_ZOOM_LEVEL_DEFAULT));
    }

    /**
     * Get map view camera location coordinates
     *
     * @return Location map view center coordinates
     */
    public Location getCameraLocation() {
        Location location = null;

        if (mMap != null) {
            LatLng coordinates = mMap.getCameraPosition().target;

            location = new Location("");
            location.setLatitude(coordinates.latitude);
            location.setLongitude(coordinates.longitude);
        }

        return location;
    }
}
