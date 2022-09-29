package fi.riista.mobile.feature.poi

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.toLocation
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.R
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.UiUtils

class PoiLocationFragment : Fragment(), OnMapReadyCallback {

    interface Manager {
        fun showPoiOnMap(latitude: Int, longitude: Int)
    }

    private lateinit var manager: Manager
    private var mapView: EntryMapView? = null
    private var latitude: Int = 0
    private var longitude: Int = 0
    private var groupDescription: String? = null
    private var locationDescription: String? = null
    private var visibleId: Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_poi_location, container, false)

        val args = requireArguments()

        latitude = args.getInt(KEY_LATITUDE, 0)
        longitude = args.getInt(KEY_LONGITUDE, 0)
        groupDescription = args.getString(KEY_GROUP_DESCRIPTION) ?: ""
        locationDescription = args.getString(KEY_LOCATION_DESCRIPTION) ?: ""
        visibleId = args.getInt(KEY_LOCATION_VISIBLE_ID, 0)

        view.findViewById<AppCompatButton>(R.id.btn_return_to_map).also { button ->
            UiUtils.addIconWithTint(
                button = button,
                icon = R.drawable.ic_arrow_back_white,
                color = R.color.colorPrimary,
                position = UiUtils.IconPosition.LEFT,
            )
            button.setOnClickListener {
                manager.showPoiOnMap(
                    latitude = latitude,
                    longitude = longitude,
                )
            }
        }
        view.findViewById<TextView>(R.id.tv_group_description).text = groupDescription
        view.findViewById<TextView>(R.id.tv_location_description).text = locationDescription
        view.findViewById<TextView>(R.id.tv_coordinates).text = getString(
            R.string.map_coordinates,
            latitude.toString(),
            longitude.toString()
        )
        view.findViewById<TextView>(R.id.tv_location_visible_id).text = "$visibleId: "

        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.setup(context, false, true)
        mapView?.getMapAsync(this)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        mapView = null
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        mapView?.setMapTileType(AppPreferences.getMapTileSource(context))
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        manager = context as Manager
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val geoLocation = ETRMSGeoLocation(latitude, longitude, GeoLocationSource.MANUAL.toBackendEnum())
        val location = geoLocation.toLocation()
        mapView?.onLocationUpdated(location)
        mapView?.setMapExternalId(
            AppPreferences.getSelectedClubAreaMapId(context),
            AppPreferences.getInvertMapColors(context)
        )
    }

    companion object {
        private const val KEY_LATITUDE = "poi_latitude"
        private const val KEY_LONGITUDE = "poi_longitude"
        private const val KEY_GROUP_DESCRIPTION = "poi_group_description"
        private const val KEY_LOCATION_VISIBLE_ID = "poi_location_visible_id"
        private const val KEY_LOCATION_DESCRIPTION = "poi_location_description"

        fun create(
            poiLocationGroupDescription: String?,
            poiLocationDescription: String?,
            poiLocationVisibleId: Int,
            location: ETRMSGeoLocation,
        ): PoiLocationFragment {

            return PoiLocationFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_GROUP_DESCRIPTION, poiLocationGroupDescription)
                    putString(KEY_LOCATION_DESCRIPTION, poiLocationDescription)
                    putInt(KEY_LOCATION_VISIBLE_ID, poiLocationVisibleId)
                    putInt(KEY_LATITUDE, location.latitude)
                    putInt(KEY_LONGITUDE, location.longitude)
                }
            }
        }
    }
}
