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
package fi.vincit.androidutilslib.network;

import java.util.Date;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.client.utils.DateUtils;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * This interceptor can be used to force CachingHttpClient to cache
 * responses that are not otherwise cacheable by rewriting the
 * Expires-header and removing some other cache-related headers.
 */
public class ForceCacheInterceptor implements HttpResponseInterceptor {
    
    private long mCacheMaxAgeMillis;
    
    public ForceCacheInterceptor(final long cacheMaxAgeMillis) {
        mCacheMaxAgeMillis = cacheMaxAgeMillis;
    }
    
    private String getNewExpiresDate() {
        Date expires = new Date(new Date().getTime() + mCacheMaxAgeMillis);
        return "" + DateUtils.formatDate(expires);
    }
    
    @Override
    public void process(final HttpResponse response, final HttpContext context) {
        response.removeHeaders("Expires");
        response.removeHeaders("Pragma");
        response.removeHeaders("Cache-Control");
        
        // CachingHttpClient requires this header to cache requests.
        response.addHeader("Expires", getNewExpiresDate());
    }
}
