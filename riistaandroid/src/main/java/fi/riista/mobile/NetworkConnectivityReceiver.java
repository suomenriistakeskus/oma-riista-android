package fi.riista.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.GameDatabase.SyncMode;
import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Receiver for changed network status
 * If user is in automatic sync mode, events are sent when network connection has been established
 */
public class NetworkConnectivityReceiver extends BroadcastReceiver {

    private WorkContext mWorkContext = null;

    public NetworkConnectivityReceiver(WorkContext context) {
        mWorkContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GameDatabase database = GameDatabase.getInstance();
        SyncMode syncMode = database.getSyncMode(context);
        if (GameDatabase.getInstance().hasSentEventsForFirstTime() && syncMode == SyncMode.SYNC_AUTOMATIC) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                GameDatabase.getInstance().sendLocalEvents(mWorkContext, true);
            }
        }
    }
}
