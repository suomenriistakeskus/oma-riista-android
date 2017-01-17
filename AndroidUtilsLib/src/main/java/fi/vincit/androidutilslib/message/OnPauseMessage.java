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
package fi.vincit.androidutilslib.message;

/**
 * This event is automatically sent by WorkContext when it's
 * OnPause() is called.
 */
public class OnPauseMessage 
{ 
    private boolean mFinishing;
    
    public OnPauseMessage(boolean finishing)
    {
        mFinishing = finishing;
    }
    
    /**
     * If the work context is associated with an Activity this
     * will tell if it is finishing. Otherwise this will
     * be false.
     */
    public boolean isFinishing()
    {
        return mFinishing;
    }
}
