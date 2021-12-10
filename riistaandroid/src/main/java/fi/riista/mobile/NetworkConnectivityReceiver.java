package fi.riista.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import fi.riista.mobile.sync.AppSync;

import static java.util.Objects.requireNonNull;

/**
 * Receiver for changed network status
 * If user is in automatic sync mode, events are sent when network connection has been established
 */
public class NetworkConnectivityReceiver extends BroadcastReceiver {

    private AppSync appSync;

    public NetworkConnectivityReceiver(@NonNull final AppSync appSync) {
        this.appSync = requireNonNull(appSync);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            appSync.syncImmediatelyIfAutomaticSyncEnabled();
        }
    }
}
