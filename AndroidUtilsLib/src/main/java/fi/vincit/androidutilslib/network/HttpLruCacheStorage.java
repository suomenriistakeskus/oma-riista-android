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

import androidx.collection.LruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.cache.HttpCacheStorage;
import cz.msebera.android.httpclient.client.cache.HttpCacheUpdateCallback;
import cz.msebera.android.httpclient.impl.client.cache.CacheConfig;
import cz.msebera.android.httpclient.impl.client.cache.DefaultHttpCacheEntrySerializer;
import fi.vincit.androidutilslib.android.lrucache.DiskLruCache;
import fi.vincit.androidutilslib.android.lrucache.DiskLruCache.Editor;
import fi.vincit.androidutilslib.android.lrucache.DiskLruCache.Snapshot;
import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;
import fi.vincit.androidutilslib.util.HashDigest;

/**
 * Disk (and optionally memory) based two-level LRU cache for caching network requests.
 * 
 * Normally only disk cache is used, but you can enable memory caching
 * by calling createMemoryCache(). This creates a in-memory cache
 * above the disk cache to speed fetches.
 */
public class HttpLruCacheStorage implements HttpCacheStorage {

    private static final int APP_VERSION = 1;
    private static final int BUFFER_SIZE = 1024 * 8;

    public static CacheConfig createDefaultCacheConfig() {
        return CacheConfig.custom()
                .setMaxCacheEntries(500) // Not used by this cache implementation.
                .setMaxObjectSize(AndroidUtilsLibConfig.Cache.DEFAULT_MAX_CACHED_OBJECT_SIZE)
                .setHeuristicCachingEnabled(true)
                .setHeuristicDefaultLifetime(AndroidUtilsLibConfig.Cache.DEFAULT_CACHE_LIFETIME_SECONDS)
                .setSharedCache(true)
                .build();
    }
    
    private static class MemoryCacheEntry {
        public byte[] data;
    }

    private File mDirectory;
    private long mMaxDiskSize;
    private DiskLruCache mDiskCache;
    private LruCache<String, MemoryCacheEntry> mMemoryCache;
    private CacheConfig mConfig;
    private DefaultHttpCacheEntrySerializer mSerializer = new DefaultHttpCacheEntrySerializer();
    private HashDigest mDigest = new HashDigest(HashDigest.MD5);

    /**
     * Creates a HttpLruCacheStorage with no backing. Call createDiskCache() or createMemoryCache() 
     * to create a storage for data. You can also call both, in which case the memory cache
     * might speed things up and avoid disk lookups.
     */
    public HttpLruCacheStorage(CacheConfig config) {
        if (config == null) {
            config = createDefaultCacheConfig();
        }
        mConfig = config;
    }

    private DiskLruCache openDiskCache() throws IOException {
        return DiskLruCache.open(mDirectory, APP_VERSION, 1, mMaxDiskSize);
    }
    
    private LruCache<String, MemoryCacheEntry> openMemoryCache(int maxSize) {
        LruCache<String, MemoryCacheEntry> cache = new LruCache<String, MemoryCacheEntry>(maxSize) {
            @Override
            protected int sizeOf(String key, MemoryCacheEntry value) {
                return value.data.length;
           }
        };
        return cache;
    }
    
    /**
     * Creates a disk based LRU cache into the given directory.
     */
    public synchronized void createDiskCache(File directory, long maxSizeBytes) throws IOException {
        if (diskCacheAvailable()) {
            // Already exists.
            return;
        }
        
        if (maxSizeBytes > 0) {
            mDirectory = directory;
            mMaxDiskSize = maxSizeBytes;

            mDiskCache = openDiskCache();
        }
    }
    
    /**
     * Creates a secondary memory based LRU cache. This can be called
     * only once, next calls do not do anything.
     */
    public synchronized void createMemoryCache(int maxSizeBytes) {
        if (mMemoryCache == null && maxSizeBytes > 0) {
            mMemoryCache = openMemoryCache(maxSizeBytes);
        }
    }
    
    /**
     * Set the maximum size of the disk cache. If the new size
     * is smaller than the current size cache is resized to
     * the new size.
     */
    public synchronized void setMaxDiskSize(long maxSize) {
        if (diskCacheAvailable()) {
            mMaxDiskSize = maxSize;
            mDiskCache.setMaxSize(maxSize);
        }
    }

