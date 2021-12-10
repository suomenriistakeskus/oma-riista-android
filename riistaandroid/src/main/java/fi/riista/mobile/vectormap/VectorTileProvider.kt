package fi.riista.mobile.vectormap

import android.content.Context
import android.graphics.*
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import fi.riista.mobile.AppConfig
import fi.riista.mobile.utils.Utils
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * Tile provider for fetching MML map tiles from Riista map server
 */
class VectorTileProvider(context: Context) : TileProvider {
    enum class AreaType {
        MOOSE, PIENRIISTA, VALTIONMAA, RHY, GAME_TRIANGLES, SEURA
    }

    private val tileSize = (0.4f * 256.0f * context.resources.displayMetrics.density).toInt()
    private val cache = VectorTileCache(context)

    var mapExternalId: String? = null
    var invertColors = false
    var areaType: AreaType? = null

    @Synchronized
    private fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
        try {
            when (areaType) {
                AreaType.MOOSE -> return URL(String.format(MOOSE_PATTERN, zoom, x, y))
                AreaType.PIENRIISTA -> return URL(String.format(PIENRIISTA_PATTERN, zoom, x, y))
                AreaType.VALTIONMAA -> return URL(String.format(VALTIONMAA_PATTERN, zoom, x, y))
                AreaType.RHY -> return URL(String.format(RHY_PATTERN, zoom, x, y))
                AreaType.GAME_TRIANGLES -> return URL(String.format(GAME_TRIANGLES_PATTERN, zoom, x, y))
                AreaType.SEURA -> {
                    val base = AppConfig.getBaseUrl()
                    return URL("$base/area/vector/$mapExternalId/$zoom/$x/$y")
                }
            }
        } catch (e: MalformedURLException) {
            throw AssertionError(e)
        }
        return null
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        if (mapExternalId == null) {
            return TileProvider.NO_TILE
        }
        val tileUrl = getTileUrl(x, y, zoom) ?: return TileProvider.NO_TILE
        val key = tileUrl.toString() + if (invertColors) { "_inverted" } else { "_normal" }

