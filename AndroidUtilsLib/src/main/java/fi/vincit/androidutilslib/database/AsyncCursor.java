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

import java.util.HashMap;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class AsyncCursor extends SQLiteCursor {
    private HashMap<String, Integer> mColumnMap = new HashMap<String, Integer>();
    
    public AsyncCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(db, driver, editTable, query);
    }
    
    @Override
    public void close() {
        mColumnMap.clear();
        mColumnMap = null;
        super.close();
    }
    
    private int getIndex(String columnName) {
        Integer index = mColumnMap.get(columnName);
        if (index == null) {
            index = getColumnIndexOrThrow(columnName);
            mColumnMap.put(columnName, index);
        }
        return index;
    }
    
    public Boolean getBool(String columnName, Boolean fallback) {
        int index = getIndex(columnName);
        if (isNull(index)) {
            return fallback;
        }
        return getInt(index) != 0;
    }
    
    public Boolean getBool(String columnName) {
        return getBool(columnName, null);
    }
    
    public Integer getInt(String columnName, Integer fallback) {
        int index = getIndex(columnName);
        if (isNull(index)) {
            return fallback;
        }
        return getInt(index);
    }
    
    public Integer getInt(String columnName) {
        return getInt(columnName, null);
    }
    
    public Long getLong(String columnName, Long fallback) {
        int index = getIndex(columnName);
        if (isNull(index)) {
            return fallback;
        }
        return getLong(index);
    }
    
    public Long getLong(String columnName) {
        return getLong(columnName, null);
    }
    
    public Double getDouble(String columnName, Double fallback) {
        int index = getIndex(columnName);
        if (isNull(index)) {
            return fallback;
        }
        return getDouble(index);
    }
    
    public Double getDouble(String columnName) {
        return getDouble(columnName, null);
    }
    
    public String getString(String columnName, String fallback) {
        int index = getIndex(columnName);
        if (isNull(index)) {
            return fallback;
        }
        return getString(index);
    }
    
    public String getString(String columnName) {
        return getString(columnName, null);
    }
    
    public byte[] getBlob(String columnName, byte[] fallback) {
        int index = getIndex(columnName);
        if (isNull(index)) {
            return fallback;
        }
        return getBlob(index);
    }
    
    public byte[] getBlob(String columnName) {
        return getBlob(columnName, null);
    }
    
}
