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

import java.io.IOException;
import java.util.Date;

import fi.vincit.httpclientandroidlib.HttpException;
import fi.vincit.httpclientandroidlib.HttpResponse;
import fi.vincit.httpclientandroidlib.HttpResponseInterceptor;
import fi.vincit.httpclientandroidlib.client.utils.DateUtils;
import fi.vincit.httpclientandroidlib.protocol.HttpContext;

/**
 * This interceptor can be used to force CachingHttpClient to cache
 * responses that are not otherwise cacheable by rewriting the
 * Expires-header and removing some other cache-related headers.
 */
public class ForceCacheInterceptor implements HttpResponseInterceptor {
    
    long mCacheMaxAgeMillis;
    
    public ForceCacheInterceptor(long cacheMaxAgeMillis) {
        mCacheMaxAgeMillis = cacheMaxAgeMillis;
    }
    
    private String getNewExpiresDate() {
        Date expires = new Date(new Date().getTime() + mCacheMaxAgeMillis);
        return "" + DateUtils.formatDate(expires);
    }
    
    @Override
    public void process(HttpResponse response, HttpContext context)  throws HttpException, IOException {
        response.removeHeaders("Expires");
        response.removeHeaders("Pragma");
        response.removeHeaders("Cache-Control");
        
        //CachingHttpClient requires this header to cache requests.
        response.addHeader("Expires", getNewExpiresDate());
    }
}
