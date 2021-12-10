package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class ListAvailableShootingTestOfficialsForRhyTask extends JsonListTask<ShootingTestOfficial> {
    protected ListAvailableShootingTestOfficialsForRhyTask(WorkContext context, long rhyId) {
        super(context, ShootingTestOfficial.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/rhy/" + rhyId + "/officials/");
    }
}
