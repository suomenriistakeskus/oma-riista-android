package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class ListAvailableShootingTestOfficialsForEventTask extends JsonListTask<ShootingTestOfficial> {
    protected ListAvailableShootingTestOfficialsForEventTask(WorkContext context, long eventId) {
        super(context, ShootingTestOfficial.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/event/" + eventId + "/qualifyingofficials/");
    }
}
