package fi.riista.mobile.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import fi.riista.mobile.R
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.utils.DiaryImageUtil
import fi.vincit.androidutilslib.view.WebImageView

class FullScreenImageDialog : DialogFragment() {

    lateinit var image: GameLogImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            image = it.getSerializable(EXTRA_IMAGE) as GameLogImage
        }

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle_NoAppBar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)

        val imageView = WebImageView(context)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.adjustViewBounds = true

        context?.let { DiaryImageUtil.setupImage(it, imageView, image, 1024, 1024, true, "b") }
        (view as FrameLayout).addView(imageView)

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

        activity?.let {
            // FIXME isMenuVisible is annotated with: @RestrictTo(LIBRARY_GROUP_PREFIX)
            if (isMenuVisible) {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        activity?.apply {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    companion object {
        const val TAG = "FullScreenImageDialog"
        const val EXTRA_IMAGE = "extraImageId"

        @JvmStatic
        fun newInstance(image: GameLogImage) = FullScreenImageDialog().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_IMAGE, image)
            }
        }
    }
}
