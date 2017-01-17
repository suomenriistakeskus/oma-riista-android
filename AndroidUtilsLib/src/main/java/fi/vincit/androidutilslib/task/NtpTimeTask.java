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
package fi.vincit.androidutilslib.task;

import android.os.SystemClock;
import fi.vincit.androidutilslib.android.AndroidSntpClient;
import fi.vincit.androidutilslib.context.WorkContext;

/**
 * Task for fetching NTP (Network Time Protocol) time from a time server.
 * Default configuration is reasonable and tries several servers with
 * some retrying if there are errors.
 * <p>
 * If the task succeeds onFinishTime() will be called with the result.
 */
public abstract class NtpTimeTask extends WorkAsyncTask {

    /**
     * NTP result received from a server and synchronized to system time.
     */
    public static class NtpTime {
        //The time computed from the NTP transaction.
        public long ntpTime;
        //The reference clock value (value of SystemClock.elapsedRealtime())
        //corresponding to the NTP time.
        public long ntpTimeReference;
        //The round trip time of the NTP transaction in milliseconds
        public long roundTripTime;
        
        /**
         * Computes the current system time using the NTP time as a reference.
         * Returns the time in milliseconds since 1970 (so you can
         * construct a Java Date-object from the value.)
         */
        public long getNow() {
            return ntpTime + SystemClock.elapsedRealtime() - ntpTimeReference;
        }
    }
    
    public static final int DEFAULT_TIMEOUT_MS = 5 * 1000;
    
    public static final String DEFAULT_TIME_SERVERS[] = {
        "pool.ntp.org",
        "time.nist.gov",
    };
    
    private NtpTime mResult;
    private String[] mTimeServers = DEFAULT_TIME_SERVERS;
    private int mTimeoutMs = DEFAULT_TIMEOUT_MS;

    public NtpTimeTask(WorkContext context) {
        super(context);
        
        //Try each server 2 times by default.
        setTryCount(2);
    }
    
    public String[] getTimeServers() {
        return mTimeServers;
    }

    /**
     * Set the time servers that will be queried in order until
     * sync succeeds or the task runs out of retries. By default
     * all servers will be tried at max 2 times, you can change this
     * by calling setTryCount().
     */
    public void setTimeServers(String[] timeServers) {
        mTimeServers = timeServers;
    }

    public int getTimeoutMs() {
        return mTimeoutMs;
    }

    /**
     * Set the server socket timeout in milliseconds.
     */
    public void setTimeoutMs(int timeoutMs) {
        mTimeoutMs = timeoutMs;
    }

    private NtpTime fetchNtpTime() {
        for (String server : mTimeServers) {
            AndroidSntpClient client = new AndroidSntpClient();
            if (client.requestTime(server, mTimeoutMs)) {
                log("NTP sync ok: " + server);
                
                NtpTime result = new NtpTime();
                result.ntpTime = client.getNtpTime();
                result.ntpTimeReference = client.getNtpTimeReference();
                result.roundTripTime = client.getRoundTripTime();
                return result;
            }
            else {
                log("NTP sync failed: " + server);
            }
        }
        return null;
    }
    
    @Override
    protected final void onAsyncRun() throws Exception {
        mResult = fetchNtpTime();
        if (mResult == null) {
            throw new RuntimeException("NTP time sync failed");
        }
    }
    
    @Override
    protected final void onFinish() {
        onFinishTime(mResult);
    }
    
    /**
     * Called in the UI thread if the NTP time has been successfully fetched.
     */
    protected abstract void onFinishTime(NtpTime time);
}
