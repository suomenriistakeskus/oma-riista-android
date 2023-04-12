package fi.riista.mobile.observation;

import static java.util.Objects.requireNonNull;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
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
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncQuery;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;

@Singleton
public class ObservationDatabase extends BaseDatabase {

    public interface ObservationsListener {
        void onObservations(List<GameObservation> observations);
    }

    private final ObservationDatabaseHelper mDatabaseHelper;

    @Inject
    public ObservationDatabase(@NonNull final ObservationDatabaseHelper databaseHelper,
                               @NonNull final UserInfoStore userInfoStore) {

        super(userInfoStore);

        mDatabaseHelper = requireNonNull(databaseHelper);
    }

    public void loadNotCopiedObservations(@NonNull final ObservationsListener listener) {
        loadObservationsQuery(listener, "SELECT * FROM observation WHERE username = ? AND commonLocalId IS NULL", getUsername());
    }

    public void setCommonLocalId(final long localId, final long commonLocalId) {
        mDatabaseHelper.write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                final ContentValues values = new ContentValues();
                values.put("commonLocalId", commonLocalId);
                final String[] whereArgs = {
                        "" + localId,
                        getUsername()
                };
                db.update("observation", values, "localId = ? AND username = ?", whereArgs);
            }
        });
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
}
