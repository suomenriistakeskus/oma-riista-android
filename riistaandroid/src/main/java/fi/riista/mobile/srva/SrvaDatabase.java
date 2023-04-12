package fi.riista.mobile.srva;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.srva.SrvaApproverInfo;
import fi.riista.mobile.models.srva.SrvaAuthorInfo;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaMethod;
import fi.riista.mobile.models.srva.SrvaSpecimen;
import fi.riista.mobile.utils.BaseDatabase;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncQuery;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;

public class SrvaDatabase extends BaseDatabase {

    private static SrvaDatabase sInstance;

    public static void init(@NonNull final Context context, @NonNull final UserInfoStore userInfoStore) {
        SrvaDatabaseHelper.init(context);

        sInstance = new SrvaDatabase(userInfoStore);
    }

    public static SrvaDatabase getInstance() {
        return sInstance;
    }

    private static SrvaEvent cursorToEvent(final AsyncCursor cursor) {
        final SrvaEvent event = new SrvaEvent();
        event.localId = cursor.getLong("localId");
        event.remoteId = cursor.getLong("remoteId");
        event.rev = cursor.getLong("rev");
        event.type = cursor.getString("type");
        event.geoLocation = new GeoLocation();
        event.geoLocation.latitude = cursor.getInt("latitude");
        event.geoLocation.longitude = cursor.getInt("longitude");
        event.geoLocation.source = cursor.getString("source");
        event.geoLocation.accuracy = cursor.getDouble("accuracy");
        event.geoLocation.altitude = cursor.getDouble("altitude");
        event.geoLocation.altitudeAccuracy = cursor.getDouble("altitudeAccuracy");
        event.pointOfTime = cursor.getString("pointOfTime");
        event.gameSpeciesCode = cursor.getInt("gameSpeciesCode");
        event.description = cursor.getString("description");
        event.canEdit = cursor.getInt("canEdit", 0) != 0;
        event.imageIds = JsonUtils.jsonToList(cursor.getString("imageIds"), String.class);
        event.eventName = cursor.getString("eventName");
        event.deportationOrderNumber = cursor.getString("deportationOrderNumber");
        event.eventType = cursor.getString("eventType");
        event.eventTypeDetail = cursor.getString("eventTypeDetail");
        event.otherEventTypeDetailDescription = cursor.getString("otherEventTypeDetailDescription");
        event.totalSpecimenAmount = cursor.getInt("totalSpecimenAmount");
        event.otherMethodDescription = cursor.getString("otherMethodDescription");
        event.otherTypeDescription = cursor.getString("otherTypeDescription");
        event.methods = JsonUtils.jsonToList(cursor.getString("methods"), SrvaMethod.class);
        event.personCount = cursor.getInt("personCount");
        event.timeSpent = cursor.getInt("timeSpent");
        event.eventResult = cursor.getString("eventResult");
        event.eventResultDetail = cursor.getString("eventResultDetail");
        event.authorInfo = JsonUtils.jsonToObject(cursor.getString("authorInfo"), SrvaAuthorInfo.class, true);
        event.specimens = JsonUtils.jsonToList(cursor.getString("specimens"), SrvaSpecimen.class);
        event.rhyId = cursor.getInt("rhyId");
        event.state = cursor.getString("state");
        event.otherSpeciesDescription = cursor.getString("otherSpeciesDescription");
        event.approverInfo = JsonUtils.jsonToObject(cursor.getString("approverInfo"), SrvaApproverInfo.class, true);
        event.mobileClientRefId = cursor.getLong("mobileClientRefId");
        event.srvaEventSpecVersion = cursor.getInt("srvaEventSpecVersion");
        event.deleted = cursor.getInt("deleted") != 0;
        event.modified = cursor.getInt("modified") != 0;
        event.localImages = JsonUtils.jsonToList(cursor.getString("localImages"), LocalImage.class);
        event.username = cursor.getString("username");
        return event;
    }

    private SrvaDatabase(@NonNull final UserInfoStore userInfoStore) {
        super(userInfoStore);
    }

    public void loadEvent(final long localId, @NonNull final SrvaEventListener listener) {
        SrvaDatabaseHelper.getInstance().query(new AsyncQuery(
                "SELECT * FROM event WHERE username = ? AND localId = ?",
                getUsername(), "" + localId) {

            private SrvaEvent mResult;

            @Override
            protected void onAsyncQuery(final AsyncCursor cursor) {
                if (cursor.moveToNext()) {
                    mResult = cursorToEvent(cursor);
                }
            }

            @Override
            protected void onFinish() {
                listener.onEvent(mResult);
            }

            @Override
            protected void onError() {
                listener.onEvent(mResult);
            }
        });
    }

    public void loadNotCopiedEvents(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND commonLocalId IS NULL", getUsername());
    }

    public void setCommonLocalId(final long localId, final long commonLocalId) {
        SrvaDatabaseHelper.getInstance().write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                final ContentValues values = new ContentValues();
                values.put("commonLocalId", commonLocalId);
                final String[] whereArgs = {
                    "" + localId,
                    getUsername()
                };
                db.update("event", values, "localId = ? AND username = ?", whereArgs);
            }
        });
    }

    private void loadEventsQuery(final SrvaEventsListener listener, final String query, final String... args) {
        SrvaDatabaseHelper.getInstance().query(new AsyncQuery(query, args) {
            private final ArrayList<SrvaEvent> results = new ArrayList<>();

            @Override
            protected void onAsyncQuery(final AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    results.add(cursorToEvent(cursor));
                }
            }

            @Override
            protected void onFinish() {
                listener.onEvents(results);
            }

            @Override
            protected void onError() {
                listener.onEvents(results);
            }
        });
    }

    public interface SrvaEventsListener {
        void onEvents(List<SrvaEvent> events);
    }

    public interface SrvaEventListener {
        void onEvent(SrvaEvent event);
    }
}
