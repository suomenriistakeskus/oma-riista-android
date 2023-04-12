package fi.riista.mobile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import fi.riista.mobile.utils.PermissionHelper
import fi.riista.mobile.utils.Utils

/**
 * Wrapper for Google API client location service.
 */
class LocationClient : Fragment() {
    private val listeners: MutableSet<LocationListener?> = mutableSetOf()
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationRequest = LocationRequest
        .Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
        .setMaxUpdateDelayMillis(MAX_UPDATE_DELAY_MS)
        .build()

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                for (listener in listeners) {
                    listener?.onLocationChanged(location)
                }
            }
        }
    }

    private var requestingLocationUpdates = false
    private var didRequestPermission = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                requestLocationUpdates()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted. TODO: Should this be shown to the user?
            } else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: Activity = requireActivity()

        // TODO Need to check whether latter parameter could be true (i.e. finish activity).
        if (Utils.isPlayServicesAvailable(activity, false)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (listeners.isEmpty()) {
            // Don't request location updates if nobody is interested in
            return
        }

        if (PermissionHelper.hasLocationPermissions()) {
            requestLocationUpdates()
        } else if (this.isResumed && !didRequestPermission) {
            didRequestPermission = true
            locationPermissionRequest.launch(PermissionHelper.locationPermission)
       }
    }

    private fun stopLocationUpdates() {
        requestingLocationUpdates = false
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    /**
     * Don't call unless location permissions are checked and OK.
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        fusedLocationClient?.let { client ->
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            requestingLocationUpdates = true
        }
    }

    /**
     * Adds the given listener.
     *
     * It is safe to add same listener multiple times. It will be called only
     * once per location update though.
     */
    fun addListener(listener: LocationListener?) {
        listeners.add(listener)
        if (!requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    fun removeListener(listener: LocationListener?) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            stopLocationUpdates()
        }
    }

    companion object {
        const val LOCATION_CLIENT_TAG = "locationClientTag"
        private const val UPDATE_INTERVAL_MS: Long = 5000
        private const val FASTEST_UPDATE_INTERVAL_MS: Long = 2000
        private const val MAX_UPDATE_DELAY_MS: Long = 10000
    }
}
