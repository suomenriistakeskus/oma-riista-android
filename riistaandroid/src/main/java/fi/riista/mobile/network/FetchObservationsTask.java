package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.GameObservation;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class FetchObservationsTask extends JsonListTask<GameObservation> {
    protected FetchObservationsTask(WorkContext workContext, int year) {
        super(workContext, GameObservation.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/gamediary/observations/" + year);
        addParameter("observationSpecVersion", "" + AppConfig.OBSERVATION_SPEC_VERSION);
    }
}
