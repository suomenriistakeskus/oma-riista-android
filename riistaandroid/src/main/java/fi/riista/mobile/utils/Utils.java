package fi.riista.mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.RiistaApplication;
import fi.vincit.androidutilslib.task.NetworkTask;
import fi.vincit.androidutilslib.task.WorkAsyncTask;

public class Utils {

    public static final String LOG_TAG = "RIISTA_LOG:";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static boolean isPlayServicesAvailable(@NonNull final Activity activity,
                                                  final boolean finishActivityIfNotUserResolvableError) {

        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        final int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else if (finishActivityIfNotUserResolvableError) {
                activity.finish();
            }
            return false;
        }

        return true;
    }

    public static void unregisterNotificationsAsync() {
        FirebaseMessaging.getInstance().deleteToken();
    }

    @Nullable
    public static PackageInfo getAppPackageInfo(@NonNull final Context context) {
        try {
            final String packageName = context.getPackageName();
            final PackageManager packageManager = context.getPackageManager();
            return packageManager.getPackageInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static String getAppVersionName() {
        final PackageInfo packageInfo = getAppPackageInfo(RiistaApplication.getInstance().getApplicationContext());
        return packageInfo != null ? packageInfo.versionName : null;
    }

    public static String parseJSONStream(final InputStream inputStream) {
        final StringBuilder sb = new StringBuilder();

        try (final BufferedReader reader =
                     new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), 8)) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (final Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }

    public static boolean isRecentTime(final Date date, final float minutes) {
        final Calendar calendar = Calendar.getInstance();
        // Subtract some time as sync could be skipped in border cases.
        return date != null && ((calendar.getTime().getTime() - date.getTime()) / 1000.0f) / 60.0f < minutes - 0.05f;
    }

    public static Locale getLanguage() {
        final List<String> languages = Arrays.asList(
                AppPreferences.LANGUAGE_CODE_FI, AppPreferences.LANGUAGE_CODE_SV, AppPreferences.LANGUAGE_CODE_EN);
        final String locale = Locale.getDefault().getLanguage();

        return languages.contains(locale) ? new Locale(locale) : new Locale(AppPreferences.LANGUAGE_CODE_EN);
    }

    public static Integer parseInt(final String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (final Exception e) {
            LogMessage("Can't parse int from: " + text);
        }
        return null;
    }

    public static Double parseDouble(final String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (final Exception e) {
            LogMessage("Can't parse double from: " + text);
        }
        return null;
    }

    public static String formatInt(final Integer value) {
        return value != null ? value.toString() : "";
    }

    public static String formatDouble(final Double value) {
        return formatDouble(value, 2);
    }

    public static String formatDouble(final Double value, final int decimals) {
        return value != null ? String.format((Locale) null, "%." + decimals + "f", value) : "";
    }

    public static boolean isTrue(@NonNull final LiveData<Boolean> value) {
        return Boolean.TRUE == value.getValue();
    }

    public static void printTaskInfo(final String logTag, final NetworkTask task) {
        if (task != null) {
            System.out.println(logTag + " " + task.getHttpStatusCode());

            if (task.getCookieStore() != null) {
                System.out.println(task.getCookieStore().toString());
            }
        }
    }

    public static void LogMessage(final String className, final String message) {
        Log.d(LOG_TAG + className, message);
    }

    public static void LogMessage(final String message) {
        LogMessage("", message);
    }

    public static Object cloneObject(final Serializable object) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(object);

            try (final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 final ObjectInputStream ois = new ObjectInputStream(bais)) {

                return ois.readObject();
            }
        } catch (final Exception e) {
            return null;
        }
    }

    public static boolean shouldDisplayVersionUpdateDialog(final String versionJson) {
        try {
            final JSONObject jsonObject = new JSONObject(versionJson);
            final String version = jsonObject.getString("android");

            return isGreaterThanCurrentVersion(version);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean isGreaterThanCurrentVersion(final String versionName) {
        String currentVersion = getAppVersionName();
        if (currentVersion == null) {
            return false;
        }

        // Remove suffix from debug build versions.
        final int suffixStart = currentVersion.indexOf("-");
        if (suffixStart != -1) {
            currentVersion = currentVersion.substring(0, suffixStart);
        }

        return versionCompare(versionName, currentVersion) > 0;
    }

    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    private static int versionCompare(final String str1, final String str2) {
        final String[] vals1 = str1.split("\\.");
        final String[] vals2 = str2.split("\\.");

        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }
}
