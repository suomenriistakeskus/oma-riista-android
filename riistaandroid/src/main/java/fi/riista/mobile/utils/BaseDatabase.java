package fi.riista.mobile.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;

import static java.util.Objects.requireNonNull;

public class BaseDatabase {

    public interface SaveListener {
        void onSaved(long id);

        void onError();
    }

    public interface DeleteListener {
        void onDelete();

        void onError();
    }

    private final UserInfoStore mUserInfoStore;

    protected BaseDatabase(@NonNull final UserInfoStore userInfoStore) {
        this.mUserInfoStore = requireNonNull(userInfoStore);
    }

    protected void deleteEntry(final AsyncDatabase db, final String table, final Long localId, final Long remoteId,
                               final boolean force, final DeleteListener listener) {

        db.write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(final SQLiteDatabase db) {
                if (remoteId != null && localId != null && !force) {
                    // Synced, mark as deleted
                    final ContentValues values = new ContentValues();
                    values.put("deleted", true);
                    db.update(table, values, "localId = ? ", new String[]{localId.toString()});
                } else if (localId != null) {
                    // Local observation, not synced yet.
                    db.delete(table, "localId = ?", new String[]{localId.toString()});
                } else {
                    Utils.LogMessage("Can't delete entry, no ids set");
                }
            }

            @Override
            protected void onFinish() {
                listener.onDelete();
            }

            @Override
            protected void onError() {
                listener.onError();
            }
        });
    }

    protected String getUsername() {
        return mUserInfoStore.getUsernameOrEmpty();
    }

    protected static AsyncCursor query(final SQLiteDatabase db, final String query, final String... args) {
        return (AsyncCursor) db.rawQuery(query, args);
    }
}
