package fi.riista.mobile.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.maps.android.clustering.Cluster
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.ui.list.ListCommonHarvestsController
import fi.riista.common.domain.harvest.ui.settings.showActorSelection
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.ui.list.ListCommonObservationsController
import fi.riista.common.domain.poi.ui.PoiController
import fi.riista.common.domain.poi.ui.PoiFilter
import fi.riista.common.domain.poi.ui.PoiViewModel
import fi.riista.common.domain.srva.ui.list.ListCommonSrvaEventsController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.util.toLocation
import fi.riista.mobile.EntryMapView
import fi.riista.mobile.LocationClient
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.activity.MapSettingsActivity
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.feature.harvest.HarvestActivity
import fi.riista.mobile.feature.observation.ObservationActivity
import fi.riista.mobile.feature.poi.PoiFilterFragment
import fi.riista.mobile.feature.poi.PoiLocationActivity
import fi.riista.mobile.feature.srva.SrvaActivity
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.ui.GameLogFilterView.GameLogFilterListener
import fi.riista.mobile.ui.MapOverlayView
import fi.riista.mobile.ui.MapOverlayView.MapViewerInterface
import fi.riista.mobile.ui.OwnHarvestsMenuProvider
import fi.riista.mobile.ui.SettingsMenuProvider
import fi.riista.mobile.ui.updateBasedOnViewModel
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.AppPreferences.MapLocation
import fi.riista.mobile.utils.CacheClearTracker.shouldClearBackgroundCache
import fi.riista.mobile.utils.CacheClearTracker.shouldClearVectorCaches
import fi.riista.mobile.utils.MapMarkerClusterManager
import fi.riista.mobile.utils.MapMarkerItem
import fi.riista.mobile.utils.MapMarkerRenderer
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.toVisibility
import fi.riista.mobile.viewmodel.GameLogViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

