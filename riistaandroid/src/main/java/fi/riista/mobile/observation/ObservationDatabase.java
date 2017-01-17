package fi.riista.mobile.observation;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.ObservationSpecimen;
import fi.riista.mobile.utils.BaseDatabase;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncQuery;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;

public class ObservationDatabase extends BaseDatabase<GameObservation> {

    public interface ObservationsListener {
        void onObservations(List<GameObservation> observations);
    }

    public interface ObservationListener {
        void onObservation(GameObservation observation);
    }

    public interface ObservationYearsListener {
        void onYears(List<Integer> years);
    }

    private static ObservationDatabase sInstance;

    public static void init(Context context) {
        ObservationDatabaseHelper.init(context);

        sInstance = new ObservationDatabase();
    }

    public static ObservationDatabase getInstance() {
        return sInstance;
    }

    public void loadObservation(long localId, final ObservationListener listener) {
        ObservationDatabaseHelper.getInstance().query(new AsyncQuery(
                "SELECT * FROM observation WHERE username = ? AND localId = ?",
                getUsername(), "" + localId) {

            private GameObservation mResult;

            @Override
            protected void onAsyncQuery(AsyncCursor cursor) {
                if (cursor.moveToNext()) {
                    mResult = cursorToObservation(cursor);
                }
            }

            @Override
            protected void onFinish() {
                listener.onObservation(mResult);
            }

            @Override
            protected void onError() {
                listener.onObservation(mResult);
            }
        });
    }

    public void deleteObservation(GameObservation observation, boolean force, DeleteListener listener) {
        deleteEntry(ObservationDatabaseHelper.getInstance(), "observation",
                observation.localId, observation.remoteId, force, listener);
    }

