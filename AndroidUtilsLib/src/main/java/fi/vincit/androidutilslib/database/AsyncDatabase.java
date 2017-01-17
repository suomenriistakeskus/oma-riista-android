/*
 * Copyright (C) 2017 Vincit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.vincit.androidutilslib.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.WorkAsyncTask;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Asynchronous SQLite database. Because you should never use Android databases in the
 * main thread this class provides basic functionality for asynchronous database
 * operations. All operations execute in a worker thread but their order is guaranteed.
 * If you use an explicit WorkContext with any operation it will control the lifetime of
 * the operation, for example if you use an Activity's WorkContext if the user navigates
 * away from the activity and it is destroyed any pending operations will be cancelled.
 * 
 * Similar to WorkAsyncTask database operations run in a background thread (and have 
 * a word "Async" in them) but notification callbacks (onFinish() , onError() etc.) are 
 * called in the UI thread.
 * 
 * There should be only one AsyncDatabase-instance for each physical database. Singleton
 * pattern is a common way to do this.
 * 
 * The class is abstract, you must implement onAsyncCreate() and onAsyncUpgrade(). For an example
 * see how they are used with SQLiteOpenHelper (it is used behind the covers).
 */
public abstract class AsyncDatabase {
    
    public static class AsyncWrite {
        protected void onAsyncWrite(SQLiteDatabase db) { }
        protected void onFinish() { }
        protected void onError() { }
    }
    
    public static class AsyncRead {
        protected void onAsyncRead(SQLiteDatabase db) { }
        protected void onFinish() { }
        protected void onError() { }
    }
    
    public static class AsyncQuery extends AsyncSqlQuery {
        public AsyncQuery(String query, String ... parameters) {
            super(query, parameters);
        }
        protected void onAsyncQuery(AsyncCursor cursor) { }
        protected void onFinish() { }
        protected void onError() { }
    }
    
    public static class AsyncAggregate extends AsyncSqlQuery  {
        public AsyncAggregate(String query, String ... parameters) {
            super(query, parameters);
        }
        protected void onFinish(Double[] values) { }
        protected void onError() { }
    }
    
    /**
     * Handle to an asynchronous operation.
     */
    public static class AsyncHandle {
        private WorkAsyncTask mTask;
        
        public AsyncHandle(WorkAsyncTask task) {
            mTask = task;
        }
        
        /**
         * Cancels the asynchronous operation. If the operation has not yet
         * started it will be run. If it is running it will be run but any
         * callbacks will not be called. Does nothing if the operation has 
         * already finished. Can be called multiple times, calls after the
         * first are ignored.
         */
        public void cancel() {
            mTask.cancel();
        }
        
        /**
         * Waits until the operation has ended. This can mean success, error
         * or cancellation. This method is only useful if you want to perform
         * synchronous operations in a thread that is not an UI thread.
         */
        public void waitForEnd() {
            mTask.waitForEnd(0);
        }
    }
    
    private AsyncDatabaseHelper mHelper;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private WorkContext mWorkContext;
    
    /**
     * Creates a new AsyncDatabase(). The name and version specify the database file
     * name and it's version number.
     */
    public AsyncDatabase(Context context, String name, int version) {
        mWorkContext = WorkContext.createFreeLocalWorkContext(context);
        
        mHelper = new AsyncDatabaseHelper(context.getApplicationContext(), name, 
                new AsyncCursorFactory(), version);
    }
    
    /**
     * Internal helper method.
     */
    public AsyncCursor rawQuery(SQLiteDatabase db, String sql, String ... parameters) {
        return (AsyncCursor)db.rawQuery(sql, parameters);
    }
    
    /**
     * Start a generic write and/or read operation. The operations will be performed
     * inside a transaction and will be rollbacked if there is an error (the operation
     * throws an exception).
     */
    public AsyncHandle write(WorkContext workContext, final AsyncWrite operation) {
        WorkAsyncTask task = new WorkAsyncTask(workContext) {
            @Override
            protected void onAsyncRun() throws Exception {
                SQLiteDatabase db = mHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    operation.onAsyncWrite(db);
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
            }
            
            @Override
            protected void onFinish() {
                operation.onFinish();
            }
            
            @Override
            protected void onError() {
                operation.onError();
            }
        };
        task.startOnExecutor(mExecutor);
        return new AsyncHandle(task);
    }
    
