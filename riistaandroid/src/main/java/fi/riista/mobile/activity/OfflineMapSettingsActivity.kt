package fi.riista.mobile.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.load.engine.cache.DiskCache
import fi.riista.mobile.R
import fi.riista.mobile.di.GlideApp
import fi.riista.mobile.utils.CacheClearTracker
import fi.riista.mobile.utils.IMAGE_CACHE_SIZE
import fi.riista.mobile.utils.VECTOR_CACHE_SIZE
import fi.riista.mobile.vectormap.VectorTileCache
import java.io.File
import java.util.concurrent.Executors

class OfflineMapSettingsActivity : BaseActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var backgroundCacheUsageTextView: TextView
    private lateinit var clearBackgroundCacheButton: AppCompatButton
    private lateinit var layerCacheUsageTextView: TextView
    private lateinit var clearLayerCacheButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_map_settings)

        setCustomTitle(getString(R.string.map_settings_offline_map_settings))

        backgroundCacheUsageTextView = findViewById(R.id.tv_background_cache_usage)
        clearBackgroundCacheButton = findViewById<AppCompatButton>(R.id.btn_clear_background_map_cache).also { button ->
            button.isEnabled = false
            button.setOnClickListener {
                button.isEnabled = false
                val glide = GlideApp.get(applicationContext)
                glide.clearMemory()
                executor.execute {
                    glide.clearDiskCache()
                }
                handler.post {
                    CacheClearTracker.markBackgroundCacheToBeCleared()
                    updateBackgroundCacheSize()
                }
            }
        }
        layerCacheUsageTextView = findViewById(R.id.tv_layer_cache_usage)
        clearLayerCacheButton = findViewById<AppCompatButton>(R.id.btn_clear_layer_cache).also { button ->
            button.isEnabled = false
            button.setOnClickListener {
                button.isEnabled = false
                val cache = VectorTileCache(applicationContext)
                executor.execute {
                    cache.clear()
                }
                handler.post {
                    CacheClearTracker.markVectorCachesToBeCleared()
                    updateLayerCacheSize()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundCacheSize()
        updateLayerCacheSize()
    }

    private fun updateBackgroundCacheSize() {
        updateCacheSize(
            cache = DiskCache.Factory.DEFAULT_DISK_CACHE_DIR,
            maxCacheSize = IMAGE_CACHE_SIZE,
            textView = backgroundCacheUsageTextView,
            button = clearBackgroundCacheButton,
        )
    }

    private fun updateLayerCacheSize() {
        updateCacheSize(
            cache = VectorTileCache.CACHE_DIR,
            maxCacheSize = VECTOR_CACHE_SIZE,
            textView = layerCacheUsageTextView,
            button = clearLayerCacheButton,
        )
    }

    private fun updateCacheSize(cache: String, maxCacheSize: Long, textView: TextView, button: AppCompatButton) {
        executor.execute {
            var size = 0L
            val cacheDir = File(cacheDir, cache)
            try {
                size = calculateDirSize(cacheDir)
            } catch (ex: RuntimeException) {
                Log.w(TAG, "Unable to calculate cache size: ${ex.message}")
            }

            handler.post {
                // When cache is full, the calculated size can be a few kilobytes larger than specified max size.
                // If that is the case, then use max size as size so it doesn't look like it is buggy.
                val sizeText = Formatter.formatFileSize(this, size.coerceAtMost(maxCacheSize))
                val maxSizeText = Formatter.formatFileSize(this, maxCacheSize)
                textView.text = getString(
                    R.string.offline_map_settings_cache_usage,
                    sizeText,
                    maxSizeText)
                button.isEnabled = true
            }
        }
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null) {
            return 0
        }

        if (!dir.isDirectory) {
            return dir.length()
        }

        var result: Long = 0
        val children = dir.listFiles()
            children?.forEach { child ->
                result += calculateDirSize(child)
            }
        return result
    }

    companion object {
        const val TAG = "OfflineMapSettingsAct"
    }
}
