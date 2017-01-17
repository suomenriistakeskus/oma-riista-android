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

import fi.vincit.androidutilslib.context.WorkContext;
import android.util.Log;

/**
 * A lightweight timer class. Timers are associated with a WorkContext 
 * and share it's lifetime. If the WorkContext gets destroyed all it's
 * timers are automatically canceled.
 * 
 * If trigger on start and autopause are both true onTrigger()
 * can be called more often than the set delay. For example
 * if user moves rapidly back and forth between two activities
 * timers are restarted every time it's activity is resumed.
 */
public abstract class WorkTimer implements Runnable
{
    private WorkContext mWorkContext;
    private int mDelayMs;
    private boolean mRepeat = false;
    private boolean mAutopause = false;
    private boolean mTriggerOnStart = false;
    
    public WorkTimer(WorkContext context, int delayMs)
    {
        mWorkContext = context;
        mDelayMs = delayMs;
    }
    
    public boolean getTriggerOnStart()
    {
        return mTriggerOnStart;
    }
    
    public void setTriggerOnStart(boolean trigger)
    {
        mTriggerOnStart = trigger;
    }
    
    public int getDelay()
    {
        return mDelayMs;
    }
    
    public void setDelay(int delayMs)
    {
        mDelayMs = delayMs;
    }
    
    public boolean getRepeat()
    {
        return mRepeat;
    }
    
    /**
     * Set if the timer should repeat every time
     * it has been triggered.
     */
    public void setRepeat(boolean repeat)
    {
        mRepeat = repeat;
    }
    
    public boolean getAutopause()
    {
        return mAutopause;
    }
    
    /**
     * Autopause determines if the timer is automatically paused
     * when the associated work context is paused (usually this
     * means that the work context's activity has been paused).
     */
    public void setAutopause(boolean autopause)
    {
        mAutopause = autopause;
    }
    
    /**
     * Starts or restarts the timer.
     */
    public void start()
    {
        stop();
        
        mWorkContext.addTimer(this, mTriggerOnStart);
    }
    
    /**
     * Stops the timer.
     */
    public void stop()
    {
        mWorkContext.removeTimer(this);
    }
    
    @Override
    public final void run() 
    {
        onTrigger();
        
        mWorkContext.removeTimer(this);
        
        if (mRepeat) {
            mWorkContext.addTimer(this, false);
        }
    }
    
    /**
     * Override this to perform timed actions. This is called
     * in the UI thread.
     */
    public abstract void onTrigger();
    
}
