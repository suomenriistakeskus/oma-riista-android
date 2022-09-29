package fi.riista.mobile.srva;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import fi.vincit.androidutilslib.database.AsyncDatabase;

class SrvaDatabaseHelper extends AsyncDatabase {

    private static final String DATABASE_NAME = "srva.db";
    private static final String TABLE_NAME = "event";
    private static final int DATABASE_VERSION_FIRST_PUBLIC = 3;
    private static final int DATABASE_VERSION_NEW_DEPORTATION_FIELDS = 4;

    private static final int DATABASE_VERSION = DATABASE_VERSION_NEW_DEPORTATION_FIELDS;

    private static final String CREATE_TABLE_EVENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    "localId INTEGER PRIMARY KEY," +
                    "remoteId INTEGER UNIQUE," +
                    "rev INTEGER," +
                    "type TEXT," +
                        //Flattened GeoLocation
                        "latitude INTEGER," +
                        "longitude INTEGER," +
                        "source TEXT," +
                        "accuracy REAL," +
                        "altitude REAL," +
                        "altitudeAccuracy REAL," +
                    "pointOfTime TEXT," +
                    "gameSpeciesCode INTEGER," +
                    "description TEXT," +
                    "canEdit INTEGER," +
                    "imageIds TEXT," + //JSON data
                    "eventName TEXT," +
                    "deportationOrderNumber TEXT," + // added in spec version 2
                    "eventType TEXT," +
                    "eventTypeDetail TEXT," + // added in spec version 2
                    "otherEventTypeDetailDescription TEXT," + // added in spec version 2
                    "totalSpecimenAmount INTEGER," +
                    "otherMethodDescription TEXT," +
                    "otherTypeDescription TEXT," +
                    "methods TEXT," + //JSON data
                    "personCount INTEGER," +
                    "timeSpent INTEGER," +
                    "eventResult TEXT," +
                    "eventResultDetail TEXT," + // added in spec version 2
                    "authorInfo TEXT," + //JSON data
                    "specimens TEXT," + //JSON data
                    "rhyId INTEGER," +
                    "state TEXT," +
                    "otherSpeciesDescription TEXT," +
                    "approverInfo TEXT," + //JSON data
                    "mobileClientRefId INTEGER," +
                    "srvaEventSpecVersion INTEGER," +
                    //Local extras
                    "deleted INTEGER," +
                    "modified INTEGER," +
                    "localImages TEXT," + //JSON data
                    "username TEXT" +
            ");";

    private static SrvaDatabaseHelper sInstance;

    public static void init(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new SrvaDatabaseHelper(context);
        }
    }

    public static SrvaDatabaseHelper getInstance() {
        return sInstance;
    }

    private SrvaDatabaseHelper(@NonNull final Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    protected void onAsyncCreate(final SQLiteDatabase db) {
        createTableIfNotExists(db);
    }

    @Override
    protected void onAsyncUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.w(SrvaDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < DATABASE_VERSION_FIRST_PUBLIC) {
            // We can drop everything if we have pre-public version of the db
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        }
        // Remember to only drop <= v4 and add all columns from later versions! (= not just single else if)
        else {
            if (oldVersion < DATABASE_VERSION_NEW_DEPORTATION_FIELDS) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN deportationOrderNumber TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN eventTypeDetail TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN otherEventTypeDetailDescription TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN eventResultDetail TEXT");
            }
        }

        createTableIfNotExists(db);
    }

    private void createTableIfNotExists(final SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EVENT);
    }
}
