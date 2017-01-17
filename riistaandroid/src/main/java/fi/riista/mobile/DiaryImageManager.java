package fi.riista.mobile;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.utils.ImageUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.view.WebImageView;

/**
 * Class for handling user image attachments for diary entry creation page
 * The manager can be given list of initial images which have already been added
 * After use, manager can be used to return list of LogImage instances
 */
public class DiaryImageManager {

    public static final boolean IMAGE_LIMIT_ENABLED = true;
    public static final int MAX_IMAGES = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_SELECT_PHOTO = 100;
    DiaryImageManagerInterface mInterface = null;
    ViewGroup mContainerView = null;
    private WorkContext mWorkContext = null;
    // Store the image to be edited after getting result from intent
    private DiaryImage mEditImage = null;
    private Uri mCameraUri = null;
    private ImageView mAddButton = null;
    private boolean mEditMode = false;
    private List<LogImage> mCurrentImages = new ArrayList<>();
    private List<DiaryImage> mDiaryImages = new ArrayList<>();

    public DiaryImageManager(WorkContext context, DiaryImageManagerInterface result) {
        mWorkContext = context;
        mInterface = result;
        mEditMode = false;
    }

    public void setItems(List<LogImage> items) {
        mCurrentImages.clear();
        mCurrentImages.addAll(items);
    }

    /**
     * Setups current images to view
     */
    public void setup(ViewGroup viewgroup) {
        mContainerView = viewgroup;
        mDiaryImages.clear();
        mContainerView.removeAllViews();
        if (mCurrentImages != null) {
            for (int i = 0; i < mCurrentImages.size() && i < MAX_IMAGES; i++) {
                addImage(mCurrentImages.get(i), i);
            }
        }
        // add new image indicator
        addImage(null, 0);
        boolean isEnabled = mEditMode && (!IMAGE_LIMIT_ENABLED || mCurrentImages.size() < MAX_IMAGES);
        mAddButton.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    /**
     * This function is used to handle onActivityResult callback
     */
    public void handleImageResult(int requestCode, int resultCode, Intent data) {
        Uri newImageUri = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            newImageUri = mCameraUri;
            addGalleryPic(newImageUri);
        } else if (requestCode == REQUEST_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            newImageUri = data.getData();
        }
        if (newImageUri != null) {
            String uuid = UUID.randomUUID().toString();

            try {
                newImageUri = copyImageToInternalStorage(newImageUri, uuid);
            } catch (Exception e) {
                Utils.LogMessage("Image handling error: " + e.getMessage());
                return;
            }

            LogImage image = new LogImage(newImageUri);
            image.uuid = uuid;

            if (!mEditImage.isUserImage) {
                mCurrentImages.add(image);
                addImage(null, mCurrentImages.size());
            } else {
                mCurrentImages.set(mEditImage.index, image);
            }
            changeImage(mEditImage, image);
        }
    }

    private Uri copyImageToInternalStorage(Uri imageUri, String uuid) throws Exception {
        final int size = ImageUtils.IMAGE_RESIZE;
        Bitmap image = ImageUtils.getBitmapFromStreamForImageView(mWorkContext.getContext(), imageUri, size, size);

        File path = ImageUtils.getImageFile(mWorkContext.getContext(), uuid);
        FileOutputStream output = new FileOutputStream(path);

        image.compress(CompressFormat.JPEG, 100, output);
        image.recycle();

        return Uri.parse(path.getPath());
    }

    /**
     * Returns list of LogImage objects
     */
    public List<LogImage> getLogImages() {
        return mCurrentImages;
    }

    /**
     * Adds new image to container view
     *
     * @param logImage LogImage object, if null, adds new image indicator instead
     * @param index    Image index
     */
    private void addImage(LogImage logImage, int index) {
        WebImageView imageView = new WebImageView(mWorkContext.getContext());
        DiaryImage diaryImage = new DiaryImage(imageView);
        diaryImage.index = index;
        if (logImage != null) {
            changeImage(diaryImage, logImage);
        } else {
            mAddButton = imageView;
            imageView.setImageDrawable(mWorkContext.getContext().getResources().getDrawable(R.drawable.ic_camera));
            imageView.setBackgroundResource(R.drawable.bg_image_button);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) mWorkContext.getContext().getResources().getDimension(R.dimen.logimage_size),
                (int) mWorkContext.getContext().getResources().getDimension(R.dimen.logimage_size));
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ScaleType.CENTER_CROP);
        mContainerView.addView(imageView);
        mDiaryImages.add(diaryImage);
        setupImageEdit(diaryImage, imageView, index);
    }

    /**
     * Changes diary image using LogImage object
     */
    private void changeImage(DiaryImage diaryImage, LogImage logImage) {
        int reqWidth = (int) mWorkContext.getContext().getResources().getDimension(R.dimen.user_image_size);
        int reqHeight = (int) mWorkContext.getContext().getResources().getDimension(R.dimen.user_image_size);
        Utils.setupImage(mWorkContext, diaryImage.imageView, logImage, reqWidth, reqHeight, false, null);
        diaryImage.isUserImage = true;
    }

    private void setupImageEdit(final DiaryImage image, final WebImageView imageView, final int index) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditMode) {
                    AlertDialog.Builder popup = new AlertDialog.Builder(mWorkContext.getContext());
                    CharSequence items[] = {mWorkContext.getContext().getResources().getString(R.string.take_picture),
                            mWorkContext.getContext().getResources().getString(R.string.pick_gallery)};
                    popup.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(mWorkContext.getContext().getPackageManager()) != null) {
                                    File imageFile = null;
                                    try {
                                        imageFile = createImageFile();
                                    } catch (IOException ignored) {
                                    }
                                    if (imageFile != null) {
                                        mCameraUri = Uri.fromFile(imageFile);
                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                Uri.fromFile(imageFile));
                                        mInterface.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                                    }
                                }
                            } else {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                mInterface.startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
                            }
                            mEditImage = image;
                        }
                    });
                    popup.setTitle(mWorkContext.getContext().getResources().getString(R.string.image_prompt));
                    popup.create().show();
                } else if (imageView.getId() == mAddButton.getId()) {
                    mInterface.viewImage(mCurrentImages.get(index).uuid);
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void addGalleryPic(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        mWorkContext.getContext().sendBroadcast(mediaScanIntent);
    }

    public void setEditMode(boolean enabled) {
        mEditMode = enabled;
        if (mAddButton != null) {
            boolean addButtonEnabled = mEditMode && (!IMAGE_LIMIT_ENABLED || mCurrentImages.size() < MAX_IMAGES);
            mAddButton.setVisibility(addButtonEnabled ? View.VISIBLE : View.GONE);
        }
    }

    public interface DiaryImageManagerInterface {
        void startActivityForResult(Intent intent, int requestCode);

        void viewImage(String uuid);
    }

    // Visual object
    private class DiaryImage {
        int index = 0;
        boolean isUserImage = false;
        WebImageView imageView = null;

        DiaryImage(WebImageView view) {
            imageView = view;
        }
    }
}
