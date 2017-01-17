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
package fi.vincit.androidutilslib;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.DownloadTask;
import android.app.Application;
import android.content.Context;

/**
 * Global shared stuff that AndroidUtilsLib uses. It is not currently required to
 * initialize the library but it is recommended to avoid some possible issues
 * on some OS versions.
 */
public class AndroidUtilsLib {

    private static Application sApplication;
    
    /**
     * Initialize AndroidUtilsLib. This must be called from your Application.onCreate()
     * before you try to use any library features. Remember to add your application class
     * to your AndroidManifest.xml file.
     */
    public static void onAppCreate(Application app) {
        sApplication = app;
        
        /**
         * Create and load the first task in the main thread. This is required because
         * each thread can has it's own class loader which can fail to load
         * some classes properly.
         */
        @SuppressWarnings("unused")
        DownloadTask task = new DownloadTask(WorkContext.createFreeLocalWorkContext(app), "");
    }
    
    /**
     * Returns the application context that was used to initialize AndroidUtilsLib.
     */
    public static Context getAppContext() {
        if (sApplication == null) {
            throw new RuntimeException("Application context is null, you must call AndroidUtilsLib.onApplicationCreate() first");
        }
        return sApplication;
    }
    
}