class MapViewer : DetailsPageFragment(), LocationListener, OnMapReadyCallback, GameLogFilterListener,
    MapViewerInterface, PoiLocationActivity.CenterMapListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userInfoStore: UserInfoStore

    private lateinit var displayMarkersButton: AppCompatImageButton
    private lateinit var displayMarkersTitle: TextView

    private var isExpanded = false
    private var editMode = false
    private var mapExternalId: String? = null
    private var startLocation: Location? = null
    private var newItem = false
    private var showItems = true
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
    private var displayMarkers = true
    private var isMeasuring = false
    private lateinit var model: GameLogViewModel
    private var enableMarkers = false
    private val markerItemsCache: MutableMap<String, MutableList<MapMarkerItem>> = HashMap()
    private lateinit var forOthersTextView: TextView

    private var enablePois = false
    private var poiController: PoiController? = null
    private lateinit var poiFilterButton: LinearLayout
    private lateinit var poiFilterButtonText: TextView
    private val disposeBag = DisposeBag()
    private var poiViewModel: PoiViewModel? = null
    private lateinit var stringProvider: StringProvider

    private val settingsMenuProvider by lazy {
        SettingsMenuProvider {
            val intent = Intent(context, MapSettingsActivity::class.java)
            startActivity(intent)
            true
        }
    }
    private val ownHarvestsMenuProvider by lazy {
        OwnHarvestsMenuProvider {
            toggleOwnHarvests()
            true
        }
    }
    private val listSrvaEventsController = ListCommonSrvaEventsController(
        metadataProvider = RiistaSDK.metadataProvider,
        srvaContext = RiistaSDK.srvaContext,
        listOnlySrvaEventsWithImages = false,
    )
    private val listObservationsController = ListCommonObservationsController(
        metadataProvider = RiistaSDK.metadataProvider,
        observationContext = RiistaSDK.observationContext,
        listOnlyObservationsWithImages = false,
    )
    private val listHarvestsController = ListCommonHarvestsController(
        harvestContext = RiistaSDK.harvestContext,
        listOnlyHarvestsWithImages = false,
    )

    private val poiResultLaunch = PoiLocationActivity.registerForActivityResult(this) { location ->
        centerMapTo(location)
    }

    private val harvestActivityResultLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            onHarvestActivityResult(result.resultCode, result.data)
        }
    private val observationActivityResultLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            onObservationActivityResult(result.resultCode, result.data)
        }
    private val srvaActivityResultLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            onSrvaActivityResult(result.resultCode, result.data)
        }

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
        showItems = arguments.getBoolean(MapViewerActivity.EXTRA_SHOW_ITEMS, true)
        locationSource = arguments.getString(MapViewerActivity.EXTRA_LOCATION_SOURCE, GameLog.LOCATION_SOURCE_MANUAL)
        mapExternalId = arguments.getString(MapViewerActivity.EXTRA_EXTERNAL_ID)

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
        filterView.centerMapListener = this
        stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        poiFilterButton = view.findViewById(R.id.poiFilterButton)
        poiFilterButtonText = view.findViewById(R.id.poiFilterButtonText)
        poiFilterButton.setOnClickListener {
            val filter = poiViewModel?.filter ?: PoiFilter(PoiFilter.PoiFilterType.ALL)
            val poiFilterFragment = PoiFilterFragment.create(filter)
            poiFilterFragment.show(requireActivity().supportFragmentManager, poiFilterFragment.tag)
        }

        model = ViewModelProvider(requireActivity(), viewModelFactory)[GameLogViewModel::class.java]
        if (enableMarkers) {
            model.refreshSeasons()
            filterView.setupTypes(
                UiUtils.isSrvaVisible(userInfoStore.getUserInfo()),
                true,
                model.getTypeSelected().value
            )
            filterView.setupSeasons(model.getSeasons().value, model.getSeasonSelected().value)
            filterView.setupSpecies(model.getSpeciesSelected().value!!, model.getCategorySelected().value)
        }
        model.isOwnHarvests().observe(viewLifecycleOwner) { ownHarvests ->
            ownHarvestsMenuProvider.setOwnHarvests(ownHarvests)
        }
        filterView.updateBasedOnViewModel(model, viewLifecycleOwner)

        model.getTypeSelected().observe(viewLifecycleOwner) { populateMarkerItems() }
        model.getSpeciesSelected().observe(viewLifecycleOwner) { populateMarkerItems() }
        model.getSeasonSelected().observe(viewLifecycleOwner) { populateMarkerItems() }

        if (enablePois) {
            val externalId = AppPreferences.getSelectedClubAreaMapId(requireContext())
            poiController = PoiController(RiistaSDK.poiContext, externalId)
        }
        savedInstanceState?.let {
            poiController?.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }
        forOthersTextView = view.findViewById(R.id.tv_showing_harvest_for_others)
        setupActionBar()
        setupActionButtons()

        requireActivity().addMenuProvider(ownHarvestsMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        requireActivity().addMenuProvider(settingsMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        ownHarvestsMenuProvider.setOwnHarvests(model.isOwnHarvests().value ?: true)
        updateOwnHarvestVisibility()

        return view
    }

    private fun setupActionBar() {
        val activity = requireActivity() as AppCompatActivity
        val actionBar = requireNotNull(activity.supportActionBar)

        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setCustomView(R.layout.actionbar_map_viewer)

        displayMarkersButton = actionBar.customView.findViewById(R.id.toolbar_display_markers_button)
        displayMarkersTitle = actionBar.customView.findViewById(R.id.toolbar_display_markers_title)

        if (enableMarkers) {
            val onClickListener = View.OnClickListener {
                displayMarkers = !displayMarkers
                setupMarkerButton()
                populateMarkerItems()
            }
            displayMarkersButton.setOnClickListener(onClickListener)
            displayMarkersTitle.setOnClickListener(onClickListener)
        } else {
            displayMarkersButton.visibility = View.GONE
            displayMarkersTitle.visibility = View.GONE
        }
    }

    private fun setupMarkerButton() {
        if (displayMarkers) {
            displayMarkersTitle.text = getString(R.string.map_hide_markers)
            filterView.visibility = View.VISIBLE
            displayMarkersButton.setImageResource(R.drawable.ic_pin_disabled_white)
        } else {
            displayMarkersTitle.text = getString(R.string.map_show_markers)
            filterView.visibility = View.GONE
            displayMarkersButton.setImageResource(R.drawable.ic_pin_enabled_white)
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
            AppPreferences.getSelectedClubAreaMapId(context),
            AppPreferences.getInvertMapColors(context)
        )
        mapView?.setShowRhyBordersLayer(AppPreferences.getShowRhyBorders(context))
        mapView?.setShowValtionmaatLayer(AppPreferences.getShowValtionmaat(context))
        mapView?.setShowGameTrianglesLayer(AppPreferences.getShowGameTriangles(context))
        mapView?.setShowLeadShotBanLayer(AppPreferences.getShowLeadShotBan(context))
        mapView?.setShowMooseRestrictionsLayer(AppPreferences.getShowMooseRestrictions(context))
        mapView?.setShowSmallGameRestrictionsLayer(AppPreferences.getShowSmallGameRestrictions(context))
        mapView?.setShowAviHuntingBanLayer(AppPreferences.getShowAviHuntingBan(context))
        overlay.updateMhMooseAreaVisibility()
        overlay.updateMhPienriistaAreaVisibility()
        if (enableMarkers) {
            displayMarkers = AppPreferences.getShowMapMarkers(context)
            setupMarkerButton()
        }

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
        listSrvaEventsController.viewModelLoadStatus.bind { loadStatus ->
            val type = model.getTypeSelected().value
            if (loadStatus is ViewModelLoadStatus.Loaded && type == GameLog.TYPE_SRVA) {
                populateMarkerItems()
            }
        }.disposeBy(disposeBag)
        listHarvestsController.viewModelLoadStatus.bind { loadStatus ->
            val type = model.getTypeSelected().value
            if (loadStatus is ViewModelLoadStatus.Loaded && type == GameLog.TYPE_HARVEST) {
                populateMarkerItems()
            }
        }.disposeBy(disposeBag)
        listObservationsController.viewModelLoadStatus.bind { loadStatus ->
            val type = model.getTypeSelected().value
            if (loadStatus is ViewModelLoadStatus.Loaded && type == GameLog.TYPE_OBSERVATION) {
                populateMarkerItems()
            }
        }.disposeBy(disposeBag)

        updateFilters()
        loadPoisIfNotLoaded()
        ensureCorrectOwnHarvestsStatus()
        updateOwnHarvestVisibility()
        updateForOthersTextVisibility()
        super.onResume()
    }

    private fun loadPoisIfNotLoaded() {
        MainScope().launch {
            poiController?.loadViewModel(refresh = false)
        }
    }

    private fun loadSrvasIfNotLoaded() {
        if (listSrvaEventsController.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        MainScope().launch {
            listSrvaEventsController.loadViewModel(refresh = true)
        }
    }

    private fun loadObservationsIfNotLoaded() {
        if (listObservationsController.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        MainScope().launch {
            listObservationsController.loadViewModel(refresh = true)
        }
    }

    private fun loadHarvestsIfNotLoaded() {
        if (listHarvestsController.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        MainScope().launch {
            listHarvestsController.loadViewModel(refresh = true)
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
        AppPreferences.setShowMapMarkers(context, displayMarkers)
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
            if (enableMarkers) {
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
        if (mapExternalId != null) {
            mapView?.setMapExternalId2(
                mapExternalId,
                true,
            )
        }
        mapView?.setShowRhyBordersLayer(AppPreferences.getShowRhyBorders(context))
        mapView?.setShowValtionmaatLayer(AppPreferences.getShowValtionmaat(context))
        mapView?.setShowGameTrianglesLayer(AppPreferences.getShowGameTriangles(context))
        mapView?.setShowLeadShotBanLayer(AppPreferences.getShowLeadShotBan(context))
        mapView?.setShowMooseRestrictionsLayer(AppPreferences.getShowMooseRestrictions(context))
        mapView?.setShowSmallGameRestrictionsLayer(AppPreferences.getShowSmallGameRestrictions(context))
        mapView?.setShowAviHuntingBanLayer(AppPreferences.getShowAviHuntingBan(context))
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

    @SuppressLint("PotentialBehaviorOverride")
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
                listHarvestsController.getLoadedViewModelOrNull()?.getByLocalId(mapMarkerItem.localId)?.let { harvest ->
                    val intent = HarvestActivity.getLaunchIntentForViewing(requireActivity(), harvest)
                    harvestActivityResultLaunch.launch(intent)
                }
            }
            GameLog.TYPE_OBSERVATION -> {
                listObservationsController.getLoadedViewModelOrNull()?.getByLocalId(mapMarkerItem.localId)?.let { observation ->
                    val intent = ObservationActivity.getLaunchIntentForViewing(requireActivity(), observation)
                    observationActivityResultLaunch.launch(intent)
                }
            }
            GameLog.TYPE_SRVA -> {
                listSrvaEventsController.getLoadedViewModelOrNull()?.getByLocalId(mapMarkerItem.localId)?.let { srvaEvent ->
                    val intent = SrvaActivity.getLaunchIntentForViewing(requireActivity(), srvaEvent)
                    srvaActivityResultLaunch.launch(intent)
                }
            }
            GameLog.TYPE_POI -> {
                val poiGroupAndLocation = poiController?.findPoiLocationAndItsGroup(mapMarkerItem.localId)
                poiGroupAndLocation?.let { poi ->
                    val context = requireContext()
                    val poiLocationGroup = poi.first
                    val poiLocation = poi.second
                    val intent = PoiLocationActivity.getIntent(
                        context = context,
                        externalId = AppPreferences.getSelectedClubAreaMapId(requireContext()),
                        poiLocationGroup = poiLocationGroup,
                        poiLocation = poiLocation,
                    )
                    poiResultLaunch.launch(intent)
                }
            }
            else -> {
            }
        }
    }

    private fun populateMarkerItems() {
        if (clusterManager == null || !showItems) {
            return
        } else if (!displayMarkers) {
            clusterManager?.clearItems()
            clusterManager?.cluster()
            return
        }
        clusterManager?.clearItems()
        when (Objects.requireNonNull(model.getTypeSelected().value)) {
            GameLog.TYPE_HARVEST -> {
                listHarvestsController.getLoadedViewModelOrNull()?.filteredHarvests?.let { harvests ->
                    for (harvest in harvests) {
                        harvest.localId?.let { localId ->
                            val location = harvest.geoLocation.toLocation()
                            val item = MapMarkerItem(
                                location.latitude,
                                location.longitude,
                                GameLog.TYPE_HARVEST,
                                localId,
                            )
                            clusterManager?.addItem((item))
                        }
                    }
                }
                clusterManager?.cluster()
            }
            GameLog.TYPE_OBSERVATION -> {
                listObservationsController.getLoadedViewModelOrNull()?.filteredObservations?.let { observations ->
                    for (observation in observations) {
                        observation.localId?.let { localId ->
                            val location = observation.location.toLocation()
                            val item = MapMarkerItem(
                                location.latitude,
                                location.longitude,
                                GameLog.TYPE_OBSERVATION,
                                localId
                            )
                            clusterManager?.addItem(item)
                        }
                    }
                    clusterManager?.cluster()
                }
            }
            GameLog.TYPE_SRVA -> {
                listSrvaEventsController.getLoadedViewModelOrNull()?.filteredSrvaEvents?.let { srvaEvents ->
                    for (srva in srvaEvents) {
                        srva.localId?.let { localId ->
                            val location = srva.location.toLocation()
                            val item = MapMarkerItem(
                                location.latitude,
                                location.longitude,
                                GameLog.TYPE_SRVA,
                                localId
                            )
                            clusterManager?.addItem(item)
                        }
                    }
                    clusterManager?.cluster()
                }
            }
            GameLog.TYPE_POI -> {
                poiViewModel?.filteredPois?.forEach { poi ->
                    poi.locations.forEach { poiLoc ->
                        val location = poiLoc.geoLocation.toLocation()
                        val item = MapMarkerItem(
                            lat = location.latitude,
                            lon = location.longitude,
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
        updateOwnHarvestVisibility()

        if (type == GameLog.TYPE_POI) {
            poiFilterButton.visibility = View.VISIBLE
            poiFilterButtonText.text = poiFilterText(poiViewModel?.filter?.poiFilterType ?: PoiFilter.PoiFilterType.ALL)
            if (AppPreferences.getSelectedClubAreaMapId(requireContext()) == null) {
                AlertDialogFragment.Builder(requireContext(), AlertDialogId.MAP_VIEWER_POI_INFO)
                    .setMessage(getString(R.string.poi_select_external_id))
                    .setPositiveButton(R.string.ok)
                    .build()
                    .show(parentFragmentManager)
            }
        } else {
            poiFilterButton.visibility = View.GONE
        }
    }

    override fun onLogSeasonSelected(season: Int) {
        model.selectLogSeason(season)
        updateFilters()
    }

    override fun onLogSpeciesSelected(speciesIds: List<Int>) {
        model.selectSpeciesIds(speciesIds)
        updateFilters()
    }

    override fun onLogSpeciesCategorySelected(categoryId: Int) {
        model.selectSpeciesCategory(categoryId)
        updateFilters()
    }

    private fun updateFilters() {
        val ownHarvest = model.isOwnHarvests().value ?: true
        val huntingYear = model.getSeasonSelected().value ?: return
        val species = model.getSpeciesSelected().value?.map { speciesCode ->
            if (speciesCode == null) {
                 Species.Other
            } else {
                Species.Known(speciesCode)
            }
        } ?: emptyList()

        when (model.getTypeSelected().value) {
            GameLog.TYPE_HARVEST -> {
                MainScope().launch {
                    loadHarvestsIfNotLoaded()
                    listHarvestsController.setFilters(ownHarvest, huntingYear, species)
                }
            }
            GameLog.TYPE_OBSERVATION -> {
                loadObservationsIfNotLoaded()
                listObservationsController.setFilters(huntingYear, species)
            }
            GameLog.TYPE_SRVA -> {
                loadSrvasIfNotLoaded()
                listSrvaEventsController.setFilters(huntingYear, species)
            }
        }
    }

    override fun centerMapTo(location: Location) {
        mapView?.moveCameraTo(location)
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

    private fun onHarvestActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (HarvestActivity.getHarvestCreatedOrModified(data.extras)) {
                loadHarvestsIfNotLoaded()
            }
        }
    }

    private fun onObservationActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (ObservationActivity.getObservationCreatedOrModified(data.extras)) {
                loadObservationsIfNotLoaded()
            }
        }
    }

    private fun onSrvaActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (SrvaActivity.getSrvaEventCreatedOrModified(data.extras)) {
                // assume srva is already saved, no need for automatic sync
                loadSrvasIfNotLoaded()
            }
        }
    }

    private fun toggleOwnHarvests() {
        MainScope().launch {
            val previousOwnHarvests = model.isOwnHarvests().value ?: true
            model.setOwnHarvests(!previousOwnHarvests)
            listHarvestsController.setOwnHarvestsFilter(!previousOwnHarvests)
            ownHarvestsMenuProvider.setOwnHarvests(!previousOwnHarvests)
            updateForOthersTextVisibility()
        }
    }

    private fun updateOwnHarvestVisibility() {
        val showOwnHarvestsToggle = RiistaSDK.preferences.showActorSelection() &&
                model.getTypeSelected().value == GameLog.TYPE_HARVEST
        ownHarvestsMenuProvider.setVisibility(showOwnHarvestsToggle)
    }

    private fun ensureCorrectOwnHarvestsStatus() {
        // If actor selection is disabled then make sure that own harvests are selected in model
        if (!RiistaSDK.preferences.showActorSelection() && model.isOwnHarvests().value == false) {
            MainScope().launch {
                model.setOwnHarvests(true)
                listHarvestsController.setOwnHarvestsFilter(true)
                ownHarvestsMenuProvider.setOwnHarvests(true)
                updateForOthersTextVisibility()
            }
        }
    }

    private fun updateForOthersTextVisibility() {
        val ownHarvests = model.isOwnHarvests().value ?: true
        forOthersTextView.visibility = (!ownHarvests).toVisibility()
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
            instance.enableMarkers = true
            return instance
        }
    }
}
