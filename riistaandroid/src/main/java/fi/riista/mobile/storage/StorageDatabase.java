package fi.riista.mobile.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase;

import static java.util.Objects.requireNonNull;

public class StorageDatabase {

    public interface UpdateListener {
        void onUpdate();

        void onError();
    }

    public interface AnnouncementsListener {
        void onFinish(List<Announcement> announcements);
    }

    private static StorageDatabase sInstance;

    private final UserInfoStore mUserInfoStore;

    public static void init(@NonNull final Context context, @NonNull final UserInfoStore userInfoStore) {
        StorageDatabaseHelper.init(context);

        sInstance = new StorageDatabase(userInfoStore);
    }

    public static StorageDatabase getInstance() {
        return sInstance;
    }

    private static ContentValues announcementToValues(final Announcement item, final String username) {
        final ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("remoteId", item.remoteId);
        values.put("status", 0);
        values.put("content", JsonUtils.objectToJson(item));
        return values;
    }

    private StorageDatabase(@NonNull final UserInfoStore userInfoStore) {
        mUserInfoStore = requireNonNull(userInfoStore);
    }

    public void updateAnnouncements(@NonNull final List<Announcement> announcements,
                                    @NonNull final UpdateListener listener) {

        final StorageDatabaseHelper helper = StorageDatabaseHelper.getInstance();

        helper.write(new AsyncDatabase.AsyncWrite() {
            protected void onAsyncWrite(final SQLiteDatabase db) {
                // Delete all existing announcements. No need to use username here.
                db.delete("announcement", null, null);

                final String username = getUsername();

                for (final Announcement item : announcements) {
                    final ContentValues values = announcementToValues(item, username);
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

    public void updateAnnouncement(@NonNull final Announcement item, @Nullable final UpdateListener listener) {
        final StorageDatabaseHelper helper = StorageDatabaseHelper.getInstance();

        helper.write(new AsyncDatabase.AsyncWrite() {
            @Override
            protected void onAsyncWrite(final SQLiteDatabase db) {
                final String username = getUsername();
                final ContentValues values = announcementToValues(item, username);

                // First try to update existing entry with a specific remoteId
                final int changes = db.update("announcement", values, "remoteId = ? AND userName = ?",
                        new String[]{"" + item.remoteId, username});
                if (changes == 0) {
                    // Row does not exist, so insert it now.
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

    public void fetchAnnouncements(@NonNull final AnnouncementsListener listener) {
        final StorageDatabaseHelper helper = StorageDatabaseHelper.getInstance();

        helper.query(new AsyncDatabase.AsyncQuery("SELECT * FROM announcement WHERE userName = ?", getUsername()) {
            private final ArrayList<Announcement> mResults = new ArrayList<>();

            protected void onAsyncQuery(final AsyncCursor cursor) {
                while (cursor.moveToNext()) {
                    mResults.add(JsonUtils.jsonToObject(cursor.getString("content"), Announcement.class));
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

    private String getUsername() {
        return mUserInfoStore.getUsernameOrEmpty();
    }
}
