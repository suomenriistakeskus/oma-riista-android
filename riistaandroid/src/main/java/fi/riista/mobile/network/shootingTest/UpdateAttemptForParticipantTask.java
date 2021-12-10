package fi.riista.mobile.network.shootingTest;

import java.util.HashMap;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestResult;
import fi.riista.mobile.models.shootingTest.ShootingTestType;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class UpdateAttemptForParticipantTask extends TextTask {
    protected UpdateAttemptForParticipantTask(WorkContext context,
                                              long attemptId,
                                              int rev,
                                              long participantId,
                                              int participantRev,
                                              ShootingTestType type,
                                              ShootingTestResult result,
                                              int hits, // [0..5]
                                              String note) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/attempt/" + attemptId);
        setHttpMethod(HttpMethod.POST);

        Map<String, Object> body = new HashMap<>();
        body.put("rev", rev);
        body.put("participantId", participantId);
        body.put("participantRev", participantRev);
        body.put("type", type.name());
        body.put("result", result.name());
        body.put("hits", hits);
        body.put("note", note);
        setJsonEntity(body);
    }
}
