package fi.riista.mobile.network;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.listener.BaseWorkAsyncTaskListener;
import fi.vincit.androidutilslib.task.TextTask;
import fi.vincit.androidutilslib.task.WorkAsyncTask;

/**
 * Login to backend
 */
public class LoginTask extends TextTask {

    private boolean mIsSilent = false;

    protected LoginTask(WorkContext workContext, String username, String password, boolean isSilent) {
        super(workContext);
        mIsSilent = isSilent;
        setHttpMethod(HttpMethod.POST);
        setBaseUrl(AppConfig.BASE_ADDRESS + "/login");
        addParameter("username", username);
        addParameter("password", password);
        addParameter("client", "mobileapiv2");
        setCookieStore(GameDatabase.getInstance().getCookieStore());

        String version = Utils.getAppVersionName(workContext.getContext());
        if (version != null) {
            setHeader("mobileClientVersion", version);
        }
        setHeader("platform", "android");
        setHeader("device", android.os.Build.MODEL);

        addTaskListener(new BaseWorkAsyncTaskListener() {
            @Override
            public void onEnd(WorkAsyncTask task) {
                //When login attempt ends, try to fetch observation metadata and SRVA parameters
                ObservationMetadataHelper.getInstance().fetchMetadata();
                SrvaParametersHelper.getInstance().fetchParameters();

                sendRegistrationToServer();
            }
        });
    }

    private void sendRegistrationToServer() {
        final FirebaseInstanceId instance = FirebaseInstanceId.getInstance();
        final String instanceId = instance.getId();
        final String instanceToken = instance.getToken();

        Log.d(Utils.LOG_TAG, "Current instanceId is: " + instanceId);
        Log.d(Utils.LOG_TAG, "Current registration id is: " + instanceToken);

        if (instanceToken == null) {
            return;
        }

        WorkContext workContext = RiistaApplication.getInstance().getWorkContext();
        RegisterPushDeviceTask registerPushDeviceTask = new RegisterPushDeviceTask(workContext, instanceToken);
        registerPushDeviceTask.start();
    }

    @Override
    protected void onError() {
        if (!mIsSilent) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getWorkContext().getContext());
            if (getHttpStatusCode() == 418) {
                builder.setMessage(getWorkContext().getContext().getResources().getString(R.string.version_outdated));
            } else if (getHttpStatusCode() == 403) {
                builder.setMessage(getWorkContext().getContext().getResources().getString(R.string.login_failed));
            } else {
                builder.setMessage(getWorkContext().getContext().getResources().getString(R.string.connecting_failed));
            }
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.show();
        }
        loginFailed();
    }

    public void loginFailed() {
        // Override this method
    }

    @Override
    protected void onFinishText(String text) {
        // Override this method
    }
}
