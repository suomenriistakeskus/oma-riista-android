package fi.riista.mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.SpeciesMapping;
import fi.riista.mobile.database.DiaryDataSource;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.LogImageSendCompletion;
import fi.riista.mobile.database.LogImageUpdate;
import fi.riista.mobile.database.LogImageUpdateStatus;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.network.BitmapWorkerTask;
import fi.riista.mobile.network.LogImageTask;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.NetworkTask;
import fi.vincit.androidutilslib.view.WebImageView;

public class Utils {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String IMAGE_PATH_FORMAT = "%s/gamediary/image/%s/resize/%dx%dx%d";
    private static final int IMAGE_SCALING_CROP = 0;
    private static final int IMAGE_SCALING_KEEP_RATIO = 1;

    public static final String LOG_TAG = "RIISTA_LOG:";

    public static final int MOOSE_ID = 47503;
    public static final int FALLOW_DEER_ID = 47484;
    public static final int WHITE_TAILED_DEER = 47629;
    public static final int WILD_FOREST_DEER = 200556;
    public static final int BEAR_ID = 47348;

    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (in != null) {
            in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String parseJSONStream(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(GameDatabase.class.getSimpleName(), e.getMessage());
        }
        return null;
    }

    public static Drawable getSpeciesImage(Context context, Integer speciesId) {
        if (speciesId != null) {
            int drawableId = SpeciesMapping.species.get(speciesId);
            if (drawableId > 0) {
                return context.getResources().getDrawable(drawableId);
            }
        }
        Drawable unknown = context.getResources().getDrawable(R.drawable.ic_no_gender).mutate();
        unknown.setColorFilter(Color.BLACK, Mode.MULTIPLY);
        return unknown;
    }

    /**
     * Setups image for WebImageView
     *
     * @param affix Can be used to use separate cache images
     */
    public static void setupImage(WorkContext context, final WebImageView imageView, LogImage logimage, int reqWidth, int reqHeight, String affix) {
        setupImage(context, imageView, logimage, reqWidth, reqHeight, true, affix);
    }

    public static void setupImage(WorkContext context, final WebImageView imageView, LogImage logimage, int reqWidth, int reqHeight, boolean keepRatio, String affix) {
        imageView.setTargetImageSize(reqWidth);
        imageView.setAnimateFadeIn(true);
        imageView.setCookieStore(GameDatabase.getInstance().getCookieStore());
        if (logimage.type == LogImage.ImageType.URI) {
            BitmapWorkerTask task = new BitmapWorkerTask(context.getContext(), imageView, logimage.uri, reqWidth, reqHeight);
            task.execute(0);
        } else {
            if (affix == null) {
                imageView.setImageURI(String.format(IMAGE_PATH_FORMAT, AppConfig.BASE_URL, logimage.uuid, reqWidth, reqHeight, keepRatio ? IMAGE_SCALING_KEEP_RATIO : IMAGE_SCALING_CROP));
            } else {
                imageView.setImageURI(String.format(IMAGE_PATH_FORMAT, AppConfig.BASE_URL, logimage.uuid, reqWidth, reqHeight, keepRatio ? IMAGE_SCALING_KEEP_RATIO : IMAGE_SCALING_CROP) + "?" + affix);
            }
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isRecentTime(Date date, float minutes) {
        Calendar calendar = Calendar.getInstance();
        // Substract some time as sync could be skipped in border cases
        if (date != null && ((calendar.getTime().getTime() - date.getTime()) / 1000.0f) / 60.0f < minutes - 0.05f) {
            return true;
        }
        return false;
    }

    public static void sendImages(WorkContext context, int eventId, LogImageUpdate imageUpdate, final LogImageSendCompletion completion) {

        final LogImageUpdateStatus status = new LogImageUpdateStatus();
        status.totalImageOperations = imageUpdate.deletedImages.size() + imageUpdate.addedImages.size();

        if (status.totalImageOperations == 0 && completion != null) {
            completion.finish(false);
            return;
        }

        if (imageUpdate.deletedImages != null) {
            for (int i = 0; i < imageUpdate.deletedImages.size(); i++) {
                LogImageTask task = new LogImageTask(context, LogImageTask.OperationType.DELETE, eventId, imageUpdate.deletedImages.get(i));
                task.start();

                // The number can be incremented without success status
                status.imageOperationsDone++;
                if (status.imageOperationsDone == status.totalImageOperations) {
                    if (completion != null) completion.finish(status.errors);
                }
            }
        }
        if (imageUpdate.addedImages != null) {
            for (int i = 0; i < imageUpdate.addedImages.size(); i++) {
                LogImageTask task = new LogImageTask(context, LogImageTask.OperationType.ADD, eventId, imageUpdate.addedImages.get(i)) {

                    @Override
                    protected void onFinishText(String text) {
                        super.onFinishText(text);
                        status.imageOperationsDone++;
                        if (status.imageOperationsDone == status.totalImageOperations) {
                            if (completion != null) completion.finish(status.errors);
                        }
                    }

                    @Override
                    protected void onError() {
                        super.onError();
                        if (getHttpStatusCode() >= 400) {
                            status.errors = true;
                        }
                        status.imageOperationsDone++;
                        if (status.imageOperationsDone == status.totalImageOperations) {
                            if (completion != null) completion.finish(status.errors);
                        }
                    }
                };
                task.start();
            }
        }
    }

    public static Locale getLanguage() {
        Locale language = new Locale(AppPreferences.LANGUAGE_CODE_EN);
        String locale = Locale.getDefault().getLanguage();
        List<String> languages = Arrays.asList(AppPreferences.LANGUAGE_CODE_FI, AppPreferences.LANGUAGE_CODE_SV, AppPreferences.LANGUAGE_CODE_EN);
        if (languages.contains(locale)) {
            language = new Locale(locale);
        }
        return language;
    }

    static public Date parseDate(String dateString) {
        Date date = null;
        String[] formats = new String[]{DiaryDataSource.ISO_8601, DiaryDataSource.ISO_8601_SHORT};
        for (String format : formats) {
            SimpleDateFormat f = new SimpleDateFormat(format);
            try {
                date = f.parse(dateString);
                return date;
            } catch (ParseException ignored) {
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, Calendar.JANUARY, 0);
        return calendar.getTime();
    }

    public static Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            LogMessage("Can't parse int from: " + text);
        }
        return null;
    }

    public static Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            LogMessage("Can't parse double from: " + text);
        }
        return null;
    }

    public static String formatInt(Integer value) {
        String text = "";
        if (value != null) {
            text = value.toString();
        }
        return text;
    }

    public static String formatDouble(Double value) {
        String text = "";
        if (value != null) {
            text = String.format((Locale) null, "%.2f", value);
        }
        return text;
    }

    public static void printTaskInfo(String logTag, NetworkTask task) {
        if (task != null)
            System.out.println(logTag + " " + task.getHttpStatusCode());

        if (task != null && task.getCookieStore() != null)
            System.out.println(task.getCookieStore().toString());
    }

    public static void LogMessage(String className, String message) {
        Log.d(LOG_TAG + className, message);
    }

    public static void LogMessage(String message) {
        LogMessage("", message);
    }

    public static Object cloneObject(Serializable object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
