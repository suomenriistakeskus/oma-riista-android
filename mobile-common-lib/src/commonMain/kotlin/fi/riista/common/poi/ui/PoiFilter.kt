package fi.riista.common.poi.ui

import fi.riista.common.poi.model.PointOfInterestType
import kotlinx.serialization.Serializable

/**
 * Filter POIs by POI type
 */
@Serializable
data class PoiFilter(val poiFilterType: PoiFilterType) {

    enum class PoiFilterType() {
        ALL,
        SIGHTING_PLACE,
        MINERAL_LICK,
        FEEDING_PLACE,
        OTHER,
    }
}

fun PoiFilter.matches(type: PointOfInterestType?): Boolean {
    return when (poiFilterType) {
        PoiFilter.PoiFilterType.ALL -> true
        PoiFilter.PoiFilterType.SIGHTING_PLACE -> type == PointOfInterestType.SIGHTING_PLACE
        PoiFilter.PoiFilterType.MINERAL_LICK -> type == PointOfInterestType.MINERAL_LICK
        PoiFilter.PoiFilterType.FEEDING_PLACE -> type == PointOfInterestType.FEEDING_PLACE
        PoiFilter.PoiFilterType.OTHER -> type == PointOfInterestType.OTHER
    }
}
