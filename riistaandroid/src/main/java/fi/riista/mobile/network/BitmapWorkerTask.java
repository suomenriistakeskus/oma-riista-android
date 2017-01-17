package fi.riista.mobile.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import fi.riista.mobile.utils.ImageUtils;
import fi.vincit.androidutilslib.R;

/**
 * This class is used to load local images in background without disrupting main thread
 */
public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private Context mContext = null;
    private Uri mUri = null;
    private int mReqWidth = 0;
    private int mReqHeight = 0;
    private Animation mFadeInAnimation = null;

    public BitmapWorkerTask(Context context, ImageView imageView, Uri uri, int reqWidth, int reqHeight) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<>(imageView);
        mContext = context;
        mUri = uri;
        mReqWidth = reqWidth;
        mReqHeight = reqHeight;

        mFadeInAnimation = AnimationUtils.loadAnimation(
                mContext.getApplicationContext(),
                R.anim.fade_in);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        return ImageUtils.getBitmapFromStreamForImageView(mContext, mUri, mReqWidth, mReqHeight);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                imageView.clearAnimation();
                imageView.startAnimation(mFadeInAnimation);
            }
        }
    }
}
