package fi.riista.mobile.observation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fi.vincit.androidutilslib.database.AsyncDatabase;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_CONTEXT_NAME;

@Singleton
class ObservationDatabaseHelper extends AsyncDatabase {

    private static final String DATABASE_NAME = "observations.db";
    private static final int DATABASE_VERSION = 8;

    private static final String TABLE_OBSERVATION = "observation";

    private static final String CREATE_TABLE_OBSERVATION =
            "CREATE TABLE IF NOT EXISTS observation (" +
                    "localId INTEGER PRIMARY KEY," +
                    "remoteId INTEGER UNIQUE," +
                    "rev INTEGER," +
                    "type TEXT," +
                    "observationSpecVersion INTEGER," +
                    "gameSpeciesCode INTEGER," +
                    "observationCategory TEXT," +
                    "observationType TEXT," +
                    "deerHuntingType TEXT," +
                    "deerHuntingTypeDescription TEXT," +
                        // Flattened GeoLocation
                        "latitude INTEGER," +
                        "longitude INTEGER," +
                        "source TEXT," +
                        "accuracy REAL," +
                        "altitude REAL," +
                        "altitudeAccuracy REAL," +
                    "pointOfTime TEXT," +
                    "description TEXT," +
                    "imageIds TEXT," + // JSON data
                    "specimens TEXT," + // JSON data
                    "canEdit INTEGER," +
                    "mobileClientRefId TEXT," +
                    "totalSpecimenAmount INTEGER," +
                    "mooselikeMaleAmount INTEGER," +
                    "mooselikeFemaleAmount INTEGER," +
                    "mooselikeFemale1CalfAmount INTEGER," +
                    "mooselikeFemale2CalfsAmount INTEGER," +
                    "mooselikeFemale3CalfsAmount INTEGER," +
                    "mooselikeFemale4CalfsAmount INTEGER," +
                    "mooselikeCalfAmount INTEGER, " +
                    "mooselikeUnknownSpecimenAmount INTEGER," +
                    "verifiedByCarnivoreAuthority INTEGER," +
                    "observerName TEXT," +
                    "observerPhoneNumber TEXT," +
                    "officialAdditionalInfo TEXT," +
                    "inYardDistanceToResidence INTEGER," +
                    "litter INTEGER," +
                    "pack INTEGER," +
                    // Local extras
                    "deleted INTEGER," +
                    "modified INTEGER," +
                    "localImages TEXT," + // JSON data
                    "username TEXT," +
                    "commonLocalId INTEGER" +
            ");";

    @Inject
    public ObservationDatabaseHelper(@NonNull @Named(APPLICATION_CONTEXT_NAME) final Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    protected void onAsyncCreate(final SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_OBSERVATION);
    }

    @Override
    protected void onAsyncUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.w(ObservationDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion <= 4) {
            db.execSQL("DROP TABLE IF EXISTS observation");
        }
        // Remember to only drop <= v4 and add all columns from later versions! (= not just single else if)
        else {
            if (oldVersion <= 5) {
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN verifiedByCarnivoreAuthority INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN observerName TEXT");
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN observerPhoneNumber TEXT");
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN officialAdditionalInfo TEXT");
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN inYardDistanceToResidence INTEGER");

                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN litter INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN pack INTEGER");

                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN mooselikeCalfAmount INTEGER");
            }
            if (oldVersion <= 6) {
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN observationCategory TEXT");
                db.execSQL("UPDATE " + TABLE_OBSERVATION + " SET observationCategory = 'NORMAL' WHERE withinMooseHunting IS null OR withinMooseHunting = 0");
                db.execSQL("UPDATE " + TABLE_OBSERVATION + " SET observationCategory = 'MOOSE_HUNTING' WHERE withinMooseHunting = 1");

                // SQLite does not support dropping columns => withinMooseHunting column remains hidden.

                db.execSQL("UPDATE " + TABLE_OBSERVATION + " SET observationSpecVersion = 4");

                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN deerHuntingType TEXT");
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN deerHuntingTypeDescription TEXT");
            }
            if (oldVersion <= 7) {
                db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD COLUMN commonLocalId INTEGER");
            }
        }

        onAsyncCreate(db);
    }
}
