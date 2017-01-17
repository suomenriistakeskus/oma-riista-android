package fi.riista.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;

import fi.riista.mobile.models.user.UserInfo;
import fi.vincit.androidutilslib.util.JsonSerializator;

/**
 * Application shared preferences
 */
public class AppPreferences {

    public static final String LANGUAGE_CODE_FI = "fi";
    public static final String LANGUAGE_CODE_SV = "sv";
    public static final String LANGUAGE_CODE_EN = "en";

    private static final String USERINFO_KEY = "userInfo";
    private static final String USERINFO_DEFAULT = "";

    private static final String MAP_SOURCE_KEY = "mapTileSource";
    private static final MapTileSource MAP_SOURCE_DEFAULT = MapTileSource.MML_TOPOGRAPHIC;

    private static final String LANGUAGE_SETTING_KEY = "userLanguage";
    private static final String LANGUAGE_SETTING_DEFAULT = LANGUAGE_CODE_EN;

    private static ObjectMapper sMapper = JsonSerializator.createDefaultMapper();

    public enum MapTileSource {
        GOOGLE(10),
        MML(20),
        MML_TOPOGRAPHIC(21),
        MML_AERIAL(22),
        MML_BACKGROUND(23);

        private final int value;

        MapTileSource(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Store user information from login reply.
     * If reply data is null then remove stored key and value.
     *
     * @param context  The context of the preferences
     * @param jsonData Login response string or null
     */
    public static void setUserInfo(Context context, String jsonData) {
        if (context != null && jsonData != null && jsonData.length() > 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(USERINFO_KEY, jsonData);
                editor.apply();
            }
        } else if (context != null) {
            // Clear stored data.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(USERINFO_KEY);
                editor.apply();
            }
        }
    }

    /**
     * Get stored user information
     *
     * @param context The context of the preferences
     * @return Latest user info or null
     */
    public static UserInfo getUserInfo(Context context) {
        String jsonData = USERINFO_DEFAULT;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null && prefs.contains(USERINFO_KEY)) {
            jsonData = prefs.getString(USERINFO_KEY, USERINFO_DEFAULT);
        }

        UserInfo info = null;
        try {
            info = sMapper.readValue(jsonData, sMapper.getTypeFactory().constructType(UserInfo.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return info;
    }

    /**
     * Store map tile source setting
     * If value is null then remove stored key and value.
     *
     * @param context    The context of the preferences
     * @param tileSource Tile source or null
     */
    public static void setMapTileSource(Context context, MapTileSource tileSource) {
        if (context != null && tileSource != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(MAP_SOURCE_KEY, tileSource.getValue());
                editor.apply();
            }
        } else if (context != null) {
            // Clear stored data.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(MAP_SOURCE_KEY);
                editor.apply();
            }
        }
    }

    /**
     * Get stored map tile source setting
     *
     * @param context The context of the preferences
     * @return Tile type setting
     */
    public static MapTileSource getMapTileSource(Context context) {
        MapTileSource mapTileSource = MAP_SOURCE_DEFAULT;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null && prefs.contains(MAP_SOURCE_KEY)) {
            int settingValue = prefs.getInt(MAP_SOURCE_KEY, MAP_SOURCE_DEFAULT.getValue());

            if (settingValue == MapTileSource.GOOGLE.getValue()) {
                mapTileSource = MapTileSource.GOOGLE;
            } else if (settingValue == MapTileSource.MML_AERIAL.getValue()) {
                mapTileSource = MapTileSource.MML_AERIAL;
            } else if (settingValue == MapTileSource.MML_BACKGROUND.getValue()) {
                mapTileSource = MapTileSource.MML_BACKGROUND;
            } else {
                mapTileSource = MapTileSource.MML_TOPOGRAPHIC;
            }
        }

        return mapTileSource;
    }

    public static void setLanguageCodeSetting(Context context, String languageTwoLetter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LANGUAGE_SETTING_KEY, languageTwoLetter);
            editor.apply();
        }
    }

    public static String getLanguageCodeSetting(Context context) {
        String languageCode = LANGUAGE_SETTING_DEFAULT;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null && prefs.contains(LANGUAGE_SETTING_KEY)) {
            languageCode = prefs.getString(LANGUAGE_SETTING_KEY, LANGUAGE_SETTING_DEFAULT);
        } else {
            // Initialize setting to system language or default on first run
            String activeLanguage = Locale.getDefault().getLanguage();

            if (activeLanguage.equals(LANGUAGE_CODE_FI) || activeLanguage.equals(LANGUAGE_CODE_SV)) {
                setLanguageCodeSetting(context, activeLanguage);
                languageCode = activeLanguage;
            } else {
                setLanguageCodeSetting(context, LANGUAGE_SETTING_DEFAULT);
            }
        }

        return languageCode;
    }
}
