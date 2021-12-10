package fi.riista.mobile.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fi.riista.mobile.event.HarvestChangeEvent;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.ImageUtils;
import fi.riista.mobile.utils.JsonUtils;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_CONTEXT_NAME;

/**
 * Class for handling harvest related database operations.
 * Uses HarvestDataSource class to modify local database.
 * The class handles automatic synchronizing when using automatic synchronization mode.
 */
@Singleton
public class HarvestDatabase {

    public static class SeasonStats {
        public SparseIntArray mCategoryData;

        public SeasonStats() {
            mCategoryData = new SparseIntArray();
        }
    }

    private static final String TAG = "HarvestDatabase";
    private static final String UPDATE_TIMES_KEY = "updateTimes";

    private final HarvestDataSource mDataSource;

    private final SparseArray<Date> mUpdateTimes = new SparseArray<>();

    private SharedPreferences mUpdateTimePreferences = null;

    @Inject
    public HarvestDatabase(@NonNull @Named(APPLICATION_CONTEXT_NAME) final Context context) {
        mDataSource = new HarvestDataSource(context);

        loadUpdateTimes(context);

        mDataSource.open();
    }

    private void loadUpdateTimes(final Context context) {
        mUpdateTimes.clear();
        mUpdateTimePreferences = context.getSharedPreferences(UPDATE_TIMES_KEY, Context.MODE_PRIVATE);

        final Map<String, ?> data = mUpdateTimePreferences.getAll();

        if (data != null) {
            for (final Map.Entry<String, ?> entry : data.entrySet()) {
                Calendar calendar = null;

                try {
                    final String dateTimeStr = (String) entry.getValue();

                    if (dateTimeStr != null) {
                        calendar = DateTimeUtils.parseCalendar(dateTimeStr, true);
                    }
                } catch (final Exception e) {
                    // Will re-init Calendar instance.
                }

                if (calendar == null) {
                    calendar = Calendar.getInstance();

                    // TODO Is this dubious looking time logic still valid?
                    calendar.set(0, Calendar.JANUARY, 0);
                }

                mUpdateTimes.put(Integer.valueOf(entry.getKey()), calendar.getTime());
            }
        }
    }

    public void setUser(final String username) {
        mDataSource.setUser(username);
    }

    public void addNewLocallyCreatedHarvest(final GameHarvest harvest) {
        harvest.mRemote = false;
        harvest.mRev = 0;
        harvest.mMobileClientRefId = GameLog.generateMobileRefId();

        mDataSource.addHarvest(harvest, false);
    }

    /**
     * Update harvest in database.
     *
     * @param harvest
     * @param removeDeletedImagesInsteadOfStatusUpdate Indicates whether image modifications are permanent or set new statuses for images
     * @return
     */
    public HarvestImageUpdate updateHarvest(final GameHarvest harvest, final boolean removeDeletedImagesInsteadOfStatusUpdate) {
        return mDataSource.updateHarvest(harvest, removeDeletedImagesInsteadOfStatusUpdate);
    }

    /**
     * Deletes harvest from database. Used when pruning harvests not existing on server anymore.
     *
     * @param localId Local ID of harvest
     */
    public void removeHarvestByLocalId(final int localId) {
        mDataSource.removeHarvestByLocalId(localId);
    }

    /**
     * Gets image update data from harvest received from list received by calling getUnsentHarvests().
     */
    public HarvestImageUpdate getImageUpdateFromUnsentHarvest(final GameHarvest harvest) {
        return mDataSource.getImageUpdateFromUnsentHarvest(harvest);
    }

    /**
     * Marks the harvest as remote persistent.
     * This is used after the text part has been delivered.
     * Remote id must be supplied with the harvest.
     *
     * @param harvest Harvest that is marked as remotely persistent
     */
    public void markHarvestAsRemote(final GameHarvest harvest) {
        mDataSource.markHarvestAsRemote(harvest);
    }

    /**
     * Marks the harvest as sent.
     * Remote id must be supplied with the harvest.
     *
     * @param harvest Harvest that is marked as sent
     */
    public void markHarvestAsSent(final GameHarvest harvest) {
        mDataSource.markHarvestAsSent(harvest);
    }

    public void markImageAsSent(final GameLogImage image) {
        mDataSource.markImageAsSent(image);
    }

    public void removeImage(final GameLogImage image) {
        mDataSource.removeImage(image);
    }

