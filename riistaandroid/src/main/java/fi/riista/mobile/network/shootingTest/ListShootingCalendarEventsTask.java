package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class ListShootingCalendarEventsTask extends JsonListTask<ShootingTestCalendarEvent> {
    protected ListShootingCalendarEventsTask(WorkContext context) {
        super(context, ShootingTestCalendarEvent.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/calendarevents");
    }
}
