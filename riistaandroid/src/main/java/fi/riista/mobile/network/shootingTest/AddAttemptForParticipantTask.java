package fi.riista.mobile.network.shootingTest;

import java.util.HashMap;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestResult;
import fi.riista.mobile.models.shootingTest.ShootingTestType;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class AddAttemptForParticipantTask extends TextTask {
    protected AddAttemptForParticipantTask(WorkContext context,
                                           long participantId,
                                           int participantRev,
                                           ShootingTestType type,
                                           ShootingTestResult result,
                                           int hits, // [0..4]
                                           String note) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/participant/" + participantId + "/attempt");
        setHttpMethod(HttpMethod.PUT);

        Map<String, Object> body = new HashMap<>();
        body.put("participantId", participantId);
        body.put("participantRev", participantRev);
        body.put("type", type.name());
        body.put("result", result.name());
        body.put("hits", hits);
        body.put("note", note);
        setJsonEntity(body);
    }
}
