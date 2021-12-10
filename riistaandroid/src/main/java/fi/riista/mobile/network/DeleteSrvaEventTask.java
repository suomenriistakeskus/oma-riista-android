package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class DeleteSrvaEventTask extends TextTask {
    protected DeleteSrvaEventTask(WorkContext context, SrvaEvent event) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/srva/srvaevent/" + event.remoteId);
        setHttpMethod(HttpMethod.DELETE);
    }
}
