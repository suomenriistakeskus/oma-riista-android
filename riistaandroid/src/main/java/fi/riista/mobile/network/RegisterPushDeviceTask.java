package fi.riista.mobile.network;

import java.util.HashMap;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

public class RegisterPushDeviceTask extends TextTask {
    private String mPushToken;

    public RegisterPushDeviceTask(WorkContext workContext, String pushToken) {
        super(workContext);

        mPushToken = pushToken;

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/push/register");
        setHttpMethod(HttpMethod.POST);

        HashMap<String, Object> object = new HashMap<>();
        object.put("platform", "ANDROID");
        object.put("deviceName", android.os.Build.MODEL);
        object.put("pushToken", pushToken);
        object.put("clientVersion", Utils.getAppVersionName());
        setJsonEntity(object);
    }

    @Override
    protected void onFinishText(final String text) {
        Utils.LogMessage("Push token sent to server: " + mPushToken);
    }

    @Override
    protected void onError() {
        Utils.LogMessage("Push token sending failed: " + getError().getMessage());
    }
}
