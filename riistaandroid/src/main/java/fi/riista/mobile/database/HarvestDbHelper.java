package fi.riista.mobile.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * SQLite open helper
 * Contains database schema and operations for database creation
 */
public class HarvestDbHelper extends SQLiteOpenHelper {

    private static final String TAG = HarvestDbHelper.class.getSimpleName();

    static final String TABLE_DIARY = "diary";
    static final String COLUMN_LOCALID = "localid";
    static final String COLUMN_ID = "id";
    static final String COLUMN_SPEC_VERSION = "apiVersion";
    // COLUMN_CLIENT_DATA_FORMAT is deprecated
    //static final String COLUMN_CLIENT_DATA_FORMAT = "clientDataFormat";
    static final String COLUMN_TYPE = "type";
    static final String COLUMN_POINTOFTIME = "pointOfTime";
    static final String COLUMN_GAMESPECIESID = "gameSpeciesId";
    static final String COLUMN_AMOUNT = "amount";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_HARVESTREPORTDONE = "harvestReportDone";
    static final String COLUMN_HARVESTREPORTREQUIRED = "harvestReportRequired";
    static final String COLUMN_HARVESTREPORTSTATE = "harvestReportState";
    static final String COLUMN_PERMITNUMBER = "permitNumber";
    static final String COLUMN_PERMITTYPE = "permitType";
    static final String COLUMN_HARVESTPERMITSTATE = "stateAcceptedToHarvestPermit";
    static final String COLUMN_CANEDIT = "canEdit";
    static final String COLUMN_REMOTE = "remote";
    static final String COLUMN_SENT = "sent";
    static final String COLUMN_REV = "rev";
    static final String COLUMN_USERNAME = "username";
    static final String COLUMN_MOBILEREFID = "mobileClientRefId";
    static final String COLUMN_PENDINGOPERATION = "pendingOperation";
    static final String COLUMN_SPECIMENDATA = "specimenData";
    static final String COLUMN_DEER_HUNTING_TYPE = "deerHuntingType";
    static final String COLUMN_DEER_HUNTING_OTHER_TYPE_DESCRIPTION = "deerHuntingOtherTypeDescription";
    static final String COLUMN_FEEDING_PLACE = "feedingPlace";
    static final String COLUMN_HUNTING_METHOD = "huntingMethod";
    static final String COLUMN_TAIGA_BEAN_GOOSE = "taigaBeanGoose";

    static final String TABLE_GEOLOCATION = "geolocation";
    static final String COLUMN_LATITUDE = "latitude";
    static final String COLUMN_LONGITUDE = "longitude";
    static final String COLUMN_ACCURACY = "accuracy";
    static final String COLUMN_HASALTITUDE = "hasAltitude";
    static final String COLUMN_ALTITUDE = "altitude";
    static final String COLUMN_ALTITUDEACCURACY = "altitudeAccuracy";
    static final String COLUMN_LOCATION_SOURCE = "source";

    static final String TABLE_IMAGE = "image";
    static final String COLUMN_DIARYID = "diaryid";
    static final String COLUMN_IMAGETYPE = "imagetype";
    static final String COLUMN_IMAGEURI = "imageurl";
    static final String COLUMN_IMAGEID = "imageid";
    static final String COLUMN_IMAGESTATUS = "imagestatus";
    static final String COLUMN_COMMON_LOCAL_ID = "commonLocalId";

    static final int IMAGETYPE_URI = 1;
    static final int IMAGETYPE_ID = 2;
    static final int IMAGESTATUS_INSERTED = 1;
    static final int IMAGESTATUS_DELETED = 2;

    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 12;

