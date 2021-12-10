package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.component1
import androidx.core.util.component2
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LocationField
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.MapUtils

interface MapOpener {
    fun openMap(location: Location)
}

class LocationOnMapViewHolder<FieldId : DataFieldId>(
    private val mapOpener: MapOpener,
    view: View,
) : DataFieldViewHolder<FieldId, LocationField<FieldId>>(view), OnMapReadyCallback {

    private val coordinatesTextView: TextView = view.findViewById(R.id.tv_coordinates)
    private val mapView: EntryMapView = view.findViewById(R.id.mapView)
    private lateinit var mapCurrent: GoogleMap
    private var geoLocation: ETRMSGeoLocation? = null

    init {
        mapView.onCreate(null)
        mapView.setup(context, false, true)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    override fun onBeforeUpdateBoundData(dataField: LocationField<FieldId>) {
        coordinatesTextView.text = context.getString(
                R.string.map_coordinates,
                dataField.location.latitude.toString(),
                dataField.location.longitude.toString())
        mapView.onLocationUpdated(toLocation(dataField.location))
        geoLocation = dataField.location
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapCurrent = googleMap
        mapCurrent.setOnMapClickListener { viewMap() }
        mapView.setShowInfoWindow(true)
        mapView.setShowAccuracy(true)
    }

    private fun toLocation(geoLocation: ETRMSGeoLocation): Location {
        val (latitude, longitude) = MapUtils.ETRMStoWGS84(geoLocation.latitude.toLong(), geoLocation.longitude.toLong())
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    private fun viewMap() {
        geoLocation?.let {
            val location = toLocation(it)
            mapOpener.openMap(location)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val mapOpener: MapOpener,
    ) : DataFieldViewHolderFactory<FieldId, LocationField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.LOCATION_ON_MAP
    ) {
        override fun createViewHolder(
                layoutInflater: LayoutInflater,
                container: ViewGroup,
                attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LocationField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_map, container, attachToRoot)
            return LocationOnMapViewHolder(mapOpener, view)
        }
    }
}
