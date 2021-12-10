package fi.riista.mobile.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.activity.LoginActivity;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.network.RegisterPushDeviceTask;
import fi.riista.mobile.storage.StorageDatabase;
import fi.riista.mobile.utils.JsonUtils;

public class RiistaFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "RiistaFirebaseMessaging";

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onNewToken(final String s) {
        Log.d(TAG, "Refreshed token: " + s);

        if (s != null) {
            sendRegistrationToServer(s);
        }
    }

    private void sendRegistrationToServer(final String token) {
        mMainHandler.post(() -> {
            final RiistaApplication app = RiistaApplication.getInstance();

            if (app.getCredentialsStore().isCredentialsSaved()) {
                new RegisterPushDeviceTask(app.getWorkContext(), token).start();
            }
        });
    }

    // Called when a notification message is delivered while application is in the foreground.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!RiistaApplication.getInstance().getCredentialsStore().isCredentialsSaved()) {

            final String json = remoteMessage.getData().get("announcement");

            if (json != null) {
                final Announcement ann = JsonUtils.jsonToObject(json, Announcement.class, true);

                if (ann != null) {
                    Log.d(TAG, "Saved announcement to database: " + ann.remoteId);

                    StorageDatabase.getInstance().updateAnnouncement(ann, null);
                }
            }

            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(final RemoteMessage remoteMessage) {
        final RemoteMessage.Notification notification = remoteMessage.getNotification();

        if (notification != null) {
            Log.d(TAG, "Got notification");
            sendNotification(notification.getTitle(), notification.getBody());
        } else {
            Log.d(TAG, "No notification payload");
        }
    }

    private void sendNotification(final String title, final String messageBody) {
        final Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.SHOW_ANNOUNCEMENTS_EXTRA, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
