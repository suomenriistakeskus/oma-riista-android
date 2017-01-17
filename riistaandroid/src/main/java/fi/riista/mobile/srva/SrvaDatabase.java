package fi.riista.mobile.srva;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
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
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncQuery;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;

public class SrvaDatabase extends BaseDatabase<SrvaEvent> {

    public interface SrvaEventListener {
        void onEvents(List<SrvaEvent> events);
    }

    private static SrvaDatabase sInstance;

    public static void init(Context context) {
        SrvaDatabaseHelper.init(context);

        sInstance = new SrvaDatabase();
    }

    public static SrvaDatabase getInstance() {
        return sInstance;
    }

    public void saveEvent(final SrvaEvent event, final SaveListener listener) {
        event.username = getUsername();

        final ContentValues values = eventToContentValues(event);

        SrvaDatabaseHelper.getInstance().write(new AsyncWrite() {
            private long localId;

            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
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

    public void deleteEvent(SrvaEvent event, boolean force, DeleteListener listener) {
        deleteEntry(SrvaDatabaseHelper.getInstance(), "event",
                event.localId, event.remoteId, force, listener);
    }

    public void loadLatestEvents(SrvaEventListener listener) {
        loadEventsQuery(listener,
                "SELECT * FROM event WHERE username = ? AND deleted = 0 AND eventType != 'OTHER' AND gameSpeciesCode IS NOT NULL ORDER BY pointOfTime DESC",
                getUsername());
    }

    public void loadDeletedRemoteEvents(SrvaEventListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted != 0 AND remoteId IS NOT NULL", getUsername());
    }

    public void loadModifiedEvents(SrvaEventListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted = 0 AND modified != 0", getUsername());
    }

    public void loadEventsWithLocalImages(SrvaEventListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted = 0 AND LENGTH(localImages) > 5", getUsername());
    }

    public void loadAllEvents(SrvaEventListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ?", getUsername());
    }

    public void loadEvents(SrvaEventListener listener) {
        loadEventsQuery(listener, "SELECT * FROM event WHERE username = ? AND deleted = 0", getUsername());
    }

    public void loadEventsWithAnyImages(SrvaEventListener listener) {
        loadEventsQuery(listener,
                "SELECT * FROM event WHERE username = ? AND deleted = 0 AND LENGTH(localImages) > 5 OR LENGTH(imageIds) > 5",
                getUsername());
    }

    private void loadEventsQuery(final SrvaEventListener listener, String query, String... args) {
        SrvaDatabaseHelper.getInstance().query(new AsyncQuery(query, args) {
            private ArrayList<SrvaEvent> results = new ArrayList<SrvaEvent>();

            @Override
            protected void onAsyncQuery(AsyncCursor cursor) {
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

    public void handleReceivedEvents(final List<SrvaEvent> events) {
        loadAllEvents(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> locals) {
                handleReceivedEvents(events, locals);
            }
        });
    }

    private void handleReceivedEvents(final List<SrvaEvent> events, final List<SrvaEvent> localEvents) {
        final String username = getUsername();

        final HashMap<Long, SrvaEvent> remotesMap = createRemoteIdMap(events);
        final HashMap<Long, SrvaEvent> localsMap = createRemoteIdMap(localEvents);

        SrvaDatabaseHelper.getInstance().write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                for (SrvaEvent event : events) {
                    boolean insert = false;

                    SrvaEvent old = localsMap.get(event.remoteId);
                    if (old != null) {
                        //We have this item locally, compare revisions
                        if (event.rev >= old.rev && !old.deleted) {
                            //Server version is newer or equal, replace our local, not deleted version with it
                            event.copyLocalAttributes(old);
                            event.localImages = removeImages(old, event);
                            insert = true;
                        }
                    } else {
                        //New from server
                        insert = true;
                    }

                    if (insert) {
                        event.username = username;
                        db.replace("event", null, eventToContentValues(event));
                    }
                }

                for (Long remoteId : localsMap.keySet()) {
                    if (!remotesMap.containsKey(remoteId)) {
                        //This remote id is in local database but it is missing from the server,
                        //which means it was deleted from the server.
                        db.delete("event", "remoteId = ?", new String[]{"" + remoteId});
                    }
                }
            }
        });
    }

    private HashMap<Long, SrvaEvent> createRemoteIdMap(List<SrvaEvent> events) {
        HashMap<Long, SrvaEvent> map = new HashMap<>();
        for (SrvaEvent event : events) {
            if (event.remoteId != null) {
                map.put(event.remoteId, event);
            }
        }
        return map;
    }

    private List<LocalImage> removeImages(SrvaEvent local, SrvaEvent remote) {
        ArrayList<LocalImage> images = new ArrayList<>();
        for (LocalImage image : local.localImages) {
            if (local.imageIds.contains(image.serverId) && !remote.imageIds.contains(image.serverId)) {
                Utils.LogMessage("Event image removed from the server: " + image.serverId);
            } else {
                images.add(image);
            }
        }
        return images;
    }

    private static SrvaEvent findOne(SQLiteDatabase db, String query, String... args) {
        SrvaEvent result = null;

        AsyncCursor cursor = query(db, query, args);
        if (cursor.moveToFirst()) {
            result = cursorToEvent(cursor);
        }
        cursor.close();

        return result;
    }

    private static SrvaEvent cursorToEvent(AsyncCursor cursor) {
        SrvaEvent event = new SrvaEvent();
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
        event.eventType = cursor.getString("eventType");
        event.totalSpecimenAmount = cursor.getInt("totalSpecimenAmount");
        event.otherMethodDescription = cursor.getString("otherMethodDescription");
        event.otherTypeDescription = cursor.getString("otherTypeDescription");
        event.methods = JsonUtils.jsonToList(cursor.getString("methods"), SrvaMethod.class);
        event.personCount = cursor.getInt("personCount");
        event.timeSpent = cursor.getInt("timeSpent");
        event.eventResult = cursor.getString("eventResult");
        event.authorInfo = JsonUtils.jsonToObject(cursor.getString("authorInfo"), SrvaAuthorInfo.class);
        event.specimens = JsonUtils.jsonToList(cursor.getString("specimens"), SrvaSpecimen.class);
        event.rhyId = cursor.getInt("rhyId");
        event.state = cursor.getString("state");
        event.otherSpeciesDescription = cursor.getString("otherSpeciesDescription");
        event.approverInfo = JsonUtils.jsonToObject(cursor.getString("approverInfo"), SrvaApproverInfo.class);
        event.mobileClientRefId = cursor.getLong("mobileClientRefId");
        event.srvaEventSpecVersion = cursor.getInt("srvaEventSpecVersion");
        event.deleted = cursor.getInt("deleted") != 0;
        event.modified = cursor.getInt("modified") != 0;
        event.localImages = JsonUtils.jsonToList(cursor.getString("localImages"), LocalImage.class);
        event.username = cursor.getString("username");
        return event;
    }

    private static ContentValues eventToContentValues(SrvaEvent event) {
        ContentValues values = new ContentValues();
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
        values.put("imageIds", objectToJson(event.imageIds));
        values.put("eventName", event.eventName);
        values.put("eventType", event.eventType);
        values.put("totalSpecimenAmount", event.totalSpecimenAmount);
        values.put("otherMethodDescription", event.otherMethodDescription);
        values.put("otherTypeDescription", event.otherTypeDescription);
        values.put("methods", objectToJson(event.methods));
        values.put("personCount", event.personCount);
        values.put("timeSpent", event.timeSpent);
        values.put("eventResult", event.eventResult);
        values.put("authorInfo", objectToJson(event.authorInfo));
        values.put("specimens", objectToJson(event.specimens));
        values.put("rhyId", event.rhyId);
        values.put("state", event.state);
        values.put("otherSpeciesDescription", event.otherSpeciesDescription);
        values.put("approverInfo", objectToJson(event.approverInfo));
        values.put("mobileClientRefId", event.mobileClientRefId);
        values.put("srvaEventSpecVersion", event.srvaEventSpecVersion);
        values.put("deleted", event.deleted);
        values.put("modified", event.modified);
        values.put("localImages", objectToJson(event.localImages));
        values.put("username", event.username);
        return values;
    }
}
