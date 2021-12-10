package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class GetShootingTestCalendarEventTask extends JsonObjectTask<ShootingTestCalendarEvent> {
    protected GetShootingTestCalendarEventTask(WorkContext context, long calendarEventId) {
        super(context, ShootingTestCalendarEvent.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/calendarevent/" + calendarEventId);
    }
}
