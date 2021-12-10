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
package fi.vincit.androidutilslib.util;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Helper class for storing persistent settings to disk in a JSON form.
 * Each named settings object should be a singleton.
 */
public class JsonSettings<T> {
    
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String SETTINGS_DIRECTORY = ".jsonsettings";
    
    private static class SettingsData<T> {
        @JsonProperty("version")
        public int version = 1;
        
        @JsonProperty("value")
        public T value;
    }
    
    private final ObjectMapper mMapper;
    private final File mSettingsFile;

    private SettingsData<T> mData;
    
    /**
     * Creates a new settings object with the given unique name. The name should
     * be a valid file name. The default value will be used to initialize settings
     * if this is the first time settings is created or if loading from the file fails.
     */
    public JsonSettings(Context context, final ObjectMapper mapper, String name, T defaultValue) {
        if (defaultValue == null) {
            throw new RuntimeException("JsonSettings default value must not be null");
        }
        mMapper = mapper;
        mSettingsFile = new File(new File(context.getApplicationContext().getFilesDir(), SETTINGS_DIRECTORY), name);
        
        mData = newData(defaultValue, 1);
        
        reload();
    }
    
    /**
     * Returns a reference to the internal settings data which can be modified or read.
     * Calling reload() can cause all previous objects obtained from this method
     * to be stale.
     */
    public synchronized T get() {
        return mData.value;
    }
    
    /**
     * Makes the argument value as current internal representation (which means it
     * will be returned by get()'s) and saves the data to a file.
     */
    public synchronized void save(T value) {
        SettingsData<T> data = newData(value, mData.version);
        
        try {
            String content = mMapper.writeValueAsString(data);
            FileUtils.write(mSettingsFile, content, DEFAULT_ENCODING);
            mData = data;
        }
        catch (Exception e) {
            throw new RuntimeException("Can't save JSON settings into a file", e);
        }
    }
    
    /**
     * Attempts to reload the internal data from the disk. All previous values 
     * obtained from get() can become stale.
     */
    public synchronized void reload() {
        if (!mSettingsFile.exists()) {
            return;
        }
        
        try {
            String content = FileUtils.readFileToString(mSettingsFile, DEFAULT_ENCODING);
            if (content != null && content.length() > 0) {
                JavaType type = mMapper.getTypeFactory().constructParametricType(SettingsData.class, mData.value.getClass());
                SettingsData<T> data = mMapper.readValue(content, type);
                if (data != null && data.value != null) {
                    mData = data;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the settings file location in the disk.
     */
    public synchronized File getFile() {
        return mSettingsFile;
    }
    
    private SettingsData<T> newData(T value, int version) {
        SettingsData<T> data = new SettingsData<T>();
        data.version = version;
        data.value = value;
        return data;
    }
    
}
