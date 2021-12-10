package fi.riista.mobile.network.shootingTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public abstract class StartShootingTestEventTask extends TextTask {
    protected StartShootingTestEventTask(WorkContext context,
                                         long calendarEventId,
                                         Long shootingTestEventId,
                                         List<Long> occupationIds) {
        super(context);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/calendarevent/" + calendarEventId + "/open");
        setHttpMethod(HttpMethod.POST);

        Map<String, Object> body = new HashMap<>();
        body.put("calendarEventId", String.valueOf(calendarEventId));
        body.put("shootingTestEventId", String.valueOf(shootingTestEventId));
        body.put("occupationIds", occupationIds);
        setJsonEntity(body);
    }
}
