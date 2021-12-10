package fi.riista.mobile.network.shootingTest;

import java.util.HashMap;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.SelectedShootingTestTypes;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class AddShootingTestParticipantTask extends TextTask {

    protected AddShootingTestParticipantTask(WorkContext context,
                                             long eventId,
                                             String hunterNumber,
                                             boolean mooseTestIntended,
                                             boolean bearTestIntended,
                                             boolean roeDeerTestIntended,
                                             boolean bowTestIntended) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/event/" + eventId + "/participant");
        setHttpMethod(HttpMethod.POST);

        SelectedShootingTestTypes types = new SelectedShootingTestTypes();
        types.mooseTestIntended = mooseTestIntended;
        types.bearTestIntended = bearTestIntended;
        types.roeDeerTestIntended = roeDeerTestIntended;
        types.bowTestIntended = bowTestIntended;

        Map<String, Object> body = new HashMap<>();
        body.put("hunterNumber", hunterNumber);
        body.put("selectedTypes", types);

        setJsonEntity(body);
    }
}
