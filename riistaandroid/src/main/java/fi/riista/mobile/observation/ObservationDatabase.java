package fi.riista.mobile.observation;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.observation.ObservationCategory;
import fi.riista.mobile.models.observation.ObservationSpecimen;
import fi.riista.mobile.models.observation.ObservationType;
import fi.riista.mobile.utils.BaseDatabase;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncQuery;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;

import static java.util.Objects.requireNonNull;

@Singleton
public class ObservationDatabase extends BaseDatabase {

    public interface ObservationsListener {
        void onObservations(List<GameObservation> observations);
    }

    public interface ObservationListener {
        void onObservation(GameObservation observation);
    }

    public interface ObservationYearsListener {
        void onYears(List<Integer> years);
    }

    private final ObservationDatabaseHelper mDatabaseHelper;

    @Inject
    public ObservationDatabase(@NonNull final ObservationDatabaseHelper databaseHelper,
                               @NonNull final UserInfoStore userInfoStore) {

        super(userInfoStore);

        mDatabaseHelper = requireNonNull(databaseHelper);
    }

    public void loadObservation(final long localId, @NonNull final ObservationListener listener) {
        mDatabaseHelper.query(new AsyncQuery(
                "SELECT * FROM observation WHERE username = ? AND localId = ?",
                getUsername(), "" + localId) {

            private GameObservation mResult;

            @Override
            protected void onAsyncQuery(final AsyncCursor cursor) {
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

    public void deleteObservation(@NonNull final GameObservation observation,
                                  final boolean force,
                                  @NonNull final DeleteListener listener) {

        deleteEntry(mDatabaseHelper, "observation", observation.localId, observation.remoteId, force, listener);
    }

    public void saveObservation(@NonNull final GameObservation observation, @NonNull final SaveListener listener) {
        observation.username = getUsername();

        final ContentValues values = observationToContentValues(observation);

        mDatabaseHelper.write(new AsyncWrite() {
            private long localId;

            @Override
            protected void onAsyncWrite(final SQLiteDatabase db) {
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

    public void loadObservationYears(@NonNull final ObservationYearsListener listener) {
        mDatabaseHelper.query(new AsyncQuery(
                "SELECT pointOfTime FROM observation WHERE username = ?", getUsername()) {

            private final HashSet<Integer> mYears = new HashSet<>();

            @Override
            protected void onAsyncQuery(final AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    final String pointOfTimeStr = cursor.getString(0);
                    final Calendar pointOfTime = DateTimeUtils.parseCalendar(pointOfTimeStr, false);

                    mYears.add(DateTimeUtils.getHuntingYearForCalendar(pointOfTime));
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

    public void loadLatestObservationSpecimens(final int amount, @NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener,
                "SELECT * FROM observation WHERE username = ? AND deleted = 0 GROUP BY gameSpeciesCode ORDER BY MAX(datetime(pointOfTime)) DESC LIMIT ?",
                getUsername(), "" + amount);
    }

    public void loadDeletedRemoteObservations(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted != 0 AND remoteId IS NOT NULL", getUsername());
    }

    public void loadModifiedObservations(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted = 0 AND modified != 0", getUsername());
    }

    public void loadAllObservations(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ?", getUsername());
    }

    public void loadObservations(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted = 0", getUsername());
    }

    public void loadObservationsWithLocalImages(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND deleted = 0 AND LENGTH(localImages) > 5", getUsername());
    }

    public void loadObservationsWithAnyImages(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener,
                "SELECT * FROM observation WHERE username = ? AND deleted = 0 AND (LENGTH(localImages) > 5 OR LENGTH(imageIds) > 5) ORDER BY pointOfTime DESC",
                getUsername());
    }

    private void loadObservationsQuery(final ObservationsListener listener, final String query, final String... args) {
        mDatabaseHelper.query(new AsyncQuery(query, args) {

            private final ArrayList<GameObservation> results = new ArrayList<>();

            @Override
            protected void onAsyncQuery(final AsyncCursor cursor) {
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

    public void handleReceivedObservations(@NonNull final List<GameObservation> observations) {
        final String username = getUsername();

        mDatabaseHelper.write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(final SQLiteDatabase db) {
                for (final GameObservation observation : observations) {
                    boolean insertOrUpdate = false;

                    final GameObservation old = findOne(db, "SELECT * FROM observation WHERE remoteId = ?", "" + observation.remoteId);
                    if (old != null) {
                        // We have this item locally, compare revisions
                        if (observation.rev >= old.rev && !old.deleted) {
                            // Server version is newer or equal, replace our local, not deleted version with it

                            // User may have local modifications but for some reason sending those
                            // to the backend failed: changes are sent first, only then are updated
                            // observations received (yes, we're implicitly depending on knowing
                            // how observation synchronization is implemented).
                            //
                            // The most probable reason for send failure is version conflict i.e.
                            // user has made local modifications but the version on the server
                            // has also been updated. When sending local modifications there's
                            // a version conflict on the backend which causes send to fail.
                            //
                            // If this is the case, the local version of the observation has most
                            // likely been already updated as previous implementation overwrote
                            // local modifications without questioning. The previous implementation
                            // also kept local attributes (incl. modified flag) and as a result
                            // user may have observations with server data and modified flag. This
                            // is problematic since the observation data received from the backend
                            // can NOT be sent back to the backend in some cases. This is e.g. the case
                            // when observation has been made within moose hunting (observationCategory ==
                            // MOOSE_HUNTING). In this case the backend calculates the totalSpeciesAmount
                            // which cannot be sent to backend (the amounts are described using
                            // moose-like fields).
                            //
                            // In order to remove errors from backend logs let's just clear
                            // modified flag from observations:
                            // - user should not lose any valid local modifications since those are
                            //   supposedly sent in the previous observation sync phase
                            // - we're preventing further errors on the backend as client no longer
                            //   attempts to send those observations (only modified observations are sent).
                            observation.copyLocalAttributes(old);
                            observation.modified = false;
                            observation.localImages = removeImages(old, observation);
                            insertOrUpdate = true;
                        }
                    } else {
                        // New from server
                        insertOrUpdate = true;
                    }

                    if (insertOrUpdate) {
                        observation.username = username;
                        db.replace("observation", null, observationToContentValues(observation));
                    }
                }
            }
        });
    }

    private List<LocalImage> removeImages(final GameObservation local, final GameObservation remote) {
        final ArrayList<LocalImage> images = new ArrayList<>();

        for (final LocalImage image : local.localImages) {
            if (local.imageIds.contains(image.serverId) && !remote.imageIds.contains(image.serverId)) {
                Utils.LogMessage("Observation image removed from the server: " + image.serverId);
            } else {
                images.add(image);
            }
        }

        return images;
    }

    private static GameObservation cursorToObservation(@NonNull final AsyncCursor cursor) {
        final GameObservation observation = new GameObservation();
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
        observation.observationCategory =
                ObservationCategory.fromString(cursor.getString("observationCategory"));
        observation.observationType = ObservationType.fromString(cursor.getString("observationType"));
        observation.deerHuntingType = DeerHuntingType.fromString(cursor.getString("deerHuntingType"));
        observation.deerHuntingTypeDescription = cursor.getString("deerHuntingTypeDescription");
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
        observation.mooselikeCalfAmount = cursor.getInt("mooselikeCalfAmount");
        observation.mooselikeUnknownSpecimenAmount = cursor.getInt("mooselikeUnknownSpecimenAmount");
        observation.verifiedByCarnivoreAuthority = cursor.getBool("verifiedByCarnivoreAuthority");
        observation.observerName = cursor.getString("observerName");
        observation.observerPhoneNumber = cursor.getString("observerPhoneNumber");
        observation.officialAdditionalInfo = cursor.getString("officialAdditionalInfo");
        observation.inYardDistanceToResidence = cursor.getInt("inYardDistanceToResidence");
        observation.litter = cursor.getBool("litter");
        observation.pack = cursor.getBool("pack");
        observation.deleted = cursor.getInt("deleted") != 0;
        observation.modified = cursor.getInt("modified") != 0;
        observation.localImages = JsonUtils.jsonToList(cursor.getString("localImages"), LocalImage.class);
        observation.username = cursor.getString("username");
        return observation;
    }

    private static ContentValues observationToContentValues(final GameObservation observation) {
        final ContentValues values = new ContentValues();
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
        values.put("imageIds", JsonUtils.objectToJson(observation.imageIds));
        values.put("observationCategory", ObservationCategory.toString(observation.observationCategory));
        values.put("observationType", ObservationType.toString(observation.observationType));
        values.put("deerHuntingType", DeerHuntingType.toString(observation.deerHuntingType));
        values.put("deerHuntingTypeDescription", observation.deerHuntingTypeDescription);
        values.put("specimens", JsonUtils.objectToJson(observation.specimens));
        values.put("canEdit", observation.canEdit);
        values.put("mobileClientRefId", observation.mobileClientRefId);
        values.put("totalSpecimenAmount", observation.totalSpecimenAmount);
        values.put("mooselikeMaleAmount", observation.mooselikeMaleAmount);
        values.put("mooselikeFemaleAmount", observation.mooselikeFemaleAmount);
        values.put("mooselikeFemale1CalfAmount", observation.mooselikeFemale1CalfAmount);
        values.put("mooselikeFemale2CalfsAmount", observation.mooselikeFemale2CalfsAmount);
        values.put("mooselikeFemale3CalfsAmount", observation.mooselikeFemale3CalfsAmount);
        values.put("mooselikeFemale4CalfsAmount", observation.mooselikeFemale4CalfsAmount);
        values.put("mooselikeCalfAmount", observation.mooselikeCalfAmount);
        values.put("mooselikeUnknownSpecimenAmount", observation.mooselikeUnknownSpecimenAmount);
        values.put("verifiedByCarnivoreAuthority", observation.verifiedByCarnivoreAuthority);
        values.put("observerName", observation.observerName);
        values.put("observerPhoneNumber", observation.observerPhoneNumber);
        values.put("officialAdditionalInfo", observation.officialAdditionalInfo);
        values.put("inYardDistanceToResidence", observation.inYardDistanceToResidence);
        values.put("litter", observation.litter);
        values.put("pack", observation.pack);
        values.put("deleted", observation.deleted);
        values.put("modified", observation.modified);
        values.put("localImages", JsonUtils.objectToJson(observation.localImages));
        values.put("username", observation.username);
        return values;
    }

    private static GameObservation findOne(final SQLiteDatabase db, final String query, final String... args) {
        GameObservation result = null;

        final AsyncCursor cursor = query(db, query, args);
        if (cursor.moveToFirst()) {
            result = cursorToObservation(cursor);
        }
        cursor.close();

        return result;
    }
}
