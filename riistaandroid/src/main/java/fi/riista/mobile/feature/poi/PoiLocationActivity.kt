package fi.riista.mobile.feature.poi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.toBackendEnum
import fi.riista.common.poi.model.PoiLocation
import fi.riista.common.poi.model.PoiLocationGroup
import fi.riista.common.util.toLocation
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.utils.AppPreferences

class PoiLocationActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mapView: EntryMapView
    private var latitude: Int = 0
    private var longitude: Int = 0
    private lateinit var groupDscription: String
    private lateinit var locationDescription: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_location)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        setCustomTitle(title)

        latitude = intent.getIntExtra(EXTRA_LATITUDE, 0)
        longitude = intent.getIntExtra(EXTRA_LONGITUDE, 0)
        groupDscription = intent.getStringExtra(EXTRA_GROUP_DESCRIPTION) ?: ""
        locationDescription = intent.getStringExtra(EXTRA_LOCATION_DESCRIPTION) ?: ""

        findViewById<TextView>(R.id.tv_group_description).text = groupDscription
        findViewById<TextView>(R.id.tv_location_description).text = locationDescription
        findViewById<TextView>(R.id.tv_coordinates).text = getString(
            R.string.map_coordinates,
            latitude.toString(),
            longitude.toString()
        )

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.setup(this, false, true)
        mapView.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        mapView.setMapTileType(AppPreferences.getMapTileSource(this))
        mapView.setMapExternalId(
            AppPreferences.getSelectedClubAreaMapId(this), AppPreferences.getInvertMapColors(
                this
            )
        )
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(p0: GoogleMap?) {
        val geoLocation = ETRMSGeoLocation(latitude, longitude, GeoLocationSource.MANUAL.toBackendEnum())
        val location = geoLocation.toLocation()
        mapView.onLocationUpdated(location)
    }

    companion object {
        private const val EXTRA_LATITUDE = "poi_latitude"
        private const val EXTRA_LONGITUDE = "poi_longitude"
        private const val EXTRA_GROUP_DESCRIPTION = "poi_group_description"
        private const val EXTRA_LOCATION_DESCRIPTION = "poi_location_description"
        private const val EXTRA_TITLE = "poi_title"

        fun getIntent(context: Context, poiLocationGroup: PoiLocationGroup, poiLocation: PoiLocation): Intent {
            val intent = Intent(context, PoiLocationActivity::class.java)
            intent.putExtra(EXTRA_GROUP_DESCRIPTION, poiLocationGroup.description)
            intent.putExtra(EXTRA_LOCATION_DESCRIPTION, poiLocation.description)

            val location = poiLocation.geoLocation
            intent.putExtra(EXTRA_LATITUDE, location.latitude)
            intent.putExtra(EXTRA_LONGITUDE, location.longitude)

            intent.putExtra(EXTRA_TITLE, "${poiLocationGroup.visibleId}-${poiLocation.visibleId}")

            return intent
        }
    }
}
