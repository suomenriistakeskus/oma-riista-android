package fi.riista.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;
import java.util.Set;

import fi.riista.mobile.models.AreaMap;

/**
 * Application shared preferences
 */
public class AppPreferences {

    public static final String LANGUAGE_CODE_FI = "fi";
    public static final String LANGUAGE_CODE_SV = "sv";
    public static final String LANGUAGE_CODE_EN = "en";

    private static final String MAP_SOURCE_KEY = "mapTileSource";
    private static final MapTileSource MAP_SOURCE_DEFAULT = MapTileSource.MML_TOPOGRAPHIC;
    private static final String MAP_SHOW_MARKERS = "mapShowMarkers";

    private static final String LANGUAGE_SETTING_KEY = "userLanguage";
    private static final String LANGUAGE_SETTING_DEFAULT = LANGUAGE_CODE_EN;

    private static final String CLUB_AREA_MAP_SETTING_KEY = "clubAreaMapId";
    private static final String MH_MOOSE_AREAS_KEY = "mhMooseAreaMapIds";
    private static final String MH_PIENRIISTA_AREAS_KEY = "mhPienriistaAreaMapIds";

    private static final String SHOW_RHY_BORDERS_KEY = "showRhyBorders";
    private static final String SHOW_VALTIONMAAT_KEY = "showValtionmaat";
    private static final String SHOW_GAME_TRIANGLES_KEY = "showGameTriangles";

    private static final String SHOW_MOOSE_RESTRICTIONS = "showMooseRestrictions";
    private static final String SHOW_SMALL_GAME_RESTRICTIONS = "showSmallGameRestrictions";
    private static final String SHOW_AVI_HUNTING_BAN = "showAviHuntingBan";

    private static final String SHOW_USER_LOCATION_SETTING_KEY = "showUserLocation";
    private static final String INVERT_MAP_COLORS_SETTING_KEY = "invertMapColors";
    private static final String LAST_MAP_LOCATION_SETTING_KEY = "lastMapLocation";
    private static final String HIDE_MAP_CONTROLS_SETTING_KEY = "hideMapControls";

    private static final String SERVER_BASE_ADDRESS_KEY = "serverBaseAddress";

