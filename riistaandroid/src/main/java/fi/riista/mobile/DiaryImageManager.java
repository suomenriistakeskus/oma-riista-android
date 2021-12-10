package fi.riista.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.utils.DiaryImageUtil;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.view.WebImageView;

import static java.util.Objects.requireNonNull;

/**
 * Class for handling user image attachments for harvest/observation/SRVA-event forms.
 * The manager can be given list of initial images of which the first one is picked.
 * After use, manager can be used to return a singleton list containing at most one GameLogImage object.
 */
public class DiaryImageManager {

    public interface ImageManagerActivityAPI {

        void viewImage(GameLogImage image);

        boolean hasPhotoPermissions();

        void requestPhotoPermissions();
    }

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_SELECT_PHOTO = 100;

    private final Context mContext;
    private final ImageManagerActivityAPI mCallingActivity;

    private ActivityResultLauncher<Intent> mCaptureImageActivityResultLauncher;
    private ActivityResultLauncher<Intent> mSelectPhotoActivityResultLauncher;
    private ViewGroup mImageViewContainer = null;
    private WebImageView mImageView = null;

    private Uri mCameraUri = null;

    private boolean mEditMode = false;

    // Store image after getting it as a result of intent.
    private GameLogImage mCurrentImage = null;

    public DiaryImageManager(@NonNull final Context context,
                             @NonNull final ImageManagerActivityAPI callingActivity) {

        mContext = requireNonNull(context);
        mCallingActivity = requireNonNull(callingActivity);
    }

    public void setItems(final List<GameLogImage> items) {
        mCurrentImage = items == null || items.isEmpty() ? null : items.get(0);
    }

    /**
     * Setups current images to view.
     */
    public void setup(
            final ViewGroup viewgroup,
            final ActivityResultLauncher<Intent> captureImageActivityResultLauncher,
            final ActivityResultLauncher<Intent> selectPhotoActivityResultLauncher
    ) {
        mCaptureImageActivityResultLauncher = captureImageActivityResultLauncher;
        mSelectPhotoActivityResultLauncher = selectPhotoActivityResultLauncher;
        mImageViewContainer = viewgroup;
        mImageViewContainer.removeAllViews();

        mImageView = new WebImageView(mContext);

        if (mCurrentImage != null) {
            DiaryImageUtil.changeImage(mContext, mImageView, mCurrentImage);
        } else {
            mImageView.setImageDrawable(getImagePlaceholderIcon());
        }

        prepareImageView(mImageView);

        mImageViewContainer.addView(mImageView);
    }

    public void handleCaptureImageResult(final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri newImageUri = null;

            // mCameraUri might be null if hosting activity has died after URI value was set.
            // TODO Implement state persistence.
            if (mCameraUri != null) {
                newImageUri = mCameraUri;
                DiaryImageUtil.addGalleryPic(mContext, newImageUri);
                mCameraUri = null;
                handleNewImage(newImageUri);
            }
        }
    }

    public void handleSelectPhotoResult(final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri newImageUri = data.getData();
            handleNewImage(newImageUri);
        }
    }

    private void handleNewImage(Uri newImageUri) {
        if (newImageUri != null) {
            final String uuid = UUID.randomUUID().toString();

            try {
                newImageUri = DiaryImageUtil.copyImageToInternalStorage(mContext, newImageUri, uuid);

                mCurrentImage = new GameLogImage(newImageUri);
                mCurrentImage.uuid = uuid;

                DiaryImageUtil.changeImage(mContext, mImageView, mCurrentImage);

            } catch (final Exception e) {
                Utils.LogMessage("Image handling error: " + e.getMessage());
            }
        }
    }

    public void updateItems(final List<GameLogImage> items) {
        setItems(items);
        if (mCurrentImage != null) {
            DiaryImageUtil.changeImage(mContext, mImageView, mCurrentImage);
        } else {
            mImageView.setImageDrawable(getImagePlaceholderIcon());
        }
    }

    /**
     * Returns internally stored GameLogImage as a singleton list, if one is present.
     */
    public List<GameLogImage> getImages() {
        final List<GameLogImage> images = new ArrayList<>(1);

        if (mCurrentImage != null) {
            images.add(mCurrentImage);
        }

        return images;
    }

    /**
     * Get placeholder icon to view in case no user-provided image is available.
     */
    private Drawable getImagePlaceholderIcon() {
        final Drawable icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_camera_padded);
        if (icon != null) {
            icon.setTintList(AppCompatResources.getColorStateList(mContext, R.color.edit_mode_button_icon_tint));
        }
        return icon;
    }

    private void prepareImageView(final WebImageView imageView) {
        // Ensuring rounded corners are not overlaid by user selected image.
        imageView.setBackgroundResource(R.drawable.bg_rounded_backround);
        imageView.setClipToOutline(true);

        imageView.setLayoutParams(getImageViewLayoutParams(mContext));
        imageView.setScaleType(ScaleType.CENTER_CROP);

        setupClickListener(imageView);
    }

    private ViewGroup.LayoutParams getImageViewLayoutParams(final Context context) {
        final int edgeLen = (int) context.getResources().getDimension(R.dimen.attach_image_button_size);
        return new LinearLayout.LayoutParams(/* width */edgeLen, /* height */edgeLen);
    }

    private void setupClickListener(final WebImageView imageView) {
        imageView.setOnClickListener(v -> {
            if (mEditMode) {
                final CharSequence[] items = {
                        mContext.getString(R.string.take_picture),
                        mContext.getString(R.string.pick_gallery)
                };

                new AlertDialog.Builder(mContext)
                        .setItems(items, (dialog, which) -> {
                            if (which == 0) {
                                if (mCallingActivity.hasPhotoPermissions()) {
                                    takePicture();
                                } else {
                                    mCallingActivity.requestPhotoPermissions();
                                }
                            } else {
                                selectPhoto();
                            }
                        })
                        .setTitle(mContext.getString(R.string.image_prompt))
                        .create()
                        .show();

            } else if (mCurrentImage != null) {
                mCallingActivity.viewImage(mCurrentImage);
            }
        });
    }

    private void takePicture() {
        // CAMERA permission is required since camera is used elsewhere in app
        // <quote>
        // Note: if you app targets M and above and declares as using the Manifest.permission.CAMERA
        // permission which is not granted, then attempting to use this action will result in a SecurityException.
        // </quote>

        if (mCallingActivity.hasPhotoPermissions()) {
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                File imageFile = null;

                try {
                    imageFile = DiaryImageUtil.createImageFile(mContext);
                } catch (final IOException ignored) {
                }

                if (imageFile != null) {
                    mCameraUri = Uri.fromFile(imageFile);

                    final Context appContext = mContext.getApplicationContext();
                    final Uri cameraUri = FileProvider.getUriForFile(
                            appContext, appContext.getPackageName() + ".provider", imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    mCaptureImageActivityResultLauncher.launch(takePictureIntent);
                }
            }
        }
    }

    private void selectPhoto() {
        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        mSelectPhotoActivityResultLauncher.launch(photoPickerIntent);
    }

    public void setEditMode(final boolean editModeOn) {
        mEditMode = editModeOn;

        if (mImageView != null) {
            final boolean enabled = mEditMode || mCurrentImage != null;

            mImageViewContainer.setEnabled(enabled);
            mImageView.setEnabled(enabled);
        }
    }
}
