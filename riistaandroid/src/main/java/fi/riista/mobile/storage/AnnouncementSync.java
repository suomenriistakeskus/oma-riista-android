package fi.riista.mobile.storage;


import android.util.Log;

import java.util.List;

import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.network.ListAnnouncementsTask;

public class AnnouncementSync {

    public interface AnnouncementSyncListener {
        void onFinish();
    }

    public void sync(final AnnouncementSyncListener listener) {
        ListAnnouncementsTask task = new ListAnnouncementsTask(RiistaApplication.getInstance().getWorkContext()) {
            @Override
            protected void onFinishObjects(List<Announcement> results) {
                StorageDatabase.getInstance().updateAnnouncements(results, new StorageDatabase.UpdateListener() {
                    @Override
                    public void onUpdate() {
                        listener.onFinish();
                    }

                    @Override
                    public void onError() {
                        listener.onFinish();
                    }
                });
            }

            @Override
            protected void onError() {
                Log.d("Announcements", "Sync error: " + getError().getMessage());

                listener.onFinish();
            }
        };
        task.start();
    }
}