    /**
     * Start a write without an explicit WorkContext.
     */
    public AsyncHandle write(AsyncWrite operation) {
        return write(mWorkContext, operation);
    }
    
    /**
     * Start a generic read operation.
     */
    public AsyncHandle read(WorkContext workContext, final AsyncRead operation) {
        WorkAsyncTask task = new WorkAsyncTask(workContext) {
            @Override
            protected void onAsyncRun() throws Exception {
                SQLiteDatabase db = mHelper.getReadableDatabase();
                operation.onAsyncRead(db);
            }
            
            @Override
            protected void onFinish() {
                operation.onFinish();
            }
            
            @Override
            protected void onError() {
                operation.onError();
            }
        };
        task.startOnExecutor(mExecutor);
        return new AsyncHandle(task);
    }
    
    /**
     * Start a generic read operation without an explicit WorkContext.
     */
    public AsyncHandle read(final AsyncRead operation) {
        return read(mWorkContext, operation);
    }
    
    /**
     * Start a query operation. The argument SQL will be executed and the resulting
     * cursor will be passed to the operations's callback. The cursor will be
     * automatically closed later.
     */
    public AsyncHandle query(WorkContext workContext, final AsyncQuery operation) {
        WorkAsyncTask task = new WorkAsyncTask(workContext) {
            @Override
            protected void onAsyncRun() throws Exception {
                SQLiteDatabase db = mHelper.getReadableDatabase();
                
                AsyncCursor c = (AsyncCursor)db.rawQuery(operation.getQuery(), operation.getParameters());
                try {
                    operation.onAsyncQuery(c);
                }
                finally {
                    if (!c.isClosed()) {
                        c.close();
                    } 
                }
            }
            
            @Override
            protected void onFinish() {
                operation.onFinish();
            }
            
            @Override
            protected void onError() {
                operation.onError();
            }
        };
        task.startOnExecutor(mExecutor);
        return new AsyncHandle(task);
    }
    
    /**
     * Start a query operation without an explicit WorkContext.
     */
    public AsyncHandle query(AsyncQuery operation) {
        return query(mWorkContext, operation);
    }
    
    /**
     * Start an aggregation operation. This means the query should use aggregate functions,
     * for example "SELECT COUNT(*), SUM(value) FROM item". The resulting array of two items 
     * will be passed to the operation's onFinish().
     */
    public AsyncHandle aggregate(WorkContext workContext, final AsyncAggregate operation) {
        return query(workContext, new AsyncQuery(operation.getQuery(), operation.getParameters()) {
            private Double[] mResults = null;
            
            @Override
            protected void onAsyncQuery(AsyncCursor cursor) { 
                if (cursor.moveToFirst()) {
                    final int columns = cursor.getColumnCount();
                    
                    mResults = new Double[columns];
                    for (int i=0; i < columns; ++i) {
                        if (cursor.isNull(i)) {
                            mResults[i] = null;
                        }
                        else {
                            mResults[i] = cursor.getDouble(i);
                        }
                    }
                }
                else {
                    throw new RuntimeException("No cursor results for aggregate()");
                }
            }
            
            @Override
            protected void onFinish() {
                operation.onFinish(mResults);
            }
            
            @Override
            protected void onError() {
                operation.onError();
            }
        });
    }
    
    /**
     * Start an aggregation operation without an explicit WorkContext.
     */
    public AsyncHandle aggregate(final AsyncAggregate operation) {
        return aggregate(mWorkContext, operation);
    }
    
    /**
     * Called when the database should be created. Same as SQLiteOpenHelper.onCreate() except
     * this will be called in a background thread.
     */
    protected abstract void onAsyncCreate(SQLiteDatabase db);
    
    /**
     * Called when the database should be updated. Same as SQLiteOpenHelper.onUpgrade() except
     * this will be called in a background thread.
     */
    protected abstract void onAsyncUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    
    private class AsyncDatabaseHelper extends SQLiteOpenHelper {
        public AsyncDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            AsyncDatabase.this.onAsyncCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            AsyncDatabase.this.onAsyncUpgrade(db, oldVersion, newVersion);
        }
    }
}
