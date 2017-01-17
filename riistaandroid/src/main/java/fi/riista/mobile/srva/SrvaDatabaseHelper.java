package fi.riista.mobile.srva;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import fi.vincit.androidutilslib.database.AsyncDatabase;

class SrvaDatabaseHelper extends AsyncDatabase {

    private static final String DATABASE_NAME = "srva.db";
    private static final int DATABASE_VERSION = 3;

    private static final String CREATE_TABLE_EVENT =
            "CREATE TABLE event (" +
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
                    "eventType TEXT," +
                    "totalSpecimenAmount INTEGER," +
                    "otherMethodDescription TEXT," +
                    "otherTypeDescription TEXT," +
                    "methods TEXT," + //JSON data
                    "personCount INTEGER," +
                    "timeSpent INTEGER," +
                    "eventResult TEXT," +
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

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new SrvaDatabaseHelper(context);
        }
    }

    public static SrvaDatabaseHelper getInstance() {
        return sInstance;
    }

    private SrvaDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    protected void onAsyncCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EVENT);
    }

    @Override
    protected void onAsyncUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //We can drop everything until we make the first public release
        db.execSQL("DROP TABLE IF EXISTS event");

        onAsyncCreate(db);
    }
}
