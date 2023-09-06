package fi.riista.mobile.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.component1
import androidx.core.util.component2
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithmAdapter
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.diary.DiaryFilter
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.LocationClient
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.feature.groupHunting.DiaryFilterDialog
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.ui.MapOverlayView
import fi.riista.mobile.ui.MapOverlayView.MapViewerInterface
import fi.riista.mobile.utils.*
import java.util.*

private enum class MarkerType(val typeName: String) {
    HARVEST_ACCEPTED("HARVEST_ACCEPTED"),
    HARVEST_PROPOSED("HARVEST_PROPOSED"),
    HARVEST_REJECTED("HARVEST_REJECTED"),

    OBSERVATION_ACCEPTED("OBSERVATION_ACCEPTED"),
    OBSERVATION_PROPOSED("OBSERVATION_PROPOSED"),
    OBSERVATION_REJECTED("OBSERVATION_REJECTED"),
    ;

    companion object {
        fun fromTypeName(typeName: String): MarkerType? {
            return values().firstOrNull { it.typeName == typeName }
        }
    }
}

interface GroupHuntingDiaryListener {
    fun updateMarkers()
    fun updateFilter()
}

class GroupHuntingMapViewer
    : DetailsPageFragment(), LocationListener, OnMapReadyCallback, MapViewerInterface, GroupHuntingDiaryListener,
    DiaryFilterDialog.DiaryFilterDialogListener {

    interface Manager {
        fun setFullscreenMode(fullscreen: Boolean)

        fun getHarvestsToBeDisplayed(): List<GroupHuntingHarvest>
        fun onHarvestClicked(harvestId: GroupHuntingHarvestId)

        fun getObservationsToBeDisplayed(): List<GroupHuntingObservation>
        fun onObservationClicked(observationId: GroupHuntingObservationId)

        fun onMultipleEntriesClicked(harvests: List<GroupHuntingHarvestId>,
                                     observations: List<GroupHuntingObservationId>)

        fun registerDiaryListener(listener: GroupHuntingDiaryListener)
        fun getHuntingGroupArea(): HuntingGroupArea?

        fun getZoomLevel(): Float?
        fun setZoomLevel(zoomLevel: Float)

        fun getLocation(): Location?
        fun setLocation(mapLocation: Location)

        fun getDiaryFilter(): DiaryFilter
        fun setDiaryFilter(diaryFilter: DiaryFilter)
    }

    private lateinit var manager: Manager

    private lateinit var mapView: EntryMapView
    private lateinit var overlay: MapOverlayView
    private lateinit var gpsPositionButton: Button
    private lateinit var setPositionButton: Button
    private lateinit var diaryFilterButton: LinearLayout
    private lateinit var diaryFilterButtonText: TextView

    private var isExpanded = false
    private var mEditMode = false

    private var mNewItem = false
    private var mLocationSource: String? = null

    private var mLocationClient: LocationClient? = null
    private var mCurrentGpsLocation: Location? = null

    private var mClusterManager: MapMarkerClusterManager<MapMarkerItem>? = null
    private var mIsMeasuring = false

    private var clusterMarkerSetupDone = false
    private var mapFocusedInitially = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.manager = (context as? Manager) ?: run {
            throw IllegalStateException("Context does not implement GroupHuntingMapViewer.Manager interface")
        }
        manager.registerDiaryListener(this)
    }

    /**
     * Toggle map view fullscreen mode
     *
     * @return Is map fullscreen afterwards
     */
    override fun onExpandCollapse(): Boolean {
        return setFullScreenEnabled(!isExpanded)
    }

    private fun setFullScreenEnabled(fullScreenEnabled: Boolean): Boolean {
        manager.setFullscreenMode(fullScreenEnabled)
        isExpanded = fullScreenEnabled

        return fullScreenEnabled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var arguments = arguments
        if (arguments == null) {
            arguments = Bundle()
        }
        mEditMode = arguments.getBoolean(MapViewerActivity.EXTRA_EDIT_MODE, false)

        mNewItem = arguments.getBoolean(MapViewerActivity.EXTRA_NEW, false)
        mLocationSource = arguments.getString(MapViewerActivity.EXTRA_LOCATION_SOURCE, GameLog.LOCATION_SOURCE_MANUAL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_group_hunting_diary_map, container, false)
        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)
        mapView.setup(inflater.context, false, true)
        mapView.getMapAsync(this)
        gpsPositionButton = view.findViewById(R.id.goToGpsPosButton)
        setPositionButton = view.findViewById(R.id.moveMarkerButton)
        diaryFilterButton = view.findViewById(R.id.diaryFilterButton)
        diaryFilterButtonText = diaryFilterButton.findViewById(R.id.diaryFilterButtonText)
        overlay = view.findViewById(R.id.map_overlay)
        overlay.setMapView(mapView)
        overlay.fragment = this
        setupActionButtons()
        return view
    }

    private fun setupActionButtons() {
        gpsPositionButton.setOnClickListener {
            if (mCurrentGpsLocation != null) {
                mapView.animateCameraTo(mCurrentGpsLocation)
            }
        }
        setPositionButton.setOnClickListener {
            val location = mapView.cameraLocation
            updateLocation(location, mLocationSource == GameLog.LOCATION_SOURCE_MANUAL)
            val result = Intent()
            result.putExtra(MapViewerActivity.RESULT_LOCATION, location)
            result.putExtra(MapViewerActivity.RESULT_LOCATION_SOURCE, mLocationSource)
            val activity = activity as BaseActivity
            activity.setResult(Activity.RESULT_OK, result)
            activity.finish()
        }
        diaryFilterButtonText.text = createDiaryFilterButtonText(manager.getDiaryFilter())
        diaryFilterButton.setOnClickListener {
            val diaryFilter = manager.getDiaryFilter()
            DiaryFilterDialog.showDialog(requireActivity(), this, diaryFilter.eventType, diaryFilter.acceptStatus)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onResume() {
        setEditMode(mEditMode)
        mapView.onResume()
        mapView.setMapTileType(AppPreferences.getMapTileSource(context))
        mLocationClient = (activity as BaseActivity).locationClient
        mLocationClient?.addListener(this)
        mapView.setMapExternalId(AppPreferences.getSelectedClubAreaMapId(context), AppPreferences.getInvertMapColors(context))
        mapView.setShowRhyBordersLayer(AppPreferences.getShowRhyBorders(context))
        mapView.setShowValtionmaatLayer(AppPreferences.getShowValtionmaat(context))
        mapView.setShowGameTrianglesLayer(AppPreferences.getShowGameTriangles(context))
        mapView.setShowLeadShotBanLayer(AppPreferences.getShowLeadShotBan(context))
        mapView.setShowMooseRestrictionsLayer(AppPreferences.getShowMooseRestrictions(context))
        mapView.setShowSmallGameRestrictionsLayer(AppPreferences.getShowSmallGameRestrictions(context))
        mapView.setShowAviHuntingBanLayer(AppPreferences.getShowAviHuntingBan(context))
        overlay.updateMhMooseAreaVisibility()
        overlay.updateMhPienriistaAreaVisibility()
        super.onResume()
    }

    override fun onPause() {
        setFullScreenEnabled(false)

        super.onPause()
        mLocationClient?.removeListener(this)

        mapView.cameraLocation?.let {
            manager.setLocation(it)
        }
        mapView.cameraZoomLevel.let {
            manager.setZoomLevel(it)
        }

        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        if (mClusterManager != null) {
            populateMarkerItems()
        }
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(map: GoogleMap) {
        val context = context
        mapFocusedInitially = false

        // Only show GPS state indicator when creating new entry.
        mapView.setShowInfoWindow(mNewItem)
        mapView.setShowAccuracy(true)
        map.setOnCameraMoveListener {
            overlay.updateCameraMoved()
        }
        overlay.updateCameraMoved()
        mapView.setMapExternalId(AppPreferences.getSelectedClubAreaMapId(context), AppPreferences.getInvertMapColors(context))
        mapView.setShowRhyBordersLayer(AppPreferences.getShowRhyBorders(context))
        mapView.setShowValtionmaatLayer(AppPreferences.getShowValtionmaat(context))
        mapView.setShowGameTrianglesLayer(AppPreferences.getShowGameTriangles(context))
        mapView.setShowLeadShotBanLayer(AppPreferences.getShowLeadShotBan(context))
        mapView.setShowMooseRestrictionsLayer(AppPreferences.getShowMooseRestrictions(context))
        mapView.setShowSmallGameRestrictionsLayer(AppPreferences.getShowSmallGameRestrictions(context))
        mapView.setShowAviHuntingBanLayer(AppPreferences.getShowAviHuntingBan(context))
        overlay.updateMhMooseAreaVisibility()
        overlay.updateMhPienriistaAreaVisibility()
        val markers = setupClusterMarkers()
        focusMap(markers)
    }

    override fun onLocationChanged(location: Location) {
        mCurrentGpsLocation = location
        gpsPositionButton.isEnabled = mCurrentGpsLocation != null

        // Always show and save latest gps position when when creating entry unless manually set already.
        if (mNewItem && mLocationSource == GameLog.LOCATION_SOURCE_GPS) {
            mapView.refreshLocationIndicators(location)
            mLocationSource = GameLog.LOCATION_SOURCE_MANUAL
        }
        overlay.setCurrentGpsLocation(mCurrentGpsLocation)
    }

    private fun updateLocation(location: Location, isSetManually: Boolean) {
        // Accuracy and altitude data is invalid when setting manual location.
        if (isSetManually) {
            location.accuracy = 0f
            if (location.hasAltitude()) {
                location.altitude = 0.0
            }
        }
    }

    private fun canEditLocation(): Boolean {
        return mEditMode
    }

    private fun setEditMode(enabled: Boolean) {
        mEditMode = enabled
        gpsPositionButton.isEnabled = canEditLocation() && mCurrentGpsLocation != null
        gpsPositionButton.visibility = if (canEditLocation()) View.VISIBLE else View.GONE
        setPositionButton.isEnabled = canEditLocation()
        setPositionButton.visibility = if (canEditLocation()) View.VISIBLE else View.GONE
        diaryFilterButton.visibility = if (canEditLocation()) View.GONE else View.VISIBLE
    }

    private fun setupClusterMarkers(): List<MapMarkerItem> {
        val map = mapView.map
        val clusterManager = MapMarkerClusterManager<MapMarkerItem>(requireActivity(), map)
            .apply {
                algorithm = NonHierarchicalDistanceBasedAlgorithm<MapMarkerItem>().apply {
                    // reduce the default clustering distance (default is 100dp). By reducing
                    // the markers won't get that easily clustered.
                    // - pins are roughly 42dp x 30dp so 60dp should leave small gap between
                    //   markers when unclustered. Actually 50dp is enough for markers but 60dp
                    //   seems a better value so that clusters don't overlap (they're bit larger
                    //   than markers)
                    maxDistanceBetweenClusteredItems = 60
                }.let { algorithm ->
                    // wrap algorithm the same way as what is done in ClusterManager implementation
                    ScreenBasedAlgorithmAdapter(PreCachingAlgorithmDecorator(algorithm))
                }
            }
            .also {
                mClusterManager = it
            }

        val renderer = MapMarkerRenderer(requireContext(), map, clusterManager).apply {
            // allow clusters that have only 2 markers. By default the value was 4 which makes
            // it possible to have two markers at the same location (e.g. harvest + related
            // observation). In these cases it was impossible for the user to select which
            // marker will get the click
            minClusterSize = 2

            registerPinAppearance(MarkerType.HARVEST_ACCEPTED.typeName, R.drawable.ic_pin_harvest)
            registerPinAppearance(MarkerType.HARVEST_PROPOSED.typeName, R.drawable.ic_pin_proposed_harvest)
            registerPinAppearance(MarkerType.HARVEST_REJECTED.typeName, R.drawable.ic_pin_rejected_harvest)
            registerPinAppearance(MarkerType.OBSERVATION_ACCEPTED.typeName, R.drawable.ic_pin_observation)
            registerPinAppearance(MarkerType.OBSERVATION_PROPOSED.typeName, R.drawable.ic_pin_proposed_observation)
            registerPinAppearance(MarkerType.OBSERVATION_REJECTED.typeName, R.drawable.ic_pin_rejected_observation)
        }
        clusterManager.renderer = renderer
        clusterManager.setOnClusterClickListener { cluster: Cluster<MapMarkerItem> ->
            val currentZoomLevel = mapView.map.cameraPosition.zoom

            if (currentZoomLevel != EntryMapView.MAP_ZOOM_LEVEL_MAX) {
                if (cluster.areMarkersRoughlyInSameLocation()) {
                    // no need to zoom in as cluster probably won't be expanded. It's faster
                    // for the user to just see e.g. a list containing entries
                    notifyClusterCannotBeExpanded(cluster)
                } else {
                    mapView.zoomBy(2.0f, cluster.position)
                }
            } else {
                notifyClusterCannotBeExpanded(cluster)
            }
            true
        }
        clusterManager.setOnClusterItemClickListener { mapMarkerItem: MapMarkerItem ->
            navigateToLogItem(mapMarkerItem)
            true
        }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        val markers = populateMarkerItems()
        clusterMarkerSetupDone = true
        return markers
    }

    private fun notifyClusterCannotBeExpanded(cluster: Cluster<MapMarkerItem>) {
        val markersByType = cluster.items.groupBy { mapMarkerItem ->
            MarkerType.fromTypeName(mapMarkerItem.type)
        }

        val harvests = markersByType.filterKeys { markerType ->
            markerType in listOf(
                    MarkerType.HARVEST_ACCEPTED,
                    MarkerType.HARVEST_PROPOSED,
                    MarkerType.HARVEST_REJECTED
            )
        }.values.flatten()
        val observations = markersByType.filterKeys { markerType ->
            markerType in listOf(
                    MarkerType.OBSERVATION_ACCEPTED,
                    MarkerType.OBSERVATION_PROPOSED,
                    MarkerType.OBSERVATION_REJECTED,
            )
        }.values.flatten()

        manager.onMultipleEntriesClicked(
                harvests = harvests.map { it.localId },
                observations = observations.map { it.localId }
        )
    }

    private fun navigateToLogItem(mapMarkerItem: MapMarkerItem) {
        when (MarkerType.fromTypeName(mapMarkerItem.type)) {
            MarkerType.HARVEST_ACCEPTED, MarkerType.HARVEST_PROPOSED, MarkerType.HARVEST_REJECTED -> {
                manager.onHarvestClicked(mapMarkerItem.localId)
            }
            MarkerType.OBSERVATION_ACCEPTED, MarkerType.OBSERVATION_PROPOSED, MarkerType.OBSERVATION_REJECTED -> {
                manager.onObservationClicked(mapMarkerItem.localId)
            }
            null -> throw IllegalStateException("<null> marker type observed during click handling")
        }
    }

    private fun populateMarkerItems(): List<MapMarkerItem> {
        val clusterManager = mClusterManager ?: return emptyList()

        clusterManager.clearItems()

        val items = getHarvestMarkers() + getObservationMarkers()
        items.forEach {
            clusterManager.addItem(it)
        }

        clusterManager.cluster()
        return items
    }

    private fun getHarvestMarkers(): List<MapMarkerItem> {
        return manager.getHarvestsToBeDisplayed()
            .map { harvest ->
                val (latitude, longitude) = with(harvest.geoLocation) {
                    MapUtils.ETRMStoWGS84(latitude.toLong(), longitude.toLong())
                }

                val markerType = when (harvest.acceptStatus) {
                    AcceptStatus.PROPOSED -> MarkerType.HARVEST_PROPOSED
                    AcceptStatus.ACCEPTED -> MarkerType.HARVEST_ACCEPTED
                    AcceptStatus.REJECTED -> MarkerType.HARVEST_REJECTED
                }.typeName

                MapMarkerItem(latitude, longitude, markerType, harvest.id)
            }
    }

    private fun getObservationMarkers(): List<MapMarkerItem> {
        return manager.getObservationsToBeDisplayed()
            .map { observation ->
                val (latitude, longitude) = with(observation.geoLocation) {
                    MapUtils.ETRMStoWGS84(latitude.toLong(), longitude.toLong())
                }

                val markerType = when (observation.acceptStatus) {
                    AcceptStatus.PROPOSED -> MarkerType.OBSERVATION_PROPOSED
                    AcceptStatus.ACCEPTED -> MarkerType.OBSERVATION_ACCEPTED
                    AcceptStatus.REJECTED -> MarkerType.OBSERVATION_REJECTED
                }.typeName

                MapMarkerItem(latitude, longitude, markerType, observation.id)
            }
    }

    override fun setMeasuring(isMeasuring: Boolean) {
        mIsMeasuring = isMeasuring

        // Prevent measure buttons and diary filter button overlapping by hiding filter button while measuring
        diaryFilterButton.visibility = (!isMeasuring).toVisibility()
    }

    override fun filterSelected(eventType: DiaryFilter.EventType, acceptStatus: DiaryFilter.AcceptStatus) {
        val diaryFilter = DiaryFilter(eventType, acceptStatus)
        diaryFilterButtonText.text = createDiaryFilterButtonText(diaryFilter)
        manager.setDiaryFilter(diaryFilter)
    }

    private fun focusMap(markers: List<MapMarkerItem>) {
        if (markers.isNotEmpty()) {
            val mapLocation = manager.getLocation()
            val zoomLevel = manager.getZoomLevel()

            if (mapLocation != null && zoomLevel != null) {
                mapView.map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                LatLng(mapLocation.latitude, mapLocation.longitude),
                                zoomLevel.toFloat()
                        )
                )
            } else {
                // Zoom automatically to fit markers
                val b = LatLngBounds.Builder()
                for (m in markers) {
                    b.include(m.position)
                }
                val bounds = b.build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, CAMERA_UPDATE_PADDING)
                mapView.map.animateCamera(cameraUpdate)
                mapFocusedInitially = true
            }
        }

        val area = manager.getHuntingGroupArea()
        if (area?.externalId != null) {
            mapView.setMapExternalId2(area.externalId, AppPreferences.getInvertMapColors(context))
        }
    }

    private fun createDiaryFilterButtonText(diaryFilter: DiaryFilter): String {
        val acceptPart = getString(
            when (diaryFilter.acceptStatus) {
                DiaryFilter.AcceptStatus.ALL -> R.string.group_hunting_diary_filter_accept_type_all
                DiaryFilter.AcceptStatus.ACCEPTED -> R.string.group_hunting_diary_filter_accept_type_accepted
                DiaryFilter.AcceptStatus.PROPOSED -> R.string.group_hunting_diary_filter_accept_type_proposed
                DiaryFilter.AcceptStatus.REJECTED -> R.string.group_hunting_diary_filter_accept_type_rejected
            }
        )
        val eventPart = getString(
            when (diaryFilter.eventType) {
                DiaryFilter.EventType.ALL -> R.string.group_hunting_diary_filter_event_type_all
                DiaryFilter.EventType.OBSERVATIONS -> R.string.group_hunting_diary_filter_event_type_observations
                DiaryFilter.EventType.HARVESTS -> R.string.group_hunting_diary_filter_event_type_harvests
            }
        ).lowercase(Locale.getDefault())
        return "$acceptPart $eventPart"
    }

    override fun updateMarkers() {
        if (clusterMarkerSetupDone) {
            val markers = populateMarkerItems()
            if (!mapFocusedInitially) {
                focusMap(markers)
            }
        }
    }

    override fun updateFilter() {
        diaryFilterButtonText.text = createDiaryFilterButtonText(manager.getDiaryFilter())
    }

    private fun Cluster<MapMarkerItem>.areMarkersRoughlyInSameLocation(): Boolean {
        val centerMarker = items.firstOrNull() ?: return false

        val locationRadiusMeters = 20
        items.forEach { marker ->
            if (marker.distanceTo(centerMarker) > locationRadiusMeters) {
                return false
            }
        }

        return true
    }

    private fun MapMarkerItem.distanceTo(other: MapMarkerItem): Double {
        if (this === other) {
            return 0.0
        }

        val results = FloatArray(size = 1)
        Location.distanceBetween(
                position.latitude, position.longitude,
                other.position.latitude, other.position.longitude,
                results
        )

        return results.first().toDouble()
    }



    companion object {
        private const val CAMERA_UPDATE_PADDING = 80
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

        fun newInstance(): GroupHuntingMapViewer {
            return GroupHuntingMapViewer()
        }
    }
}
