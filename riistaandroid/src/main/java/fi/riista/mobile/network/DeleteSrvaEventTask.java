package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class DeleteSrvaEventTask extends TextTask {
    protected DeleteSrvaEventTask(WorkContext context, SrvaEvent event) {
        super(context);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/srva/srvaevent/" + event.remoteId);
        setHttpMethod(HttpMethod.DELETE);
    }
}
