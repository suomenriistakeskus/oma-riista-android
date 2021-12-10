package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Preload permit information for user
 */
public class PreloadPermitsTask extends TextTask {

    protected PreloadPermitsTask(WorkContext context) {
        super(context);
        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/preloadPermits");
    }

    @Override
    protected void onFinishText(String text) {
        // Override this method
    }

    @Override
    protected void onError() {
        // Override this method
    }
}
