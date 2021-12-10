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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;

/**
 * Thread safe cookie store implementation.
 */
public class SynchronizedCookieStore implements CookieStore {

    private CookieStore mCookieStore;

    /**
     * Constructs a cookie store using a basic cookie store.
     */
    public SynchronizedCookieStore() {
        mCookieStore = new BasicCookieStore();
    }

    /**
     * Construct a cookies store using an existing store. The argument
     * store will be used.
     */
    public SynchronizedCookieStore(CookieStore store) {
        mCookieStore = store;
    }

    /**
     * Returns the internal, not thread safe cookie store.
     */
    public synchronized CookieStore getCookieStore() {
        return mCookieStore;
    }

    @Override
    public synchronized void addCookie(Cookie cookie) {
        mCookieStore.addCookie(cookie);
    }

    @Override
    public synchronized void clear() {
        mCookieStore.clear();
    }

    @Override
    public synchronized boolean clearExpired(Date date) {
        return mCookieStore.clearExpired(date);
    }

    @Override
    public synchronized List<Cookie> getCookies() {
        return new ArrayList<>(mCookieStore.getCookies());
    }

    @Override
    public synchronized String toString() {
        return mCookieStore.toString();
    }
}
