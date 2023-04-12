package fi.riista.mobile.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fi.riista.mobile.RiistaApplication

object PermissionHelper {
    val photoPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val locationPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    @JvmStatic
    fun hasPhotoPermissions() = photoPermissions.checkPermissions()

    @JvmStatic
    /**
     * Request permission to use camera and write to external storage.
     * If given activity doesn't handle onRequestPermissionResult with given [requestCode], then
     * user has to tap item again to use granted permission.
     */
    fun requestPhotoPermissions(activity: Activity, requestCode: Int) {
        if (!hasPhotoPermissions()) {
            ActivityCompat.requestPermissions(activity, photoPermissions, requestCode)
        }
    }

    fun hasLocationPermissions() = locationPermission.checkPermissions()

    private fun Array<String>.checkPermissions() = this.fold(initial = true) { value, permission ->
        value && checkIfGranted(permission)
    }

    private fun checkIfGranted(permission: String) = ContextCompat.checkSelfPermission(
        RiistaApplication.getInstance(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

