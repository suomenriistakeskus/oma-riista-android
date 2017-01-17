package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.utils.JsonUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class PostSrvaEventTask extends JsonObjectTask<SrvaEvent> {
    protected PostSrvaEventTask(WorkContext context, SrvaEvent event) {
        super(context, SrvaEvent.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());

        if (event.remoteId != null) {
            setBaseUrl(AppConfig.BASE_URL + "/srva/srvaevent/" + event.remoteId);
            setHttpMethod(HttpMethod.PUT);
        } else {
            setBaseUrl(AppConfig.BASE_URL + "/srva/srvaevent");
            setHttpMethod(HttpMethod.POST);
        }
        setHttpEntity(JsonUtils.createJsonStringEntity(event));
    }
}
