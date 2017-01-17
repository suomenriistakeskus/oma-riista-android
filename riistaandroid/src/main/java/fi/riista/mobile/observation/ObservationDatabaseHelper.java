package fi.riista.mobile.observation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import fi.vincit.androidutilslib.database.AsyncDatabase;

class ObservationDatabaseHelper extends AsyncDatabase {

    private static final String DATABASE_NAME = "observations.db";
    private static final int DATABASE_VERSION = 5;

    private static final String CREATE_TABLE_OBSERVATION =
            "CREATE TABLE observation (" +
                    "localId INTEGER PRIMARY KEY," +
                    "remoteId INTEGER UNIQUE," +
                    "rev INTEGER," +
                    "type TEXT," +
                    "observationSpecVersion INTEGER," +
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
                    "imageIds TEXT," + //JSON data
                    "observationType TEXT," +
                    "withinMooseHunting INTEGER," +
                    "specimens TEXT," + //JSON data
                    "canEdit INTEGER," +
                    "mobileClientRefId TEXT," +
                    "totalSpecimenAmount INTEGER," +
                    "mooselikeMaleAmount INTEGER," +
                    "mooselikeFemaleAmount INTEGER," +
                    "mooselikeFemale1CalfAmount INTEGER," +
                    "mooselikeFemale2CalfsAmount INTEGER," +
                    "mooselikeFemale3CalfsAmount INTEGER," +
                    "mooselikeFemale4CalfsAmount INTEGER," +
                    "mooselikeUnknownSpecimenAmount INTEGER," +
                    //Local extras
                    "deleted INTEGER," +
                    "modified INTEGER," +
                    "localImages TEXT," + //JSON data
                    "username TEXT" +
            ");";

    private static ObservationDatabaseHelper sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new ObservationDatabaseHelper(context);
        }
    }

    public static ObservationDatabaseHelper getInstance() {
        return sInstance;
    }

    private ObservationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    protected void onAsyncCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_OBSERVATION);
    }

    @Override
    protected void onAsyncUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //We can drop everything until we make the first public release
        db.execSQL("DROP TABLE IF EXISTS observation");

        onAsyncCreate(db);
    }
}
