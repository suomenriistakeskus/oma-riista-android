package fi.riista.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fi.riista.mobile.database.DiaryEntryUpdate.UpdateType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.network.LogImageTask;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.MapUtils;
import fi.riista.mobile.utils.Utils;

/**
 * SQLite data source
 * Completes database operations on SQLite level
 */
public class DiaryDataSource {

    public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String ISO_8601_SHORT = "yyyy-MM-dd'T'HH:mm:ss";
    private static SQLiteDatabase mDatabase;
    private static DiaryHelper mDbHelper;
    private String mUsername = "";

    DiaryDataSource(Context context) {
        mDbHelper = new DiaryHelper(context);
    }

    private static LogImage logImageFromCursor(Cursor cursor, boolean getDeletedImages) {
        int imageTypeCol = cursor.getColumnIndex(DiaryHelper.COLUMN_IMAGETYPE);
        if (imageTypeCol != -1) {
            int imageStatus = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_IMAGESTATUS));
            if (imageStatus != DiaryHelper.IMAGESTATUS_DELETED || getDeletedImages) {
                int imageType = cursor.getInt(imageTypeCol);
                LogImage image = null;
                if (imageType == DiaryHelper.IMAGETYPE_ID) {
                    image = new LogImage(cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_IMAGEID)));
                } else if (imageType == DiaryHelper.IMAGETYPE_URI) {
                    image = new LogImage(Uri.parse(cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_IMAGEURI))));
                }
                if (image != null) {
                    image.diaryEntryId = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_DIARYID));
                    image.uuid = cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_IMAGEID));
                    image.imageStatus = imageStatus;
                    return image;
                }
            }
        }
        return null;
    }

    private static ContentValues eventToContentValues(GameHarvest event) {
        ContentValues values = new ContentValues();
        values.put(DiaryHelper.COLUMN_ID, event.mId);
        values.put(DiaryHelper.COLUMN_API_DATA_FORMAT, event.mApiDataFormat);
        values.put(DiaryHelper.COLUMN_CLIENT_DATA_FORMAT, event.mClientDataFormat);
        values.put(DiaryHelper.COLUMN_TYPE, event.mType);
        values.put(DiaryHelper.COLUMN_GAMESPECIESID, event.mSpeciesID);
        values.put(DiaryHelper.COLUMN_AMOUNT, event.mAmount);
        values.put(DiaryHelper.COLUMN_DESCRIPTION, event.mMessage);
        DateFormat df = new SimpleDateFormat(ISO_8601);
        values.put(DiaryHelper.COLUMN_POINTOFTIME, df.format(event.mTime.getTime()));
        values.put(DiaryHelper.COLUMN_REV, event.mRev);
        values.put(DiaryHelper.COLUMN_REMOTE, event.mRemote);
        values.put(DiaryHelper.COLUMN_SENT, event.mSent);
        values.put(DiaryHelper.COLUMN_HARVESTREPORTDONE, event.mHarvestReportDone);
        values.put(DiaryHelper.COLUMN_HARVESTREPORTREQUIRED, event.mHarvestReportRequired);
        values.put(DiaryHelper.COLUMN_HARVESTREPORTSTATE, event.mHarvestReportState);
        values.put(DiaryHelper.COLUMN_PERMITNUMBER, event.mPermitNumber);
        values.put(DiaryHelper.COLUMN_PERMITTYPE, event.mPermitType);
        values.put(DiaryHelper.COLUMN_HARVESTPERMITSTATE, event.mStateAcceptedToHarvestPermit);
        values.put(DiaryHelper.COLUMN_CANEDIT, event.mCanEdit);
        values.put(DiaryHelper.COLUMN_PENDINGOPERATION, event.mPendingOperation.value());
        if (event.mSpecimen != null) {
            try {

                values.put(DiaryHelper.COLUMN_SPECIMENDATA, JsonUtils.objectToJson(event.mSpecimen).getBytes());
            } catch (Exception e) {
                Log.w(DiaryDataSource.class.getSimpleName(), "Failed to serialize specimen data!");
            }
        } else {
            values.put(DiaryHelper.COLUMN_SPECIMENDATA, (Byte) null);
        }
        return values;
    }

    private static ContentValues eventGeovaluesToContentValues(GameHarvest event) {
        ContentValues geoValues = new ContentValues();
        geoValues.put(DiaryHelper.COLUMN_DIARYID, event.mLocalId);
        geoValues.put(DiaryHelper.COLUMN_LATITUDE, event.mCoordinates.first);
        geoValues.put(DiaryHelper.COLUMN_LONGITUDE, event.mCoordinates.second);
        geoValues.put(DiaryHelper.COLUMN_ACCURACY, event.mAccuracy);
        geoValues.put(DiaryHelper.COLUMN_HASALTITUDE, event.mHasAltitude);
        geoValues.put(DiaryHelper.COLUMN_ALTITUDE, event.mAltitude);
        geoValues.put(DiaryHelper.COLUMN_ALTITUDEACCURACY, event.mAltitudeAccuracy);
        geoValues.put(DiaryHelper.COLUMN_LOCATION_SOURCE, event.mLocationSource);
        return geoValues;
    }

    /**
     * Create event from sqlite cursor
     *
     * @param cursor Sqlite cursor
     * @param images Images to be added to event
     * @return GameHarvest object
     */
    private static GameHarvest createEvent(Cursor cursor, List<LogImage> images) {
        Location location = new Location("");
        Pair<Double, Double> loc = MapUtils.ETRMStoWGS84(cursor.getLong(cursor.getColumnIndex(DiaryHelper.COLUMN_LATITUDE)),
                cursor.getLong(cursor.getColumnIndex(DiaryHelper.COLUMN_LONGITUDE)));
        location.setLatitude(loc.first);
        location.setLongitude(loc.second);
        location.setAccuracy(cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_ACCURACY)));
        location.setAltitude(cursor.getFloat(cursor.getColumnIndex(DiaryHelper.COLUMN_ALTITUDE)));
        Calendar calendar = Calendar.getInstance();
        Date date = Utils.parseDate(cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_POINTOFTIME)));
        calendar.setTime(date);
        GameHarvest event = new GameHarvest(cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_GAMESPECIESID)),
                cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_AMOUNT)),
                cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_DESCRIPTION)),
                calendar,
                cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_TYPE)),
                location, images);
        int latitudeIndex = cursor.getColumnIndex(DiaryHelper.COLUMN_LATITUDE);
        int longitudeIndex = cursor.getColumnIndex(DiaryHelper.COLUMN_LONGITUDE);
        if (latitudeIndex != -1 && longitudeIndex != -1) {
            event.mCoordinates = new Pair<Long, Long>(cursor.getLong(latitudeIndex), cursor.getLong(longitudeIndex));
        }
        event.mAccuracy = cursor.getFloat(cursor.getColumnIndex(DiaryHelper.COLUMN_ACCURACY));
        event.mHasAltitude = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_HASALTITUDE)) == 1);
        event.mAltitude = cursor.getFloat(cursor.getColumnIndex(DiaryHelper.COLUMN_ALTITUDE));
        event.mAltitudeAccuracy = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_ALTITUDEACCURACY));
        event.mLocationSource = cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_LOCATION_SOURCE));

        event.mId = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_ID));
        event.mApiDataFormat = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_API_DATA_FORMAT));
        event.mClientDataFormat = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_CLIENT_DATA_FORMAT));
        event.mLocalId = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_LOCALID));
        event.mRev = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_REV));
        event.mSent = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_SENT)) == 1);
        event.mRemote = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_REMOTE)) == 1);
        event.mHarvestReportDone = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_HARVESTREPORTDONE)) == 1);
        event.mHarvestReportRequired = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_HARVESTREPORTREQUIRED)) == 1);
        event.mHarvestReportState = cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_HARVESTREPORTSTATE));
        event.mPermitNumber = cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_PERMITNUMBER));
        event.mPermitType = cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_PERMITTYPE));
        event.mStateAcceptedToHarvestPermit = cursor.getString(cursor.getColumnIndex(DiaryHelper.COLUMN_HARVESTPERMITSTATE));
        event.mCanEdit = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_CANEDIT)) == 1);
        event.mMobileClientRefId = cursor.getLong(cursor.getColumnIndex(DiaryHelper.COLUMN_MOBILEREFID));
        event.mPendingOperation = DiaryHelper.UpdateType.valueOf(cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_PENDINGOPERATION)));
        int specimenDataIndex = cursor.getColumnIndex(DiaryHelper.COLUMN_SPECIMENDATA);
        if (specimenDataIndex != -1) {
            try {
                // Column value may be null when migrating from old db version resulting in exception.
                String data = new String(cursor.getBlob(specimenDataIndex));

                event.mSpecimen = JsonUtils.jsonToList(data, Specimen.class);
            } catch (Exception e) {
                Log.d(DiaryDataSource.class.getSimpleName(), "Failed to deserialize specimen data: " + e.getMessage());
            }
        }
        return event;
    }

    public synchronized void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        if (mDatabase != null && mDatabase.isOpen() && mDatabase.inTransaction()) {
            mDatabase.endTransaction();
        }

        if (mDbHelper != null)
            mDbHelper.close();
    }

    public GameHarvest getEventByLocalId(int localId) {
        return getEventByLocalId(localId, false);
    }

    private GameHarvest getEventByLocalId(int localId, boolean getDeletedImages) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            QueryConditionList whereConditions = new QueryConditionList();
            whereConditions.add(DiaryHelper.COLUMN_LOCALID + " = ?", Integer.valueOf(localId).toString());
            addCommonWhereConditions(whereConditions);
            String query = diarySelectPart(false)
                    + diaryJoinPart()
                    + " WHERE " + whereConditions.getWhereCondition();
            query += " ORDER BY " + DiaryHelper.COLUMN_POINTOFTIME;
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            List<GameHarvest> logEvents = aggregateEvents(cursor, getDeletedImages);
            cursor.close();
            if (logEvents.size() == 1) {
                return logEvents.get(0);
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public List<GameHarvest> getCatchesByYear(int year) {
        List<GameHarvest> logEventList = new ArrayList<GameHarvest>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            QueryConditionList whereConditions = new QueryConditionList();
            whereConditions.add("((year = " + year + " AND month > 7) OR (year = " + (year + 1) + " AND month < 8))", null);
            whereConditions.add(DiaryHelper.COLUMN_TYPE + " = ?", GameHarvest.TYPE_HARVEST);
            addCommonWhereConditions(whereConditions);

            String query = diarySelectPart(true)
                    + diaryJoinPart()
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " ORDER BY " + DiaryHelper.COLUMN_POINTOFTIME;
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            logEventList = aggregateEvents(cursor, false);
            cursor.close();
        } catch (SQLException ignored) {
        }
        return logEventList;
    }

    public List<GameHarvest> getAllEvents() {
        List<GameHarvest> logEventList = new ArrayList<GameHarvest>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            QueryConditionList whereConditions = new QueryConditionList();
            addCommonWhereConditions(whereConditions);
            String query = diarySelectPart(false) + diaryJoinPart();
            if (whereConditions.getCount() > 0)
                query += " WHERE " + whereConditions.getWhereCondition();
            query += " ORDER BY datetime(" + DiaryHelper.COLUMN_POINTOFTIME + ") ASC";
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            logEventList = aggregateEvents(cursor, false);
            cursor.close();
        } catch (SQLException ignored) {
        }
        return logEventList;
    }

    public List<GameHarvest> getLatestSpeciesEvents(int amount, EventTypeFilter filter) {

        List<GameHarvest> logEventList = new ArrayList<GameHarvest>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            QueryConditionList whereConditions = new QueryConditionList();

            String query = diarySelectPart(false) + diaryJoinPart();
            if (filter == EventTypeFilter.CATCHES) {
                whereConditions.add(DiaryHelper.COLUMN_TYPE + " = ?", GameHarvest.TYPE_HARVEST);
            } else if (filter == EventTypeFilter.OBSERVATIONS) {
                whereConditions.add(DiaryHelper.COLUMN_TYPE + " = ?", GameHarvest.TYPE_OBSERVATION);
            } else { //CATCHES_AND_OBSERVATIONS
                List<String> params = new ArrayList<String>();
                params.add(GameHarvest.TYPE_HARVEST);
                params.add(GameHarvest.TYPE_OBSERVATION);
                whereConditions.addWithList(DiaryHelper.COLUMN_TYPE + " = ? OR " + DiaryHelper.COLUMN_TYPE + " = ?", params);
            }
            addCommonWhereConditions(whereConditions);

            query += " WHERE " + whereConditions.getWhereCondition();
            query += " GROUP BY " + DiaryHelper.COLUMN_GAMESPECIESID;
            query += " ORDER BY datetime(" + DiaryHelper.COLUMN_POINTOFTIME + ") DESC";
            if (amount != 0) {
                query += " LIMIT " + amount;
            }
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            logEventList = aggregateEvents(cursor, false);
            cursor.close();
        } catch (SQLException ignored) {
        }
        return logEventList;
    }

    // Gets list of years which contain events that are not sent to the server yet
    public List<Integer> getUnsentEventYears() {

        QueryConditionList whereConditions = new QueryConditionList();
        whereConditions.add(DiaryHelper.COLUMN_SENT + " = 0", null);
        addCommonWhereConditions(whereConditions);

        List<Integer> years = new ArrayList<Integer>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            String query = "SELECT "
                    + "strftime('%Y', " + DiaryHelper.COLUMN_POINTOFTIME + ") as year, "
                    + "strftime('%m', " + DiaryHelper.COLUMN_POINTOFTIME + ") as month"
                    + " FROM " + DiaryHelper.TABLE_DIARY
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " GROUP BY year";

            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            while (!cursor.isAfterLast()) {
                calendar.set(Calendar.YEAR, cursor.getInt(cursor.getColumnIndex("year")));
                calendar.set(Calendar.MONTH, cursor.getInt(cursor.getColumnIndex("month")) - 1); // Convert to 0-based
                years.add(DateTimeUtils.getSeasonStartYearFromDate(calendar));
                cursor.moveToNext();
            }
        } catch (SQLException ignored) {
        }
        return years;
    }

    // Gets list of events that are not yet sent
    public List<GameHarvest> getUnsentEvents() {

        List<GameHarvest> logEventList = new ArrayList<GameHarvest>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            QueryConditionList whereConditions = new QueryConditionList();
            whereConditions.add(DiaryHelper.COLUMN_SENT + " = 0", null);
            addCommonWhereConditions(whereConditions);

            String query = diarySelectPart(false) + diaryJoinPart()
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " ORDER BY " + DiaryHelper.COLUMN_POINTOFTIME + " ASC";
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            logEventList = aggregateEvents(cursor, true);
            cursor.close();
        } catch (SQLException ignored) {
        }
        return logEventList;
    }

    // Takes new event - inserts if it doesn't exist, updates if exists
    boolean insertReceivedEvent(GameHarvest event) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            QueryConditionList whereConditions = new QueryConditionList();
            whereConditions.add(DiaryHelper.COLUMN_ID + " = " + event.mId, null);
            addCommonWhereConditions(whereConditions);

            String query = "SELECT * FROM " + DiaryHelper.TABLE_DIARY + " WHERE " + whereConditions.getWhereCondition();
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            if (cursor.getCount() == 0) {
                addLocalEvent(event, true);
                return true;
            } else {
                cursor.moveToFirst();
                boolean isRemote = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_REMOTE)) == 1);
                boolean isSent = (cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_SENT)) == 1);
                int currentRev = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_REV));
                int storedFormatVersion = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_CLIENT_DATA_FORMAT));
                // Event can be replaced if server rev is newer or if it's same and the event has not been modified
                if (!isRemote || event.mRev > currentRev || (event.mClientDataFormat > storedFormatVersion && isSent) || (event.mRev == currentRev && isSent)) {
                    cursor.moveToFirst();
                    event.mLocalId = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_LOCALID));
                    editLocalEvent(event, true);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds local event to sqlite database
     *
     * @param event
     * @param permanent
     * @return object containing information about added images
     */
    public LogImageUpdate addLocalEvent(GameHarvest event, boolean permanent) {

        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.beginTransaction();
            ContentValues values = eventToContentValues(event);
            values.put(DiaryHelper.COLUMN_USERNAME, mUsername);
            values.put(DiaryHelper.COLUMN_MOBILEREFID, event.mMobileClientRefId);
            Long localId = mDatabase.insert(DiaryHelper.TABLE_DIARY, null, values);
            event.mLocalId = localId.intValue();
            ContentValues geoValues = eventGeovaluesToContentValues(event);
            mDatabase.insert(DiaryHelper.TABLE_GEOLOCATION, null, geoValues);
            LogImageUpdate update = editDiaryImages(localId.intValue(), new ArrayList<LogImage>(), event.mImages, permanent);
            mDatabase.setTransactionSuccessful();
            return update;
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * Updates images in local database using current list of images and new list of images
     *
     * @param diaryLocalId  Local image id
     * @param currentImages Current images in local database
     * @param newImages     New images to be saved to local database
     * @param permanent,    New images as marked as added, deleted images are deleted, otherwise marked as deleted
     * @returns LogImageUpdate object containing information about added and deleted images
     */
    public LogImageUpdate editDiaryImages(int diaryLocalId, List<LogImage> currentImages, List<LogImage> newImages, boolean permanent) {
        List<LogImage> deletedImages = new ArrayList<LogImage>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            for (int i = 0; i < currentImages.size(); i++) {
                boolean exists = false;
                int foundNewImageIndex = 0;
                for (int newImageIndex = 0; newImageIndex < newImages.size(); newImageIndex++) {
                    // Local image is same if UUIDs match
                    if (currentImages.get(i).type == LogImage.ImageType.URI &&
                            newImages.get(newImageIndex).type == LogImage.ImageType.URI &&
                            newImages.get(newImageIndex).uuid.equals(currentImages.get(i).uuid)) {
                        foundNewImageIndex = newImageIndex;
                        exists = true;
                        break;
                    } else if (currentImages.get(i).type == LogImage.ImageType.UUID &&
                            newImages.get(newImageIndex).type == LogImage.ImageType.UUID &&
                            currentImages.get(i).uuid.equals(newImages.get(newImageIndex).uuid)) {
                        // Online image uuids can only appear once
                        foundNewImageIndex = newImageIndex;
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    newImages.remove(foundNewImageIndex);
                } else {
                    deletedImages.add(currentImages.get(i));
                }
            }
            // Delete any images that got deleted
            if (deletedImages.size() > 0) {
                QueryConditionList whereConditions = new QueryConditionList();
                String removeWhereClause = DiaryHelper.COLUMN_IMAGEID + " IN (";
                List<String> deletedUuids = new ArrayList<String>();
                String deleteList = "";
                for (int i = 0; i < deletedImages.size(); i++) {
                    if (!deleteList.equals("")) {
                        deleteList += ", ";
                    }
                    deletedUuids.add(deletedImages.get(i).uuid);
                    deleteList += "?";
                }
                removeWhereClause += deleteList;
                removeWhereClause += ")";
                whereConditions.addWithList(removeWhereClause, deletedUuids);
                whereConditions.add(DiaryHelper.COLUMN_DIARYID + " = " + diaryLocalId, null);
                if (permanent) {
                    mDatabase.delete(DiaryHelper.TABLE_IMAGE, whereConditions.getWhereCondition(), whereConditions.getValueArray());
                } else {
                    ContentValues values = new ContentValues();
                    values.put(DiaryHelper.COLUMN_IMAGESTATUS, DiaryHelper.IMAGESTATUS_DELETED);
                    mDatabase.update(DiaryHelper.TABLE_IMAGE, values, whereConditions.getWhereCondition(), whereConditions.getValueArray());
                }
            }
            // Insert any images that were new
            if (newImages.size() > 0) {
                for (int i = 0; i < newImages.size(); i++) {
                    ContentValues values = new ContentValues();
                    if (newImages.get(i).type == LogImage.ImageType.URI) {
                        values.put(DiaryHelper.COLUMN_IMAGETYPE, DiaryHelper.IMAGETYPE_URI);
                        values.put(DiaryHelper.COLUMN_IMAGEURI, newImages.get(i).uri.toString());
                        if (!permanent) {
                            values.put(DiaryHelper.COLUMN_IMAGESTATUS, DiaryHelper.IMAGESTATUS_INSERTED);
                        }
                    } else {
                        values.put(DiaryHelper.COLUMN_IMAGETYPE, DiaryHelper.IMAGETYPE_ID);
                    }
                    values.put(DiaryHelper.COLUMN_IMAGEID, newImages.get(i).uuid);
                    values.put(DiaryHelper.COLUMN_DIARYID, diaryLocalId);
                    mDatabase.insert(DiaryHelper.TABLE_IMAGE, null, values);
                }
            }
        } catch (SQLException ignored) {
        }
        LogImageUpdate logImageUpdate = new LogImageUpdate();
        logImageUpdate.addedImages = newImages;
        logImageUpdate.deletedImages = deletedImages;
        return logImageUpdate;
    }

    /**
     * Gets image update data from event received from list received with call getUnsentEvents
     *
     * @param event
     * @return
     */
    LogImageUpdate getImageUpdateFromUnsentEvent(GameHarvest event) {
        LogImageUpdate update = new LogImageUpdate();
        List<LogImage> addedImages = new ArrayList<LogImage>();
        List<LogImage> deletedImages = new ArrayList<LogImage>();
        for (int i = 0; i < event.mImages.size(); i++) {
            if (event.mImages.get(i).imageStatus == DiaryHelper.IMAGESTATUS_DELETED) {
                deletedImages.add(event.mImages.get(i));
            } else if (event.mImages.get(i).imageStatus == DiaryHelper.IMAGESTATUS_INSERTED) {
                addedImages.add(event.mImages.get(i));
            }
        }
        update.addedImages = addedImages;
        update.deletedImages = deletedImages;
        return update;
    }

    /**
     * Edits given event in sqlite database using given GameHarvest
     *
     * @param event
     * @param permanent
     * @return Image update data
     */
    public LogImageUpdate editLocalEvent(GameHarvest event, boolean permanent) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            GameHarvest currentEvent = getEventByLocalId(event.mLocalId, true);
            ContentValues values = eventToContentValues(event);
            mDatabase.update(DiaryHelper.TABLE_DIARY, values, DiaryHelper.COLUMN_LOCALID + " = " + event.mLocalId, new String[]{});
            ContentValues geoValues = eventGeovaluesToContentValues(event);
            mDatabase.update(DiaryHelper.TABLE_GEOLOCATION, geoValues, DiaryHelper.COLUMN_DIARYID + " = " + event.mLocalId, new String[]{});
            LogImageUpdate update = editDiaryImages(event.mLocalId, currentEvent.mImages, event.mImages, permanent);
            return update;
        } catch (SQLException ignored) {
        }
        LogImageUpdate update = new LogImageUpdate();
        update.addedImages = new ArrayList<LogImage>();
        update.deletedImages = new ArrayList<LogImage>();
        return update;
    }

    public void markEventAsRemote(GameHarvest event) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DiaryHelper.COLUMN_ID, event.mId);
            values.put(DiaryHelper.COLUMN_REMOTE, 1);
            values.put(DiaryHelper.COLUMN_REV, event.mRev);
            mDatabase.update(DiaryHelper.TABLE_DIARY, values, DiaryHelper.COLUMN_LOCALID + " = " + event.mLocalId, new String[]{});
        } catch (SQLException ignored) {
        }
    }

    /**
     * Update local entry after completing API call to backend.
     *
     * @param event Log entry containing data received from backend
     */
    public void markEventAsSent(GameHarvest event) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DiaryHelper.COLUMN_ID, event.mId);
            values.put(DiaryHelper.COLUMN_REMOTE, 1);
            values.put(DiaryHelper.COLUMN_SENT, 1);
            values.put(DiaryHelper.COLUMN_REV, event.mRev);
            values.put(DiaryHelper.COLUMN_HARVESTREPORTREQUIRED, event.mHarvestReportRequired);
            values.put(DiaryHelper.COLUMN_HARVESTREPORTDONE, event.mHarvestReportDone);
            values.put(DiaryHelper.COLUMN_HARVESTREPORTSTATE, event.mHarvestReportState);
            values.put(DiaryHelper.COLUMN_PERMITNUMBER, event.mPermitNumber);
            values.put(DiaryHelper.COLUMN_PERMITTYPE, event.mPermitType);
            values.put(DiaryHelper.COLUMN_HARVESTPERMITSTATE, event.mStateAcceptedToHarvestPermit);
            values.put(DiaryHelper.COLUMN_CANEDIT, event.mCanEdit);
            mDatabase.update(DiaryHelper.TABLE_DIARY, values, DiaryHelper.COLUMN_LOCALID + " = " + event.mLocalId, new String[]{});
        } catch (SQLException ignored) {
        }
    }

    public void markImageAsSent(LogImage image, LogImageTask.OperationType operationType) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            if (operationType == LogImageTask.OperationType.ADD) {
                ContentValues values = new ContentValues();
                values.put(DiaryHelper.COLUMN_IMAGETYPE, DiaryHelper.IMAGETYPE_ID);
                values.put(DiaryHelper.COLUMN_IMAGESTATUS, 0);
                mDatabase.update(DiaryHelper.TABLE_IMAGE, values, DiaryHelper.COLUMN_IMAGEID + " = ?", new String[]{image.uuid});
            } else if (operationType == LogImageTask.OperationType.DELETE) {
                mDatabase.delete(DiaryHelper.TABLE_IMAGE, DiaryHelper.COLUMN_IMAGEID + " = ?", new String[]{image.uuid});
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Prunes all events from certain year which do not match with the given ids
     */
    public List<DiaryEntryUpdate> pruneNonexistentRemoteEvents(int startYear, List<Integer> remoteIds) {
        List<DiaryEntryUpdate> updates = new ArrayList<DiaryEntryUpdate>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < remoteIds.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(remoteIds.get(i));
            }
            String idList = builder.toString();

            QueryConditionList whereConditions = new QueryConditionList();
            whereConditions.add("((year = " + startYear + " AND month > 7) OR (year = " + (startYear + 1) + " AND month < 8))", null);
            whereConditions.add(DiaryHelper.COLUMN_REMOTE + " = 1", null);
            whereConditions.add(DiaryHelper.COLUMN_ID + " NOT IN (" + idList + ")", null);
            addCommonWhereConditions(whereConditions);

            String query = diarySelectPart(true) + diaryJoinPart()
                    + " WHERE " + whereConditions.getWhereCondition();
            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DiaryEntryUpdate update = new DiaryEntryUpdate();
                update.event = createEvent(cursor, new ArrayList<LogImage>());
                update.type = UpdateType.DELETE;
                updates.add(update);
                removeLocalEvent(cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_LOCALID)));
                cursor.moveToNext();
            }
        } catch (SQLException ignored) {
        }
        return updates;
    }

    /**
     * Deletes local event. Used when pruning nonexistent remote events
     *
     * @param localid Local id
     */
    void removeLocalEvent(int localid) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.beginTransaction();
            try {
                mDatabase.delete(DiaryHelper.TABLE_DIARY, DiaryHelper.COLUMN_LOCALID + " = " + localid, new String[]{});
                mDatabase.delete(DiaryHelper.TABLE_GEOLOCATION, DiaryHelper.COLUMN_DIARYID + " = " + localid, new String[]{});
                mDatabase.delete(DiaryHelper.TABLE_IMAGE, DiaryHelper.COLUMN_DIARYID + " = " + localid, new String[]{});
                mDatabase.setTransactionSuccessful();
            } finally {
                mDatabase.endTransaction();
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Iterates sqlite cursor and builds complete GameHarvest objects
     *
     * @param cursor           sqlite cursor
     * @param getDeletedImages
     * @return List of GameHarvest objects
     */
    private List<GameHarvest> aggregateEvents(Cursor cursor, boolean getDeletedImages) {
        cursor.moveToFirst();
        List<LogImage> logImages = new ArrayList<LogImage>();
        boolean first = true;
        int lastId = 0;
        List<GameHarvest> logEvents = new ArrayList<GameHarvest>();
        while (!cursor.isAfterLast()) {
            int eventId = cursor.getInt(cursor.getColumnIndex(DiaryHelper.COLUMN_LOCALID));
            if (!first && eventId != lastId) {
                cursor.moveToPrevious();
                logEvents.add(createEvent(cursor, logImages));
                logImages = new ArrayList<LogImage>();
                cursor.moveToNext();
            }
            LogImage image = logImageFromCursor(cursor, getDeletedImages);
            if (image != null)
                logImages.add(image);
            lastId = eventId;
            cursor.moveToNext();
            first = false;
        }
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            logEvents.add(createEvent(cursor, logImages));
        }
        return logEvents;
    }

    /**
     * Builds common query select string
     *
     * @param dateParts include date parts (month, year)
     * @return
     */
    private String diarySelectPart(boolean dateParts) {
        String select = "SELECT *";
        if (dateParts) {
            select += ", cast(strftime('%m', " + DiaryHelper.COLUMN_POINTOFTIME + ") as integer) as month, "
                    + "cast(strftime('%Y', " + DiaryHelper.COLUMN_POINTOFTIME + ") as integer) as year";
        }
        select += " FROM " + DiaryHelper.TABLE_DIARY;
        return select;
    }

    /**
     * Returns common query join string
     */
    private String diaryJoinPart() {
        String join = " JOIN " + DiaryHelper.TABLE_GEOLOCATION
                + " ON " + DiaryHelper.TABLE_GEOLOCATION + "." + DiaryHelper.COLUMN_DIARYID + " = " + DiaryHelper.TABLE_DIARY + "." + DiaryHelper.COLUMN_LOCALID
                + " LEFT JOIN " + DiaryHelper.TABLE_IMAGE
                + " ON " + DiaryHelper.TABLE_IMAGE + "." + DiaryHelper.COLUMN_DIARYID + " = " + DiaryHelper.TABLE_DIARY + "." + DiaryHelper.COLUMN_LOCALID;
        return join;
    }

    private void addCommonWhereConditions(QueryConditionList list) {
        list.add(DiaryHelper.COLUMN_USERNAME + " = ?", mUsername);
    }

    public List<Integer> getEventStartYears() {
        List<Integer> years = new ArrayList<Integer>();
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            QueryConditionList whereConditions = new QueryConditionList();
            addCommonWhereConditions(whereConditions);

            String query = "SELECT "
                    + DiaryHelper.COLUMN_POINTOFTIME + ", "
                    + "cast(strftime('%Y', " + DiaryHelper.COLUMN_POINTOFTIME + ") as integer) as year, "
                    + "cast(strftime('%m', " + DiaryHelper.COLUMN_POINTOFTIME + ") as integer) as month"
                    + " FROM " + DiaryHelper.TABLE_DIARY
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " GROUP BY year, month";

            Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            while (!cursor.isAfterLast()) {
                calendar.set(Calendar.YEAR, cursor.getInt(cursor.getColumnIndex("year")));
                calendar.set(Calendar.MONTH, cursor.getInt(cursor.getColumnIndex("month")) - 1); // Convert to 0-based
                int startYear = DateTimeUtils.getSeasonStartYearFromDate(calendar);
                if (!years.contains(startYear)) {
                    years.add(startYear);
                }
                cursor.moveToNext();
            }
        } catch (SQLException e) {

        }
        return years;
    }

    public List<LogImage> getAllLogImages() {
        mDatabase = mDbHelper.getWritableDatabase();
        QueryConditionList whereConditions = new QueryConditionList();
        addCommonWhereConditions(whereConditions);
        String query = "SELECT * FROM " + DiaryHelper.TABLE_IMAGE;
        query += " JOIN " + DiaryHelper.TABLE_DIARY + " ON " + DiaryHelper.TABLE_IMAGE + "." + DiaryHelper.COLUMN_DIARYID + " = " + DiaryHelper.TABLE_DIARY + "." + DiaryHelper.COLUMN_LOCALID;
        query += " WHERE " + whereConditions.getWhereCondition();
        query += " ORDER BY datetime(" + DiaryHelper.COLUMN_POINTOFTIME + ") DESC";

        Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray());
        cursor.moveToFirst();
        List<LogImage> logImages = new ArrayList<LogImage>();
        while (!cursor.isAfterLast()) {
            LogImage image = logImageFromCursor(cursor, false);
            if (image != null)
                logImages.add(image);
            cursor.moveToNext();
        }
        cursor.close();
        return logImages;
    }

    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        mDatabase.endTransaction();
    }

    public void setUser(String username) {
        mUsername = username;
    }

    public enum EventTypeFilter {
        CATCHES,
        OBSERVATIONS,
        CATCHES_AND_OBSERVATIONS
    }

    /**
     * Temporary helper class to easy build of where conditions
     */
    private class QueryConditionList {
        List<String> mConditions = new ArrayList<String>();
        List<String> mParams = new ArrayList<String>();

        void add(String condition, String param) {
            mConditions.add(condition);
            if (param != null)
                mParams.add(param);
        }

        void addWithList(String condition, List<String> params) {
            mConditions.add(condition);
            if (params != null)
                mParams.addAll(params);
        }

        String getWhereCondition() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mConditions.size(); i++) {
                if (i > 0) {
                    builder.append(" AND ");
                }
                builder.append(mConditions.get(i));
            }
            return builder.toString();
        }

        public int getCount() {
            return mConditions.size();
        }

        String[] getValueArray() {
            return mParams.toArray(new String[mParams.size()]);
        }
    }
}
