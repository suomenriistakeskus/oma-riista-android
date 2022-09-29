package fi.riista.mobile.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import fi.riista.common.logging.getLogger
import fi.riista.mobile.R
import fi.riista.mobile.ui.AlertDialogItemConfigurator
import java.io.File
import java.io.IOException
import java.util.*

typealias OnImageCaptured = (uuid: String, path: String) -> Unit

class ImageCaptureHelper: HasSavedInstanceState {
    /**
     * What to do when an image has been captured?
     *
     * Set internally when [initialize] is called.
     */
    private var onImageCaptured: OnImageCaptured? = null

    private lateinit var fragment: Fragment

    private val context: Context by lazy {
        fragment.requireContext()
    }

    private lateinit var captureImageActivityResultLaunch: ActivityResultLauncher<Intent>

    private lateinit var checkPermissionsLaunch: ActivityResultLauncher<Array<String>>

    /**
     * The uri where image is being saved. Should be saved to savedInstanceState.
     */
    private var targetImageUri: Uri? = null


    /**
     * Initializes / prepares the [ImageCaptureHelper] to be used with given [fragment]
     */
    fun initialize(fragment: Fragment, onImageCaptured: OnImageCaptured) {
        this.fragment = fragment
        this.onImageCaptured = onImageCaptured
        registerForActivityResults()
    }

    /**
     * Checks the needed permissions, asks for them if needed and captures an image
     */
    fun checkPermissionsAndCaptureImage() {
        if (PermissionHelper.hasPhotoPermissions(context)) {
            captureImage()
        } else {
            // TODO: check if permissions rationale should be shown?
            checkPermissionsLaunch.launch(PermissionHelper.photoPermissions)
        }
    }

    private fun registerForActivityResults() {
        captureImageActivityResultLaunch = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            val capturedImageUri = targetImageUri
            if (result.resultCode == android.app.Activity.RESULT_OK && capturedImageUri != null) {
                copySelectedFileToInternalStorage(capturedImageUri)
            }
            targetImageUri = null
        }

        checkPermissionsLaunch = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (PermissionHelper.hasPhotoPermissions(context)) {
                captureImage()
            } else {
                // nop
            }
        }
    }

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context.packageManager) == null) {
            logger.w { "Couldn't resolve an activity for capturing an image" }
            return
        }

        val imageFile: File? = try {
            DiaryImageUtil.createImageFile(context)
        } catch (ignored: IOException) {
            null
        }

        if (imageFile == null) {
            logger.w { "Couldn't open the target image file!" }
            return
        }

        targetImageUri = Uri.fromFile(imageFile)

        val cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)

        captureImageActivityResultLaunch.launch(takePictureIntent)
    }

    private fun copySelectedFileToInternalStorage(uri: Uri) {
        val callback = onImageCaptured ?: kotlin.run {
            logger.d { "No imageCaptured callback, not copying image under app directory" }
            return
        }

        val uuid = UUID.randomUUID().toString()
        val copiedImageUri = DiaryImageUtil.copyImageToInternalStorage(
            context = context,
            uuid = uuid,
            imageUri = uri,
        )

        // implicit dependency to DiaryImageUtil: uuid is also filename
        callback(uuid, copiedImageUri.toString())
    }

    override fun saveInstanceState(outState: Bundle) {
        targetImageUri?.let {
            outState.putString(KEY_TARGET_IMAGE_URI, it.toString())
        }
    }

    override fun restoreFromSavedInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getString(KEY_TARGET_IMAGE_URI)
            ?.let {
                targetImageUri = Uri.parse(it)
            }
    }


    companion object {
        private const val KEY_TARGET_IMAGE_URI = "EICH_picture_uri"

        private val logger by getLogger(ImageCaptureHelper::class)
    }
}


fun AlertDialogItemConfigurator.addItem(imageCaptureHelper: ImageCaptureHelper) {
    addItem(R.string.take_picture) {
        imageCaptureHelper.checkPermissionsAndCaptureImage()
    }
}