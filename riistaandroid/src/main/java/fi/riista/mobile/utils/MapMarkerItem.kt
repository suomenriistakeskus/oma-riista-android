package fi.riista.mobile.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import fi.riista.common.domain.poi.model.PointOfInterestType

class MapMarkerItem(private val mPosition: LatLng, val type: String, val localId: Long, val poiType: PointOfInterestType?, val poiText: String?) : ClusterItem {

    constructor(
        lat: Double, lon: Double, type: String, id: Long, poiType: PointOfInterestType? = null, poiText: String? = null) : this(LatLng(lat, lon), type, id, poiType, poiText)

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String? {
        return null
    }

    override fun getSnippet(): String? {
        return null
    }
}
