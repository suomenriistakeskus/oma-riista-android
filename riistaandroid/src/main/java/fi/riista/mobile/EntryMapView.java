package fi.riista.mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import fi.riista.mobile.vectormap.VectorTileProvider;

/**
 * MapView wrapper
 * Call {@link #setup(android.content.Context, boolean, boolean)} before other usage.
 */
public class EntryMapView extends MapView implements OnMapReadyCallback {

    public static final float MAP_ZOOM_LEVEL_MIN = 4;
    public static final float MAP_ZOOM_LEVEL_MAX = 16;
    public static final float MAP_ZOOM_LEVEL_DEFAULT = 15;

    private GoogleMap mMap;
    private Marker mLocationMarker;
    private Circle mAccuracyCircle;

    private boolean mShowInfoWindow;
    private boolean mShowAccuracy;
    private boolean mShowLocation = true;
    private AppPreferences.MapTileSource mTiletype;

    private MyInfoWindowAdapter mMyInfoWindowAdapter;
    private TileOverlay mMmlOverlay;
    private TileOverlay mVectorOverlay;
    private TileOverlay mVectorOverlay2;

    private TileOverlay mValtionmaatOverlay;
    private TileOverlay mRhyBordersOverlay;
    private TileOverlay mMhMooseOverlay;
    private TileOverlay mMhPienriistaOverlay;
    private TileOverlay mGameTrianglesOverlay;

    private TextView mCopyrightText;

    // Stores location in case map is not yet ready. Once map loading is finished this location will be displayed.
    private Location mUpdatedLocation;

    public EntryMapView(final Context context) {
        this(context, null);
    }

    public EntryMapView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    //Leaks the map abstraction a bit, prefer to implement new functionality inside this class if possible
    public GoogleMap getMap() {
        return mMap;
    }

    public void clearBackgroundCache() {
        if (mMmlOverlay != null) {
            mMmlOverlay.clearTileCache();
        }
    }

    public void clearVectorCaches() {
        final TileOverlay[] overlays = {
                mVectorOverlay,
                mVectorOverlay2,
                mValtionmaatOverlay,
                mRhyBordersOverlay,
                mMhMooseOverlay,
                mMhPienriistaOverlay,
                mGameTrianglesOverlay,
        };
        for (TileOverlay overlay : overlays) {
            if (overlay != null) {
                overlay.clearTileCache();
            }
        }
    }

    public void setup(final Context context, final boolean showGpsLevel, final boolean showAccuracy) {
        mShowInfoWindow = showGpsLevel;
        mShowAccuracy = showAccuracy;

        getMapAsync(this);
        MapsInitializer.initialize(context);

        mMyInfoWindowAdapter = new MyInfoWindowAdapter(context);
    }

    public void setShowInfoWindow(final boolean show) {
        if (show && !mShowInfoWindow) {
            mMap.setInfoWindowAdapter(mMyInfoWindowAdapter);
        } else if (!show && mShowInfoWindow) {
            mMap.setInfoWindowAdapter(null);
        }
        mShowInfoWindow = show;
    }

