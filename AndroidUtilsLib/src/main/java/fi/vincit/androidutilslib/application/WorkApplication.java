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
package fi.vincit.androidutilslib.application;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;
import android.app.Application;
import android.util.Log;

/**
 * An example Application which has an observer WorkContext attached to it. You 
 * can inherit you application from this class or do these same things manually
 * if you need an observer work context.
 */
public class WorkApplication extends Application implements WorkContextProvider
{
    private static WorkContext mAppWorkContext;
    
    @Override
    public void onCreate() 
    {
        super.onCreate();
        
        mAppWorkContext = new WorkContext();
        mAppWorkContext.onCreateObserver(getApplicationContext());
    }
    
    /**
     * Returns the work context associated with the application. This
     * can be used anywhere in the application process, although
     * usually more fine grained work control should be used.
     */
    @Override
    public WorkContext getWorkContext() {
        return mAppWorkContext;
    }
}
