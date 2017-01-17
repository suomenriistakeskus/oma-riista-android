package fi.riista.mobile;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for Google API client location service.
 */
public class LocationClient extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String LOCATION_CLIENT_TAG = "locationClientTag";

    private static final long UPDATE_INTERVAL_MS = 5000;
    private static final long FASTEST_UPDATE_INTEVAL_MS = 2000;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Set<LocationListener> mListeners;

    public LocationClient() {
        mListeners = new HashSet<>();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesAvailable()) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL_MS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTEVAL_MS);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
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
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void addListener(LocationListener listener) {
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

    private boolean servicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0).show();
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        for (LocationListener listener : mListeners) {
            listener.onLocationChanged(location);
        }
    }
}
