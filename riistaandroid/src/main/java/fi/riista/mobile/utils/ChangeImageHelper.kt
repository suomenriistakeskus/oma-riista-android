package fi.riista.mobile.utils

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import fi.riista.common.domain.model.EntityImage
import fi.riista.mobile.R
import fi.riista.mobile.ui.configureItems
import fi.riista.mobile.ui.dataFields.viewHolder.EntityImageActionLauncher

typealias OnEntityImageChanged = (image: EntityImage) -> Unit

class ChangeImageHelper: HasSavedInstanceState, EntityImageActionLauncher {

    private var context: Context? = null

    private val captureImageHelper = ImageCaptureHelper()
    private val selectPhotoHelper = ImagePickerHelper()

    private var onImageChanged: OnEntityImageChanged? = null

    /**
     * Initializes / prepares the [ChangeImageHelper] to be used with given [fragment].
     *
     * Call this e.g. when [Fragment.onAttach] is being called.
     */
    fun initialize(fragment: Fragment, onImageChanged: OnEntityImageChanged) {
        context = fragment.context
        this.onImageChanged = onImageChanged

        captureImageHelper.initialize(fragment, onImageCaptured = ::onImageFileSaved)
        selectPhotoHelper.initialize(fragment, onImagePicked = ::onImageFileSaved)
    }

    override fun launchEntityImageSelection() {
        context?.let {
            AlertDialog.Builder(it)
                .configureItems {
                    addItem(captureImageHelper)
                    addItem(selectPhotoHelper)
                }
                .setTitle(R.string.image_prompt)
                .create()
                .show()
        }

    }

    private fun onImageFileSaved(uuid: String, path: String?) {
        onImageChanged?.let { callback ->
            callback(
                EntityImage(
                    serverId = uuid,
                    localIdentifier = null,
                    localUrl = path,
                    status = EntityImage.Status.LOCAL,
                )
            )
        }
    }

    override fun saveInstanceState(outState: Bundle) {
        captureImageHelper.saveInstanceState(outState)
    }

    override fun restoreFromSavedInstanceState(savedInstanceState: Bundle) {
        captureImageHelper.restoreFromSavedInstanceState(savedInstanceState)
    }
}