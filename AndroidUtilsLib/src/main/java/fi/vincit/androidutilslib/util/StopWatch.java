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

import android.os.SystemClock;

public class StopWatch {
    
    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    
    private long mTimeMs = 0;
    private long mPeriodMs;
    private boolean mForceElapsed = false;
    
    public StopWatch(long periodMs, boolean resetPeriod) {
        if (resetPeriod) {
            reset();
        }
        setPeriod(periodMs);
    }
    
    public void setPeriod(long periodMs) {
        mPeriodMs = periodMs;
    }
    
    public void forceElapsed() {
        mForceElapsed = true;
    }
    
    public void reset() {
        mTimeMs = SystemClock.elapsedRealtime();
        mForceElapsed = false;
    }
    
    public boolean hasElapsed() {
        return SystemClock.elapsedRealtime() >= (mTimeMs + mPeriodMs) || mForceElapsed;
    }
    
    public boolean resetIfElapsed() {
        if (hasElapsed()) {
            reset();
            return true;
        }
        return false;
    }

}
