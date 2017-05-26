package fi.riista.mobile.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import fi.riista.mobile.AppLifecycleHandler;
import fi.riista.mobile.activity.LoginActivity;
import fi.riista.mobile.database.DiaryEntryUpdate.UpdateType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.network.LogImageTask;
import fi.riista.mobile.network.LoginTask;
import fi.riista.mobile.network.PostHarvestTask;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.ImageUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.network.SynchronizedCookieStore;

/**
 * Class for handling database operations
 * Uses DiaryDataSource class to modify local database
 * The class handles automatic synchronizing when using automatic synchronization mode
 */
public class GameDatabase implements DiarySync.DiarySyncReceiver {

    private final static int UPDATE_INTERVAL_SECONDS = 300;

    private static String UPDATE_TIMES_KEY = "updateTimes";
    private static String syncModeKey = "SyncMode";
    private static GameDatabase mInstance = null;
    // Keep list of events that are being sent - this prevents duplicate sends with slow connection
    private List<Integer> mEventsBeingSent = new ArrayList<>();
    private DiaryDataSource mDiaryDataSource = null;
    private SynchronizedCookieStore mCookieStore = new SynchronizedCookieStore();
    private String PREFS_LOGIN_USERNAME_KEY = "username";
    private String PREFS_LOGIN_PASSWORD_KEY = "password";

    private Handler mSyncHandler = new Handler();
    private Runnable mSyncTask = null;
    private boolean mIsSyncQueued = false;

    // Whether events have been tried to be sent for first time
    private boolean mFirstTimeSent = false;
    private SharedPreferences mUpdateTimePreferences = null;
    private SparseArray<Date> mUpdateTimes = new SparseArray<>();
    // Current session details
    private String mCurrentUsername = "";
    private List<DatabaseUpdateListener> mRegisteredListeners = new ArrayList<>();

    private GameDatabase() {

    }

    public static GameDatabase getInstance() {
        if (mInstance == null) {
            mInstance = new GameDatabase();
        }
        return mInstance;
    }

