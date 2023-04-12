package fi.riista.mobile.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static final int IMAGE_RESIZE = 1024;

    private static final String IMAGE_DIRECTORY = "images";
    private static final String TAG = "ImageUtils";

    private static int calculateInSampleSize(final BitmapFactory.Options options,
                                             final int reqWidth,
                                             final int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqWidth <= 0 || reqHeight <= 0) {
            return 1;
        }

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {

                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap getOrientationCorrectedImage(final Bitmap bitmap, final int orientation) {
        if (bitmap == null) {
            return null;
        }

        final Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                Log.d(TAG, "Correcting image orientation 90 degrees");
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                Log.d(TAG, "Correcting image orientation 180 degrees");
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                Log.d(TAG, "Correcting image orientation 270 degrees");
                break;
            default:
                Log.d(TAG, "Image orientation is correct. No need to fix it.");
                return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int getOrientationFromImage(final String imageFilePath) {
        try {
            final ExifInterface exif = new ExifInterface(imageFilePath);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        } catch (final Exception exception) {
            Log.d(TAG, "Could not find EXIF data from image");
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    private static int getOrientationFromImage(final Context context, final Uri imageUri) {
        final Cursor cursor = context.getContentResolver().query(imageUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor == null || cursor.getCount() != 1) {
            if (cursor != null) {
                cursor.close();
            }
            Log.d(TAG, "Could not image orientation: " + imageUri.toString());
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        cursor.moveToFirst();
        final int orientationDegrees = cursor.getInt(0);
        cursor.close();

        if (orientationDegrees == 90) {
            Log.d(TAG, "Image orientation is 90 degrees");
            return ExifInterface.ORIENTATION_ROTATE_90;
        } else if (orientationDegrees == 180) {
            Log.d(TAG, "Image orientation is 180 degrees");
            return ExifInterface.ORIENTATION_ROTATE_180;
        } else if (orientationDegrees == 270) {
            Log.d(TAG, "Image orientation is 270 degrees");
            return ExifInterface.ORIENTATION_ROTATE_270;
        } else {
            Log.d(TAG, "Image orientation is 0 degrees.");
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    /**
     * Fetches image from given uri and resizes with given dimensions
     */
    public static Bitmap getBitmapFromStreamForImageView(final Context context,
                                                         final Uri uri,
                                                         final int reqWidth,
                                                         final int reqHeight) {
        try {
            InputStream imageStream = openStream(context, uri);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

            imageStream = openStream(context, uri);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            final Bitmap image = BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

            final int orientation = deduceOrientation(context, uri);
            return getOrientationCorrectedImage(image, orientation);

        } catch (final FileNotFoundException e) {
            Log.d(TAG, "File not found: " + uri);
        } catch (final IOException e) {
            Log.d(TAG, "Closing stream failed: " + e.getMessage());
        }

        return null;
    }

    public static InputStream openStream(final Context context, final Uri uri) throws IOException {
        final String uriString = uri.toString();

        return uriString.startsWith("content://") || uriString.startsWith("file://")
                ? context.getContentResolver().openInputStream(uri)
                : new FileInputStream(uriString);
    }

    /**
     * Fetch full size bitmap image for upload.
     */
    public static Bitmap getBitmapForUpload(final Context context, final Uri uri) {
        try (final InputStream imageStream = openStream(context, uri)) {

            final Bitmap image = BitmapFactory.decodeStream(imageStream, null, null);
            final int orientation = deduceOrientation(context, uri);
            return getOrientationCorrectedImage(image, orientation);

        } catch (final FileNotFoundException e) {
            Log.d(TAG, "File not found: " + uri);
        } catch (final IOException e) {
            Log.d(TAG, "Closing stream failed: " + e.getMessage());
        }
        return null;
    }

    private static int deduceOrientation(final Context context, final Uri uri) {
        return uri.toString().startsWith("content://")
                ? getOrientationFromImage(context, uri)
                : getOrientationFromImage(uri.getPath());
    }

    public static File getImagesDir(final Context context) {
        return new File(context.getFilesDir(), IMAGE_DIRECTORY);
    }

    public static File getImageFile(final Context context, final String uuid) {
        final File dir = getImagesDir(context);
        dir.mkdirs();
        return new File(dir, uuid);
    }
}
