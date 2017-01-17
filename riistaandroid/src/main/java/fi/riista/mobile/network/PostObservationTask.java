package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.utils.JsonUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class PostObservationTask extends JsonObjectTask<GameObservation> {
    protected PostObservationTask(WorkContext workContext, GameObservation observation) {
        super(workContext, GameObservation.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());

        if (observation.remoteId != null) {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/observation/" + observation.remoteId);
            setHttpMethod(HttpMethod.PUT);
        } else {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/observation");
            setHttpMethod(HttpMethod.POST);
        }
        setHttpEntity(JsonUtils.createJsonStringEntity(observation));
    }
}
