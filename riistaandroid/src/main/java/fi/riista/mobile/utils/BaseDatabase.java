package fi.riista.mobile.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.models.user.UserInfo;
import fi.vincit.androidutilslib.database.AsyncCursor;
import fi.vincit.androidutilslib.database.AsyncDatabase;
import fi.vincit.androidutilslib.database.AsyncDatabase.AsyncWrite;
import fi.vincit.androidutilslib.util.JsonSerializator;

public class BaseDatabase<T> {

    public interface SaveListener {
        void onSaved(long id);

        void onError();
    }

    public interface DeleteListener {
        void onDelete();

        void onError();
    }

    private static ObjectMapper sMapper = JsonSerializator.createDefaultMapper();

    protected void deleteEntry(AsyncDatabase db, final String table, final Long localId, final Long remoteId,
                               final boolean force, final DeleteListener listener) {
        db.write(new AsyncWrite() {
            @Override
            protected void onAsyncWrite(SQLiteDatabase db) {
                if (remoteId != null && localId != null && !force) {
                    //Synced, mark as deleted
                    ContentValues values = new ContentValues();
                    values.put("deleted", true);
                    db.update(table, values, "localId = ? ", new String[]{localId.toString()});
                } else if (localId != null) {
                    //Local observation, not synced yet.
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

    public static String getUsername() {
        UserInfo info = AppPreferences.getUserInfo(RiistaApplication.getInstance());
        if (info != null && info.getUsername() != null) {
            return info.getUsername();
        }
        return "";
    }

    protected static AsyncCursor query(SQLiteDatabase db, String query, String... args) {
        return (AsyncCursor) db.rawQuery(query, args);
    }

    protected static String objectToJson(Object obj) {
        try {
            return sMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
