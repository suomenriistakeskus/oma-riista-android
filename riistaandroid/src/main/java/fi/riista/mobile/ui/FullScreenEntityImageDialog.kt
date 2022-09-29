package fi.riista.mobile.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.common.domain.model.EntityImage
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.getImageUrl
import fi.riista.mobile.riistaSdkHelpers.loadEntityImage

interface FullScreenEntityImageDialogLauncher {
    fun showEntityImageInFullscreen(entityImage: EntityImage)
}

class FullScreenEntityImageDialog : DialogFragment() {

    private lateinit var entityImage: EntityImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        entityImage = getEntityImageFromArgs(requireArguments())

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle_NoAppBar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)

        val context = requireContext()

        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.adjustViewBounds = true
        (view as FrameLayout).addView(imageView)

        val imageUrl = entityImage.getImageUrl(
            widthPixels = 1024,
            heightPixels = 1024,
            keepAspectRatio = true,
            affix = "b",
        )

        if (imageUrl != null) {
            Glide.with(context)
                .asBitmap()
                .error(R.drawable.ic_image_placeholder_24)
                .loadEntityImage(imageUrl)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder_24)
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        dialog?.apply {
            window?.setLayout(/*width*/ LayoutParams.MATCH_PARENT, /*height*/ LayoutParams.MATCH_PARENT)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onDestroy() {
        super.onDestroy()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    companion object {
        const val TAG = "FullScreenEntityImageDialog"
        private const val PREFIX = TAG
        private const val KEY_ENTITY_IMAGE = "${PREFIX}_entity_image"

        @JvmStatic
        fun newInstance(entityImage: EntityImage) = FullScreenEntityImageDialog().apply {
            arguments = Bundle().also { bundle ->
                entityImage.serializeToBundleAsJson(bundle, key = KEY_ENTITY_IMAGE)
            }
        }

        private fun getEntityImageFromArgs(arguments: Bundle): EntityImage {
            return requireNotNull(arguments.deserializeJson(key = KEY_ENTITY_IMAGE)) {
                "Failed to deserialize entity image from args"
            }
        }
    }
}
