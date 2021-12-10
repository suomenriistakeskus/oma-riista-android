package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class ReopenShootingTestEventTask extends TextTask {
    protected ReopenShootingTestEventTask(WorkContext context, long eventId) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/event/" + eventId + "/reopen");
        setHttpMethod(HttpMethod.POST);
    }
}
