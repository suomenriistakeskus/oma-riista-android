package fi.riista.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import fi.riista.common.domain.poi.model.PointOfInterestType
import fi.riista.mobile.R
import fi.riista.mobile.models.GameLog


typealias MarkerItemType = String

class MapMarkerRenderer(
    val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MapMarkerItem>
) : DefaultClusterRenderer<MapMarkerItem>(context, map, clusterManager) {

    private val sightingPlaceIconGenerator = IconGenerator(context)
    private val feedingPlaceIconGenerator = IconGenerator(context)
    private val mineralLickIconGenerator = IconGenerator(context)
    private val otherIconGenerator = IconGenerator(context)
    private val genericIconGenerator = IconGenerator(context)
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val pinDrawablesByItemType = mutableMapOf<MarkerItemType, Int>()
    private val pinBitmapsByItemType = mutableMapOf<MarkerItemType, BitmapDescriptor>()

    init {
        val sightingPlaceMarkerView = inflater.inflate(R.layout.poi_marker, null, false)
        sightingPlaceIconGenerator.setContentView(sightingPlaceMarkerView)
        sightingPlaceIconGenerator.setColor(ContextCompat.getColor(context, R.color.poi_sighting_place_color))

        val feedingPlaceMarkerView = inflater.inflate(R.layout.poi_marker, null, false)
        feedingPlaceIconGenerator.setContentView(feedingPlaceMarkerView)
        feedingPlaceIconGenerator.setColor(ContextCompat.getColor(context, R.color.poi_feeding_place_color))

        val mineralLickMarkerView = inflater.inflate(R.layout.poi_marker, null, false)
        mineralLickIconGenerator.setContentView(mineralLickMarkerView)
        mineralLickIconGenerator.setColor(ContextCompat.getColor(context, R.color.poi_mineral_lick_color))

        val otherMarkerView = inflater.inflate(R.layout.poi_marker, null, false)
        otherIconGenerator.setContentView(otherMarkerView)
        otherIconGenerator.setColor(ContextCompat.getColor(context, R.color.poi_other_color))

        val genericMarkerView = inflater.inflate(R.layout.poi_marker, null, false)
        genericIconGenerator.setContentView(genericMarkerView)
        genericIconGenerator.setColor(Color.BLACK)
    }

    override fun onBeforeClusterItemRendered(item: MapMarkerItem, markerOptions: MarkerOptions) {
        markerOptions.icon(getOrCreateBitmapForItemType(item))
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    /**
     * Gets the cached bitmap or creates one for the [itemType] if [itemType] != TYPE_POI.
     * For POI items a marker with describing text is created.
     * Throws [IllegalStateException] if there's no drawable for the given [itemType].
     */
    private fun getOrCreateBitmapForItemType(item: MapMarkerItem): BitmapDescriptor {
        if (item.type == GameLog.TYPE_POI) {
            val iconGenerator = when (item.poiType) {
                PointOfInterestType.SIGHTING_PLACE -> sightingPlaceIconGenerator
                PointOfInterestType.FEEDING_PLACE -> feedingPlaceIconGenerator
                PointOfInterestType.MINERAL_LICK -> mineralLickIconGenerator
                PointOfInterestType.OTHER -> otherIconGenerator
                null -> genericIconGenerator // Should never happen
            }

            return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(item.poiText))
        }

        val cachedBitmap = pinBitmapsByItemType[item.type]
        if (cachedBitmap != null) {
            return cachedBitmap
        }

        return bitmapDescriptorFromVector(context, item.type)
            .also { bitmap ->
                pinBitmapsByItemType[item.type] = bitmap
            }
    }

    /**
     * Creates a [BitmapDescriptor] for the given [itemType] based on previously registered
     * drawables. Throws [IllegalStateException] if there's no drawable for the given [itemType] or
     * if a bitmap could not be created based on registered drawable id.
     */
    private fun bitmapDescriptorFromVector(context: Context, itemType: MarkerItemType): BitmapDescriptor {
        val drawableId = pinDrawablesByItemType[itemType]
                ?: throw IllegalStateException("No drawable registered for item type $itemType")
        val icon = ContextCompat.getDrawable(context, drawableId)
                ?: throw IllegalStateException("Failed to create drawable for item type $itemType")

        icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)

        val bitmap = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun registerPinAppearance(itemType: MarkerItemType, @DrawableRes drawableResId: Int) {
        pinDrawablesByItemType[itemType] = drawableResId

        // clear cached bitmap in case drawable was just updated
        pinBitmapsByItemType.remove(itemType)
    }
}
