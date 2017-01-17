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
package fi.vincit.androidutilslib.activity;

import android.support.v4.app.FragmentActivity;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;
import android.app.Activity;
import android.os.Bundle;

/**
 * An example FragmentActivity which has a global WorkContext attached to it. You can inherit
 * you activities from this class or do these same things manually
 * in your activities if it is not possible (for example if you use
 * MapActivity or some other third party library which require you 
 * to subclass their activities).
 */
public class WorkActivity extends FragmentActivity implements WorkContextProvider
{
    private WorkContext mWorkContext;
    
    @Override
    public WorkContext getWorkContext() {
        return mWorkContext;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Create the WorkContext before calling super so that restored fragments etc. can use it.
        mWorkContext = new WorkContext();
        mWorkContext.onCreateGlobal(this);
        
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mWorkContext.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mWorkContext.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorkContext.onDestroy();
    }
}
