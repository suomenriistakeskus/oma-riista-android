package fi.riista.mobile.network;

import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.Permit;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Get permit information from server matching permit number
 */
public class CheckPermitNumberTask extends TextTask {

    protected CheckPermitNumberTask(WorkContext context, String permitNumber) {
        super(context);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/gamediary/checkPermitNumber");
        setHttpMethod(HttpMethod.POST);
        addParameter("permitNumber", permitNumber);
    }

    @Override
    protected void onFinishText(String text) {
        ParseTask task = new ParseTask(text) {
            @Override
            public void onPostExecute(Void result) {
                onDone(mPermit);
            }
        };
        task.execute();
    }

    @Override
    protected void onError() {
        // Override this method
    }

    protected void onDone(Permit permit) {
        // Override this method
    }

    private class ParseTask extends AsyncTask<Void, Void, Void> {
        Permit mPermit;
        private String mText;

        ParseTask(String text) {
            mText = text;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JodaModule());

            try {
                mPermit = mapper.readValue(mText, Permit.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
