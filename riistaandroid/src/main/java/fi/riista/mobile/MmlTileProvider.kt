package fi.riista.mobile

import android.content.Context
import android.util.Log
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.FutureTarget
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import fi.riista.mobile.di.GlideApp
import fi.riista.mobile.utils.AppPreferences.MapTileSource
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Tile provider for fetching MML map tiles from Riista map server
 */
internal class MmlTileProvider(
    private val width: Int,
    private val height: Int,
    private val context: Context,
) : TileProvider {

    // URL format for currently selected map type
    private var urlFormat = MML_TOPOGRAPHIC_TILE_URL_FORMAT

    fun setMapType(mapType: MapTileSource) {
        urlFormat = when (mapType) {
            MapTileSource.MML_AERIAL -> MML_AERIAL_TILE_URL_FORMAT
            MapTileSource.MML_BACKGROUND -> MML_BACKGROUND_TILE_URL_FORMAT
            else -> MML_TOPOGRAPHIC_TILE_URL_FORMAT
        }
    }

    @Synchronized
    private fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
        val url = try {
            URL(String.format(urlFormat, zoom, x, tmsConvert(y, zoom)))
        } catch (e: MalformedURLException) {
            throw AssertionError(e)
        }
        return url
    }

    private fun tmsConvert(y: Int, zoom: Int): Int {
        return (1 shl zoom) - y - 1
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        var tile: Tile? = null
        try {
            val tileUrl = getTileUrl(x, y, zoom)
            val headers = LazyHeaders.Builder()
                .addHeader(REFERER_KEY, REFERER_VALUE)
                .build()
            val glideUrl = GlideUrl(tileUrl, headers)

            var file = loadResource(glideUrl, true)
            if (file.isExpired()) {
                try {
                    file.delete()
                } catch (e: IOException) {
                    Log.w(TAG, "Deleting a file caused IOException")
                }
                file = loadResource(glideUrl, false)
            }
            tile = Tile(width, height, file.readBytes())

            // Catch all possible exceptions and return null, otherwise the same tile is not requested again,
            // even fetching it might work in future, when e.g. network connection works again.
        } catch (e: IOException) {
            Log.i(TAG, "Unable to get Tile")
        } catch (e: AssertionError) {
            Log.w(TAG, "Invalid tile url, x=$x, y=$y, zoom=$zoom")
        } catch (e: GlideException) {
            Log.i(TAG, "Unable to get Tile, GlideException")
        } catch (e: ExecutionException) {
            Log.i(TAG, "Unable to get Tile, ExecutionException")
        } catch (e: InterruptedException) {
            Log.i(TAG, "Unable to get Tile, InterruptedException")
        } catch (e: CancellationException) {
            Log.i(TAG, "Unable to get Tile, CancellationException")
        }
        return tile
    }

    private fun loadResource(glideUrl: GlideUrl, allowCached: Boolean): File {
        val futureTarget: FutureTarget<File> = GlideApp.with(context)
            .downloadOnly() // uses DiskCacheStrategy.DATA, which caches only the original downloaded image
            .load(glideUrl)
            .skipMemoryCache(!allowCached)
            .submit(width, height)
        return futureTarget.get()
    }

    companion object {
        private const val TAG = "MmlTileProvider"
        private const val REFERER_KEY = "Referer"
        private const val REFERER_VALUE = "https://oma.riista.fi"

        private const val MML_TOPOGRAPHIC_TILE_URL_FORMAT =
            "http://kartta.riista.fi/tms/1.0.0/maasto_mobile/EPSG_3857/%d/%d/%d.png"
        private const val MML_AERIAL_TILE_URL_FORMAT =
            "http://kartta.riista.fi/tms/1.0.0/orto_mobile/EPSG_3857/%d/%d/%d.png"
        private const val MML_BACKGROUND_TILE_URL_FORMAT =
            "http://kartta.riista.fi/tms/1.0.0/tausta_mobile/EPSG_3857/%d/%d/%d.png"
    }
}

private val MAX_CACHED_FILE_AGE: Long = TimeUnit.DAYS.toMillis(30)

private fun File.isExpired(): Boolean {
    val age = System.currentTimeMillis() - lastModified()
    return age > MAX_CACHED_FILE_AGE
}