    public List<GameHarvest> getHarvestsByHuntingYear(final int huntingYear) {
        return mDataSource.getHarvestsByHuntingYear(huntingYear);
    }

    public GameHarvest getHarvestByLocalId(final int localId) {
        return mDataSource.getHarvestByLocalId(localId);
    }

    public List<GameHarvest> getAllHarvests() {
        // This gets called before load() where data source is initialized.
        if (mDataSource != null) {
            return mDataSource.getAllHarvests();
        }

        return new ArrayList<>();
    }

    public List<GameHarvest> getUnsentHarvests() {
        return mDataSource.getUnsentHarvests();
    }

    public List<Integer> getHuntingYearsWithUnsentHarvests() {
        return mDataSource.getHuntingYearsWithUnsentHarvests();
    }

    public void close() {
        mDataSource.close();
    }

    /**
     * Handle list of received harvest updates for a season.
     * Save changed harvests to database.
     *
     * @param year   Season start year
     * @param jArray List of harvest entries
     * @return Harvest change events
     */
    public List<HarvestChangeEvent> handleReceivedHarvestUpdates(final int year, final JSONArray jArray) {
        final List<HarvestChangeEvent> harvestChanges = new ArrayList<>();

        try {
            final List<Integer> remoteIds = new ArrayList<>();

            try {
                mDataSource.beginTransaction();

                for (int i = 0; i < jArray.length(); i++) {
                    if (jArray.get(i) instanceof JSONObject) {
                        final JSONObject object = jArray.getJSONObject(i);

                        final JSONArray imageIds = object.getJSONArray("imageIds");
                        final List<GameLogImage> images = new ArrayList<>(imageIds.length());

                        for (int imageIndex = 0; imageIndex < imageIds.length(); imageIndex++) {
                            images.add(new GameLogImage(imageIds.getString(imageIndex)));
                        }

                        final String pointOfTimeStr = object.getString("pointOfTime");
                        final Calendar pointOfTime = DateTimeUtils.parseCalendar(pointOfTimeStr, true);

                        final GameHarvest harvest = new GameHarvest(
                                object.getInt("harvestSpecVersion"),
                                object.getInt("gameSpeciesCode"),
                                null, // location
                                pointOfTime,
                                object.getInt("amount"),
                                images);

                        harvest.mId = object.getInt("id");
                        remoteIds.add(harvest.mId);

                        harvest.mSent = true;
                        harvest.mRemote = true;
                        harvest.mRev = object.getInt("rev");

                        if (!object.isNull("canEdit")) {
                            harvest.mCanEdit = object.getBoolean("canEdit");
                        }

                        final JSONObject location = object.getJSONObject("geoLocation");

                        harvest.mCoordinates = new Pair<>(location.getLong("latitude"), location.getLong("longitude"));

                        if (!location.isNull("source")) {
                            harvest.mLocationSource = location.getString("source");
                        }
                        if (!location.isNull("accuracy")) {
                            harvest.mAccuracy = (float) location.getDouble("accuracy");
                        }
                        if (!location.isNull("altitude")) {
                            harvest.mHasAltitude = true;
                            harvest.mAltitude = location.getDouble("altitude");
                        }
                        if (!location.isNull("altitudeAccuracy")) {
                            harvest.mAltitudeAccuracy = location.getDouble("altitudeAccuracy");
                        }

                        harvest.mDescription = object.isNull("description") ? "" : object.getString("description");

                        if (!object.isNull("harvestReportDone")) {
                            harvest.mHarvestReportDone = object.getBoolean("harvestReportDone");
                        }

                        harvest.mHarvestReportRequired = object.getBoolean("harvestReportRequired");

                        if (!object.isNull("harvestReportState")) {
                            harvest.mHarvestReportState = object.getString("harvestReportState");
                        }

                        if (!object.isNull("permitNumber")) {
                            harvest.mPermitNumber = object.getString("permitNumber");
                        }
                        if (!object.isNull("permitType")) {
                            harvest.mPermitType = object.getString("permitType");
                        }
                        if (!object.isNull("stateAcceptedToHarvestPermit")) {
                            harvest.mStateAcceptedToHarvestPermit = object.getString("stateAcceptedToHarvestPermit");
                        }

                        if (!object.isNull("deerHuntingType")) {
                            harvest.mDeerHuntingType = DeerHuntingType.fromString(object.getString("deerHuntingType"));
                        }
                        if (!object.isNull("deerHuntingOtherTypeDescription")) {
                            harvest.mDeerHuntingOtherTypeDescription = object.getString("deerHuntingOtherTypeDescription");
                        }
                        if (!object.isNull("feedingPlace")) {
                            harvest.mFeedingPlace = object.getBoolean("feedingPlace");
                        }
                        if (!object.isNull("huntingMethod")) {
                            harvest.mHuntingMethod = GreySealHuntingMethod.valueOf(object.getString("huntingMethod"));
                        }
                        if (!object.isNull("taigaBeanGoose")) {
                            harvest.mTaigaBeanGoose = object.getBoolean("taigaBeanGoose");
                        }

                        parseSpecimenData(object, harvest);

                        if (mDataSource.insertOrUpdateReceivedHarvest(harvest)) {
                            harvestChanges.add(HarvestChangeEvent.inserted(harvest));
                        }
                    }
                }
                // Delete all remote harvests that no longer exist.
                final List<HarvestChangeEvent> changeEvents = mDataSource.pruneNonexistentRemoteHarvests(year, remoteIds);
                harvestChanges.addAll(changeEvents);
                mDataSource.setTransactionSuccessful();
            } finally {
                mDataSource.endTransaction();
            }

            // Set update time for this year
            final Date date = Calendar.getInstance().getTime();
            mUpdateTimes.put(year, date);

            if (mUpdateTimePreferences != null) {
                mUpdateTimePreferences.edit()
                        .putString(String.valueOf(year), DateTimeUtils.formatDate(date))
                        .apply();
            }
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return harvestChanges;
    }

    private void parseSpecimenData(final JSONObject json, final GameHarvest harvest) {
        final JSONArray specimenData = json.optJSONArray("specimens");

        if (specimenData != null) {
            harvest.mSpecimen = JsonUtils.jsonToList(specimenData.toString(), HarvestSpecimen.class);
        }
    }

    // Returns list of species IDs
    public List<Integer> getSpeciesIdsOfMostRecentHarvests(final int count) {
        final List<GameHarvest> latestHarvests = mDataSource.getMostRecentHarvests(count);
        final List<Integer> list = new ArrayList<>(latestHarvests.size());

        if (count > 0) {
            for (final GameHarvest harvest : latestHarvests) {
                if (!list.contains(harvest.mSpeciesID)) {
                    list.add(harvest.mSpeciesID);

                    if (list.size() == count) {
                        break;
                    }
                }
            }
        }

        return list;
    }

    public void pruneHarvestsRemovedOnServer(final Collection<GameHarvest> harvests) {
        for (final GameHarvest harvest : harvests) {
            if (harvest.mPendingOperation == HarvestDbHelper.UpdateType.DELETE) {
                mDataSource.removeHarvestByLocalId(harvest.mLocalId);
            }
        }
    }

    /**
     * Updates harvest images using given new images.
     * This can be used to prune deleted images.
     *
     * @param context        Context
     * @param harvestLocalId ID of locally persisted harvest
     * @param currentImages  Current images
     */
    public void pruneNonexistentLocalImages(final Context context,
                                            final int harvestLocalId,
                                            final List<GameLogImage> currentImages) {

        final int numCurrentImages = currentImages.size();
        final ArrayList<GameLogImage> newImages = new ArrayList<>(numCurrentImages);

        for (final GameLogImage image : currentImages) {
            boolean addImage = true;

            if (image.type == GameLogImage.ImageType.URI) {
                try (final InputStream ignored = ImageUtils.openStream(context, image.uri)) {
                } catch (final IOException e) {
                    addImage = false;
                }
            }

            if (addImage) {
                newImages.add(image);
            }
        }

        if (newImages.size() < numCurrentImages) {
            mDataSource.updateImages(harvestLocalId, currentImages, newImages, true);
        }
    }

    /**
     * Update times need to be cleared when user logs out. Otherwise user keeps checking for updates against wrong date
     */
    public void clearUpdateTimes() {
        mUpdateTimePreferences.edit().clear().apply();
    }

    public List<Integer> getHuntingYearsOfHarvests() {
        return mDataSource.getHuntingYearsOfHarvests();
    }

    public Date getUpdateTimeForHarvestYear(final int year) {
        return mUpdateTimes.get(year);
    }

    public List<GameLogImage> getAllHarvestImages() {
        return mDataSource.getAllHarvestImages();
    }
}
