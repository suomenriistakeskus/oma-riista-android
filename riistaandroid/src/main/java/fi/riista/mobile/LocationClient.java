package fi.riista.mobile;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashSet;
import java.util.Set;

import fi.riista.mobile.utils.Utils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Wrapper for Google API client location service.
 */
public class LocationClient extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String LOCATION_CLIENT_TAG = "locationClientTag";

    private static final long UPDATE_INTERVAL_MS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_MS = 2000;
    private static final int REQUEST_LOCATION_PERMISSIONS_CODE = 6767;

    private final Set<LocationListener> mListeners;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private boolean mDidRequestPermission = false;

    public LocationClient() {
        mListeners = new HashSet<>();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = requireActivity();

        // TODO Need to check whether latter parameter could be true (i.e. finish activity).
        if (Utils.isPlayServicesAvailable(activity, false)) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL_MS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mGoogleApiClient != null && !mListeners.isEmpty()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(final Bundle bundle) {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {

            // FIXME accessing deprecated LocationServices.FusedLocationApi
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else if (!mDidRequestPermission) {
            mDidRequestPermission = true;

            final String[] permissions = { ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION };
            requestPermissions(permissions, REQUEST_LOCATION_PERMISSIONS_CODE);
        }
    }

    private static int checkSelfPermission(final String permission) {
        return ContextCompat.checkSelfPermission(RiistaApplication.getInstance(), permission);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {

        if (REQUEST_LOCATION_PERMISSIONS_CODE == requestCode) {
            if (grantResults.length == 2 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
    }

    /**
     * Adds the given listener.
     *
     * It is safe to add same listener multiple times. It will be called only
     * once per location update though.
     */
    public void addListener(final LocationListener listener) {
        if (mListeners.isEmpty() && listener != null && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        mListeners.add(listener);
    }

    public void removeListener(LocationListener listener) {
        mListeners.remove(listener);

        if (mListeners.isEmpty() && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        for (final LocationListener listener : mListeners) {
            listener.onLocationChanged(location);
        }
    }
}
