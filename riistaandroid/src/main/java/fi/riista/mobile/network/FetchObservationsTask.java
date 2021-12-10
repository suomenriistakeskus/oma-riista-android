package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class FetchObservationsTask extends JsonListTask<GameObservation> {
    protected FetchObservationsTask(WorkContext workContext, int year) {
        super(workContext, GameObservation.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/observations/" + year);
        addParameter("observationSpecVersion", "" + AppConfig.OBSERVATION_SPEC_VERSION);
    }
}
