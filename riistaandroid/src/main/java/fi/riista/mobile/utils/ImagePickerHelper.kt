package fi.riista.mobile.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import fi.riista.common.logging.getLogger
import fi.riista.mobile.R
import fi.riista.mobile.ui.AlertDialogItemConfigurator
import java.util.*

typealias OnImagePicked = (uuid: String, path: String) -> Unit

class ImagePickerHelper {

    private lateinit var fragment: Fragment

    private val context: Context by lazy {
        fragment.requireContext()
    }

    private var onImagePicked: OnImagePicked? = null

    private lateinit var selectPhotoLauncher: ActivityResultLauncher<Intent>


    /**
     * Initializes / prepares the [ImageCaptureHelper] to be used with given [fragment]
     */
    fun initialize(fragment: Fragment, onImagePicked: OnImagePicked) {
        this.fragment = fragment
        this.onImagePicked = onImagePicked
        registerForActivityResults()
    }

    /**
     */
    fun selectPhoto() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        selectPhotoLauncher.launch(photoPickerIntent)
    }

    private fun registerForActivityResults() {
        selectPhotoLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { imageUri ->
                    copySelectedFileToInternalStorage(imageUri)
                }
            }
        }
    }

    private fun copySelectedFileToInternalStorage(uri: Uri) {
        val callback = onImagePicked ?: kotlin.run {
            logger.d { "No onImagePicked callback, not copying image under app directory" }
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

    companion object {
        private val logger by getLogger(ImagePickerHelper::class)
    }
}


fun AlertDialogItemConfigurator.addItem(imagePickerHelper: ImagePickerHelper) {
    addItem(R.string.pick_gallery) {
        imagePickerHelper.selectPhoto()
    }
}