package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class SearchPersonWithHunterNumberTask extends JsonObjectTask<ShootingTestSearchPersonResult> {
    protected SearchPersonWithHunterNumberTask(WorkContext context, long eventId, String hunterNumber) {
        super(context, ShootingTestSearchPersonResult.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/event/" + eventId + "/findhunter/hunternumber");
        setHttpMethod(HttpMethod.POST);

        addParameter("hunterNumber", hunterNumber);
    }
}
