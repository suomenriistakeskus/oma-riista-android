package fi.riista.mobile.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    val photoPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @JvmStatic
    fun hasPhotoPermissions(context: Context): Boolean {
        return photoPermissions.fold(initial = true) { value, permission ->
            value && checkIfGranted(context, permission)
        }
    }

    @JvmStatic
    /**
     * Request permission to use camera and write to external storage.
     * If given activity doesn't handle onRequestPermissionResult with given [requestCode], then
     * user has to tap item again to use granted permission.
     */
    fun requestPhotoPermissions(activity: Activity, requestCode: Int) {
        if (!hasPhotoPermissions(activity as Context)) {
            ActivityCompat.requestPermissions(activity, photoPermissions, requestCode)
        }
    }

    private fun checkIfGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }


}