    /**
     * Deletes all cached items from the disk and memory.
     */
    public synchronized void clearAll() {
        if (memoryCacheAvailable()) {
            mMemoryCache.evictAll();
        }
        
        if (diskCacheAvailable()) {
            try {
                mDiskCache.delete();

                mDiskCache = openDiskCache();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }  
        }
    }

    public synchronized CacheConfig getConfig() {
        return mConfig;
    }

    private String keyToHash(String key) {
        mDigest.reset();
        mDigest.update(key);
        return mDigest.hexDigest();
    }
    
    private boolean memoryCacheAvailable() {
        return mMemoryCache != null;
    }

    private boolean diskCacheAvailable() {
        return mDiskCache != null && !mDiskCache.isClosed();
    }
    
    /**
     * Attempts to convert a stream to HttpCacheEntry, returns null on failure.
     */
    private HttpCacheEntry streamToEntry(InputStream stream) {
        HttpCacheEntry entry = null;
        try {
            entry = mSerializer.readFrom(stream);
        } 
        catch (IOException e) {
            
        }
        return entry;
    }
    
    /**
     * Attempts to convert a HttpCacheEntry into a byte array, returns
     * null on failure.
     */
    private byte[] entryToByteArray(HttpCacheEntry entry) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mSerializer.writeTo(entry, out);
        } 
        catch (IOException e) {
            return null;
        }
        return out.toByteArray();
    }

    @Override
    public synchronized HttpCacheEntry getEntry(String key) throws IOException {
        HttpCacheEntry result = null;

        String keyHash = keyToHash(key);
        
        if (memoryCacheAvailable()) {
            MemoryCacheEntry value = mMemoryCache.get(keyHash);
            if (value != null) {
                result = streamToEntry(new ByteArrayInputStream(value.data));
            }
        }
        
        if (diskCacheAvailable() && result == null) {
            // Not found from memory cache, try disk.
            Snapshot value = mDiskCache.get(keyHash);
            if (value != null) {
                result = streamToEntry(new BufferedInputStream(value.getInputStream(0), BUFFER_SIZE));
                value.close();
                
                if (result != null) {
                    // We got it from disk but it is not in memory, so
                    // put it to the memory cache now.
                    putMemoryEntry(keyHash, result);
                }
            }
        }
        return result;
    }

    private void putMemoryEntry(String keyHash, HttpCacheEntry entry) {
        if (memoryCacheAvailable()) {
            byte[] data = entryToByteArray(entry);
            if (data != null) {
                MemoryCacheEntry cacheEntry = new MemoryCacheEntry();
                cacheEntry.data = data;
                mMemoryCache.put(keyHash, cacheEntry);
            }
        }
    }
    
    @Override
    public synchronized void putEntry(String key, HttpCacheEntry entry) throws IOException {
        String keyHash = keyToHash(key);
        
        putMemoryEntry(keyHash, entry);
        
        if (diskCacheAvailable()) {
            Editor edit = mDiskCache.edit(keyHash);
            if (edit != null) {
                try {
                    OutputStream out = new BufferedOutputStream(edit.newOutputStream(0), BUFFER_SIZE);
                    mSerializer.writeTo(entry, out);
                    out.close();

                    edit.commit();
                } 
                finally {
                    edit.abortUnlessCommitted();
                }
            }
        }
    }

    @Override
    public synchronized void removeEntry(String key) throws IOException {
        String keyHash = keyToHash(key);
        
        if (memoryCacheAvailable()) {
            mMemoryCache.remove(keyHash);
        }
        
        if (diskCacheAvailable()) {
            mDiskCache.remove(keyHash); 
        }
    }

    @Override
    public synchronized void updateEntry(String key, HttpCacheUpdateCallback callback) throws IOException {
        if (memoryCacheAvailable() || diskCacheAvailable()) {
            HttpCacheEntry oldEntry = getEntry(key);
            HttpCacheEntry newEntry = callback.update(oldEntry);

            if (newEntry != null) {
                putEntry(key, newEntry);
            } 
            else {
                removeEntry(key);
            } 
        }
    }

}