    public static long generateMobileRefId(Random random) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long secondsSinceEpoch = calendar.getTimeInMillis() / 1000L;
        return (secondsSinceEpoch << 32) + random.nextInt();
    }

    public void registerListener(DatabaseUpdateListener listener) {
        mRegisteredListeners.add(listener);
    }

    public void unregisterListener(DatabaseUpdateListener listener) {
        mRegisteredListeners.remove(listener);
    }

    @Override
    public void syncCompleted(List<DiaryEntryUpdate> updatedEntries) {
        updateEvents(updatedEntries);
    }

    private void updateEvents(List<DiaryEntryUpdate> updatedEntries) {
        for (int i = 0; i < mRegisteredListeners.size(); i++) {
            mRegisteredListeners.get(i).eventsUpdated(updatedEntries);
        }
    }

    public void addNewLocallyCreatedEvent(GameHarvest event) {
        event.mRemote = false;
        event.mRev = 0;
        event.mMobileClientRefId = generateMobileRefId(new Random());
        mDiaryDataSource.addLocalEvent(event, false);
        doUpdatesLocally(event, UpdateType.INSERT);
    }

    public void removeEvent(final WorkContext context, final GameHarvest event, final EditEventCompletion completion) {
        event.mSent = false;
        event.mPendingOperation = DiaryHelper.UpdateType.DELETE;
        SyncMode syncMode = getSyncMode(context.getContext());

        if (!event.mRemote) {
            mDiaryDataSource.removeLocalEvent(event.mLocalId);
            completion.editSuccessful();
            doUpdatesLocally(event, UpdateType.DELETE);
        } else if (syncMode == SyncMode.SYNC_AUTOMATIC) {
            PostHarvestTask task = new PostHarvestTask(context, event, false) {
                @Override
                protected void eventSent() {
                    mDiaryDataSource.removeLocalEvent(event.mLocalId);
                    completion.editSuccessful();
                    doUpdatesLocally(event, UpdateType.DELETE);
                }

                @Override
                protected void eventWaitingDelivery() {
                    mDiaryDataSource.removeLocalEvent(event.mLocalId);
                    completion.editSuccessful();
                    doUpdatesLocally(event, UpdateType.DELETE);
                }

                @Override
                protected void eventOutdated() {
                    completion.editFailed();
                }

                @Override
                protected void eventSendingFailed() {
                    completion.editFailed();
                }
            };
            task.start();
        } else {
            mDiaryDataSource.editLocalEvent(event, false);
            completion.editSuccessful();
            doUpdatesLocally(event, UpdateType.DELETE);
        }
    }

    private void doUpdatesLocally(GameHarvest event, UpdateType type) {
        DiaryEntryUpdate update = new DiaryEntryUpdate();
        update.event = event;
        update.type = type;
        List<DiaryEntryUpdate> updates = new ArrayList<>();
        updates.add(update);
        updateEvents(updates);
    }

    public void editEvent(final WorkContext context, final GameHarvest event, final EditEventCompletion completion) {
        SyncMode syncMode = getSyncMode(context.getContext());
        if (syncMode == SyncMode.SYNC_AUTOMATIC) {
            PostHarvestTask task = new PostHarvestTask(context, event, false) {
                @Override
                protected void eventSent() {
                    completion.editSuccessful();
                    doUpdatesLocally(event, UpdateType.UPDATE);
                }

                @Override
                protected void imageCompletion(boolean errors, int imagesSent) {
                    if (!errors) {
                        doUpdatesLocally(event, UpdateType.UPDATE);
                    }
                }

                @Override
                protected void eventWaitingDelivery() {
                    completion.editSuccessful();
                    doUpdatesLocally(event, UpdateType.INSERT);
                }

                @Override
                protected void eventOutdated() {
                    completion.editEventOutdated();
                }

                @Override
                protected void eventSendingFailed() {
                    Utils.printTaskInfo("eventSendingFailed()", this);
                    completion.editFailed();
                }
            };
            task.start();
        } else {
            mDiaryDataSource.editLocalEvent(event, false);
            completion.editSuccessful();
            doUpdatesLocally(event, UpdateType.UPDATE);
        }
    }

    /**
     * Edits the event locally
     *
     * @param event
     * @param permanent Indicates whether image modifications are permanent or set new statuses for images
     * @return
     */
    public LogImageUpdate editLocalEvent(GameHarvest event, boolean permanent) {
        return mDiaryDataSource.editLocalEvent(event, permanent);
    }

    /**
     * Gets image update data from event received from list received with call getUnsentEvents
     */
    public LogImageUpdate getImageUpdateFromUnsentEvent(GameHarvest event) {
        return mDiaryDataSource.getImageUpdateFromUnsentEvent(event);
    }

    /**
     * Marks the event as remote
     * This is used after the text part has been delivered
     * Remote id must be supplied with the event
     *
     * @param event Event that is marked as remote
     */
    public void markEventAsRemote(GameHarvest event) {
        mDiaryDataSource.markEventAsRemote(event);
    }

    /**
     * Marks the event as sent
     * Remote id must be supplied with the event
     *
     * @param event Event that is marked as sent
     */
    public void markEventAsSent(GameHarvest event) {
        mDiaryDataSource.markEventAsSent(event);
    }

    public void markImageAsSent(LogImage image, LogImageTask.OperationType operationType) {
        mDiaryDataSource.markImageAsSent(image, operationType);
    }

    public List<GameHarvest> getCatchesByYear(int year) {
        return mDiaryDataSource.getCatchesByYear(year);
    }

    public GameHarvest getEventByLocalId(int id) {
        return mDiaryDataSource.getEventByLocalId(id);
    }

    public List<GameHarvest> getAllEvents() {
        // This gets called before load() where data source is initialized
        if (mDiaryDataSource != null) {
            return mDiaryDataSource.getAllEvents();
        } else {
            return new ArrayList<>();
        }
    }

    public void load(Context context) {
        mDiaryDataSource = new DiaryDataSource(context);
        loadUpdateTimes(context);
        mDiaryDataSource.open();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            mCurrentUsername = prefs.getString(PREFS_LOGIN_USERNAME_KEY, null);
            mDiaryDataSource.setUser(mCurrentUsername);
        } catch (Exception ignored) {
        }
    }

    public void close() {
        mDiaryDataSource.close();
    }

    /**
     * Handle list of received entries for a season.
     * Save changed entries to database
     *
     * @param year   Season start year
     * @param jArray List of entries
     * @return
     */
    public List<DiaryEntryUpdate> handleReceivedEntries(int year, JSONArray jArray) {
        List<DiaryEntryUpdate> updatedEntries = new ArrayList<>();
        try {
            List<Integer> remoteIds = new ArrayList<>();
            try {
                mDiaryDataSource.beginTransaction();
                for (int i = 0; i < jArray.length(); i++) {
                    if (jArray.get(i) instanceof JSONObject) {
                        JSONObject object = jArray.getJSONObject(i);
                        Calendar time = Calendar.getInstance();
                        SimpleDateFormat format = new SimpleDateFormat(DiaryDataSource.ISO_8601);
                        Date date = format.parse(object.getString("pointOfTime"));
                        time.setTime(date);
                        List<LogImage> images = new ArrayList<>();
                        JSONArray imageIds = object.getJSONArray("imageIds");
                        for (int imageIndex = 0; imageIndex < imageIds.length(); imageIndex++) {
                            images.add(new LogImage(imageIds.getString(imageIndex)));
                        }
                        String description = "";
                        if (!object.isNull("description")) {
                            description = object.getString("description");
                        }

                        GameHarvest event = new GameHarvest(object.getInt("gameSpeciesCode"), object.getInt("amount"), description, time, object.getString("type"), null, images);
                        JSONObject location = object.getJSONObject("geoLocation");
                        event.mCoordinates = new Pair<>(location.getLong("latitude"), location.getLong("longitude"));
                        if (!location.isNull("source")) {
                            event.mLocationSource = location.getString("source");
                        }
                        if (!location.isNull("accuracy"))
                            event.mAccuracy = (float) location.getDouble("accuracy");
                        if (!location.isNull("altitude")) {
                            event.mHasAltitude = true;
                            event.mAltitude = location.getDouble("altitude");
                        }
                        if (!location.isNull("altitudeAccuracy")) {
                            event.mAltitudeAccuracy = location.getDouble("altitudeAccuracy");
                        }

                        event.mId = object.getInt("id");

                        if (!object.isNull("harvestReportDone")) {
                            event.mHarvestReportDone = object.getBoolean("harvestReportDone");
                        }
                        event.mHarvestReportRequired = object.getBoolean("harvestReportRequired");
                        if (!object.isNull("harvestReportState")) {
                            event.mHarvestReportState = object.getString("harvestReportState");
                        }

                        if (!object.isNull("permitNumber")) {
                            event.mPermitNumber = object.getString("permitNumber");
                        }
                        if (!object.isNull("permitType")) {
                            event.mPermitType = object.getString("permitType");
                        }
                        if (!object.isNull("stateAcceptedToHarvestPermit")) {
                            event.mStateAcceptedToHarvestPermit = object.getString("stateAcceptedToHarvestPermit");
                        }
                        if (!object.isNull("canEdit")) {
                            event.mCanEdit = object.getBoolean("canEdit");
                        }

                        parseSpecimenData(object, event);

                        event.mRev = object.getInt("rev");
                        event.mApiDataFormat = object.getInt("harvestSpecVersion");
                        event.mRemote = true;
                        event.mSent = true;
                        remoteIds.add(event.mId);

                        if (mDiaryDataSource.insertReceivedEvent(event)) {
                            DiaryEntryUpdate update = new DiaryEntryUpdate();
                            update.event = event;
                            update.type = UpdateType.INSERT;
                            updatedEntries.add(update);
                        }
                    }
                }
                // Delete all remote entries that no longer exist
                List<DiaryEntryUpdate> updates = mDiaryDataSource.pruneNonexistentRemoteEvents(year, remoteIds);
                updatedEntries.addAll(updates);
                mDiaryDataSource.setTransactionSuccessful();
            } finally {
                mDiaryDataSource.endTransaction();
            }
            // Set update time for this year
            Calendar calendar = Calendar.getInstance();
            mUpdateTimes.put(year, calendar.getTime());
            if (mUpdateTimePreferences != null) {
                SharedPreferences.Editor editor = mUpdateTimePreferences.edit();
                DateFormat df = new SimpleDateFormat(DiaryDataSource.ISO_8601);
                editor.putString(String.valueOf(year), df.format(calendar.getTime()));
                editor.apply();
            }
        } catch (JSONException | ParseException e) {
            Log.e(GameDatabase.class.getSimpleName(), e.getMessage());
        }
        return updatedEntries;
    }

    private void parseSpecimenData(JSONObject json, GameHarvest event) {
        JSONArray specimenData = json.optJSONArray("specimens");
        if (specimenData != null) {
            List<Specimen> specimenList = JsonUtils.jsonToList(specimenData.toString(), Specimen.class);
            event.mSpecimen = specimenList;
        }
    }

    // Returns list of species IDs
    public List<Integer> getLatestSpecies(int amount) {
        List<GameHarvest> latestEvents = mDiaryDataSource.getLatestSpeciesEvents(amount, DiaryDataSource.EventTypeFilter.CATCHES);
        List<Integer> list = new ArrayList<>();
        if (amount > 0) {
            for (int i = 0; i < latestEvents.size(); i++) {
                if (!list.contains(latestEvents.get(i).mSpeciesID)) {
                    list.add(latestEvents.get(i).mSpeciesID);
                    if (list.size() == amount) {
                        break;
                    }
                }
            }
        }
        return list;
    }

    // First fetch latest data and then try to send unsent events
    // This reduces the risk of overwriting newer events
    // For now, edited entries always get overwritten
    public void sendLocalEvents(final WorkContext context, boolean sync) {
        if (sync) {
            List<Integer> years = mDiaryDataSource.getUnsentEventYears();
            DiarySync diarysync = new DiarySync(context, this) {
                @Override
                protected void syncCompleted() {
                    sendUnsentEvents(context);
                }
            };
            diarysync.sync(true, years);
        } else {
            sendUnsentEvents(context);
        }
    }

    // Performs full sync by first syncing all available calendar years and then sending unsent events
    private void syncAndSend(final WorkContext context) {
        DiarySync diarysync = new DiarySync(context, this) {
            @Override
            protected void syncCompleted() {
                sendUnsentEvents(context);
            }
        };
        diarysync.sync(true, null);
    }

    private void sendUnsentEvents(final WorkContext context) {
        final List<GameHarvest> events = mDiaryDataSource.getUnsentEvents();
        final List<GameHarvest> completedEvents = new ArrayList<>();
        final List<GameHarvest> completedEventImageSubmissions = new ArrayList<>();
        final List<DiaryEntryUpdate> completedImageSubmissions = new ArrayList<>();
        final List<DiaryEntryUpdate> sentEvents = new ArrayList<>();
        final AtomicInteger numImagesSent = new AtomicInteger();
        numImagesSent.set(0);
        for (int i = 0; i < events.size(); i++) {

            if (!mEventsBeingSent.contains(events.get(i).mLocalId)) {
                mEventsBeingSent.add(events.get(i).mLocalId);

                final GameHarvest event = events.get(i);
                PostHarvestTask task = new PostHarvestTask(context, event, !event.mRemote) {
                    @Override
                    protected void eventSent() {
                        DiaryEntryUpdate update = new DiaryEntryUpdate();
                        update.type = event.mPendingOperation == DiaryHelper.UpdateType.DELETE ? UpdateType.DELETE : UpdateType.INSERT;
                        update.event = event;
                        sentEvents.add(update);
                    }

                    @Override
                    protected void eventSendingFailed() {
                        if (!event.mRemote) {
                            DiaryEntryUpdate update = new DiaryEntryUpdate();
                            update.type = event.mPendingOperation == DiaryHelper.UpdateType.DELETE ? UpdateType.DELETE : UpdateType.INSERT;
                            update.event = event;
                            sentEvents.add(update);
                        }
                    }

                    @Override
                    protected void eventWaitingDelivery() {
                        DiaryEntryUpdate update = new DiaryEntryUpdate();
                        update.type = event.mPendingOperation == DiaryHelper.UpdateType.DELETE ? UpdateType.DELETE : UpdateType.UPDATE;
                        update.event = event;
                        sentEvents.add(update);
                    }

                    @Override
                    protected void textCompletion() {
                        completedEvents.add(event);
                        if (mEventsBeingSent.contains(event.mLocalId))
                            mEventsBeingSent.remove(Integer.valueOf(event.mLocalId));
                        if (completedEvents.size() == events.size()) {
                            pruneRemovedLocalEntries(completedEvents);
                            updateEvents(sentEvents);
                        }
                    }

                    @Override
                    protected void imageCompletion(boolean errors, int sentImages) {
                        if (!errors) {
                            DiaryEntryUpdate update = new DiaryEntryUpdate();
                            update.type = UpdateType.UPDATE;
                            update.event = event;
                            numImagesSent.getAndAdd(sentImages);
                            completedImageSubmissions.add(update);
                        }
                        completedEventImageSubmissions.add(event);
                        if (completedImageSubmissions.size() == events.size() && numImagesSent.get() > 0) {
                            updateEvents(completedImageSubmissions);
                        }
                    }
                };
                task.start();
            }
        }
    }

    private void pruneRemovedLocalEntries(List<GameHarvest> updateList) {
        for (GameHarvest event : updateList) {
            if (event.mPendingOperation == DiaryHelper.UpdateType.DELETE) {
                mDiaryDataSource.removeLocalEvent(event.mLocalId);
            }
        }
    }

    /**
     * Updates diary image using given new images
     * This can be used to prune deleted images
     *
     * @param context Context
     * @param event   Target event
     */
    public void PruneNonexistentLocalImages(Context context, GameHarvest event) {
        List<LogImage> newImages = new ArrayList<>();
        for (int i = 0; i < event.mImages.size(); i++) {
            if (event.mImages.get(i).type != LogImage.ImageType.URI) {
                newImages.add(event.mImages.get(i));
            } else {
                try {
                    InputStream imageStream = ImageUtils.openStream(context, event.mImages.get(i).uri);
                    imageStream.close();
                    newImages.add(event.mImages.get(i));
                } catch (IOException ignored) {
                }
            }
        }
        if (newImages.size() < event.mImages.size())
            mDiaryDataSource.editDiaryImages(event.mLocalId, event.mImages, newImages, true);
    }

    /**
     * Checks if login credential are stored in shared preferences
     */
    public boolean credentialsStored(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = null;
        try {
            username = prefs.getString(PREFS_LOGIN_USERNAME_KEY, null);
        } catch (Exception ignored) {
        }
        return (username != null);
    }

    /**
     * Stores credentials
     */
    public void storeCredentials(Context context, String username, String password) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mCurrentUsername = username;
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_LOGIN_USERNAME_KEY, username);
        editor.putString(PREFS_LOGIN_PASSWORD_KEY, password);
        editor.apply();
    }

    /**
     * Update times need to be cleared when user logs out. Otherwise user keeps checking for updates against wrong date
     */
    public void clearUpdateTimes() {
        SharedPreferences.Editor editor = mUpdateTimePreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Removes stored login credentials
     */
    public void removeCredentials(Context context) {
        if (credentialsStored(context)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(PREFS_LOGIN_USERNAME_KEY);
            editor.remove(PREFS_LOGIN_PASSWORD_KEY);
            editor.remove("");
            editor.apply();
        }
    }

    /**
     * Tries to login using stored credentials
     * If the login doesn't succeed, user is forced to log out
     *
     * @param doSync Sync after successful login
     */
    public void loginWithStoredCredentials(final WorkContext context, final boolean doSync) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getContext());
        try {
            final String username = sharedPrefs.getString(PREFS_LOGIN_USERNAME_KEY, "");
            String password = sharedPrefs.getString(PREFS_LOGIN_PASSWORD_KEY, "");
            LoginTask task = new LoginTask(context, username, password, true) {
                @Override
                public void loginFailed() {
                    if (getHttpStatusCode() >= 400 && getHttpStatusCode() < 500) {
                        removeCredentials(context.getContext());
                        Intent intent = new Intent(context.getContext(), LoginActivity.class);
                        context.getContext().startActivity(intent);
                    }
                }

                @Override
                protected void onFinishText(String text) {
                    AppPreferences.setUserInfo(context.getContext(), text);

                    if (doSync) {
                        mCurrentUsername = username;
                        DiarySync sync = new DiarySync(context, GameDatabase.this);
                        // Try syncing again once
                        sync.sync(false, null);
                    }
                }
            };
            task.start();
        } catch (Exception ignored) {
        }
    }

    /**
     * This function is called on init and when the synchronization settings change
     *
     * @param workContext
     * @param initialWait Wait time before making first synchronization in milliseconds
     */
    public synchronized void initSync(final WorkContext workContext, int initialWait) {
        if (mIsSyncQueued) {
            return;
        }

        mSyncTask = new Runnable() {
            @Override
            public void run() {
                // Synchronize and send unsent events
                syncAndSend(workContext);

                synchronized (GameDatabase.this) {
                    // Only continue further syncs when app is active
                    if (AppLifecycleHandler.getInstance().isApplicationInForeground() && getSyncMode(workContext.getContext()) == SyncMode.SYNC_AUTOMATIC) {
                        mSyncHandler.postDelayed(this, UPDATE_INTERVAL_SECONDS * 1000);
                        mIsSyncQueued = true;
                    } else {
                        mIsSyncQueued = false;
                        Log.d(Utils.LOG_TAG, "Not queuing next sync");
                    }
                }
            }
        };

        mSyncHandler.postDelayed(mSyncTask, initialWait);
        mIsSyncQueued = true;
    }

    public synchronized void stopSyncing() {
        mSyncHandler.removeCallbacks(mSyncTask);
        mIsSyncQueued = false;
    }

    public synchronized void doSyncAndResetTimer() {
        mSyncHandler.removeCallbacks(mSyncTask);
        mSyncHandler.post(mSyncTask);
        mIsSyncQueued = true;
    }

    public void manualSync(final WorkContext workContext) {
        // Synchnonizes and sends unsent events
        syncAndSend(workContext);
    }

    List<Integer> getEventStartYears() {
        return mDiaryDataSource.getEventStartYears();
    }

    public SynchronizedCookieStore getCookieStore() {
        return mCookieStore;
    }

    private void loadUpdateTimes(Context context) {
        mUpdateTimes.clear();
        mUpdateTimePreferences = context.getSharedPreferences(UPDATE_TIMES_KEY, Context.MODE_PRIVATE);
        Map<String, ?> data = mUpdateTimePreferences.getAll();
        if (data != null) {
            for (Map.Entry<String, ?> entry : data.entrySet()) {
                Date date = Utils.parseDate((String) entry.getValue());
                mUpdateTimes.put(Integer.valueOf(entry.getKey()), date);
            }
        }
    }

    Date getUpdateTimeForEventYear(int year) {
        return mUpdateTimes.get(year);
    }

    // Broadcast receiver uses this to determine if it needs to send try sending any events yet
    public boolean hasSentEventsForFirstTime() {
        return mFirstTimeSent;
    }

    public void setSyncMode(WorkContext context, SyncMode mode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getContext());
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(syncModeKey, mode.ordinal());
            editor.apply();
            if (mode == SyncMode.SYNC_MANUAL) {
                stopSyncing();
            } else {
                initSync(context, UPDATE_INTERVAL_SECONDS * 1000);
            }
        }
    }

    public SyncMode getSyncMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(syncModeKey)) {
            int syncMode = prefs.getInt(syncModeKey, 0);
            if (SyncMode.values().length > syncMode) {
                return SyncMode.values()[syncMode];
            }
        }
        return SyncMode.SYNC_AUTOMATIC;
    }

    public List<LogImage> getAllLogImages() {
        return mDiaryDataSource.getAllLogImages();
    }

    public enum SyncMode {
        SYNC_MANUAL,
        SYNC_AUTOMATIC
    }

    public interface DatabaseUpdateListener {
        void eventsUpdated(List<DiaryEntryUpdate> updatedEvents);
    }

    public static class Statistics {
        public ArrayList<Integer> mMonthlyData;
        public Integer mTotalCatches = 0;
        public SparseIntArray mCategoryData;

        public Statistics() {
            mMonthlyData = new ArrayList<>(Collections.nCopies(12, 0));
            mCategoryData = new SparseIntArray();
        }
    }
}
