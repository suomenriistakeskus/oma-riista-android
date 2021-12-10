package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.Permit;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

/**
 * Get permit information for permit number.
 */
public class CheckPermitNumberTask extends JsonObjectTask<Permit> {

    protected CheckPermitNumberTask(final WorkContext context,
                                    final String permitNumber,
                                    final int harvestSpecVersion) {

        super(context, AppConfig.getBaseUrl() + "/gamediary/checkPermitNumber", Permit.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setHttpMethod(HttpMethod.POST);

        addParameter("permitNumber", permitNumber);
        addParameter("harvestSpecVersion", String.valueOf(harvestSpecVersion));
    }

    @Override
    protected void onFinishObject(Permit result) {
        // Override this method
    }

    @Override
    protected void onError() {
        // Override this method
    }
}
