package fi.riista.mobile.database;

import static java.util.Objects.requireNonNull;

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
import java.util.List;

import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.MapUtils;

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
        final Calendar pointOfTime = DateTimeUtils.parseCalendar(pointOfTimeStr);

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

    List<GameHarvest> getNotCopiedHarvests() {
        try {
            mDatabase = mDbHelper.getWritableDatabase();

            final QueryConditionList whereConditions = new QueryConditionList()
                    .add(HarvestDbHelper.COLUMN_TYPE + " = ?", GameLog.TYPE_HARVEST)
                    .add(HarvestDbHelper.COLUMN_COMMON_LOCAL_ID + " IS NULL", null)
                    .withActiveUserCondition();

            String query = selectFromHarvest(false)
                    + joinLocationAndImages()
                    + " WHERE " + whereConditions.getWhereCondition();

            try (final Cursor cursor = mDatabase.rawQuery(query, whereConditions.getValueArray())) {
                return collectHarvests(cursor, false);
            }
        } catch (final SQLException ignored) {
        }

        return new ArrayList<>(0);
    }

    void setCommonLocalId(final int localId, final long commonLocalId) {
        try {
            mDatabase = mDbHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            final ContentValues values = new ContentValues();
            values.put("commonLocalId", commonLocalId);
            final String[] whereArgs = {
                "" + localId,
                mUsername
            };
            mDatabase.update(
                    HarvestDbHelper.TABLE_DIARY,
                    values,
                    HarvestDbHelper.COLUMN_LOCALID + " = ? AND username = ?",
                    whereArgs
            );
            mDatabase.setTransactionSuccessful();
        } catch (final SQLException ignored) {
        } finally {
            mDatabase.endTransaction();
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

        String[] getValueArray() {
            return mParams.toArray(new String[mParams.size()]);
        }
    }
}