    // Database creation SQL statement
    private static final String DIARY_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_DIARY + "("
            + COLUMN_LOCALID + " integer primary key autoincrement, "
            + COLUMN_ID + " integer, "
            + COLUMN_SPEC_VERSION + " integer default 0, "
            // COLUMN_CLIENT_DATA_FORMAT is deprecated
            //+ COLUMN_CLIENT_DATA_FORMAT + " integer default 0, "
            + COLUMN_TYPE + " varchar(15), "
            + COLUMN_POINTOFTIME + " varchar(30), "
            + COLUMN_GAMESPECIESID + " integer, "
            + COLUMN_AMOUNT + " integer, "
            + COLUMN_DESCRIPTION + " varchar(200), "
            + COLUMN_HARVESTREPORTDONE + " integer, "
            + COLUMN_HARVESTREPORTREQUIRED + " integer, "
            + COLUMN_HARVESTREPORTSTATE + " varchar(30), "
            + COLUMN_PERMITNUMBER + " varchar(30), "
            + COLUMN_PERMITTYPE + " varchar(255), "
            + COLUMN_HARVESTPERMITSTATE + " varchar(30), "
            + COLUMN_DEER_HUNTING_TYPE + " varchar(255), "
            + COLUMN_DEER_HUNTING_OTHER_TYPE_DESCRIPTION + " text, "
            + COLUMN_FEEDING_PLACE + " integer, "
            + COLUMN_HUNTING_METHOD + " text, "
            + COLUMN_TAIGA_BEAN_GOOSE + " integer, "
            + COLUMN_CANEDIT + " integer, "
            + COLUMN_REV + " integer, "
            + COLUMN_REMOTE + " integer, "
            + COLUMN_SENT + " integer, "
            + COLUMN_USERNAME + " varchar(100), "
            + COLUMN_MOBILEREFID + " integer, "
            + COLUMN_PENDINGOPERATION + " integer, "
            + COLUMN_SPECIMENDATA + " blob, "
            + COLUMN_COMMON_LOCAL_ID + " integer"
            + ")";

    private static final String GEOLOCATION_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GEOLOCATION + " ("
            + COLUMN_DIARYID + " integer primary key, "
            + COLUMN_LATITUDE + " integer, "
            + COLUMN_LONGITUDE + " integer, "
            + COLUMN_ACCURACY + " real, "
            + COLUMN_HASALTITUDE + " integer, "
            + COLUMN_ALTITUDE + " real, "
            + COLUMN_ALTITUDEACCURACY + " real, "
            + COLUMN_LOCATION_SOURCE + " varchar(15)"
            + ")";

    private static final String IMAGE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_IMAGE + "("
            + COLUMN_DIARYID + " integer, "
            + COLUMN_IMAGETYPE + " integer, "
            + COLUMN_IMAGEURI + " varchar(100), "
            + COLUMN_IMAGEID + " varchar(50), "
            + COLUMN_IMAGESTATUS + " integer"
            + ")";

    HarvestDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        database.execSQL(DIARY_CREATE);
        database.execSQL(GEOLOCATION_CREATE);
        database.execSQL(IMAGE_CREATE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion <= 1) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_HARVESTREPORTREQUIRED + " integer");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_HARVESTREPORTSTATE + " varchar(30)");
        }
        if (oldVersion <= 2) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_MOBILEREFID + " integer");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_PENDINGOPERATION + " integer");
        }
        if (oldVersion <= 4) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_SPECIMENDATA + " blob");
            db.execSQL("ALTER TABLE " + TABLE_GEOLOCATION + " ADD COLUMN " + COLUMN_LOCATION_SOURCE + " varchar(15)");
        }
        if (oldVersion <= 6) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_HARVESTPERMITSTATE + " varchar(30)");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_CANEDIT + " integer");
        }
        if (oldVersion <= 7) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_SPEC_VERSION + " integer default 0");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_PERMITNUMBER + " varchar(30)");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_PERMITTYPE + " varchar(255)");
        }
        /*
         * COLUMN_CLIENT_DATA_FORMAT is deprecated
        if (oldVersion <= 8) {
            db.execSQL("ALTER TABLE "+ TABLE_DIARY + " ADD COLUMN " + COLUMN_CLIENT_DATA_FORMAT + " integer default 0");
        }
         */
        if (oldVersion <= 9) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_FEEDING_PLACE + " integer");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_HUNTING_METHOD + " text");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_TAIGA_BEAN_GOOSE + " integer");
        }
        if (oldVersion <= 10) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_DEER_HUNTING_TYPE + " varchar(255)");
            db.execSQL(
                    "ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_DEER_HUNTING_OTHER_TYPE_DESCRIPTION + " text");
        }
        if (oldVersion <= 11) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_COMMON_LOCAL_ID + " integer");
        }

        onCreate(db);
    }

    public enum UpdateType {
        NONE(0),
        INSERT(1),
        UPDATE(2),
        DELETE(3);

        private static final Map<Integer, UpdateType> valueMap = new HashMap<>();

        static {
            for (final UpdateType value : UpdateType.values()) {
                valueMap.put(value.mValue, value);
            }
        }

        private final int mValue;

        UpdateType(final int value) {
            this.mValue = value;
        }

        public static UpdateType valueOf(final int num) {
            return valueMap.get(num);
        }

        public int value() {
            return mValue;
        }
    }
}
