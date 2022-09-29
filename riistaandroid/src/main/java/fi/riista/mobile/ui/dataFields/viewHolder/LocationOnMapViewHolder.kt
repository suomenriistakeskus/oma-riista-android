package fi.riista.mobile.ui.dataFields.viewHolder

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.util.component1
import androidx.core.util.component2
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.button.MaterialButton
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.R
import fi.riista.mobile.pages.MapExternalIdProvider
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.Constants
import fi.riista.mobile.utils.MapUtils

interface MapOpener {
    fun openMap(location: Location)
}

class LocationOnMapViewHolder<FieldId : DataFieldId>(
    private val mapOpener: MapOpener,
    private val mapExternalIdProvider: MapExternalIdProvider?,
    view: View,
) : DataFieldViewHolder<FieldId, LocationField<FieldId>>(view), OnMapReadyCallback {

    private val coordinatesTextView: TextView = view.findViewById(R.id.tv_coordinates)
    private val mapView: EntryMapView = view.findViewById(R.id.mapView)
    private val unknownLocationOverlay: RelativeLayout = view.findViewById(R.id.unknown_location_overlay)
    private val setupLocationButton: MaterialButton = view.findViewById(R.id.btn_setup_location_on_map)
    private lateinit var mapCurrent: GoogleMap
    private var locationAndZoomLevel: EtrsLocationAndZoomLevel? = null

    init {
        mapView.onCreate(null)
        mapView.setup(context, false, true)
        mapView.onResume()
        mapView.getMapAsync(this)

        setupLocationButton.setOnClickListener {
            viewMap()
        }
    }

    override fun onBeforeUpdateBoundData(dataField: LocationField<FieldId>) {
        val locationAndZoomLevel = dataField.location.toEtrsLocationAndZoomLevel()
        if (dataField.location is CommonLocation.Known) {
            coordinatesTextView.text = context.getString(
                R.string.map_coordinates,
                locationAndZoomLevel.etrsLocation.latitude.toString(),
                locationAndZoomLevel.etrsLocation.longitude.toString(),
            )
            coordinatesTextView.visibility = View.VISIBLE
            unknownLocationOverlay.visibility = View.GONE
        } else {
            coordinatesTextView.visibility = View.GONE
            if (unknownLocationOverlay.visibility == View.GONE) {
                unknownLocationOverlay.alpha = 0f
                unknownLocationOverlay.visibility = View.VISIBLE
                unknownLocationOverlay.animate()
                    .setStartDelay(2000)
                    .setDuration(300)
                    .alpha(1f)
                    .start()
            }
        }
        mapView.onLocationUpdated(locationAndZoomLevel.toLocation())
        this.locationAndZoomLevel = locationAndZoomLevel
    }

    private fun CommonLocation.toEtrsLocationAndZoomLevel(): EtrsLocationAndZoomLevel {
        val (etrsLocation, zoomLevel) = when (val location = this) {
            is CommonLocation.Known ->
                location.etrsLocation to null
            CommonLocation.Unknown ->
                Constants.DEFAULT_MAP_LOCATION.toETRMSGeoLocation(GeoLocationSource.MANUAL) to
                        Constants.DEFAULT_MAP_ZOOM_LEVEL
        }

        return EtrsLocationAndZoomLevel(etrsLocation, zoomLevel)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapCurrent = googleMap
        mapCurrent.setOnMapClickListener { viewMap() }
        mapView.setShowInfoWindow(true)
        mapView.setShowAccuracy(true)
        mapView.setMapExternalId(
            AppPreferences.getSelectedClubAreaMapId(context),
            AppPreferences.getInvertMapColors(context),
        )
        mapExternalIdProvider?.let {
            mapView.setMapExternalId2(
                it.getMapExternalId(),
                true,
            )
        }
    }

    private fun EtrsLocationAndZoomLevel.toLocation(): Location {
        val (latitude, longitude) = MapUtils.ETRMStoWGS84(etrsLocation.latitude.toLong(), etrsLocation.longitude.toLong())
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude

        if (zoomLevel != null) {
            location.extras = Bundle().apply {
                putFloat(EntryMapView.KEY_ZOOM_LEVEL, zoomLevel)
            }
        }
        return location
    }

    private fun viewMap() {
        locationAndZoomLevel?.toLocation()?.let { location ->
            mapOpener.openMap(location)
        }
    }

    class Factory<FieldId : DataFieldId>(
        private val mapOpener: MapOpener,
        private val mapExternalIdProvider: MapExternalIdProvider? = null,
    ) : DataFieldViewHolderFactory<FieldId, LocationField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.LOCATION_ON_MAP
    ) {
        override fun createViewHolder(
                layoutInflater: LayoutInflater,
                container: ViewGroup,
                attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, LocationField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_map, container, attachToRoot)
            return LocationOnMapViewHolder(mapOpener, mapExternalIdProvider, view)
        }
    }
}

private data class EtrsLocationAndZoomLevel(
    val etrsLocation: ETRMSGeoLocation,
    val zoomLevel: Float?,
)
