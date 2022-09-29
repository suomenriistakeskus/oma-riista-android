package fi.riista.mobile.srva;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.srva.SrvaApproverInfo;
import fi.riista.mobile.models.srva.SrvaAuthorInfo;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaMethod;
import fi.riista.mobile.models.srva.SrvaSpecimen;
import fi.riista.mobile.utils.BaseDatabase;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.riista.mobile.utils.Utils;
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

    private static SrvaEvent findOne(final SQLiteDatabase db, final String query, final String... args) {
        SrvaEvent result = null;

        final AsyncCursor cursor = query(db, query, args);
        if (cursor.moveToFirst()) {
            result = cursorToEvent(cursor);
        }
        cursor.close();

        return result;
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

    private static ContentValues eventToContentValues(final SrvaEvent event) {
        final ContentValues values = new ContentValues();
        values.put("localId", event.localId);
        values.put("remoteId", event.remoteId);
        values.put("rev", event.rev);
        values.put("type", event.type);
        values.put("latitude", event.geoLocation.latitude);
        values.put("longitude", event.geoLocation.longitude);
        values.put("source", event.geoLocation.source);
        values.put("accuracy", event.geoLocation.accuracy);
        values.put("altitude", event.geoLocation.altitude);
        values.put("altitudeAccuracy", event.geoLocation.altitudeAccuracy);
        values.put("pointOfTime", event.pointOfTime);
        values.put("gameSpeciesCode", event.gameSpeciesCode);
        values.put("description", event.description);
        values.put("canEdit", event.canEdit);
        values.put("imageIds", JsonUtils.objectToJson(event.imageIds));
        values.put("eventName", event.eventName);
        values.put("deportationOrderNumber", event.deportationOrderNumber);
        values.put("eventType", event.eventType);
        values.put("eventTypeDetail", event.eventTypeDetail);
        values.put("otherEventTypeDetailDescription", event.otherEventTypeDetailDescription);
        values.put("totalSpecimenAmount", event.totalSpecimenAmount);
        values.put("otherMethodDescription", event.otherMethodDescription);
        values.put("otherTypeDescription", event.otherTypeDescription);
        values.put("methods", JsonUtils.objectToJson(event.methods));
        values.put("personCount", event.personCount);
        values.put("timeSpent", event.timeSpent);
        values.put("eventResult", event.eventResult);
        values.put("eventResultDetail", event.eventResultDetail);
        values.put("authorInfo", JsonUtils.objectToJson(event.authorInfo));
        values.put("specimens", JsonUtils.objectToJson(event.specimens));
        values.put("rhyId", event.rhyId);
        values.put("state", event.state);
        values.put("otherSpeciesDescription", event.otherSpeciesDescription);
        values.put("approverInfo", JsonUtils.objectToJson(event.approverInfo));
        values.put("mobileClientRefId", event.mobileClientRefId);
        values.put("srvaEventSpecVersion", event.srvaEventSpecVersion);
        values.put("deleted", event.deleted);
        values.put("modified", event.modified);
        values.put("localImages", JsonUtils.objectToJson(event.localImages));
        values.put("username", event.username);
        return values;
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

    public void saveEvent(@NonNull final SrvaEvent event, @NonNull final SaveListener listener) {
        event.username = getUsername();

        final ContentValues values = eventToContentValues(event);

        SrvaDatabaseHelper.getInstance().write(new AsyncWrite() {
            private long localId;

            @Override
            protected void onAsyncWrite(final SQLiteDatabase db) {
                localId = db.replaceOrThrow("event", null, values);
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

    public void loadSrvaYears(@NonNull final SrvaYearsListener listener) {
        SrvaDatabaseHelper.getInstance().query(new AsyncQuery(
                "SELECT pointOfTime FROM event WHERE username = ?", getUsername()) {

            private final HashSet<Integer> mYears = new HashSet<>();

            @Override
            protected void onAsyncQuery(final AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    final String pointOfTime = cursor.getString(0);
                    final DateTime dateTime = DateTimeUtils.parseDateTime(pointOfTime);

                    mYears.add(dateTime.getYear());
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

    public void deleteEvent(@NonNull final SrvaEvent event,
                            final boolean force,
                            @NonNull final DeleteListener listener) {
        deleteEvent(event.localId, event.remoteId, force, listener);
    }

    public void deleteEvent(@Nullable final Long localEventId,
                            @Nullable final Long remoteEventId,
                            final boolean force,
                            @NonNull final DeleteListener listener) {
        deleteEntry(SrvaDatabaseHelper.getInstance(), "event",
                localEventId, remoteEventId, force, listener);
    }

    public void loadLatestEvents(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener,
                "SELECT * FROM event WHERE username = ? AND deleted = 0 AND eventType != 'OTHER' AND gameSpeciesCode IS NOT NULL ORDER BY pointOfTime DESC",
                getUsername());
    }

    public void loadDeletedRemoteEvents(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted != 0 AND remoteId IS NOT NULL", getUsername());
    }

    public void loadModifiedEvents(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted = 0 AND modified != 0", getUsername());
    }

    public void loadEventsWithLocalImages(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted = 0 AND LENGTH(localImages) > 5", getUsername());
    }

    public void loadAllEvents(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ?", getUsername());
    }

    public void loadEvents(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted = 0", getUsername());
    }

    public void loadEventsWithAnyImages(@NonNull final SrvaEventsListener listener) {
        loadEventsQuery(listener,
                "SELECT * FROM event WHERE username = ? AND deleted = 0 AND (LENGTH(localImages) > 5 OR LENGTH(imageIds) > 5) ORDER BY pointOfTime DESC",
                getUsername());
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

    public void handleReceivedEvents(@NonNull final List<SrvaEvent> events) {
        loadAllEvents(locals -> handleReceivedEvents(events, locals));
    }

    private void handleReceivedEvents(final List<SrvaEvent> events, final List<SrvaEvent> localEvents) {
        final String username = getUsername();

        final HashMap<Long, SrvaEvent> remotesMap = createRemoteIdMap(events);
        final HashMap<Long, SrvaEvent> localsMap = createRemoteIdMap(localEvents);

        SrvaDatabaseHelper.getInstance().write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                for (final SrvaEvent event : events) {
                    boolean insertOrUpdate = false;

                    final SrvaEvent old = localsMap.get(event.remoteId);
                    if (old != null) {
                        // We have this item locally, compare revisions
                        if (event.rev >= old.rev && !old.deleted) {
                            // Server version is newer or equal, replace our local, not deleted version with it

                            // User may have local modifications but for some reason sending those
                            // to the backend failed: changes are sent first, only then are updated
                            // srvas received (yes, we're implicitly depending on knowing how srva
                            // synchronization is implemented).
                            //
                            // The most probable reason for send failure is version conflict i.e.
                            // user has made local modifications but the version on the server
                            // has also been updated. When sending local modifications there's
                            // a version conflict on the backend which causes send to fail.
                            //
                            // If this is the case, the local version of the srva has most
                            // likely been already updated as previous implementation overwrote
                            // local modifications without questioning. The previous implementation
                            // also kept local attributes (incl. modified flag) and as a result
                            // user may have srvas with server data and modified flag.
                            //
                            // In order to prevent errors in the backend logs let's just clear
                            // modified flag from srva:
                            // - user should not lose any valid local modifications since those are
                            //   supposedly sent in the previous srva sync phase
                            // - we're preventing errors on the backend as client no longer
                            //   attempts to send those srvas (only modified srvas are sent).
                            event.copyLocalAttributes(old);
                            event.modified = false;
                            event.localImages = removeImages(old, event);
                            insertOrUpdate = true;
                        }
                    } else {
                        // New from server
                        insertOrUpdate = true;
                    }

                    if (insertOrUpdate) {
                        event.username = username;
                        db.replace("event", null, eventToContentValues(event));
                    }
                }

                for (final Long remoteId : localsMap.keySet()) {
                    if (!remotesMap.containsKey(remoteId)) {
                        // This remote id is in local database but it is missing from the server,
                        // which means it was deleted from the server.
                        db.delete("event", "remoteId = ?", new String[]{"" + remoteId});
                    }
                }
            }
        });
    }

    private HashMap<Long, SrvaEvent> createRemoteIdMap(final List<SrvaEvent> events) {
        final HashMap<Long, SrvaEvent> map = new HashMap<>();

        for (final SrvaEvent event : events) {
            if (event.remoteId != null) {
                map.put(event.remoteId, event);
            }
        }

        return map;
    }

    private List<LocalImage> removeImages(final SrvaEvent local, final SrvaEvent remote) {
        final ArrayList<LocalImage> images = new ArrayList<>();

        for (final LocalImage image : local.localImages) {
            if (local.imageIds.contains(image.serverId) && !remote.imageIds.contains(image.serverId)) {
                Utils.LogMessage("Event image removed from the server: " + image.serverId);
            } else {
                images.add(image);
            }
        }

        return images;
    }

    public interface SrvaEventsListener {
        void onEvents(List<SrvaEvent> events);
    }

    public interface SrvaEventListener {
        void onEvent(SrvaEvent event);
    }

    public interface SrvaYearsListener {
        void onYears(List<Integer> years);
    }
}
