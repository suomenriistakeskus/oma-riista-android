package fi.riista.mobile.firebase;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.network.RegisterPushDeviceTask;
import fi.vincit.androidutilslib.context.WorkContext;

public class RiistaFirebaseInstanceIdService  extends FirebaseInstanceIdService {
    private static final String TAG = RiistaFirebaseInstanceIdService.class.getSimpleName();

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        if (refreshedToken != null) {
            sendRegistrationToServer(refreshedToken);
        }
    }

    private void sendRegistrationToServer(final String token) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (GameDatabase.getInstance().credentialsStored(RiistaApplication.getInstance())) {
                    WorkContext workContext = RiistaApplication.getInstance().getWorkContext();
                    new RegisterPushDeviceTask(workContext, token).start();
                }
            }
        });
    }
}
