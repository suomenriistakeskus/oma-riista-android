package fi.riista.mobile.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.maps.android.clustering.Cluster
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.poi.ui.PoiController
import fi.riista.common.poi.ui.PoiFilter
import fi.riista.common.poi.ui.PoiViewModel
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.LocationClient
import fi.riista.mobile.R
import fi.riista.mobile.activity.*
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.feature.poi.PoiFilterFragment
import fi.riista.mobile.feature.poi.PoiLocationActivity
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.models.srva.SrvaEvent
import fi.riista.mobile.observation.ObservationDatabase
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.srva.SrvaDatabase
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.ui.GameLogFilterView.GameLogFilterListener
import fi.riista.mobile.ui.MapOverlayView
import fi.riista.mobile.ui.MapOverlayView.MapViewerInterface
import fi.riista.mobile.utils.*
import fi.riista.mobile.utils.AppPreferences.MapLocation
import fi.riista.mobile.utils.CacheClearTracker.shouldClearBackgroundCache
import fi.riista.mobile.utils.CacheClearTracker.shouldClearVectorCaches
import fi.riista.mobile.viewmodel.GameLogViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.math.cos
import kotlin.math.sin

class MapViewer : DetailsPageFragment(), LocationListener, OnMapReadyCallback, GameLogFilterListener,
    MapViewerInterface {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userInfoStore: UserInfoStore

    @Inject
    lateinit var harvestDatabase: HarvestDatabase

    @Inject
    lateinit var observationDatabase: ObservationDatabase

    private var isExpanded = false
    private var editMode = false
    private var startLocation: Location? = null
    private var newItem = false
    private var locationSource: String? = null
    private var mapView: EntryMapView? = null
    private var locationClient: LocationClient? = null
    private var currentGpsLocation: Location? = null
    private lateinit var crosshair: ImageView
    private lateinit var gpsPositionButton: Button
    private lateinit var setPositionButton: Button
    private lateinit var overlay: MapOverlayView
    private var overlayMode = false
    private var clusterManager: MapMarkerClusterManager<MapMarkerItem>? = null
    private lateinit var filterView: GameLogFilterView
    private var displayPins = false
    private var isMeasuring = false
    private lateinit var model: GameLogViewModel
    private var enablePins = false
    private val markerItemsCache: MutableMap<String, MutableList<MapMarkerItem>> = HashMap()

    private var enablePois = false
    private var poiController: PoiController? = null
    private lateinit var poiFilterButton: LinearLayout
    private lateinit var poiFilterButtonText: TextView
    private val disposeBag = DisposeBag()
    private var poiViewModel: PoiViewModel? = null
    private lateinit var stringProvider: StringProvider

    /**
     * Toggle map view fullscreen mode
     *
     * @return Is map fullscreen afterwards
     */
    override fun onExpandCollapse(): Boolean {
        val activity = activity
        if (activity is MainActivity || activity is MapViewerActivity) {
            val isFullscreen = !isExpanded
            (activity as FullScreenExpand).setFullscreenMode(isFullscreen)
            isExpanded = isFullscreen
        }
        return isExpanded
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var arguments = arguments
        if (arguments == null) {
            arguments = Bundle()
            overlayMode = true
        }
        editMode = arguments.getBoolean(MapViewerActivity.EXTRA_EDIT_MODE, false)
        startLocation = arguments.getParcelable(MapViewerActivity.EXTRA_START_LOCATION)
        newItem = arguments.getBoolean(MapViewerActivity.EXTRA_NEW, false)
        locationSource = arguments.getString(MapViewerActivity.EXTRA_LOCATION_SOURCE, GameLog.LOCATION_SOURCE_MANUAL)
        if (overlayMode) {
            val loc = AppPreferences.getLastMapLocation(context)
            if (loc != null) {
                startLocation = Location("")
                startLocation?.latitude = loc.latitude
                startLocation?.longitude = loc.longitude
                val bundle = Bundle()
                bundle.putFloat("zoomLevel", loc.zoom)
                startLocation?.extras = bundle
            }
        }
        setFragmentResultListener(PoiFilterFragment.REQUEST_KEY) { _, bundle ->
            val filterType = PoiFilter.PoiFilterType.valueOf(bundle.getString(PoiFilterFragment.KEY_FILTER) ?: "ALL")
            poiController?.eventDispatcher?.dispatchPoiFilterChanged(PoiFilter(filterType))
            poiFilterButtonText.text = poiFilterText(poiViewModel?.filter?.poiFilterType ?: PoiFilter.PoiFilterType.ALL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mapviewer, container, false)
        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(mapViewBundle)
        mapView?.setup(inflater.context, false, true)
        mapView?.getMapAsync(this)
        crosshair = view.findViewById(R.id.map_crosshair)
        gpsPositionButton = view.findViewById(R.id.goToGpsPosButton)
        setPositionButton = view.findViewById(R.id.moveMarkerButton)
        overlay = view.findViewById(R.id.map_overlay)
        overlay.setMapView(mapView)
        overlay.fragment = this
        filterView = view.findViewById(R.id.map_filter_view)
        filterView.listener = this
        stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        poiFilterButton = view.findViewById(R.id.poiFilterButton)
        poiFilterButtonText = view.findViewById(R.id.poiFilterButtonText)
        poiFilterButton.setOnClickListener {
            val filter = poiViewModel?.filter ?: PoiFilter(PoiFilter.PoiFilterType.ALL)
            val poiFilterFragment = PoiFilterFragment.create(filter)
            poiFilterFragment.show(requireActivity().supportFragmentManager, poiFilterFragment.tag)
        }

        model = ViewModelProvider(requireActivity(), viewModelFactory).get(
            GameLogViewModel::class.java
        )
        if (enablePins) {
            model.refreshSeasons()
            filterView.setupTypes(
                UiUtils.isSrvaVisible(userInfoStore.getUserInfo()),
                true,
                model.getTypeSelected().value
            )
            filterView.setupSeasons(model.getSeasons().value, model.getSeasonSelected().value)
            filterView.setupSpecies(model.getSpeciesSelected().value!!, model.getCategorySelected().value)
        }
        model.getTypeSelected().observe(viewLifecycleOwner, { populateMarkerItems() })
        model.getSeasonSelected().observe(viewLifecycleOwner, { populateMarkerItems() })
        model.getSpeciesSelected().observe(viewLifecycleOwner, { speciesIds: List<Int?>? ->
            filterView.setupSpecies(speciesIds!!, model.getCategorySelected().value)
            populateMarkerItems()
        })
        model.getSeasons().observe(
            viewLifecycleOwner,
            { seasons: List<Int>? -> filterView.setupSeasons(seasons, model.getSeasonSelected().value) }
        )

        if (enablePois) {
            val externalId = AppPreferences.getSelectedClubAreaMapId(requireContext())
            poiController = PoiController(RiistaSDK.poiContext, externalId)
        }
        savedInstanceState?.let {
            poiController?.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }
        setupActionBar()
        setupActionButtons()
        return view
    }

    private fun setupActionBar() {
        val activity = requireActivity() as AppCompatActivity
        val actionBar = requireNotNull(activity.supportActionBar)

        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setCustomView(R.layout.actionbar_map_viewer)
        val displayPinsButton: AppCompatImageButton =
            actionBar.customView.findViewById(R.id.toolbar_display_pins_button)
        val displayPinsTitle = actionBar.customView.findViewById<View>(R.id.toolbar_display_pins_title)
        if (enablePins) {
            val onClickListener = View.OnClickListener {
                displayPins = !displayPins
                if (displayPins) {
                    filterView.visibility = View.VISIBLE
                    displayPinsButton.setImageResource(R.drawable.ic_pin_enabled_white)
                } else {
                    filterView.visibility = View.GONE
                    displayPinsButton.setImageResource(R.drawable.ic_pin_disabled_white)
                }
                populateMarkerItems()
            }
            displayPinsButton.setOnClickListener(onClickListener)
            displayPinsTitle.setOnClickListener(onClickListener)
        } else {
            displayPinsButton.visibility = View.GONE
            displayPinsTitle.visibility = View.GONE
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                val intent = Intent(context, MapSettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupActionButtons() {
        gpsPositionButton.setOnClickListener {
            if (currentGpsLocation != null) {
                mapView?.animateCameraTo(currentGpsLocation)
            }
        }
        setPositionButton.setOnClickListener {
            val location = mapView?.cameraLocation
            if (location != null) {
                updateLocation(location, locationSource == GameLog.LOCATION_SOURCE_MANUAL)
                val result = Intent()
                result.putExtra(MapViewerActivity.RESULT_LOCATION, location)
                result.putExtra(MapViewerActivity.RESULT_LOCATION_SOURCE, locationSource)
                val activity = activity as BaseActivity?
                activity?.setResult(Activity.RESULT_OK, result)
                activity?.finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        mapView = null
    }

    override fun onResume() {
        setEditMode(editMode)
        mapView?.onResume()
        if (shouldClearBackgroundCache()) {
            mapView?.clearBackgroundCache()
        }
        if (shouldClearVectorCaches()) {
            mapView?.clearVectorCaches()
        }
        mapView?.setMapTileType(AppPreferences.getMapTileSource(context))
        if (canEditLocation() || overlayMode) {
            locationClient = (activity as BaseActivity?)?.locationClient
            locationClient?.addListener(this)
        }
        if (overlayMode) {
            overlay.updateMapSettings()
        }
        mapView?.setMapExternalId(
            AppPreferences.getSelectedClubAreaMapId(context), AppPreferences.getInvertMapColors(
                context
            )
        )
        mapView?.setShowRhyBordersLayer(AppPreferences.getShowRhyBorders(context))
        mapView?.setShowValtionmaatLayer(AppPreferences.getShowValtionmaat(context))
        mapView?.setShowGameTrianglesLayer(AppPreferences.getShowGameTriangles(context))
        overlay.updateMhMooseAreaVisibility()
        overlay.updateMhPienriistaAreaVisibility()

        poiController?.externalId = AppPreferences.getSelectedClubAreaMapId(context)
        poiController?.viewModelLoadStatus?.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading -> {
                    poiFilterButton.visibility = View.GONE
                }
                ViewModelLoadStatus.LoadFailed -> {
                    Log.w("MapViewer", "Loading POIs failed")
                    poiFilterButton.visibility = View.GONE
                    poiViewModel = null
                    populateMarkerItems() // This will remove any possible existing markers from the map
                }
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = viewModelLoadStatus.viewModel
                    poiViewModel = viewModel.pois
                    poiFilterButton.visibility = if (model.getTypeSelected().value == GameLog.TYPE_POI) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    filterView.poiFilter = viewModel.pois?.filter
                    poiFilterButtonText.text =
                        poiFilterText(poiViewModel?.filter?.poiFilterType ?: PoiFilter.PoiFilterType.ALL)
                    populateMarkerItems()
                }
            }
        }?.disposeBy(disposeBag)

        loadPoisIfNotLoaded()

        super.onResume()
    }

    private fun loadPoisIfNotLoaded() {
        if (poiController?.getLoadedViewModelOrNull() != null) {
            return
        }

        MainScope().launch {
            poiController?.loadViewModel(refresh = false)
        }
    }

    override fun onPause() {
        val activity = activity
        if (activity is MainActivity || activity is MapViewerActivity) {
            (activity as FullScreenExpand).setFullscreenMode(false)
            isExpanded = false
        }
        super.onPause()
        if (overlayMode) {
            // Save camera location
            val camera = mapView?.cameraLocation
            val cameraZoomLevel = mapView?.cameraZoomLevel
            if (camera != null && cameraZoomLevel != null) {
                val loc = MapLocation()
                loc.latitude = camera.latitude
                loc.longitude = camera.longitude
                loc.zoom = cameraZoomLevel
                AppPreferences.setLastMapLocation(context, loc)
            }
        }
        locationClient?.removeListener(this)
        mapView?.onPause()

        disposeBag.disposeAll()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        if (clusterManager != null) {
            populateMarkerItems()
        }
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView?.onSaveInstanceState(mapViewBundle)
        poiController?.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    override fun onMapReady(map: GoogleMap) {
        val context = context
        if (startLocation != null) {
            if (overlayMode) {
                mapView?.moveCameraTo(startLocation)
            } else {
                setInitialLocation(startLocation)
            }
        }
        // Only show GPS state indicator when creating new entry.

        mapView?.setShowInfoWindow(newItem)
        mapView?.setShowAccuracy(true)
        map.setOnCameraMoveListener {
            if (enablePins) {
                val maxZoomLevel = mapView?.map?.maxZoomLevel
                val zoom = mapView?.map?.cameraPosition?.zoom
                if (maxZoomLevel != null && zoom != null && zoom < maxZoomLevel) {
                    resetMarkerClusters()
                }
            }
            overlay.updateCameraMoved()
        }
        overlay.updateCameraMoved()
        mapView?.setMapExternalId(
            AppPreferences.getSelectedClubAreaMapId(context),
            AppPreferences.getInvertMapColors(context)
        )
        mapView?.setShowRhyBordersLayer(AppPreferences.getShowRhyBorders(context))
        mapView?.setShowValtionmaatLayer(AppPreferences.getShowValtionmaat(context))
        mapView?.setShowGameTrianglesLayer(AppPreferences.getShowGameTriangles(context))
        overlay.updateMhMooseAreaVisibility()
        overlay.updateMhPienriistaAreaVisibility()
        setupClusterMarkers()
    }

    override fun onLocationChanged(location: Location) {
        currentGpsLocation = location
        gpsPositionButton.isEnabled = currentGpsLocation != null

        // Always show and save latest gps position when when creating entry unless manually set already.
        if (overlayMode || newItem && locationSource == GameLog.LOCATION_SOURCE_GPS) {
            mapView?.refreshLocationIndicators(location)
            locationSource = GameLog.LOCATION_SOURCE_MANUAL
        }
        overlay.setCurrentGpsLocation(currentGpsLocation)
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

    private fun setInitialLocation(location: Location?) {
        if (location != null) {
            mapView?.moveCameraTo(location)
            mapView?.refreshLocationIndicators(location)
        }
    }

    private fun canEditLocation(): Boolean {
        return editMode
    }

    private fun setEditMode(enabled: Boolean) {
        editMode = enabled
        gpsPositionButton.isEnabled = canEditLocation() && currentGpsLocation != null
        gpsPositionButton.visibility = if (canEditLocation()) View.VISIBLE else View.GONE
        setPositionButton.isEnabled = canEditLocation()
        setPositionButton.visibility = if (canEditLocation()) View.VISIBLE else View.GONE
    }

    private fun setupClusterMarkers() {
        val map = mapView?.map ?: return
        clusterManager = MapMarkerClusterManager(requireActivity(), map)
        val renderer = MapMarkerRenderer(requireContext(), map, clusterManager!!)
        renderer.registerPinAppearance(GameLog.TYPE_HARVEST, R.drawable.ic_pin_harvest)
        renderer.registerPinAppearance(GameLog.TYPE_OBSERVATION, R.drawable.ic_pin_observation)
        renderer.registerPinAppearance(GameLog.TYPE_SRVA, R.drawable.ic_pin_srva)
        // Pin appearance is not registered for TYPE_POI, as it is displayed as text
        clusterManager?.renderer = renderer
        clusterManager?.setOnClusterClickListener { cluster: Cluster<MapMarkerItem> ->
            val currentZoomLevel = map.cameraPosition.zoom

            // only show markers if users is in the max zoom level
            if (currentZoomLevel != EntryMapView.MAP_ZOOM_LEVEL_MAX) {
                mapView?.zoomBy(1.0f)
                return@setOnClusterClickListener true
            }
            expandClusterMarkers(cluster)
            true
        }
        clusterManager?.setOnClusterItemClickListener { mapMarkerItem: MapMarkerItem ->
            navigateToLogItem(mapMarkerItem)
            true
        }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        populateMarkerItems()
    }

    private fun resetMarkerClusters() {
        // get markers back to the original position if they were relocated
        clusterManager?.removeItems(markerItemsCache[MARKER_ADDED_LIST])
        clusterManager?.addItems(markerItemsCache[MARKER_DELETE_LIST])
        clusterManager?.cluster()
        markerItemsCache[MARKER_ADDED_LIST]?.clear()
        markerItemsCache[MARKER_DELETE_LIST]?.clear()
    }

    private fun expandClusterMarkers(cluster: Cluster<MapMarkerItem>) {
        // relocate the markers around the current cluster position
        var counter = 0
        val rotateFactor = 360.toFloat() / cluster.items.size
        for (item in cluster.items) {
            val lat =
                cluster.position.latitude + MARKER_DEFAULT_RADIUS * cos((++counter * rotateFactor).toDouble())
            val lng = cluster.position.longitude + MARKER_DEFAULT_RADIUS * sin((counter * rotateFactor).toDouble())
            val copy = MapMarkerItem(lat, lng, item.type, item.localId)
            clusterManager?.removeItem(item)
            clusterManager?.addItem(copy)
            clusterManager?.cluster()
            markerItemsCache[MARKER_ADDED_LIST]?.add(copy)
            markerItemsCache[MARKER_DELETE_LIST]?.add(item)
        }
    }

    private fun navigateToLogItem(mapMarkerItem: MapMarkerItem) {
        when (mapMarkerItem.type) {
            GameLog.TYPE_HARVEST -> {
                val harvest = harvestDatabase.getHarvestByLocalId(mapMarkerItem.localId.toInt())
                val intent = Intent(context, HarvestActivity::class.java)
                intent.putExtra(HarvestActivity.EXTRA_HARVEST, harvest)
                startActivity(intent)
            }
            GameLog.TYPE_OBSERVATION -> observationDatabase.loadObservation(mapMarkerItem.localId) { observation: GameObservation? ->
                val intent1 = Intent(context, EditActivity::class.java)
                intent1.putExtra(EditActivity.EXTRA_OBSERVATION, observation)
                startActivity(intent1)
            }
            GameLog.TYPE_SRVA -> SrvaDatabase.getInstance().loadEvent(mapMarkerItem.localId) { srvaEvent: SrvaEvent? ->
                val intent1 = Intent(context, EditActivity::class.java)
                intent1.putExtra(EditActivity.EXTRA_SRVA_EVENT, srvaEvent)
                startActivity(intent1)
            }
            GameLog.TYPE_POI -> {
                val poiGroupAndLocation = poiController?.findPoiLocationAndItsGroup(mapMarkerItem.localId)
                poiGroupAndLocation?.let { poi ->
                    val poiLocationGroup = poi.first
                    val poiLocation = poi.second
                    val intent = PoiLocationActivity.getIntent(requireContext(), poiLocationGroup, poiLocation)
                    startActivity(intent)
                }
            }
            else -> {
            }
        }
    }

    private fun populateMarkerItems() {
        if (clusterManager == null) {
            return
        } else if (!displayPins) {
            clusterManager?.clearItems()
            clusterManager?.cluster()
            return
        }
        clusterManager?.clearItems()
        when (Objects.requireNonNull(model.getTypeSelected().value)) {
            GameLog.TYPE_HARVEST -> {
                val harvests = harvestDatabase.allHarvests
                for (harvest in model.filterHarvestsWithCurrent(harvests)) {
                    val item = MapMarkerItem(
                        harvest.mLocation.latitude,
                        harvest.mLocation.longitude,
                        GameLog.TYPE_HARVEST,
                        harvest.mLocalId.toLong()
                    )
                    clusterManager?.addItem(item)
                }
                clusterManager?.cluster()
            }
            GameLog.TYPE_OBSERVATION -> observationDatabase.loadAllObservations { observations: List<GameObservation> ->
                for (observation in model.filterObservationsWithCurrent(observations)) {
                    val item = MapMarkerItem(
                        observation.toLocation().latitude,
                        observation.toLocation().longitude,
                        observation.type,
                        observation.localId
                    )
                    clusterManager?.addItem(item)
                }
                clusterManager?.cluster()
            }
            GameLog.TYPE_SRVA -> SrvaDatabase.getInstance().loadAllEvents { events: List<SrvaEvent> ->
                for (srva in model.filterSrvasWithCurrent(events)) {
                    val item = MapMarkerItem(
                        srva.toLocation().latitude,
                        srva.toLocation().longitude,
                        srva.type,
                        srva.localId
                    )
                    clusterManager?.addItem(item)
                }
                clusterManager?.cluster()
            }
            GameLog.TYPE_POI -> {
                poiViewModel?.filteredPois?.forEach { poi ->
                    poi.locations.forEach { poiLoc ->
                        val (latitude, longitude) = with(poiLoc.geoLocation) {
                            MapUtils.ETRMStoWGS84(latitude.toLong(), longitude.toLong())
                        }
                        val item = MapMarkerItem(
                            lat = latitude,
                            lon = longitude,
                            type = GameLog.TYPE_POI,
                            id = poiLoc.id,
                            poiText = "${poi.visibleId}-${poiLoc.visibleId}",
                            poiType = poi.type.value,
                        )
                        clusterManager?.addItem(item)
                    }
                }
                clusterManager?.cluster()
            }
        }
    }

    override fun setMeasuring(isMeasuring: Boolean) {
        this.isMeasuring = isMeasuring

        // Prevent measure buttons and poi filter button overlapping by hiding filter button while measuring
        poiFilterButton.visibility = (!isMeasuring && model.getTypeSelected().value == GameLog.TYPE_POI).toVisibility()
    }

    override fun onLogTypeSelected(type: String) {
        model.selectLogType(type)

        if (type == GameLog.TYPE_POI) {
            poiFilterButton.visibility = View.VISIBLE
            poiFilterButtonText.text = poiFilterText(poiViewModel?.filter?.poiFilterType  ?: PoiFilter.PoiFilterType.ALL)
            if (AppPreferences.getSelectedClubAreaMapId(requireContext()) == null) {
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.poi_select_external_id))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        // Do nothing
                    }
                    .show()
            }
        } else {
            poiFilterButton.visibility = View.GONE
        }
    }

    override fun onLogSeasonSelected(season: Int) {
        model.selectLogSeason(season)
    }

    override fun onLogSpeciesSelected(speciesIds: List<Int>) {
        model.selectSpeciesIds(speciesIds)
    }

    override fun onLogSpeciesCategorySelected(categoryId: Int) {
        model.selectSpeciesCategory(categoryId)
    }

    interface FullScreenExpand {
        fun setFullscreenMode(fullscreen: Boolean)
    }

    private fun poiFilterText(type: PoiFilter.PoiFilterType): String {
        val context = requireContext()
        return when (type) {
            PoiFilter.PoiFilterType.SIGHTING_PLACE -> context.getString(R.string.poi_filter_type_sighting_place)
            PoiFilter.PoiFilterType.MINERAL_LICK -> context.getString(R.string.poi_filter_type_mineral_lick)
            PoiFilter.PoiFilterType.FEEDING_PLACE -> context.getString(R.string.poi_filter_type_feeding_place)
            PoiFilter.PoiFilterType.OTHER -> context.getString(R.string.poi_filter_type_other)
            PoiFilter.PoiFilterType.ALL -> context.getString(R.string.poi_filter_type_all)
        }
    }

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val MARKER_DEFAULT_RADIUS = 0.0007
        private const val MARKER_DELETE_LIST = "itemsDeleted"
        private const val MARKER_ADDED_LIST = "itemsAdded"
        private const val CONTROLLER_STATE_PREFIX = "MV_poi_controller"

        @JvmStatic
        fun newInstance(enablePois: Boolean): MapViewer {
            val instance = MapViewer()
            instance.enablePois = enablePois
            instance.markerItemsCache[MARKER_ADDED_LIST] = ArrayList()
            instance.markerItemsCache[MARKER_DELETE_LIST] = ArrayList()
            instance.enablePins = true
            return instance
        }
    }
}
