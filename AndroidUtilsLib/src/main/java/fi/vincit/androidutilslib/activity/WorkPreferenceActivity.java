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

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * See WorkActivity.
 */
public class WorkPreferenceActivity extends PreferenceActivity implements WorkContextProvider 
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
