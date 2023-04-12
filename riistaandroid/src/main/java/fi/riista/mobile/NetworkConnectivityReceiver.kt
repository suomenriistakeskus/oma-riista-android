package fi.riista.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import fi.riista.mobile.sync.AppSync

/**
 * Receiver for changed network status.
 *
 * If user is in automatic sync mode, events are sent when network connection has been established.
 */
class NetworkConnectivityReceiver(private val appSync: AppSync) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected) {
            appSync.enableSyncPrecondition(AppSync.SyncPrecondition.CONNECTED_TO_NETWORK)
        } else {
            // Don't disable sync preconditions as this behaviour has not been fully tested in all possible
            // cases (e.g. low/occasional network connectivity deep in the forest). The reasoning here is that
            // it is better to try syncing without network than to disable sync precondition and not sync at all
            // if connected event is not received.
            // - it seems that some of the code related to receiving network connectivity information has
            //   been deprecated (e.g. intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION) in MainActivity)
            // -> there's a small risk that connectivity intent is not received
            //appSync.disableSyncPrecondition(AppSync.SyncPrecondition.CONNECTED_TO_NETWORK);
        }
    }
}
