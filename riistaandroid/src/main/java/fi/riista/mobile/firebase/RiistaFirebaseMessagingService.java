package fi.riista.mobile.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.LoginActivity;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.storage.StorageDatabase;
import fi.riista.mobile.utils.JsonUtils;

public class RiistaFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = RiistaFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!GameDatabase.getInstance().credentialsStored(this)) {
            //We are not logged in, do nothing.
            return;
        }

        String json = remoteMessage.getData().get("announcement");
        if (json != null) {
            Announcement ann = JsonUtils.jsonToObject(json, Announcement.class);
            if (ann != null) {
                Log.d(TAG, "Saved announcement to database: " + ann.remoteId);
                StorageDatabase.getInstance().updateAnnouncement(ann, null);
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Got notification");
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
        else {
            Log.d(TAG, "No notification payload");
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.SHOW_ANNOUNCEMENTS_EXTRA, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
