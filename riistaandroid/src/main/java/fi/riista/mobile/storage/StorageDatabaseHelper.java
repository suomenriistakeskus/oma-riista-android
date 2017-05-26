package fi.riista.mobile.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import fi.vincit.androidutilslib.database.AsyncDatabase;

class StorageDatabaseHelper extends AsyncDatabase {

    private static final String DATABASE_NAME = "storagedatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_ANNOUNCEMENT =
            "CREATE TABLE IF NOT EXISTS announcement(" +
                "username TEXT," +
                "remoteId INT," +
                "status INT," +
                "content TEXT" +
            ")";

    private static StorageDatabaseHelper sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new StorageDatabaseHelper(context);
        }
    }

    public static StorageDatabaseHelper getInstance() {
        return sInstance;
    }

    private StorageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    protected void onAsyncCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ANNOUNCEMENT);
    }

    @Override
    protected void onAsyncUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS announcement");

        onAsyncCreate(db);
    }
}
