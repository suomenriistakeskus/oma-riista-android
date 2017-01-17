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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.observation.ObservationDatabase.ObservationsListener;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.srva.SrvaDatabase.SrvaEventListener;
import fi.vincit.androidutilslib.task.WorkAsyncTask;

public class ImageUtils {

    private static final String IMAGE_DIRECTORY = "images";
    public static final int IMAGE_RESIZE = 1024;

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    private static Bitmap getOrientationCorrectedImage(Bitmap bitmap, int orientation) {

        if (bitmap == null) {
            return null;
        }

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                Log.d("ImageUtils", "Correcting image orientation 90 degrees");
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                Log.d("ImageUtils", "Correcting image orientation 180 degrees");
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                Log.d("ImageUtils", "Correcting image orientation 270 degrees");
                break;
            default:
                Log.d("ImageUtils", "Image orientation is correct. No need to fix it.");
                return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int getOrientationFromImage(String imageFilePath) {

        ExifInterface exif;

        try {
            exif = new ExifInterface(imageFilePath);
        } catch (Exception exception) {
            Log.d("ImageUtils", "Could not find EXIF data from image");
            return ExifInterface.ORIENTATION_NORMAL;
        }

        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    private static int getOrientationFromImage(Context context, Uri imageUri) {
        Cursor cursor = context.getContentResolver().query(imageUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor == null || cursor.getCount() != 1) {
            if (cursor != null) cursor.close();
            Log.d("ImageUtils", "Could not image orientation: " + imageUri.toString());
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        cursor.moveToFirst();
        int orientationDegrees = cursor.getInt(0);
        cursor.close();

        if (orientationDegrees == 90) {
            Log.d("ImageUtils", "Image orientation is 90 degrees");
            return ExifInterface.ORIENTATION_ROTATE_90;
        } else if (orientationDegrees == 180) {
            Log.d("ImageUtils", "Image orientation is 180 degrees");
            return ExifInterface.ORIENTATION_ROTATE_180;
        } else if (orientationDegrees == 270) {
            Log.d("ImageUtils", "Image orientation is 270 degrees");
            return ExifInterface.ORIENTATION_ROTATE_270;
        } else {
            Log.d("ImageUtils", "Image orientation is 0 degrees.");
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    /**
     * Fetches image from given uri and resizes with given dimensions
     */
    public static Bitmap getBitmapFromStreamForImageView(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            InputStream imageStream = openStream(context, uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

            imageStream = openStream(context, uri);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            Bitmap image = BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

            int orientation = ExifInterface.ORIENTATION_UNDEFINED;
            if (uri.toString().substring(0, 10).equals("content://")) {
                orientation = ImageUtils.getOrientationFromImage(context, uri);
            } else {
                orientation = ImageUtils.getOrientationFromImage(uri.getPath());
            }
            return getOrientationCorrectedImage(image, orientation);
        } catch (FileNotFoundException e) {
            Log.d("ImageUtils", "File not found: " + uri.toString());
        } catch (IOException e) {
            Log.d("ImageUtils", "Closing stream failed: " + e.getMessage());
        }
        return null;
    }

    public static InputStream openStream(Context context, Uri uri) throws IOException {
        InputStream imageStream;
        String uriString = uri.toString();
        if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
            imageStream = context.getContentResolver().openInputStream(uri);
        } else {
            imageStream = new FileInputStream(uriString);
        }
        return imageStream;
    }

    /**
     * Fetch full size bitmap image for upload.
     */
    public static Bitmap getBitmapForUpload(Context context, Uri uri) {
        try {
            InputStream imageStream = openStream(context, uri);
            Bitmap image = BitmapFactory.decodeStream(imageStream, null, null);
            imageStream.close();

            int orientation = ExifInterface.ORIENTATION_UNDEFINED;
            if (uri.toString().substring(0, 10).equals("content://")) {
                orientation = ImageUtils.getOrientationFromImage(context, uri);
            } else {
                orientation = ImageUtils.getOrientationFromImage(uri.getPath());
            }
            return getOrientationCorrectedImage(image, orientation);
        } catch (FileNotFoundException e) {
            Log.d("ImageUtils", "File not found: " + uri.toString());
        } catch (IOException e) {
            Log.d("ImageUtils", "Closing stream failed: " + e.getMessage());
        }
        return null;
    }

    private static File getImagesDir(Context context) {
        return new File(context.getFilesDir(), IMAGE_DIRECTORY);
    }

    public static File getImageFile(Context context, String uuid) {
        File dir = getImagesDir(context);
        dir.mkdirs();
        return new File(dir, uuid);
    }

    public static void removeUnusedImagesAsync() {
        final ArrayList<LogImage> localImages = new ArrayList<>();

        ObservationDatabase.getInstance().loadObservationsWithLocalImages(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                for (GameObservation observation : observations) {
                    localImages.addAll(observation.getAllImages());
                }

                SrvaDatabase.getInstance().loadEventsWithLocalImages(new SrvaEventListener() {
                    @Override
                    public void onEvents(List<SrvaEvent> events) {
                        for (SrvaEvent event : events) {
                            localImages.addAll(event.getAllImages());
                        }

                        removeImagesAsync(localImages);
                    }
                });
            }
        });
    }

    private static void removeImagesAsync(final List<LogImage> localImages) {
        WorkAsyncTask task = new WorkAsyncTask(RiistaApplication.getInstance().getWorkContext()) {
            @Override
            protected void onAsyncRun() throws Exception {
                List<LogImage> huntingImages = GameDatabase.getInstance().getAllLogImages();
                localImages.addAll(huntingImages);

                HashSet<String> images = new HashSet<>();
                for (LogImage image : localImages) {
                    images.add(image.uuid);
                }

                File imagesDir = getImagesDir(RiistaApplication.getInstance());
                String[] files = imagesDir.list();

                int removeCount = 0;
                if (files != null) {
                    for (String fileName : files) {
                        if (!images.contains(fileName)) {
                            Utils.LogMessage("Removing image: " + fileName);

                            File imageFile = new File(imagesDir, fileName);
                            if (imageFile.delete()) {
                                removeCount++;
                            }
                        }
                    }
                }
                Utils.LogMessage("Removed images: " + removeCount);
            }
        };
        task.startSerial();
    }

}
