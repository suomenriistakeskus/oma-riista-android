package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class DeleteAttemptForParticipantTask extends TextTask {
    protected DeleteAttemptForParticipantTask(WorkContext context, long attemptId) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/attempt/" + attemptId);
        setHttpMethod(HttpMethod.DELETE);
    }
}
