package fi.riista.mobile.vectormap

import android.content.Context
import android.util.Log
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.signature.ObjectKey
import fi.riista.mobile.utils.VECTOR_CACHE_SIZE
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class VectorTileCache(context: Context) {

    private class CacheDirProvider(val context: Context) : DiskLruCacheFactory.CacheDirectoryGetter {
        override fun getCacheDirectory(): File = File(context.cacheDir, CACHE_DIR)
    }

    private class CacheWriter(val data: ByteArray) : DiskCache.Writer {
        override fun write(file: File): Boolean {
            return try {
                file.writeBytes(data)
                true
            } catch (e: IOException) {
                false
            }
        }
    }

    private val cache = DiskLruCacheFactory(CacheDirProvider(context), VECTOR_CACHE_SIZE).build()

    fun get(key: String): File? {
        val file = cache?.get(ObjectKey(key))
        if (file != null && file.isExpired()) {
            try {
                file.delete()
            } catch (e: IOException) {
                Log.w(TAG, "Deleting a file caused IOException")
            }
            return null
        }
        return file
    }

    fun put(key: String, data: ByteArray) {
        val writer = CacheWriter(data)
        cache?.put(ObjectKey(key), writer)
    }

    fun clear() {
        cache?.clear()
    }

    companion object {
        const val TAG = "VectorTileCache"
        const val CACHE_DIR = "vector_image_disk_cache"
    }
}

private val MAX_CACHED_FILE_AGE: Long = TimeUnit.DAYS.toMillis(30)

private fun File.isExpired(): Boolean {
    val age = System.currentTimeMillis() - lastModified()
    return age > MAX_CACHED_FILE_AGE
}
