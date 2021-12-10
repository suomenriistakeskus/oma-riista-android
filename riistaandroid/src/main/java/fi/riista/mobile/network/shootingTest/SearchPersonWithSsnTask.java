package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class SearchPersonWithSsnTask extends JsonObjectTask<ShootingTestSearchPersonResult> {
    protected SearchPersonWithSsnTask(WorkContext context, long eventId, String ssn) {
        super(context, ShootingTestSearchPersonResult.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/event/" + eventId + "/findperson/ssn");
        setHttpMethod(HttpMethod.POST);

        // POST method -> goes to request body
        addParameter("ssn", ssn);
    }
}