        var tileData: ByteArray? = null
        try {
            val cached = cache.get(key)
            if (cached != null) {
                tileData = cached.readBytes()
            } else {
                val vectorTile = downloadData(tileUrl)
                val image = renderVectorTile(vectorTile, tileSize, zoom)
                if (image != null) {
                    tileData = bitmapToImage(image)
                    cache.put(key, tileData)
                }
            }

            if (tileData != null) {
                return Tile(tileSize, tileSize, tileData)
            }
        } catch (e: Exception) {
            Utils.LogMessage("Tile error: " + tileUrl + ": " + e.message)
        }
        return null
    }

    private fun downloadData(tileUrl: URL): VectorTile.Tile? {
        var inputStream: InputStream? = null
        try {
            val urlConnection = tileUrl.openConnection() as HttpURLConnection
            inputStream = urlConnection.inputStream
            val dataBytes = IOUtils.toByteArray(BufferedInputStream(inputStream))
            return VectorTile.Tile.parseFrom(dataBytes)
        } catch (e: Exception) {
            Utils.LogMessage("Tile error: " + tileUrl + ": " + e.message)
        } finally {
            if (inputStream != null) try {
                inputStream.close()
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    private val fillPaint: Paint
        get() {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.alpha = 50
            paint.style = Paint.Style.FILL
            when (areaType) {
                AreaType.MOOSE -> paint.color = AREA_MH_MOOSE_COLOR
                AreaType.PIENRIISTA -> paint.color = AREA_MH_PIENRIISTA_COLOR
                AreaType.VALTIONMAA -> paint.color = AREA_VALTIONMAA_COLOR
                AreaType.RHY -> {
                    paint.color = Color.TRANSPARENT
                    paint.alpha = 0
                }
                AreaType.GAME_TRIANGLES -> paint.color = AREA_GAME_TRIANGLES_COLOR
                else -> paint.color = AREA_COLOR
            }
            return paint
        }

    private fun getBorderPaint(): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color =
            if (areaType == AreaType.RHY) RHY_BORDER_COLOR else BORDER_COLOR
        paint.alpha = 255
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 0f
        return paint
    }

    private fun renderVectorTile(vectorTile: VectorTile.Tile?, mTileSize: Int, zoom: Int): Bitmap? {
        if (!invertColors && (vectorTile!!.layers == null || vectorTile.layers.isEmpty())) {
            return null
        }
        val bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cursor = MvtCursor()
        val fillPaint = fillPaint
        val borderPaint = getBorderPaint()
        if (vectorTile?.layers != null) {
            for (layer in vectorTile.layers) {
                val scale = mTileSize.toFloat() / layer.extent.toFloat()
                val transform = Matrix()
                transform.setScale(scale, scale)
                for (i in layer.features.indices) {
                    val feature = layer.features[i]
                    val shouldSkipFeatureRender = skipFeatureRender(layer, feature)
                    if ((areaType == AreaType.MOOSE || areaType == AreaType.PIENRIISTA) && shouldSkipFeatureRender) {
                        continue
                    }
                    cursor.reset()
                    when (feature.type) {
                        VectorTile.Tile.POLYGON -> {
                            val rings = decodeRings(feature.geometry, cursor)
                            val polys = decodePolygons(rings)
                            for (polygon in polys) {
                                val path = polygon.toPath()
                                path.transform(transform)
                                canvas.drawPath(path, fillPaint)
                                if (zoom >= 5) {
                                    canvas.drawPath(path, borderPaint)
                                }
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
        if (invertColors) {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val index = y * width + x
                    val color = pixels[index]
                    when {
                        color == 0 -> {
                            //Outside
                            pixels[index] = AREA_INVERT_COLOR
                        }
                        Color.green(color) < 50 -> {
                            //Border, do nothing
                        }
                        else -> {
                            //Filled area, reset to transparent
                            pixels[index] = Color.TRANSPARENT
                        }
                    }
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        }
        return bitmap
    }

    private fun skipFeatureRender(layer: VectorTile.Tile.Layer, feature: VectorTile.Tile.Feature): Boolean {
        if (layer.keys == null || layer.values == null || feature.tags == null) {
            Utils.LogMessage(this.javaClass.simpleName, "Tile content incomplete")
            return true
        }
        var matchesExternalId = false
        var j = 0
        while (j < feature.tags.size - 1) {
            val keyIndex = feature.tags[j]
            val valIndex = feature.tags[j + 1]
            val valid = keyIndex >= 0 && keyIndex < layer.keys.size && valIndex >= 0 && valIndex < layer.values.size
            if (valid && AREA_NAME_KEY == layer.keys[keyIndex] && layer.values[valIndex] != null && layer.values[valIndex].stringValue != null) {
                if (layer.values[valIndex].stringValue.startsWith(mapExternalId!!)) {
                    matchesExternalId = true
                    break
                }
            }
            j += 2
        }
        return !matchesExternalId
    }

    companion object {
        private const val MOOSE_PATTERN = "https://kartta.riista.fi/vector/hirvi/%s/%s/%s"
        private const val PIENRIISTA_PATTERN = "https://kartta.riista.fi/vector/pienriista/%s/%s/%s"
        private const val VALTIONMAA_PATTERN = "https://kartta.riista.fi/vector/metsahallitus/%s/%s/%s"
        private const val RHY_PATTERN = "https://kartta.riista.fi/vector/rhy/%s/%s/%s"
        private const val GAME_TRIANGLES_PATTERN = "https://kartta.riista.fi/vector/riistakolmiot/%s/%s/%s"
        private const val AREA_NAME_KEY = "KOHDE_NIMI"
        private const val CMD_MOVE_TO = 1
        private const val CMD_LINE_TO = 2
        private const val CMD_CLOSE_PATH = 7
        private val AREA_COLOR = Color.argb(64, 0, 255, 0)
        private val AREA_INVERT_COLOR = Color.argb(64, 255, 0, 0)
        private const val BORDER_COLOR = Color.BLACK
        private val AREA_MH_MOOSE_COLOR = Color.argb(64, 0, 128, 128)
        private val AREA_MH_PIENRIISTA_COLOR = Color.argb(64, 128, 128, 0)
        private val AREA_VALTIONMAA_COLOR = Color.argb(64, 0, 0, 255)
        private val AREA_GAME_TRIANGLES_COLOR = Color.argb(64, 255, 0, 0)
        private const val RHY_BORDER_COLOR = Color.BLUE

        private fun bitmapToImage(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            bitmap.recycle()
            return stream.toByteArray()
        }

        private fun decodeRings(input: IntArray?, cursor: MvtCursor): List<LinearRing> {
            if (input == null || input.isEmpty()) {
                return emptyList()
            }
            var i = 0
            var cmd: Int
            var cmdLength: Int
            val rings: MutableList<LinearRing> = LinkedList()
            while (i <= input.size - 9) {
                cmd = input[i++]
                cmdLength = cmd shr 3
                if (cmd and 0x7 != CMD_MOVE_TO || cmdLength != 1) {
                    break
                }
                cursor.decodeMoveTo(input[i++], input[i++])
                cmd = input[i++]
                cmdLength = cmd shr 3
                if (cmd and 0x7 != CMD_LINE_TO || cmdLength < 2) {
                    break
                }
                if (cmdLength * 2 + i + 1 > input.size) {
                    break
                }
                val linearRing = LinearRing(cmdLength + 2)
                linearRing[0, cursor.x] = cursor.y
                for (lineToIndex in 0 until cmdLength) {
                    cursor.decodeMoveTo(input[i++], input[i++])
                    linearRing[lineToIndex + 1, cursor.x] = cursor.y
                }
                cmd = input[i++]
                cmdLength = cmd shr 3
                if (cmd and 0x7 != CMD_CLOSE_PATH || cmdLength != 1) {
                    break
                }

                // Close path
                linearRing[linearRing.size() - 1, linearRing.getX(0)] = linearRing.getY(0)
                rings.add(linearRing)
            }
            return rings
        }

        private fun decodePolygons(rings: List<LinearRing>): List<Polygon> {
            if (rings.isEmpty()) {
                return emptyList()
            }
            if (rings.size == 1) {
                return listOf(Polygon(rings[0]))
            }
            val polygons: MutableList<Polygon> = LinkedList()
            var polygon: Polygon? = null
            var ccw: Boolean? = null
            for (linearRing in rings) {
                val area = linearRing.signedArea()
                if (area == 0L) {
                    continue
                }
                if (ccw == null) {
                    ccw = area < 0
                }
                if (ccw == area < 0) {
                    if (polygon != null) {
                        polygons.add(polygon)
                    }
                    polygon = Polygon(linearRing)
                } else {
                    polygon!!.addInnerRing(linearRing)
                }
            }
            if (polygon != null) {
                polygons.add(polygon)
            }
            return polygons
        }
    }
}