    public void zoomBy(final float direction) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomBy(direction));
        }
    }

    public void zoomBy(final float direction, final LatLng focusLocation) {
        if (mMap != null) {
            final Point focusPoint = mMap.getProjection().toScreenLocation(focusLocation);
            mMap.animateCamera(CameraUpdateFactory.zoomBy(direction, focusPoint));
        }
    }

    public float calculateScale(final int pixelSize) {
        if (mMap != null) {
            final LatLng start = mMap.getProjection().fromScreenLocation(new Point(0, getHeight() / 2));
            final LatLng end = mMap.getProjection().fromScreenLocation(new Point(pixelSize, getHeight() / 2));

            final float[] results = new float[1];
            Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
            return results[0];
        }

        return 0.0f;
    }

    public void setShowAccuracy(final boolean show) {
        mShowAccuracy = show;
    }

    @Override
    public void onMapReady(final GoogleMap map) {
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
        if (mMap != null) {
            mMap.setMaxZoomPreference(MAP_ZOOM_LEVEL_MAX);
            mMap.setMinZoomPreference(MAP_ZOOM_LEVEL_MIN);

            setMapTileType(AppPreferences.getMapTileSource(getContext()));
        }
    }

    public void setMapExternalId(final String externalId, final boolean invert) {
        if (mVectorOverlay != null) {
            mVectorOverlay.remove();
            mVectorOverlay = null;
        }

        mVectorOverlay = createVectorOverlay(externalId, invert);
    }

    public void setMapExternalId2(final String externalId, final boolean invert) {
        if (mVectorOverlay2 != null) {
            mVectorOverlay2.remove();
            mVectorOverlay2 = null;
        }

        // TODO: Should this overlay have a different color (i.e. different AreaType)?
        mVectorOverlay2 = createVectorOverlay(externalId, invert);
    }

    private TileOverlay createVectorOverlay(final String externalId, final boolean invert) {
        if (externalId != null && mMap != null) {
            final VectorTileProvider tileProvider = new VectorTileProvider(getContext());
            tileProvider.setAreaType(VectorTileProvider.AreaType.SEURA);
            tileProvider.setMapExternalId(externalId);
            tileProvider.setInvertColors(invert);

            return mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(2f));
        }
        return null;
    }

    public void setShowValtionmaatLayer(final boolean show) {
        if (mValtionmaatOverlay != null) {
            mValtionmaatOverlay.remove();
            mValtionmaatOverlay = null;
        }

        if (show && mMap != null) {
            final VectorTileProvider tileProvider = new VectorTileProvider(getContext());
            tileProvider.setAreaType(VectorTileProvider.AreaType.VALTIONMAA);
            tileProvider.setMapExternalId("-1");
            tileProvider.setInvertColors(false);

            mValtionmaatOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(3f));
        }
    }

    public void setShowRhyBordersLayer(final boolean show) {
        if (mRhyBordersOverlay != null) {
            mRhyBordersOverlay.remove();
            mRhyBordersOverlay = null;
        }

        if (show && mMap != null) {
            final VectorTileProvider tileProvider = new VectorTileProvider(getContext());
            tileProvider.setAreaType(VectorTileProvider.AreaType.RHY);
            tileProvider.setMapExternalId("-1");
            tileProvider.setInvertColors(false);

            mRhyBordersOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(4f));
        }
    }

    public void setShowMhMooseLayer(final boolean show, final String areaCode) {
        if (mMhMooseOverlay != null) {
            mMhMooseOverlay.remove();
            mMhMooseOverlay = null;
        }

        if (show && mMap != null) {
            final VectorTileProvider tileProvider = new VectorTileProvider(getContext());
            tileProvider.setAreaType(VectorTileProvider.AreaType.MOOSE);
            tileProvider.setMapExternalId(areaCode);
            tileProvider.setInvertColors(false);

            mMhMooseOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(5f));
        }
    }

    public void setShowMhPienriistaLayer(final boolean show, final String areaCode) {
        if (mMhPienriistaOverlay != null) {
            mMhPienriistaOverlay.remove();
            mMhPienriistaOverlay = null;
        }

        if (show && mMap != null) {
            final VectorTileProvider tileProvider = new VectorTileProvider(getContext());
            tileProvider.setAreaType(VectorTileProvider.AreaType.PIENRIISTA);
            tileProvider.setMapExternalId(areaCode);
            tileProvider.setInvertColors(false);

            mMhPienriistaOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(6f));
        }
    }

    public void setShowGameTrianglesLayer(final boolean show) {
        if (mGameTrianglesOverlay != null) {
            mGameTrianglesOverlay.remove();
            mGameTrianglesOverlay = null;
        }

        if (show && mMap != null) {
            final VectorTileProvider tileProvider = new VectorTileProvider(getContext());
            tileProvider.setAreaType(VectorTileProvider.AreaType.GAME_TRIANGLES);
            tileProvider.setMapExternalId("-1");
            tileProvider.setInvertColors(false);

            mGameTrianglesOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(7f));
        }
    }

    private TextView setupCopyright() {
        // Display copyright text in bottom right corner
        final TextView copyrightText = new TextView(getContext());
        copyrightText.setEnabled(false);
        copyrightText.setText(R.string.map_copyright_mml);
        copyrightText.setPadding(0, 0, 5, 0);
        copyrightText.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, copyrightText.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, copyrightText.getId());

        copyrightText.setLayoutParams(params);

        return copyrightText;
    }

    public void setMapTileType(final AppPreferences.MapTileSource tileType) {
        if (mMap == null || mTiletype == tileType) {
            // Map type will be set after OnMapReady is called
            return;
        }

        mTiletype = tileType;

        if (mMmlOverlay != null) {
            mMmlOverlay.remove();
            mMmlOverlay = null;
        }

        if (mCopyrightText != null) {
            removeView(mCopyrightText);
            mCopyrightText = null;
        }

        mLocationMarker = null;
        mAccuracyCircle = null;

        mMap.clear();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(false);
        }

        final MmlTileProvider tileProvider;

        if (mTiletype == AppPreferences.MapTileSource.GOOGLE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            tileProvider = null;
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);

            tileProvider = new MmlTileProvider(256, 256, getContext());
            tileProvider.setMapType(mTiletype);

            if (mMmlOverlay == null) {
                mMmlOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(1f));
            }

            if (mCopyrightText == null) {
                mCopyrightText = setupCopyright();
                mCopyrightText.setText(R.string.map_copyright_mml);
                addView(mCopyrightText);
            }
        }

        if (mUpdatedLocation != null) {
            onLocationUpdated(mUpdatedLocation);
            mUpdatedLocation = null;
        }

        if (tileProvider != null) {
            tileProvider.setMapType(tileType);
        }
        if (mMmlOverlay != null) {
            mMmlOverlay.clearTileCache();
        }
    }

    public void onLocationUpdated(final Location location) {
        moveCameraTo(location);
        refreshGpsInfoWindow(location);
        refreshLocationIndicators(location);
    }

    public void refreshLocationIndicators(@NonNull final Location location) {
        // Location pinpoint marker
        if (mLocationMarker != null) {
            mLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        } else if (mMap != null) {
            final MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zIndex(1000.0f);
            mLocationMarker = mMap.addMarker(options);
            mLocationMarker.showInfoWindow();
        }

        if (mLocationMarker != null) {
            mLocationMarker.setVisible(mShowLocation);
        }

        refreshGpsInfoWindow(location);
        refreshAccuraryIndicator(location);
    }

    public void setLocationVisible(final boolean visible) {
        mShowLocation = visible;

        final boolean show = visible && AppPreferences.getShowUserMapLocation(getContext());

        if (mAccuracyCircle != null) {
            mAccuracyCircle.setVisible(show);
        }
        if (mLocationMarker != null) {
            mLocationMarker.setVisible(show);
        }

        if (ContextCompat.checkSelfPermission(RiistaApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(RiistaApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    refreshLocationIndicators(location);
                }
            });
        }
    }

    private void refreshGpsInfoWindow(final Location location) {
        if (mShowInfoWindow && mMyInfoWindowAdapter != null) {
            mMyInfoWindowAdapter.newLocation(location);

            if (mLocationMarker != null) {
                mLocationMarker.showInfoWindow();
            }
        } else if (mMap != null) {
            mMap.setInfoWindowAdapter(null);
        }
    }

    private void refreshAccuraryIndicator(final Location location) {
        final double accuracy = location.getAccuracy();

        // Draw accuracy circle around location marker only if it is positive. Negative values crash application.
        // Use 10 cm as a threshold value (as well as a "floating point epsilon") since it is highly unlikely that
        // location accuracy of a mobile phone could be more precise in the near future.
        final boolean shouldDrawAccuracyCircle = mShowAccuracy && accuracy > 0.1;

        if (shouldDrawAccuracyCircle) {
            final double lat = location.getLatitude();
            final double lng = location.getLongitude();

            if (mAccuracyCircle != null) {
                mAccuracyCircle.setCenter(new LatLng(lat, lng));
                mAccuracyCircle.setRadius(accuracy);
            } else if (mMap != null) {
                final CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(lat, lng))
                        .fillColor(ColorUtils.setAlphaComponent(getResources().getColor(R.color.colorUndefined), 0x40))
                        .strokeColor(R.color.colorUndefined)
                        .strokeWidth(1)
                        .radius(accuracy)
                        .zIndex(2f);

                mAccuracyCircle = mMap.addCircle(circleOptions);
            }

            if (mAccuracyCircle != null) {
                mAccuracyCircle.setVisible(mShowLocation);
            }

        } else if (mAccuracyCircle != null) {
            mAccuracyCircle.remove();
            mAccuracyCircle = null;
        }
    }

    // Should use a custom object instead of a Location so we could add stuff to it easier...
    private float getZoomExtra(final Location location) {
        final Bundle extras = location.getExtras();

        return extras != null
                ? extras.getFloat("zoomLevel", MAP_ZOOM_LEVEL_DEFAULT)
                : MAP_ZOOM_LEVEL_DEFAULT;
    }

    public void moveCameraTo(final Location location) {
        if (mMap == null) {
            // Map not yet ready, store location for later
            mUpdatedLocation = location;
        } else {
            final float zoom = getZoomExtra(location);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        }
    }

    public void animateCameraTo(final Location location) {
        if (mMap == null) {
            // Map not yet ready, store location for later
            mUpdatedLocation = location;
        } else {
            final float zoom = getZoomExtra(location);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
        }
    }

    /**
     * Get map view camera location coordinates
     *
     * @return Location map view center coordinates
     */
    public Location getCameraLocation() {
        if (mMap == null) {
            return null;
        }

        final LatLng coordinates = mMap.getCameraPosition().target;

        final Location location = new Location("");
        location.setLatitude(coordinates.latitude);
        location.setLongitude(coordinates.longitude);
        return location;
    }

    public float getCameraZoomLevel() {
        return mMap != null ? mMap.getCameraPosition().zoom : 0.0f;
    }
}
