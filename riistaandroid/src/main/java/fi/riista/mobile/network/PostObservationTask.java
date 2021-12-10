package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.riista.mobile.utils.JsonUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class PostObservationTask extends JsonObjectTask<GameObservation> {
    protected PostObservationTask(WorkContext workContext, GameObservation observation) {
        super(workContext, GameObservation.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());

        if (observation.remoteId != null) {
            setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/observation/" + observation.remoteId);
            setHttpMethod(HttpMethod.PUT);
        } else {
            setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/observation");
            setHttpMethod(HttpMethod.POST);
        }
        setHttpEntity(JsonUtils.createJsonStringEntity(observation));
    }
}
