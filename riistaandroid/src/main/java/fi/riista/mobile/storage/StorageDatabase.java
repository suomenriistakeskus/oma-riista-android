package fi.riista.mobile.storage;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.utils.BaseDatabase;
import fi.riista.mobile.utils.JsonUtils;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase;

public class StorageDatabase {

    public interface UpdateListener {
        void onUpdate();
        void onError();
    }

    public interface AnnouncementsListener {
        void onFinish(List<Announcement> announcements);
    }

    private static StorageDatabase sInstance;

    public static void init(Context context) {
        StorageDatabaseHelper.init(context);

        sInstance = new StorageDatabase();
    }

    public static StorageDatabase getInstance() {
        return sInstance;
    }

    private ContentValues announcementToValues(Announcement item, String userName) {
        ContentValues values = new ContentValues();
        values.put("username", userName);
        values.put("remoteId", item.remoteId);
        values.put("status", 0);
        values.put("content", JsonUtils.objectToJson(item));
        return values;
    }

    public void updateAnnouncements(final List<Announcement> annoucements, final UpdateListener listener) {
        StorageDatabaseHelper helper = StorageDatabaseHelper.getInstance();
        final String userName = BaseDatabase.getUsername();
        helper.write(new AsyncDatabase.AsyncWrite() {
            protected void onAsyncWrite(SQLiteDatabase db) {
                //Delete all existing announcements. No need to use userName here.
                db.delete("announcement", null, null);

                for (Announcement item : annoucements) {
                    ContentValues values = announcementToValues(item, userName);
                    db.replace("announcement", null, values);
                }
            }

            protected void onFinish() {
                listener.onUpdate();
            }

            protected void onError() {
                listener.onError();
            }
        });
    }

    public void updateAnnouncement(final Announcement item, final UpdateListener listener) {
        StorageDatabaseHelper helper = StorageDatabaseHelper.getInstance();
        final String userName = BaseDatabase.getUsername();

        helper.write(new AsyncDatabase.AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                ContentValues values = announcementToValues(item, userName);

                //First try to update existing entry with a specific remoteId
                int changes = db.update("announcement", values, "remoteId = ? AND userName = ?",
                        new String[] {"" + item.remoteId, userName});
                if (changes == 0) {
                    //Row does not exist, so insert it now.
                    db.insert("announcement", null, values);
                }
            }

            @Override
            protected void onFinish() {
                if (listener != null)
                    listener.onUpdate();
            }

            @Override
            protected void onError() {
                if (listener != null)
                    listener.onError();
            }
        });
    }

    public void fetchAnnouncements(final AnnouncementsListener listener) {
        StorageDatabaseHelper helper = StorageDatabaseHelper.getInstance();
        final String userName = BaseDatabase.getUsername();

        helper.query(new AsyncDatabase.AsyncQuery("SELECT * FROM announcement WHERE userName = ?", userName) {
            private ArrayList<Announcement> mResults = new ArrayList<Announcement>();

            protected void onAsyncQuery(AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    Announcement ann = JsonUtils.jsonToObject(cursor.getString("content"), Announcement.class);
                    mResults.add(ann);
                }
            }

            protected void onFinish() {
                listener.onFinish(mResults);
            }

            protected void onError() {
                listener.onFinish(mResults);
            }
        });
    }

}