    public void saveObservation(final GameObservation observation, final SaveListener listener) {
        observation.username = getUsername();

        final ContentValues values = observationToContentValues(observation);

        ObservationDatabaseHelper.getInstance().write(new AsyncWrite() {
            private long localId;

            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                localId = db.replaceOrThrow("observation", null, values);
            }

            @Override
            protected void onFinish() {
                listener.onSaved(localId);
            }

            @Override
            protected void onError() {
                listener.onError();
            }
        });
    }

    public void loadObservationYears(final ObservationYearsListener listener) {
        ObservationDatabaseHelper.getInstance().query(new AsyncQuery(
                "SELECT pointOfTime FROM observation WHERE username = ?", getUsername()) {

            private HashSet<Integer> mYears = new HashSet<>();

            @Override
            protected void onAsyncQuery(AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    String pointOfTime = cursor.getString(0);
                    DateTime dateTime = DateTimeUtils.parseDate(pointOfTime);
                    mYears.add(DateTimeUtils.getSeasonStartYearFromDate(dateTime.toCalendar(null)));
                }
            }

            @Override
            protected void onFinish() {
                listener.onYears(new ArrayList<>(mYears));
            }

            @Override
            protected void onError() {
                listener.onYears(new ArrayList<>(mYears));
            }
        });
    }

    public void loadLatestObservationSpecimens(int amount, ObservationsListener listener) {
        loadObservationsQuery(listener,
                "SELECT * FROM observation WHERE username = ? AND deleted = 0 GROUP BY gameSpeciesCode ORDER BY pointOfTime DESC LIMIT ?",
                getUsername(), "" + amount);
    }

    public void loadDeletedRemoteObservations(ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted != 0 AND remoteId IS NOT NULL", getUsername());
    }

    public void loadModifiedObservations(ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted = 0 AND modified != 0", getUsername());
    }

    public void loadAllObservations(ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ?", getUsername());
    }

    public void loadObservations(ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted = 0", getUsername());
    }

    public void loadObservationsWithLocalImages(ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted = 0 AND LENGTH(localImages) > 5", getUsername());
    }

    public void loadObservationsWithAnyImages(ObservationsListener listener) {
        loadObservationsQuery(listener,
                "SELECT * FROM observation WHERE username = ? AND deleted = 0 AND LENGTH(localImages) > 5 OR LENGTH(imageIds) > 5",
                getUsername());
    }

    private void loadObservationsQuery(final ObservationsListener listener, String query, String... args) {
        ObservationDatabaseHelper.getInstance().query(new AsyncQuery(query, args) {
            private ArrayList<GameObservation> results = new ArrayList<>();

            @Override
            protected void onAsyncQuery(AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    results.add(cursorToObservation(cursor));
                }
            }

            @Override
            protected void onFinish() {
                listener.onObservations(results);
            }

            @Override
            protected void onError() {
                listener.onObservations(results);
            }
        });
    }

    void handleReceivedObservations(final List<GameObservation> observations) {
        final String username = getUsername();

        ObservationDatabaseHelper.getInstance().write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                for (GameObservation observation : observations) {
                    boolean insert = false;

                    GameObservation old = findOne(db, "SELECT * FROM observation WHERE remoteId = ?", "" + observation.remoteId);
                    if (old != null) {
                        //We have this item locally, compare revisions
                        if (observation.rev >= old.rev && !old.deleted) {
                            //Server version is newer or equal, replace our local, not deleted version with it
                            observation.copyLocalAttributes(old);
                            observation.localImages = removeImages(old, observation);
                            insert = true;
                        }
                    } else {
                        //New from server
                        insert = true;
                    }

                    if (insert) {
                        observation.username = username;
                        db.replace("observation", null, observationToContentValues(observation));
                    }
                }
            }
        });
    }

    private List<LocalImage> removeImages(GameObservation local, GameObservation remote) {
        ArrayList<LocalImage> images = new ArrayList<>();
        for (LocalImage image : local.localImages) {
            if (local.imageIds.contains(image.serverId) && !remote.imageIds.contains(image.serverId)) {
                Utils.LogMessage("Observation image removed from the server: " + image.serverId);
            } else {
                images.add(image);
            }
        }
        return images;
    }

    private static GameObservation cursorToObservation(AsyncCursor cursor) {
        GameObservation observation = new GameObservation();
        observation.localId = cursor.getLong("localId");
        observation.remoteId = cursor.getLong("remoteId");
        observation.rev = cursor.getLong("rev");
        observation.type = cursor.getString("type");
        observation.observationSpecVersion = cursor.getInt("observationSpecVersion");
        observation.geoLocation = new GeoLocation();
        observation.geoLocation.latitude = cursor.getInt("latitude");
        observation.geoLocation.longitude = cursor.getInt("longitude");
        observation.geoLocation.source = cursor.getString("source");
        observation.geoLocation.accuracy = cursor.getDouble("accuracy");
        observation.geoLocation.altitude = cursor.getDouble("altitude");
        observation.geoLocation.altitudeAccuracy = cursor.getDouble("altitudeAccuracy");
        observation.pointOfTime = cursor.getString("pointOfTime");
        observation.gameSpeciesCode = cursor.getInt("gameSpeciesCode");
        observation.description = cursor.getString("description");
        observation.imageIds = JsonUtils.jsonToList(cursor.getString("imageIds"), String.class);
        observation.observationType = cursor.getString("observationType");
        observation.withinMooseHunting = cursor.getBool("withinMooseHunting");
        observation.specimens = JsonUtils.jsonToList(cursor.getString("specimens"), ObservationSpecimen.class);
        observation.canEdit = cursor.getInt("canEdit", 0) != 0;
        observation.mobileClientRefId = cursor.getLong("mobileClientRefId");
        observation.totalSpecimenAmount = cursor.getInt("totalSpecimenAmount");
        observation.mooselikeMaleAmount = cursor.getInt("mooselikeMaleAmount");
        observation.mooselikeFemaleAmount = cursor.getInt("mooselikeFemaleAmount");
        observation.mooselikeFemale1CalfAmount = cursor.getInt("mooselikeFemale1CalfAmount");
        observation.mooselikeFemale2CalfsAmount = cursor.getInt("mooselikeFemale2CalfsAmount");
        observation.mooselikeFemale3CalfsAmount = cursor.getInt("mooselikeFemale3CalfsAmount");
        observation.mooselikeFemale4CalfsAmount = cursor.getInt("mooselikeFemale4CalfsAmount");
        observation.mooselikeUnknownSpecimenAmount = cursor.getInt("mooselikeUnknownSpecimenAmount");
        observation.deleted = cursor.getInt("deleted") != 0;
        observation.modified = cursor.getInt("modified") != 0;
        observation.localImages = JsonUtils.jsonToList(cursor.getString("localImages"), LocalImage.class);
        observation.username = cursor.getString("username");
        return observation;
    }

    private static ContentValues observationToContentValues(GameObservation observation) {
        ContentValues values = new ContentValues();
        values.put("localId", observation.localId);
        values.put("remoteId", observation.remoteId);
        values.put("rev", observation.rev);
        values.put("type", observation.type);
        values.put("observationSpecVersion", observation.observationSpecVersion);
        values.put("latitude", observation.geoLocation.latitude);
        values.put("longitude", observation.geoLocation.longitude);
        values.put("source", observation.geoLocation.source);
        values.put("accuracy", observation.geoLocation.accuracy);
        values.put("altitude", observation.geoLocation.altitude);
        values.put("altitudeAccuracy", observation.geoLocation.altitudeAccuracy);
        values.put("pointOfTime", observation.pointOfTime);
        values.put("gameSpeciesCode", observation.gameSpeciesCode);
        values.put("description", observation.description);
        values.put("imageIds", objectToJson(observation.imageIds));
        values.put("observationType", observation.observationType);
        values.put("withinMooseHunting", observation.withinMooseHunting);
        values.put("specimens", objectToJson(observation.specimens));
        values.put("canEdit", observation.canEdit);
        values.put("mobileClientRefId", observation.mobileClientRefId);
        values.put("totalSpecimenAmount", observation.totalSpecimenAmount);
        values.put("mooselikeMaleAmount", observation.mooselikeMaleAmount);
        values.put("mooselikeFemaleAmount", observation.mooselikeFemaleAmount);
        values.put("mooselikeFemale1CalfAmount", observation.mooselikeFemale1CalfAmount);
        values.put("mooselikeFemale2CalfsAmount", observation.mooselikeFemale2CalfsAmount);
        values.put("mooselikeFemale3CalfsAmount", observation.mooselikeFemale3CalfsAmount);
        values.put("mooselikeFemale4CalfsAmount", observation.mooselikeFemale4CalfsAmount);
        values.put("mooselikeUnknownSpecimenAmount", observation.mooselikeUnknownSpecimenAmount);
        values.put("deleted", observation.deleted);
        values.put("modified", observation.modified);
        values.put("localImages", objectToJson(observation.localImages));
        values.put("username", observation.username);
        return values;
    }

    private static GameObservation findOne(SQLiteDatabase db, String query, String... args) {
        GameObservation result = null;

        AsyncCursor cursor = query(db, query, args);
        if (cursor.moveToFirst()) {
            result = cursorToObservation(cursor);
        }
        cursor.close();

        return result;
    }
}
