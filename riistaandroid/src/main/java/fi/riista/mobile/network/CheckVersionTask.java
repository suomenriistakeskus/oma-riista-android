package fi.riista.mobile.network;

import android.os.Build;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public class CheckVersionTask extends TextTask {

    public CheckVersionTask(WorkContext context) {
        super(context);
        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setHeader("platform", "android");
        setHeader("osversion", String.valueOf(Build.VERSION.SDK_INT));
        setBaseUrl(AppConfig.getBaseUrl() + "/release");
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
