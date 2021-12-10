package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class DeleteObservationTask extends TextTask {
    protected DeleteObservationTask(WorkContext workContext, GameObservation observation) {
        super(workContext);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/observation/" + observation.remoteId);
        setHttpMethod(HttpMethod.DELETE);
    }
}
