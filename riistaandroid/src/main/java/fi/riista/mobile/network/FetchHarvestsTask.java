package fi.riista.mobile.network;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.DiaryEntryUpdate;
import fi.riista.mobile.database.GameDatabase;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Download diary entries for one season
 */
public class FetchHarvestsTask extends TextTask {

    private int mStartYear = 0;

    protected FetchHarvestsTask(WorkContext context, int startYear) {
        super(context);
        mStartYear = startYear;
        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/gamediary/harvests/" + startYear);
        addParameter("harvestSpecVersion", "" + AppConfig.HARVEST_SPEC_VERSION);
    }

    @Override
    protected void onFinishText(String text) {
        ParseTask task = new ParseTask(text) {
            @Override
            public void onPostExecute(Void result) {
                onLoad(mUpdates);
            }
        };
        task.execute();
    }

    protected void onLoad(List<DiaryEntryUpdate> updatedEntries) {
        // Override this method
    }

    private class ParseTask extends AsyncTask<Void, Void, Void> {
        List<DiaryEntryUpdate> mUpdates;
        private String mText;

        ParseTask(String text) {
            mText = text;
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONArray jArray;
            try {
                jArray = new JSONArray(mText);
                mUpdates = GameDatabase.getInstance().handleReceivedEntries(mStartYear, jArray);
            } catch (Exception e) {
                // Can get at least IllegalStateException
                e.printStackTrace();
            }
            return null;
        }
    }
}
