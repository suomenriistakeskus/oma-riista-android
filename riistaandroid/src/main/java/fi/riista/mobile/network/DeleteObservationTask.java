package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.GameObservation;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class DeleteObservationTask extends TextTask {
    protected DeleteObservationTask(WorkContext workContext, GameObservation observation) {
        super(workContext);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/gamediary/observation/" + observation.remoteId);
        setHttpMethod(HttpMethod.DELETE);
    }
}
