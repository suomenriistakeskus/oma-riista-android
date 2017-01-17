package fi.riista.mobile.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.network.FetchHarvestsTask;
import fi.riista.mobile.observation.ObservationSync;
import fi.riista.mobile.observation.ObservationSync.ObservationSyncListener;
import fi.riista.mobile.srva.SrvaSync;
import fi.riista.mobile.srva.SrvaSync.SrvaSyncListener;
import fi.riista.mobile.utils.ImageUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Task for synchronizing diary entries from server to device
 * Server is first asked if given season diary entries have been updates since given date
 * If there have been modifications, list of all entries from given season is loaded
 */
public class DiarySync {

    private static float SYNC_MIN_INTERVAL = 0.5f;
    private List<Integer> mYears = null;
    private int mSyncsDone = 0;
    private WorkContext mWorkContext = null;
    private List<DiaryEntryUpdate> allUpdatedEntries = new ArrayList<DiaryEntryUpdate>();
    private DiarySyncReceiver mSource = null;

    DiarySync(WorkContext workContext, DiarySyncReceiver source) {
        mWorkContext = workContext;
        mSource = source;
    }

    /**
     * Synchronize data with database
     *
     * @param retry     Whether sync is tried again after successful login
     * @param syncYears - leave null to fetch server event years
     */
    void sync(final boolean retry, List<Integer> syncYears) {

        if (mWorkContext.getContext() != null && mWorkContext.getContext() instanceof DiarySyncContext) {
            ((DiarySyncContext) mWorkContext.getContext()).syncStarted();
        }

        if (syncYears == null) {
            GameYearFetchingTask yearTask = new GameYearFetchingTask(mWorkContext) {

                @Override
                protected void onError() {
                    // If error happened to any other reason than network being unreachable, try logging in again
                    if (getHttpStatusCode() != -1)
                        tryLogin(retry);
                    syncCompletedInternal();
                }

                @Override
                protected void onFinishText(String text) {
                    PermitManager.getInstance(getWorkContext().getContext()).preloadPermits(getWorkContext());

                    List<Integer> harvestYears = GameDatabase.getInstance().getEventStartYears();
                    try {
                        JSONObject object = new JSONObject(text);

                        List<Integer> serverHarvestYears = parseYears(object.getJSONArray("harvestYears"));
                        for (Integer year : serverHarvestYears) {
                            if (!harvestYears.contains(year)) {
                                harvestYears.add(year);
                            }
                        }
                        List<Integer> observationYears = parseYears(object.getJSONArray("observationYears"));

                        syncYears(harvestYears, observationYears, retry);
                    } catch (JSONException e) {
                        Utils.LogMessage("Can't parse years:" + e.getMessage());
                    }
                }
            };
            yearTask.start();
        } else {
            syncYears(syncYears, null, retry);
        }
    }

    private static List<Integer> parseYears(JSONArray jArray) throws JSONException {
        List<Integer> years = new ArrayList<>();
        for (int i = 0; i < jArray.length(); i++) {
            int year = jArray.getInt(i);
            years.add(year);
        }
        return years;
    }

    private void syncYears(List<Integer> harvestYears, List<Integer> observationYears, final boolean retry) {
        final GameDatabase database = GameDatabase.getInstance();
        mYears = harvestYears;
        mSyncsDone = 0;

        //Sync observations
        ObservationSync observationSync = new ObservationSync();
        observationSync.sync(observationYears, new ObservationSyncListener() {
            @Override
            public void onFinish() {
                //Sync SRVA events
                SrvaSync srvaSync = new SrvaSync();
                srvaSync.sync(new SrvaSyncListener() {
                    @Override
                    public void onFinish() {
                        //Then sync harvest
                        if (mYears.size() > 0) {
                            for (int i = 0; i < mYears.size(); i++) {
                                Date date = database.getUpdateTimeForEventYear(mYears.get(i));
                                if (date != null && Utils.isRecentTime(date, SYNC_MIN_INTERVAL)) {
                                    syncDone(null);
                                } else {
                                    final int year = mYears.get(i);
                                    syncData(year, retry);
                                }
                            }
                        } else {
                            syncCompletedInternal();
                        }
                    }
                });
            }
        });
    }

    private void syncData(int year, final boolean retry) {
        FetchHarvestsTask entryTask = new FetchHarvestsTask(mWorkContext, year) {
            @Override
            protected void onLoad(List<DiaryEntryUpdate> updatedEntries) {
                if (DiarySync.this != null) {
                    syncDone(updatedEntries);
                }
            }

            @Override
            protected void onError() {
                tryLogin(retry);
                if (DiarySync.this != null) {
                    syncDone(null);
                }
            }
        };
        entryTask.start();
    }

    private void syncDone(List<DiaryEntryUpdate> updatedEntries) {
        if (updatedEntries != null) {
            allUpdatedEntries.addAll(updatedEntries);
        }
        mSyncsDone++;
        if (mSyncsDone == mYears.size()) {
            syncCompletedInternal();
        }
    }

    private void syncCompletedInternal() {
        if (mWorkContext.getContext() != null && mWorkContext.getContext() instanceof DiarySyncContext) {
            ((DiarySyncContext) mWorkContext.getContext()).syncCompleted();
        }
        mSource.syncCompleted(allUpdatedEntries);

        ImageUtils.removeUnusedImagesAsync();

        syncCompleted();
    }

    protected void syncCompleted() {
        // Override
    }

    private void tryLogin(boolean retry) {
        GameDatabase database = GameDatabase.getInstance();
        if (database.credentialsStored(mWorkContext.getContext())) {
            database.loginWithStoredCredentials(mWorkContext, retry);
        }
    }

    public interface DiarySyncContext {
        void syncStarted();

        void syncCompleted();
    }

    interface DiarySyncReceiver {
        void syncCompleted(List<DiaryEntryUpdate> updatedEntries);
    }

    private class GameYearFetchingTask extends TextTask {
        GameYearFetchingTask(WorkContext context) {
            super(context);
            setCookieStore(GameDatabase.getInstance().getCookieStore());
            setHttpMethod(HttpMethod.GET);
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/account");
        }

        @Override
        protected void onFinishText(String text) {
            // Override
        }
    }
}
