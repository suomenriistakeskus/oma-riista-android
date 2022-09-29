package fi.riista.common.domain.poi.model

import fi.riista.common.model.ETRMSGeoLocation

typealias PoiLocationId = Long

/**
 * Container of a point of interest. Each [PoiLocation] is a part of a [PoiLocationGroup].
 * @param id Unique identifier of this [PoiLocation], i.e. no other [PoiLocation] has the same id even in other [PoiLocationGroup]s.
 * @param poiId Identifier of the [PoiLocationGroup] this [PoiLocation] is part of.
 * @param visibleId Identifier shown to the user. Should be unique inside the [PoiLocationGroup].
 * @param geoLocation Location of this POI.
 */
data class PoiLocation(
    val id: PoiLocationId,
    val poiId: PoiId,
    val description: String? = null,
    val visibleId: Int,
    val geoLocation: ETRMSGeoLocation,
)