    public static void clearAll(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
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

    public static void setShowMapMarkers(final Context context, final boolean showMapMarkers) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(MAP_SHOW_MARKERS, showMapMarkers);
            editor.apply();
        }
    }

    public static boolean getShowMapMarkers(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null && prefs.contains(MAP_SHOW_MARKERS)) {
            return prefs.getBoolean(MAP_SHOW_MARKERS, true);
        }
        return true;
    }

    public static void setLanguageCodeSetting(final Context context, final String languageTwoLetter) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LANGUAGE_SETTING_KEY, languageTwoLetter);
            editor.apply();
        }
    }

    public static String getLanguageCodeSetting(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String languageCode = LANGUAGE_SETTING_DEFAULT;

        if (prefs != null && prefs.contains(LANGUAGE_SETTING_KEY)) {
            languageCode = prefs.getString(LANGUAGE_SETTING_KEY, LANGUAGE_SETTING_DEFAULT);
        } else {
            // Initialize setting to system language or default on first run
            final String activeLanguage = Locale.getDefault().getLanguage();

            if (activeLanguage.equals(LANGUAGE_CODE_FI) || activeLanguage.equals(LANGUAGE_CODE_SV)) {
                setLanguageCodeSetting(context, activeLanguage);
                languageCode = activeLanguage;
            } else {
                setLanguageCodeSetting(context, LANGUAGE_SETTING_DEFAULT);
            }
        }

        return languageCode;
    }

    public static void setSelectedClubAreaMapId(Context context, String externalId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putString(CLUB_AREA_MAP_SETTING_KEY, externalId).apply();
        }
    }

    public static String getSelectedClubAreaMapId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getString(CLUB_AREA_MAP_SETTING_KEY, null);
        }
        return null;
    }

    public static void setSelectedMhMooseAreaMapIds(Context context, Set<AreaMap> areaCodes) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putString(MH_MOOSE_AREAS_KEY, areaCodes != null ? JsonUtils.objectToJson(areaCodes) : null).apply();
        }
    }

    public static Set<AreaMap> getSelectedMhMooseAreaMapIds(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            String value = prefs.getString(MH_MOOSE_AREAS_KEY, null);
            return value != null ? JsonUtils.jsonToSet(value, AreaMap.class) : null;
        }
        return null;
    }

    public static void setSelectedMhPienriistaAreasMapIds(Context context, Set<AreaMap> areaCodes) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putString(MH_PIENRIISTA_AREAS_KEY, areaCodes != null ? JsonUtils.objectToJson(areaCodes) : null).apply();
        }
    }

    public static Set<AreaMap> getSelectedMhPienriistaAreasMapIds(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            String value = prefs.getString(MH_PIENRIISTA_AREAS_KEY, null);
            return value != null ? JsonUtils.jsonToSet(value, AreaMap.class) : null;
        }
        return null;
    }

    public static void setShowRhyBorders(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_RHY_BORDERS_KEY, show).apply();
        }
    }

    public static boolean getShowRhyBorders(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_RHY_BORDERS_KEY, false);
        }
        return false;
    }

    public static void setShowValtionmaat(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_VALTIONMAAT_KEY, show).apply();
        }
    }

    public static boolean getShowValtionmaat(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_VALTIONMAAT_KEY, false);
        }
        return false;
    }

    public static void setShowGameTriangles(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_GAME_TRIANGLES_KEY, show).apply();
        }
    }

    public static boolean getShowGameTriangles(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_GAME_TRIANGLES_KEY, false);
        }
        return false;
    }

    public static void setShowMooseRestrictions(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_MOOSE_RESTRICTIONS, show).apply();
        }
    }

    public static boolean getShowMooseRestrictions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_MOOSE_RESTRICTIONS, false);
        }
        return false;
    }

    public static void setShowSmallGameRestrictions(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_SMALL_GAME_RESTRICTIONS, show).apply();
        }
    }

    public static boolean getShowSmallGameRestrictions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_SMALL_GAME_RESTRICTIONS, false);
        }
        return false;
    }

    public static void setShowAviHuntingBan(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_AVI_HUNTING_BAN, show).apply();
        }
    }

    public static boolean getShowAviHuntingBan(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_AVI_HUNTING_BAN, false);
        }
        return false;
    }

    public static void setShowUserMapLocation(Context context, boolean show) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(SHOW_USER_LOCATION_SETTING_KEY, show).apply();
        }
    }

    public static boolean getShowUserMapLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(SHOW_USER_LOCATION_SETTING_KEY, true);
        }
        return true;
    }

    public static void setInvertMapColors(Context context, boolean invert) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(INVERT_MAP_COLORS_SETTING_KEY, invert).apply();
        }
    }

    public static boolean getInvertMapColors(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(INVERT_MAP_COLORS_SETTING_KEY, false);
        }
        return false;
    }

    public static void setLastMapLocation(Context context, MapLocation loc) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putString(LAST_MAP_LOCATION_SETTING_KEY, JsonUtils.objectToJson(loc)).apply();
        }
    }

    public static MapLocation getLastMapLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            String json = prefs.getString(LAST_MAP_LOCATION_SETTING_KEY, null);
            if (json != null) {
                return JsonUtils.jsonToObject(json, MapLocation.class, true);
            }
        }
        return null;
    }

    public static void setHideMapControls(Context context, boolean hide) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putBoolean(HIDE_MAP_CONTROLS_SETTING_KEY, hide).apply();
        }
    }

    public static boolean getHideMapControls(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getBoolean(HIDE_MAP_CONTROLS_SETTING_KEY, false);
        }
        return false;
    }

    public static void setServerBaseAddress(Context context, String serverBaseAddress) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            prefs.edit().putString(SERVER_BASE_ADDRESS_KEY, serverBaseAddress).apply();
        }
    }

    public static String getServerBaseAddress(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            return prefs.getString(SERVER_BASE_ADDRESS_KEY, null);
        }
        return null;
    }

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

    public static class MapLocation {
        @JsonProperty("latitude")
        public double latitude;

        @JsonProperty("longitude")
        public double longitude;

        @JsonProperty("zoom")
        public float zoom;
    }
}
