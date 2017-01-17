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
package fi.vincit.androidutilslib.config;

/**
 * Parameters that can be adjusted to configure how several features
 * of this library work. These are set to reasonable defaults so
 * there is no need to change these if you don't need to.
 *
 * These should be set from your Application.onCreate() before you use
 * anything from this library.
 */
public class AndroidUtilsLibConfig {

    public static class Task {
        /**
         * Enables tasks to log information about their execution. You can set
         * this to false if you don't want to see those messages in the log.
         * This setting can be changed at any time.
         */
        public static boolean ENABLE_LOGGING = true;
        
        /**
         * Default log tag that is used by all the tasks to output debug
         * log messages. If this in null the class name of the task
         * will be used.
         */
        public static String DEFAULT_LOG_TAG = null;
        
        /**
         * Default user agent string send by NetworkTask and friends.
         */
        public static String DEFAULT_USER_AGENT = "Android mobile";
    }
    
    /**
     * Settings related to network caching. Most of these only affect the
     * internal default cache, if you create additional caches you must
     * configure them manually.
     */
    public static class Cache {
        /**
         * Default cache directory name. This directory is created into the
         * application cache directory.
         */
        public static String DEFAULT_CACHE_DIR_NAME = ".taskcache";
        
        /**
         * Default cache size (in bytes) for HttpLruCacheStorage that is shared with
         * all tasks that use NetworkTask. Items are saved on the disk
         * in the application cache directory. Set to 0 to disable.
         */
        public static long DEFAULT_DISK_CACHE_SIZE = 1048576 * 10; // 10 megabytes
        
        /**
         * Default cache size (in bytes) for in-memory cache. This is the smaller cache
         * above the disk cache. Set to 0 to disable.
         */
        public static int DEFAULT_MEMORY_CACHE_SIZE = 0; // Disabled

        /**
         * Default maximum size (in bytes) for items that can be cached.
         */
        public static int DEFAULT_MAX_CACHED_OBJECT_SIZE = 1024 * 512; // 512 kilobytes
        
        /**
         * If the server does not specify cache headers basic heuristics will be
         * used to determine how long items should be cached. This is also
         * used in some other places (like in ImageTask).
         */
        public static long DEFAULT_CACHE_LIFETIME_SECONDS = 60 * 30; // 30 minutes
        
        /**
         * Configuration for bitmap caching in WebImageView. This is basically
         * a third level cache above disk and in-memory caches and avoids
         * thread switch, data transfer and encoding overhead.
         */
        public static class Bitmap {
            /**
             * Default cache size (in bytes) that WebImageView uses to cache bitmaps
             * in memory. Set to 0 to disable.
             */
            public static int DEFAULT_BITMAP_CACHE_SIZE = 1048576 * 1; // 1 megabyte
            
            /**
             * Default maximum size (in bytes) for bitmaps that can be cached.
             */
            public static int DEFAULT_MAX_BITMAP_SIZE = 1024 * 256; // 256 kilobytes
        }
    }

}
