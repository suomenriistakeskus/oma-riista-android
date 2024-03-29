package fi.riista.mobile.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.View
import fi.riista.mobile.AppConfig
import fi.riista.mobile.R
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.network.BitmapWorkerTask
import fi.vincit.androidutilslib.view.WebImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object DiaryImageUtil {

    private const val IMAGE_PATH_FORMAT = "%s/gamediary/image/%s/resize/%dx%dx%d"
    private const val IMAGE_SCALING_CROP = 0
    private const val IMAGE_SCALING_KEEP_RATIO = 1

    /**
     * Changes diary image using GameLogImage object
     */
    @JvmStatic
    fun changeImage(context: Context, imageView: WebImageView, image: GameLogImage) {
        val edgeLen = context.resources.getDimension(R.dimen.attach_image_button_size).toInt()

        setupImage(context, imageView, image, /* width */edgeLen, /* height */edgeLen, false, null)
    }

    @JvmStatic
    fun setupImage(context: Context, imageView: WebImageView, image: GameLogImage, reqWidth: Int, reqHeight: Int, keepRatio: Boolean, affix: String?) {
        imageView.targetImageSize = reqWidth
        imageView.isAnimateFadeIn = true
        imageView.setCookieStore(CookieStoreSingleton.INSTANCE.cookieStore)

        if (image.type == GameLogImage.ImageType.URI && FileUtils.fileExists(image.uri?.path)) {
            val task = BitmapWorkerTask(context, imageView, image.uri, reqWidth, reqHeight)
            task.execute(0)
        } else {
            imageView.setImageURI(getImageUri(image.uuid, reqWidth, reqHeight, keepRatio, affix))
            imageView.visibility = View.VISIBLE
        }
    }

    internal fun getImageUri(imageUuid: String, reqWidth: Int, reqHeight: Int, keepRatio: Boolean, affix: String?): String {
        val imageUri = String.format(IMAGE_PATH_FORMAT, AppConfig.getBaseUrl(), imageUuid, reqWidth, reqHeight, if (keepRatio) IMAGE_SCALING_KEEP_RATIO else IMAGE_SCALING_CROP)

        return if (affix == null) imageUri else "$imageUri?$affix"
    }

    @JvmStatic
    @Throws(Exception::class)
    fun copyImageToInternalStorage(context: Context, imageUri: Uri, uuid: String): Uri {
        val size = ImageUtils.IMAGE_RESIZE
        val image = ImageUtils.getBitmapFromStreamForImageView(context, imageUri, size, size)
        val path = ImageUtils.getImageFile(context, uuid)

        image.compress(Bitmap.CompressFormat.JPEG, 85, FileOutputStream(path))
        image.recycle()

        return Uri.parse(path.path)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }

    @JvmStatic
    fun addGalleryPic(context: Context, uri: Uri) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }
}
