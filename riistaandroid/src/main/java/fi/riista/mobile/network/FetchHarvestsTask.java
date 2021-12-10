package fi.riista.mobile.network;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.event.HarvestChangeEvent;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Download diary entries for one season
 */
public class FetchHarvestsTask extends TextTask {

    private final HarvestDatabase mHarvestDatabase;
    private final int mHuntingYear;

    // TODO Remove `harvestSpecVersion` parameter when deer pilot 2020 is over.
    protected FetchHarvestsTask(final WorkContext context,
                                final HarvestDatabase harvestDatabase,
                                final int huntingYear,
                                final int harvestSpecVersion) {
        super(context);

        mHarvestDatabase = harvestDatabase;
        mHuntingYear = huntingYear;

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/harvests/" + huntingYear);

        addParameter("harvestSpecVersion", String.valueOf(harvestSpecVersion));
    }

    @Override
    protected void onFinishText(final String text) {
        final ParseTask task = new ParseTask(text) {
            @Override
            public void onPostExecute(final Void result) {
                onLoad(mChangeEvents);
            }
        };
        task.execute();
    }

    protected void onLoad(final List<HarvestChangeEvent> harvestChanges) {
        // Override this method
    }

    private class ParseTask extends AsyncTask<Void, Void, Void> {

        private String mText;
        List<HarvestChangeEvent> mChangeEvents;

        ParseTask(final String text) {
            mText = text;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                mChangeEvents = mHarvestDatabase.handleReceivedHarvestUpdates(mHuntingYear, new JSONArray(mText));
            } catch (final Exception e) {
                // Can get at least IllegalStateException.
                e.printStackTrace();
            }
            return null;
        }
    }
}
