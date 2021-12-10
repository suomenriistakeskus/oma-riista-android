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

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.riista.mobile.event.HarvestChangeEvent;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GameLogImage.ImageType;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.MapUtils;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * SQLite data source for harvests.
 * Completes harvest-related database operations on SQLite level.
 */
public class HarvestDataSource {

    private static final String TAG = HarvestDataSource.class.getSimpleName();

    private static SQLiteDatabase mDatabase;

    private final HarvestDbHelper mDbHelper;
    private String mUsername = "";

    HarvestDataSource(@NonNull final Context context) {
        requireNonNull(context);
        mDbHelper = new HarvestDbHelper(context);
    }

    void setUser(final String username) {
        mUsername = username;
    }

    private static GameLogImage getImageFromCursor(final Cursor cursor, final boolean includeDeletedImages) {
        final int imageTypeCol = cursor.getColumnIndex(HarvestDbHelper.COLUMN_IMAGETYPE);
        GameLogImage image = null;

        if (imageTypeCol != -1) {
            final int imageStatus = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_IMAGESTATUS));

            if (imageStatus != HarvestDbHelper.IMAGESTATUS_DELETED || includeDeletedImages) {
                final int imageType = cursor.getInt(imageTypeCol);

                if (imageType == HarvestDbHelper.IMAGETYPE_ID) {
                    image = new GameLogImage(cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_IMAGEID)));
                } else if (imageType == HarvestDbHelper.IMAGETYPE_URI) {
                    image = new GameLogImage(Uri.parse(cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_IMAGEURI))));
                }

                if (image != null) {
                    image.uuid = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_IMAGEID));
                    image.imageStatus = imageStatus;
                }
            }
        }

        return image;
    }

    private static ContentValues harvestToContentValues(final GameHarvest harvest) {
        final ContentValues values = new ContentValues();

        values.put(HarvestDbHelper.COLUMN_TYPE, GameLog.TYPE_HARVEST);
        values.put(HarvestDbHelper.COLUMN_SPEC_VERSION, harvest.mHarvestSpecVersion);

        values.put(HarvestDbHelper.COLUMN_ID, harvest.mId);

        values.put(HarvestDbHelper.COLUMN_SENT, harvest.mSent);
        values.put(HarvestDbHelper.COLUMN_REMOTE, harvest.mRemote);
        values.put(HarvestDbHelper.COLUMN_REV, harvest.mRev);
        values.put(HarvestDbHelper.COLUMN_PENDINGOPERATION, harvest.mPendingOperation.value());
        values.put(HarvestDbHelper.COLUMN_CANEDIT, harvest.mCanEdit);

        values.put(HarvestDbHelper.COLUMN_GAMESPECIESID, harvest.mSpeciesID);
        values.put(HarvestDbHelper.COLUMN_POINTOFTIME, DateTimeUtils.formatDate(harvest.mTime.getTime()));
        values.put(HarvestDbHelper.COLUMN_AMOUNT, harvest.mAmount);
        values.put(HarvestDbHelper.COLUMN_DESCRIPTION, harvest.mDescription);

        values.put(HarvestDbHelper.COLUMN_HARVESTREPORTDONE, harvest.mHarvestReportDone);
        values.put(HarvestDbHelper.COLUMN_HARVESTREPORTREQUIRED, harvest.mHarvestReportRequired);
        values.put(HarvestDbHelper.COLUMN_HARVESTREPORTSTATE, harvest.mHarvestReportState);
        values.put(HarvestDbHelper.COLUMN_PERMITNUMBER, harvest.mPermitNumber);
        values.put(HarvestDbHelper.COLUMN_PERMITTYPE, harvest.mPermitType);
        values.put(HarvestDbHelper.COLUMN_HARVESTPERMITSTATE, harvest.mStateAcceptedToHarvestPermit);

        values.put(HarvestDbHelper.COLUMN_DEER_HUNTING_TYPE, DeerHuntingType.toString(harvest.mDeerHuntingType));
        values.put(HarvestDbHelper.COLUMN_DEER_HUNTING_OTHER_TYPE_DESCRIPTION, harvest.mDeerHuntingOtherTypeDescription);
        values.put(HarvestDbHelper.COLUMN_FEEDING_PLACE, harvest.mFeedingPlace);
        values.put(HarvestDbHelper.COLUMN_HUNTING_METHOD, GreySealHuntingMethod.toString(harvest.mHuntingMethod));
        values.put(HarvestDbHelper.COLUMN_TAIGA_BEAN_GOOSE, harvest.mTaigaBeanGoose);

        if (harvest.mSpecimen != null) {
            try {
                values.put(HarvestDbHelper.COLUMN_SPECIMENDATA, JsonUtils.objectToJson(harvest.mSpecimen).getBytes());
            } catch (final Exception e) {
                Log.w(TAG, "Failed to serialize specimen data!");
            }
        } else {
            // Must not send null. Response will contain empty list which will be stored to database.
            values.put(HarvestDbHelper.COLUMN_SPECIMENDATA, JsonUtils.objectToJson(new ArrayList<>()).getBytes());
        }

        return values;
    }

    private static ContentValues harvestGeovaluesToContentValues(final GameHarvest harvest) {
        final ContentValues geoValues = new ContentValues();
        geoValues.put(HarvestDbHelper.COLUMN_DIARYID, harvest.mLocalId);
        geoValues.put(HarvestDbHelper.COLUMN_LATITUDE, harvest.mCoordinates.first);
        geoValues.put(HarvestDbHelper.COLUMN_LONGITUDE, harvest.mCoordinates.second);
        geoValues.put(HarvestDbHelper.COLUMN_ACCURACY, harvest.mAccuracy);
        geoValues.put(HarvestDbHelper.COLUMN_HASALTITUDE, harvest.mHasAltitude);
        geoValues.put(HarvestDbHelper.COLUMN_ALTITUDE, harvest.mAltitude);
        geoValues.put(HarvestDbHelper.COLUMN_ALTITUDEACCURACY, harvest.mAltitudeAccuracy);
        geoValues.put(HarvestDbHelper.COLUMN_LOCATION_SOURCE, harvest.mLocationSource);
        return geoValues;
    }

    /**
     * Get/read harvest from SQLite cursor.
     *
     * @param cursor SQlite cursor
     * @param images Images to be added to harvest
     * @return GameHarvest object
     */
    private static GameHarvest getHarvest(final Cursor cursor, final List<GameLogImage> images) {
        final Pair<Double, Double> loc = MapUtils.ETRMStoWGS84(
                cursor.getLong(cursor.getColumnIndex(HarvestDbHelper.COLUMN_LATITUDE)),
                cursor.getLong(cursor.getColumnIndex(HarvestDbHelper.COLUMN_LONGITUDE)));

        final Location location = new Location("");
        location.setLatitude(loc.first);
        location.setLongitude(loc.second);
        location.setAccuracy(cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_ACCURACY)));
        location.setAltitude(cursor.getFloat(cursor.getColumnIndex(HarvestDbHelper.COLUMN_ALTITUDE)));

        final String pointOfTimeStr = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_POINTOFTIME));
        final Calendar pointOfTime = DateTimeUtils.parseCalendar(pointOfTimeStr, true);

        final GameHarvest harvest = new GameHarvest(
                cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_SPEC_VERSION)),
                cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_GAMESPECIESID)),
                location,
                pointOfTime,
                cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_AMOUNT)),
                images);

        harvest.mId = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_ID));
        harvest.mLocalId = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_LOCALID));
        harvest.mMobileClientRefId = cursor.getLong(cursor.getColumnIndex(HarvestDbHelper.COLUMN_MOBILEREFID));

        harvest.mSent = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_SENT)) == 1;
        harvest.mRemote = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_REMOTE)) == 1;
        harvest.mRev = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_REV));
        harvest.mPendingOperation = HarvestDbHelper.UpdateType.valueOf(cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_PENDINGOPERATION)));
        harvest.mCanEdit = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_CANEDIT)) == 1;

        final int latitudeIndex = cursor.getColumnIndex(HarvestDbHelper.COLUMN_LATITUDE);
        final int longitudeIndex = cursor.getColumnIndex(HarvestDbHelper.COLUMN_LONGITUDE);

        if (latitudeIndex != -1 && longitudeIndex != -1) {
            harvest.mCoordinates = new Pair<>(cursor.getLong(latitudeIndex), cursor.getLong(longitudeIndex));
        }

        harvest.mAccuracy = cursor.getFloat(cursor.getColumnIndex(HarvestDbHelper.COLUMN_ACCURACY));
        harvest.mHasAltitude = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_HASALTITUDE)) == 1;
        harvest.mAltitude = cursor.getFloat(cursor.getColumnIndex(HarvestDbHelper.COLUMN_ALTITUDE));
        harvest.mAltitudeAccuracy = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_ALTITUDEACCURACY));
        harvest.mLocationSource = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_LOCATION_SOURCE));

        harvest.mDescription = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_DESCRIPTION));

        harvest.mHarvestReportDone = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_HARVESTREPORTDONE)) == 1;
        harvest.mHarvestReportRequired = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_HARVESTREPORTREQUIRED)) == 1;
        harvest.mHarvestReportState = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_HARVESTREPORTSTATE));

        harvest.mPermitNumber = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_PERMITNUMBER));
        harvest.mPermitType = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_PERMITTYPE));
        harvest.mStateAcceptedToHarvestPermit = cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_HARVESTPERMITSTATE));

        harvest.mDeerHuntingType = DeerHuntingType.fromString(
                cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_DEER_HUNTING_TYPE)));
        harvest.mDeerHuntingOtherTypeDescription =
                cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_DEER_HUNTING_OTHER_TYPE_DESCRIPTION));
        harvest.mFeedingPlace = cursor.isNull(cursor.getColumnIndex(HarvestDbHelper.COLUMN_FEEDING_PLACE)) ? null : cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_FEEDING_PLACE)) == 1;
        harvest.mHuntingMethod = GreySealHuntingMethod.fromString(
                cursor.getString(cursor.getColumnIndex(HarvestDbHelper.COLUMN_HUNTING_METHOD)));
        harvest.mTaigaBeanGoose = cursor.isNull(cursor.getColumnIndex(HarvestDbHelper.COLUMN_TAIGA_BEAN_GOOSE)) ? null : cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_TAIGA_BEAN_GOOSE)) == 1;

        final int specimenDataIndex = cursor.getColumnIndex(HarvestDbHelper.COLUMN_SPECIMENDATA);

        if (specimenDataIndex != -1) {
            try {
                // Column value may be null when migrating from old db version resulting in exception.
                final String data = new String(cursor.getBlob(specimenDataIndex));
                harvest.mSpecimen = JsonUtils.jsonToList(data, HarvestSpecimen.class);

            } catch (final Exception e) {
                Log.d(TAG, "Failed to deserialize specimen data: " + e.getMessage());
            }
        }

        if (harvest.mSpecimen == null) {
            harvest.mSpecimen = new ArrayList<>(0);
        }

        return harvest;
    }

    public synchronized void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        if (mDatabase != null && mDatabase.isOpen() && mDatabase.inTransaction()) {
            mDatabase.endTransaction();
        }

        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    GameHarvest getHarvestByLocalId(final int localId) {
        return getHarvestByLocalId(localId, false);
    }

    private GameHarvest getHarvestByLocalId(final int localId, final boolean includeDeletedImages) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add(HarvestDbHelper.COLUMN_LOCALID + " = ?", Integer.toString(localId))
                    .withActiveUserCondition();

            final String query = selectFromHarvest(false)
                    + joinLocationAndImages()
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " ORDER BY " + HarvestDbHelper.COLUMN_POINTOFTIME;

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {

                final List<GameHarvest> harvests = collectHarvests(cursor, includeDeletedImages);

                if (harvests.size() == 1) {
                    return harvests.get(0);
                }
            }

        } catch (final SQLException ignored) {
        }

        return null;
    }

    List<GameHarvest> getHarvestsByHuntingYear(final int year) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add("((year = " + year + " AND month > 7) OR (year = " + (year + 1) + " AND month < 8))", null)
                    .add(HarvestDbHelper.COLUMN_TYPE + " = ?", GameLog.TYPE_HARVEST)
                    .withActiveUserCondition();

            final String query = selectFromHarvest(true)
                    + joinLocationAndImages()
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " ORDER BY " + HarvestDbHelper.COLUMN_POINTOFTIME;

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {
                return collectHarvests(cursor, false);
            }

        } catch (final SQLException ignored) {
        }

        return new ArrayList<>(0);
    }

    List<GameHarvest> getAllHarvests() {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList activeUserCondition = new QueryConditionList().withActiveUserCondition();

            String query = selectFromHarvest(false) + joinLocationAndImages();
            if (activeUserCondition.getCount() > 0) {
                query += " WHERE " + activeUserCondition.getWhereCondition();
            }
            query += " ORDER BY datetime(" + HarvestDbHelper.COLUMN_POINTOFTIME + ") ASC";

            try (final Cursor cursor = mDatabase.rawQuery(query, activeUserCondition.getValueArray())) {
                return collectHarvests(cursor, false);
            }
        } catch (final SQLException ignored) {
        }

        return new ArrayList<>(0);
    }

    List<GameHarvest> getMostRecentHarvests(final int count) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add(HarvestDbHelper.COLUMN_TYPE + " = ?", GameLog.TYPE_HARVEST)
                    .withActiveUserCondition();

            // Use MAX with ORDER BY, otherwise sorting is done by one of the grouped dates, and order is wrong sometimes
            String query = selectFromHarvest(false)
                    + joinLocationAndImages()
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " GROUP BY " + HarvestDbHelper.COLUMN_GAMESPECIESID
                    + " ORDER BY MAX(datetime(" + HarvestDbHelper.COLUMN_POINTOFTIME + ")) DESC";

            if (count != 0) {
                query += " LIMIT " + count;
            }

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {
                return collectHarvests(cursor, false);
            }
        } catch (final SQLException ignored) {
        }

        return new ArrayList<>(0);
    }

    // Returns list of hunting years which contain harvests that are not sent to the server yet.
    List<Integer> getHuntingYearsWithUnsentHarvests() {
        final QueryConditionList whereConditions = new QueryConditionList()
                .add(HarvestDbHelper.COLUMN_SENT + " = 0", null)
                .withActiveUserCondition();

        final List<Integer> huntingYears = new ArrayList<>();

        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final String query = "SELECT "
                    + "strftime('%Y', " + HarvestDbHelper.COLUMN_POINTOFTIME + ") as year, "
                    + "strftime('%m', " + HarvestDbHelper.COLUMN_POINTOFTIME + ") as month"
                    + " FROM " + HarvestDbHelper.TABLE_DIARY
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " GROUP BY year";

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {
                cursor.moveToFirst();

                final Calendar calendar = Calendar.getInstance();

                while (!cursor.isAfterLast()) {
                    calendar.set(Calendar.YEAR, cursor.getInt(cursor.getColumnIndex("year")));
                    calendar.set(Calendar.MONTH, cursor.getInt(cursor.getColumnIndex("month")) - 1); // Convert to 0-based

                    huntingYears.add(DateTimeUtils.getHuntingYearForCalendar(calendar));

                    cursor.moveToNext();
                }
            }
        } catch (final SQLException ignored) {
        }

        return huntingYears;
    }

    // Returns list of harvests that are not yet sent.
    List<GameHarvest> getUnsentHarvests() {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add(HarvestDbHelper.COLUMN_SENT + " = 0", null)
                    .withActiveUserCondition();

            final String query = selectFromHarvest(false)
                    + joinLocationAndImages()
                    + " WHERE " + whereConditions.getWhereCondition()
                    + " ORDER BY " + HarvestDbHelper.COLUMN_POINTOFTIME + " ASC";

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {
                return collectHarvests(cursor, true);
            }
        } catch (final SQLException ignored) {
        }

        return new ArrayList<>(0);
    }

    // Takes harvest - inserts if it doesn't exist, otherwise updates it
    boolean insertOrUpdateReceivedHarvest(final GameHarvest harvest) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add(HarvestDbHelper.COLUMN_ID + " = " + harvest.mId, null)
                    .withActiveUserCondition();

            final String query = "SELECT * FROM " + HarvestDbHelper.TABLE_DIARY + " WHERE " + whereConditions.getWhereCondition();

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {

                if (cursor.getCount() == 0) {
                    addHarvest(harvest, true);
                    return true;
                } else {
                    cursor.moveToFirst();

                    final boolean isRemote = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_REMOTE)) == 1;
                    final boolean isSent = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_SENT)) == 1;
                    final int currentRev = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_REV));
                    final int storedSpecVersion = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_SPEC_VERSION));

                    // Harvest can be replaced if server rev is newer or if it's same and the harvest has not been modified
                    // TODO: Is this really working as expected?
                    if (!isRemote
                            || harvest.mRev > currentRev
                            || (isSent && (harvest.mRev == currentRev || harvest.mHarvestSpecVersion != storedSpecVersion))) {

                        cursor.moveToFirst();
                        harvest.mLocalId = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_LOCALID));
                        updateHarvest(harvest, true);
                        return true;
                    }
                }
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Adds harvest into database.
     *
     * @param harvest            Harvest object to be added to database
     * @param receivedFromRemote
     * @return object containing information about added images
     */
    HarvestImageUpdate addHarvest(final GameHarvest harvest, final boolean receivedFromRemote) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            final ContentValues values = harvestToContentValues(harvest);
            values.put(HarvestDbHelper.COLUMN_USERNAME, mUsername);
            values.put(HarvestDbHelper.COLUMN_MOBILEREFID, harvest.mMobileClientRefId);

            final long rowId = mDatabase.insert(HarvestDbHelper.TABLE_DIARY, null, values);
            final int localId = Long.valueOf(rowId).intValue();
            harvest.mLocalId = localId;

            final ContentValues geoValues = harvestGeovaluesToContentValues(harvest);
            mDatabase.insert(HarvestDbHelper.TABLE_GEOLOCATION, null, geoValues);

            final HarvestImageUpdate update = updateImages(localId, new ArrayList<>(), harvest.mImages, receivedFromRemote);

            mDatabase.setTransactionSuccessful();
            return update;
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * Updates images in using current list of images and new list of images.
     *
     * @param harvestLocalId                           Local ID of harvest
     * @param existingImages                           Currently existing images in database
     * @param imagesOnUpdate                           Images present within update operation
     * @param removeDeletedImagesInsteadOfStatusUpdate If true deleted images are removed from database;
     *                                                 otherwise image status is updated for newly added and deleted
     *                                                 images.
     * @return HarvestImageUpdate object containing information about added and deleted images
     */
    HarvestImageUpdate updateImages(final int harvestLocalId,
                                    final List<GameLogImage> existingImages,
                                    final List<GameLogImage> imagesOnUpdate,
                                    final boolean removeDeletedImagesInsteadOfStatusUpdate) {

        final List<GameLogImage> newImages = new ArrayList<>(imagesOnUpdate);
        final List<GameLogImage> imagesToBeRemoved = new ArrayList<>(existingImages.size());

        try {
            mDatabase = mDbHelper.getWritableDatabase();

            for (final GameLogImage existingImage : existingImages) {
                int foundNewImageIndex = -1;

                for (int newImageIndex = 0; newImageIndex < newImages.size(); newImageIndex++) {
                    final GameLogImage newImage = newImages.get(newImageIndex);

                    // Local image is same if UUIDs and types match.

                    if (existingImage.uuid.equals(newImage.uuid) && existingImage.type == newImage.type) {
                        foundNewImageIndex = newImageIndex;
                        break;
                    }
                }

                if (foundNewImageIndex >= 0) {
                    newImages.remove(foundNewImageIndex);
                } else {
                    imagesToBeRemoved.add(existingImage);
                }
            }

            // Delete any images that got deleted.
            if (!imagesToBeRemoved.isEmpty()) {
                final List<String> deletedUUIDs = new ArrayList<>(imagesToBeRemoved.size());
                final StringBuilder questionMarks = new StringBuilder();

                for (final GameLogImage deletedImage : imagesToBeRemoved) {
                    if (questionMarks.length() > 0) {
                        questionMarks.append(", ");
                    }
                    questionMarks.append("?");

                    deletedUUIDs.add(deletedImage.uuid);
                }

                final String sqlInCondition =
                        HarvestDbHelper.COLUMN_IMAGEID + " IN (" + questionMarks.toString() + ")";

                final QueryConditionList whereConditions = new QueryConditionList()
                        .addWithList(sqlInCondition, deletedUUIDs)
                        .add(HarvestDbHelper.COLUMN_DIARYID + " = " + harvestLocalId, null);

                if (removeDeletedImagesInsteadOfStatusUpdate) {
                    mDatabase.delete(HarvestDbHelper.TABLE_IMAGE, whereConditions.getWhereCondition(), whereConditions.getValueArray());
                } else {
                    final ContentValues values = new ContentValues();
                    values.put(HarvestDbHelper.COLUMN_IMAGESTATUS, HarvestDbHelper.IMAGESTATUS_DELETED);
                    mDatabase.update(HarvestDbHelper.TABLE_IMAGE, values, whereConditions.getWhereCondition(), whereConditions.getValueArray());
                }
            }

            // Insert any images that were new.
            if (!newImages.isEmpty()) {
                for (final GameLogImage newImage : newImages) {
                    final ContentValues values = new ContentValues();
                    values.put(HarvestDbHelper.COLUMN_IMAGEID, newImage.uuid);
                    values.put(HarvestDbHelper.COLUMN_DIARYID, harvestLocalId);

                    if (newImage.type == ImageType.URI) {
                        values.put(HarvestDbHelper.COLUMN_IMAGETYPE, HarvestDbHelper.IMAGETYPE_URI);
                        values.put(HarvestDbHelper.COLUMN_IMAGEURI, newImage.uri.toString());

                        if (!removeDeletedImagesInsteadOfStatusUpdate) {
                            values.put(HarvestDbHelper.COLUMN_IMAGESTATUS, HarvestDbHelper.IMAGESTATUS_INSERTED);
                        }
                    } else {
                        values.put(HarvestDbHelper.COLUMN_IMAGETYPE, HarvestDbHelper.IMAGETYPE_ID);
                    }

                    mDatabase.insert(HarvestDbHelper.TABLE_IMAGE, null, values);
                }
            }
        } catch (final SQLException ignored) {
        }

        return new HarvestImageUpdate(newImages, imagesToBeRemoved);
    }

    /**
     * Returns image update data from harvest received from list received with call getUnsentHarvests
     *
     * @param harvest
     */
    HarvestImageUpdate getImageUpdateFromUnsentHarvest(final GameHarvest harvest) {
        final int numImages = harvest.mImages.size();
        final List<GameLogImage> addedImages = new ArrayList<>(numImages);
        final List<GameLogImage> deletedImages = new ArrayList<>(numImages);

        for (final GameLogImage image : harvest.mImages) {
            if (image.imageStatus == HarvestDbHelper.IMAGESTATUS_DELETED) {
                deletedImages.add(image);
            } else if (image.imageStatus == HarvestDbHelper.IMAGESTATUS_INSERTED) {
                addedImages.add(image);
            }
        }

        return new HarvestImageUpdate(addedImages, deletedImages);
    }

    /**
     * Update given harvest in database using given GameHarvest object.
     *
     * @param harvest
     * @param removeDeletedImagesInsteadOfStatusUpdate
     * @return Image update data
     */
    HarvestImageUpdate updateHarvest(final GameHarvest harvest,
                                     final boolean removeDeletedImagesInsteadOfStatusUpdate) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            final GameHarvest currentHarvest = getHarvestByLocalId(harvest.mLocalId, true);
            final ContentValues values = harvestToContentValues(harvest);

            mDatabase.update(HarvestDbHelper.TABLE_DIARY, values, HarvestDbHelper.COLUMN_LOCALID + " = " + harvest.mLocalId, new String[]{});

            final ContentValues geoValues = harvestGeovaluesToContentValues(harvest);

            mDatabase.update(HarvestDbHelper.TABLE_GEOLOCATION, geoValues, HarvestDbHelper.COLUMN_DIARYID + " = " + harvest.mLocalId, new String[]{});

            final HarvestImageUpdate imageUpdate = updateImages(
                    harvest.mLocalId, currentHarvest.mImages, harvest.mImages, removeDeletedImagesInsteadOfStatusUpdate);

            mDatabase.setTransactionSuccessful();
            return imageUpdate;
        } catch (final SQLException ignored) {
            return HarvestImageUpdate.empty();
        } finally {
            mDatabase.endTransaction();
        }
    }

    void markHarvestAsRemote(final GameHarvest harvest) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final ContentValues values = new ContentValues();
            values.put(HarvestDbHelper.COLUMN_ID, harvest.mId);
            values.put(HarvestDbHelper.COLUMN_REMOTE, 1);
            values.put(HarvestDbHelper.COLUMN_REV, harvest.mRev);

            mDatabase.update(HarvestDbHelper.TABLE_DIARY, values, HarvestDbHelper.COLUMN_LOCALID + " = " + harvest.mLocalId, new String[]{});

        } catch (final SQLException ignored) {
        }
    }

    /**
     * Mark harvest as sent after completing sync to backend.
     *
     * @param harvest Harvest object containing updated data received from backend
     */
    void markHarvestAsSent(final GameHarvest harvest) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final ContentValues values = new ContentValues();
            values.put(HarvestDbHelper.COLUMN_ID, harvest.mId);

            values.put(HarvestDbHelper.COLUMN_SENT, 1);
            values.put(HarvestDbHelper.COLUMN_REMOTE, 1);
            values.put(HarvestDbHelper.COLUMN_REV, harvest.mRev);
            values.put(HarvestDbHelper.COLUMN_CANEDIT, harvest.mCanEdit);

            values.put(HarvestDbHelper.COLUMN_HARVESTREPORTREQUIRED, harvest.mHarvestReportRequired);
            values.put(HarvestDbHelper.COLUMN_HARVESTREPORTDONE, harvest.mHarvestReportDone);
            values.put(HarvestDbHelper.COLUMN_HARVESTREPORTSTATE, harvest.mHarvestReportState);
            values.put(HarvestDbHelper.COLUMN_PERMITNUMBER, harvest.mPermitNumber);
            values.put(HarvestDbHelper.COLUMN_PERMITTYPE, harvest.mPermitType);
            values.put(HarvestDbHelper.COLUMN_HARVESTPERMITSTATE, harvest.mStateAcceptedToHarvestPermit);

            values.put(HarvestDbHelper.COLUMN_DEER_HUNTING_TYPE, DeerHuntingType.toString(harvest.mDeerHuntingType));
            values.put(HarvestDbHelper.COLUMN_DEER_HUNTING_OTHER_TYPE_DESCRIPTION, harvest.mDeerHuntingOtherTypeDescription);
            values.put(HarvestDbHelper.COLUMN_FEEDING_PLACE, harvest.mFeedingPlace);
            values.put(HarvestDbHelper.COLUMN_HUNTING_METHOD, GreySealHuntingMethod.toString(harvest.mHuntingMethod));
            values.put(HarvestDbHelper.COLUMN_TAIGA_BEAN_GOOSE, harvest.mTaigaBeanGoose);

            mDatabase.update(HarvestDbHelper.TABLE_DIARY,
                    values,
                    HarvestDbHelper.COLUMN_LOCALID + " = " + harvest.mLocalId,
                    new String[]{});

        } catch (final SQLException ignored) {
        }
    }

    void markImageAsSent(final GameLogImage image) {
        final ContentValues values = new ContentValues();
        values.put(HarvestDbHelper.COLUMN_IMAGETYPE, HarvestDbHelper.IMAGETYPE_ID);
        values.put(HarvestDbHelper.COLUMN_IMAGESTATUS, 0);

        final String[] imageIdParam = {image.uuid};

        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.update(HarvestDbHelper.TABLE_IMAGE, values, HarvestDbHelper.COLUMN_IMAGEID + " = ?", imageIdParam);
        } catch (final SQLException ignored) {
        }
    }

    void removeImage(final GameLogImage image) {
        final String[] imageIdParam = {image.uuid};

        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.delete(HarvestDbHelper.TABLE_IMAGE, HarvestDbHelper.COLUMN_IMAGEID + " = ?", imageIdParam);
        } catch (final SQLException ignored) {
        }
    }

    /**
     * Prunes all harvests from certain hunting year which do not match with the given ids.
     */
    List<HarvestChangeEvent> pruneNonexistentRemoteHarvests(final int huntingYear, final List<Integer> remoteIds) {
        final List<HarvestChangeEvent> changeEvents = new ArrayList<>();

        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final StringBuilder idBuilder = new StringBuilder();

            for (int i = 0; i < remoteIds.size(); i++) {
                if (i > 0) {
                    idBuilder.append(", ");
                }
                idBuilder.append(remoteIds.get(i));
            }
            final String idList = idBuilder.toString();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add("((year = " + huntingYear + " AND month > 7) OR (year = " + (huntingYear + 1) + " AND month < 8))", null)
                    .add(HarvestDbHelper.COLUMN_REMOTE + " = 1", null)
                    .add(HarvestDbHelper.COLUMN_ID + " NOT IN (" + idList + ")", null)
                    .withActiveUserCondition();

            final String query = selectFromHarvest(true)
                    + joinLocationAndImages()
                    + " WHERE " + whereConditions.getWhereCondition();

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    final GameHarvest harvest = getHarvest(cursor, emptyList());
                    changeEvents.add(HarvestChangeEvent.deleted(harvest));
                    removeHarvestByLocalId(harvest.mLocalId);
                    cursor.moveToNext();
                }
            }
        } catch (final SQLException ignored) {
        }

        return changeEvents;
    }

    /**
     * Deletes harvest by local ID. Used when pruning nonexistent remote harvests.
     *
     * @param localId Local ID of harvest
     */
    void removeHarvestByLocalId(final int localId) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            try {
                mDatabase.delete(HarvestDbHelper.TABLE_DIARY, HarvestDbHelper.COLUMN_LOCALID + " = " + localId, new String[]{});
                mDatabase.delete(HarvestDbHelper.TABLE_GEOLOCATION, HarvestDbHelper.COLUMN_DIARYID + " = " + localId, new String[]{});
                mDatabase.delete(HarvestDbHelper.TABLE_IMAGE, HarvestDbHelper.COLUMN_DIARYID + " = " + localId, new String[]{});
                mDatabase.setTransactionSuccessful();
            } finally {
                mDatabase.endTransaction();
            }
        } catch (final SQLException ignored) {
        }
    }

    /**
     * Iterates over SQLite cursor and collects GameHarvest objects into a List.
     *
     * @param cursor           SQLite cursor
     * @param includeDeletedImages
     * @return List of GameHarvest objects
     */
    private List<GameHarvest> collectHarvests(final Cursor cursor, final boolean includeDeletedImages) {
        cursor.moveToFirst();

        final List<GameHarvest> harvests = new ArrayList<>();
        List<GameLogImage> images = new ArrayList<>();
        boolean first = true;
        int lastId = 0;

        while (!cursor.isAfterLast()) {
            final int localId = cursor.getInt(cursor.getColumnIndex(HarvestDbHelper.COLUMN_LOCALID));

            if (!first && localId != lastId) {
                cursor.moveToPrevious();
                harvests.add(getHarvest(cursor, images));
                images = new ArrayList<>();
                cursor.moveToNext();
            }

            final GameLogImage image = getImageFromCursor(cursor, includeDeletedImages);

            if (image != null) {
                images.add(image);
            }

            lastId = localId;
            cursor.moveToNext();
            first = false;
        }

        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            harvests.add(getHarvest(cursor, images));
        }

        return harvests;
    }

    /**
     * Builds common query select string
     *
     * @param dateParts include date parts (month, year)
     */
    private String selectFromHarvest(final boolean dateParts) {
        String select = "SELECT *";

        if (dateParts) {
            select += ", cast(strftime('%m', " + HarvestDbHelper.COLUMN_POINTOFTIME + ") as integer) as month, "
                    + "cast(strftime('%Y', " + HarvestDbHelper.COLUMN_POINTOFTIME + ") as integer) as year";
        }

        select += " FROM " + HarvestDbHelper.TABLE_DIARY;

        return select;
    }

    /**
     * Returns common query join string
     */
    private String joinLocationAndImages() {
        return " JOIN " + HarvestDbHelper.TABLE_GEOLOCATION
                + " ON " + HarvestDbHelper.TABLE_GEOLOCATION + "." + HarvestDbHelper.COLUMN_DIARYID + " = " + HarvestDbHelper.TABLE_DIARY + "." + HarvestDbHelper.COLUMN_LOCALID
                + " LEFT JOIN " + HarvestDbHelper.TABLE_IMAGE
                + " ON " + HarvestDbHelper.TABLE_IMAGE + "." + HarvestDbHelper.COLUMN_DIARYID + " = " + HarvestDbHelper.TABLE_DIARY + "." + HarvestDbHelper.COLUMN_LOCALID;
    }

    List<Integer> getHuntingYearsOfHarvests() {
        final Set<Integer> huntingYears = new HashSet<>();

        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList activeUserCondition = new QueryConditionList().withActiveUserCondition();

            final String query = "SELECT "
                    + HarvestDbHelper.COLUMN_POINTOFTIME + ", "
                    + "cast(strftime('%Y', " + HarvestDbHelper.COLUMN_POINTOFTIME + ") as integer) as year, "
                    + "cast(strftime('%m', " + HarvestDbHelper.COLUMN_POINTOFTIME + ") as integer) as month"
                    + " FROM " + HarvestDbHelper.TABLE_DIARY
                    + " WHERE " + activeUserCondition.getWhereCondition()
                    + " GROUP BY year, month";

            try (final Cursor cursor = mDatabase.rawQuery(query, activeUserCondition.getValueArray())) {
                cursor.moveToFirst();

                final Calendar calendar = Calendar.getInstance();

                while (!cursor.isAfterLast()) {
                    calendar.set(Calendar.YEAR, cursor.getInt(cursor.getColumnIndex("year")));
                    calendar.set(Calendar.MONTH, cursor.getInt(cursor.getColumnIndex("month")) - 1); // Convert to 0-based

                    huntingYears.add(DateTimeUtils.getHuntingYearForCalendar(calendar));

                    cursor.moveToNext();
                }
            }
        } catch (final SQLException ignored) {
        }

        final ArrayList result = new ArrayList<>(huntingYears);
        Collections.sort(result);
        return result;
    }

    List<GameLogImage> getAllHarvestImages() {
        mDatabase = mDbHelper.getWritableDatabase();

        final QueryConditionList activeUserCondition = new QueryConditionList().withActiveUserCondition();

        final String query = "SELECT * FROM " + HarvestDbHelper.TABLE_IMAGE
                + " JOIN " + HarvestDbHelper.TABLE_DIARY + " ON " + HarvestDbHelper.TABLE_IMAGE + "." + HarvestDbHelper.COLUMN_DIARYID + " = " + HarvestDbHelper.TABLE_DIARY + "." + HarvestDbHelper.COLUMN_LOCALID
                + " WHERE " + activeUserCondition.getWhereCondition()
                + " ORDER BY datetime(" + HarvestDbHelper.COLUMN_POINTOFTIME + ") DESC";

        try (final Cursor cursor = mDatabase.rawQuery(query, activeUserCondition.getValueArray())) {
            cursor.moveToFirst();

            final List<GameLogImage> images = new ArrayList<>();

            while (!cursor.isAfterLast()) {
                final GameLogImage image = getImageFromCursor(cursor, false);

                if (image != null) {
                    images.add(image);
                }

                cursor.moveToNext();
            }

            return images;
        }
    }

    void beginTransaction() {
        mDatabase.beginTransaction();
    }

    void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    void endTransaction() {
        mDatabase.endTransaction();
    }

    /**
     * Temporary helper class to easy build of where conditions
     */
    private class QueryConditionList {

        final List<String> mConditions = new ArrayList<>();
        final List<String> mParams = new ArrayList<>();

        QueryConditionList add(final String condition, final String param) {
            mConditions.add(condition);

            if (param != null) {
                mParams.add(param);
            }

            return this;
        }

        QueryConditionList withActiveUserCondition() {
            return add(HarvestDbHelper.COLUMN_USERNAME + " = ?", mUsername);
        }

        QueryConditionList addWithList(final String condition, final List<String> params) {
            mConditions.add(condition);

            if (params != null) {
                mParams.addAll(params);
            }

            return this;
        }

        String getWhereCondition() {
            final StringBuilder builder = new StringBuilder();

            for (int i = 0; i < mConditions.size(); i++) {
                if (i > 0) {
                    builder.append(" AND ");
                }
                builder.append(mConditions.get(i));
            }

            return builder.toString();
        }

        int getCount() {
            return mConditions.size();
        }

        String[] getValueArray() {
            return mParams.toArray(new String[mParams.size()]);
        }
    }
}
