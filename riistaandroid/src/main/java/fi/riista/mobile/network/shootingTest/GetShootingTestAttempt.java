package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class GetShootingTestAttempt extends JsonObjectTask<ShootingTestAttemptDetailed> {
    protected GetShootingTestAttempt(WorkContext context, long attemptId) {
        super(context, ShootingTestAttemptDetailed.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/attempt/" + attemptId);
    }
}
