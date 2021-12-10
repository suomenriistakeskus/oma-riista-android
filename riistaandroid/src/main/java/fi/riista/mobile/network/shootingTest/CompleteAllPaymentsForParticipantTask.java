package fi.riista.mobile.network.shootingTest;

import java.util.HashMap;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class CompleteAllPaymentsForParticipantTask extends TextTask {
    protected CompleteAllPaymentsForParticipantTask(WorkContext context, long participantId, int rev) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/participant/" + participantId + "/payment");
        setHttpMethod(HttpMethod.PUT);

        Map<String, Object> body = new HashMap<>();
        body.put("rev", rev);
        setJsonEntity(body);
    }
}
