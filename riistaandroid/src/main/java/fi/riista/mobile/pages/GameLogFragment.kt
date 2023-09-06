package fi.riista.mobile.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.ui.list.ListCommonHarvestsController
import fi.riista.common.domain.harvest.ui.settings.showActorSelection
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.ui.list.ListCommonObservationsController
import fi.riista.common.domain.srva.ui.list.ListCommonSrvaEventsController
import fi.riista.common.network.sync.SynchronizationLevel
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.adapter.GameLogAdapter
import fi.riista.mobile.database.HarvestDatabase.SeasonStats
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.feature.harvest.HarvestActivity
import fi.riista.mobile.feature.observation.ObservationActivity
import fi.riista.mobile.feature.srva.SrvaActivity
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.sync.SyncMode
import fi.riista.mobile.sync.SynchronizationEvent
import fi.riista.mobile.ui.AddMenuProvider
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.ui.GameLogFilterView.GameLogFilterListener
import fi.riista.mobile.ui.GameLogListItem
import fi.riista.mobile.ui.GameLogListItem.OnClickListItemListener
import fi.riista.mobile.ui.OwnHarvestsMenuProvider
import fi.riista.mobile.ui.RefreshMenuProvider
import fi.riista.mobile.ui.updateBasedOnViewModel
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.UiUtils.isSrvaVisible
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.toVisibility
import fi.riista.mobile.viewmodel.GameLogViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max

class GameLogFragment : PageFragment(), GameLogFilterListener, OnClickListItemListener, AppSync.AppSyncListener {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var userInfoStore: UserInfoStore

    @Inject
    internal lateinit var appSync: AppSync

    @Inject
    internal lateinit var syncConfig: SyncConfig

    private val disposeBag = DisposeBag()

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var adapter: GameLogAdapter
    private var displayItems: MutableList<GameLogListItem> = ArrayList()
    private val calendarYears = SparseArray<CalendarYear?>()
    private lateinit var filterView: GameLogFilterView
    private lateinit var model: GameLogViewModel
    private lateinit var forOthersTextView: TextView

    private val refreshMenuProvider by lazy {
        RefreshMenuProvider {
            startSync()
            true
        }
    }
    private val addMenuProvider by lazy {
        AddMenuProvider {
            startAddingEntity()
            true
        }
    }
    private val ownHarvestsMenuProvider by lazy {
        OwnHarvestsMenuProvider {
            toggleOwnHarvests()
            true
        }
    }
    private val harvestActivityResultLaunch = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> onHarvestActivityResult(result.resultCode, result.data)
    }
    private val observationActivityResultLaunch = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> onObservationActivityResult(result.resultCode, result.data)
    }
    private val srvaActivityResultLaunch = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> onSrvaActivityResult(result.resultCode, result.data)
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

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gamelog, container, false)
        setupActionBar(R.layout.actionbar_gamelog, true)

        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.srl_refresh_layout)
            ?.also { layout ->
                layout.setOnRefreshListener {
                    //layout.isRefreshing = false // use other indicator instead
                    appSync.scheduleImmediateSyncUsing(syncMode = SyncMode.SYNC_MANUAL)
                }
                layout.setColorSchemeResources(R.color.colorPrimary)
            }

        val context: Context = requireContext()
        val recyclerView = view.findViewById<RecyclerView>(R.id.historyListView)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        adapter = GameLogAdapter(context, displayItems, this)
        recyclerView.adapter = adapter
        filterView = view.findViewById(R.id.log_filter_view)
        filterView.listener = this
        model = ViewModelProvider(requireActivity(), viewModelFactory)[GameLogViewModel::class.java]
        if (GameLog.TYPE_POI == model.getTypeSelected().value) {
            // POIs are not supported here, default to harvests
            model.selectLogType(GameLog.TYPE_HARVEST)
        }
        model.isOwnHarvests().observe(viewLifecycleOwner) { ownHarvests ->
            ownHarvestsMenuProvider.setOwnHarvests(ownHarvests)
        }
        filterView.updateBasedOnViewModel(model, viewLifecycleOwner)

        model.refreshSeasons()
        filterView.setupTypes(
            isSrvaVisible(userInfoStore.getUserInfo()),
            false,
            model.getTypeSelected().value
        )
        filterView.setupSeasons(model.getSeasons().value, model.getSeasonSelected().value)
        filterView.setupSpecies(model.getSpeciesSelected().value!!, model.getCategorySelected().value)

        forOthersTextView = view.findViewById(R.id.tv_showing_harvest_for_others)

        requireActivity().addMenuProvider(ownHarvestsMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        requireActivity().addMenuProvider(refreshMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        requireActivity().addMenuProvider(addMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        updateOwnHarvestVisibility()
        return view
    }

    private fun loadHarvestsIfNotLoaded() {
        if (listHarvestsController.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        MainScope().launch {
            listHarvestsController.loadViewModel(refresh = true)
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

    private fun loadSrvasIfNotLoaded() {
        if (listSrvaEventsController.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        MainScope().launch {
            listSrvaEventsController.loadViewModel(refresh = true)
        }
    }

    private fun clearList() {
        displayItems.clear()
        adapter.notifyDataSetChanged()
    }

    private fun startAddingEntity() {
        val typeSelected = model.getTypeSelected().value
        if (GameLog.TYPE_HARVEST == typeSelected) {
            val intent = HarvestActivity.getLaunchIntentForCreating(requireActivity(), speciesCode = null)
            harvestActivityResultLaunch.launch(intent)
        } else if (GameLog.TYPE_OBSERVATION == typeSelected) {
            val intent = ObservationActivity.getLaunchIntentForCreating(requireActivity(), speciesCode = null)
            observationActivityResultLaunch.launch(intent)
        } else if (GameLog.TYPE_SRVA == typeSelected) {
            val intent = SrvaActivity.getLaunchIntentForCreating(requireActivity())
            srvaActivityResultLaunch.launch(intent)
        }
    }

    private fun startSync() {
        if (appSync.scheduleImmediateSyncUsing(syncMode = SyncMode.SYNC_MANUAL)) {
            // user initiated manual synchronization using refresh button
            // -> prevent other sync possibility (swipe-to-refresh)
            swipeRefreshLayout?.isEnabled = false
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

    override fun onStart() {
        super.onStart()
        setViewTitle(getString(R.string.title_game_log))

        appSync.manualSynchronizationPossible.bindAndNotify { manualSyncPossible ->
            // don't disable swipe-to-refresh as that would remove swipe-to-refresh indicator
            // if manual sync has been started using swipe-to-refresh
            updateSynchronizationUI(
                manualSyncPossible = manualSyncPossible,
                allowDisablingSwipeToRefresh = false,
            )
        }.disposeBy(disposeBag)
        appSync.addSyncListener(listener = this, notifyImmediately = false)
        listSrvaEventsController.viewModelLoadStatus.bind { loadStatus ->
            if (loadStatus is ViewModelLoadStatus.Loaded) {
                val type = model.getTypeSelected().value
                if (type == GameLog.TYPE_SRVA) {
                    listSrvaEventsController.getLoadedViewModelOrNull()?.filteredSrvaEvents?.let { srvaEvents ->
                        val items = srvaEvents.map { srvaEvent ->
                            GameLogListItem.fromSrva(srvaEvent)
                        }
                        addItems(items, true)
                    }
                }
            }
        }.disposeBy(disposeBag)
        listHarvestsController.viewModelLoadStatus.bind { loadStatus ->
            if (loadStatus is ViewModelLoadStatus.Loaded) {
                val type = model.getTypeSelected().value
                if (type == GameLog.TYPE_HARVEST) {
                    listHarvestsController.getLoadedViewModelOrNull()?.filteredHarvests?.let { harvests ->
                        val items = harvests.map { harvest ->
                            GameLogListItem.fromHarvest(harvest)
                        }
                        addItems(items, false)
                    }
                }
            }
        }.disposeBy(disposeBag)
        listObservationsController.viewModelLoadStatus.bind { loadStatus ->
            if (loadStatus is ViewModelLoadStatus.Loaded) {
                val type = model.getTypeSelected().value
                if (type == GameLog.TYPE_OBSERVATION) {
                    listObservationsController.getLoadedViewModelOrNull()?.filteredObservations?.let { observations ->
                        val items = observations.map { observation ->
                            GameLogListItem.fromObservation(observation)
                        }
                        addItems(items, false)
                    }
                }
            }
        }.disposeBy(disposeBag)
        updateFilters()
    }

    override fun onStop() {
        super.onStop()
        disposeBag.disposeAll()
        appSync.removeSyncListener(this)
    }

    override fun onResume() {
        super.onResume()
        ensureCorrectOwnHarvestsStatus()
        updateOwnHarvestVisibility()
        updateForOthersTextVisibility()

        updateSynchronizationUI(
            manualSyncPossible = appSync.manualSynchronizationPossible.value,
            allowDisablingSwipeToRefresh = true
        )
    }

    private fun updateSynchronizationUI(manualSyncPossible: Boolean, allowDisablingSwipeToRefresh: Boolean) {
        updateManualSyncButtonIndicator(manualSyncPossible)

        // update swipe-to-refresh state as it may have been used to start the sync or it
        // may have been disabled because of manual sync
        if (manualSyncPossible) {
            swipeRefreshLayout?.isRefreshing = false
            swipeRefreshLayout?.isEnabled = true
        } else {
            if (allowDisablingSwipeToRefresh) {
                swipeRefreshLayout?.isEnabled = false
            }
        }
    }

    private fun updateManualSyncButtonIndicator(manualSyncPossible: Boolean) {
        refreshMenuProvider.setCanRefresh(manualSyncPossible)
        refreshMenuProvider.setVisibility(syncConfig.syncMode == SyncMode.SYNC_MANUAL)
    }

    private fun onHarvestActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (HarvestActivity.getHarvestCreatedOrModified(data.extras)) {
                updateFilters()
            }
        }
    }

    private fun onObservationActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (ObservationActivity.getObservationCreatedOrModified(data.extras)) {
                updateFilters()
            }
        }
    }

    private fun onSrvaActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (SrvaActivity.getSrvaEventCreatedOrModified(data.extras)) {
                // assume srva is already saved, no need for automatic sync
                updateFilters()
            }
        }
    }

    private fun addItems(items: List<GameLogListItem>, hideStats: Boolean) {
        if (activity != null) {
            clearList()

            setupCalendarYears(items)

            displayItems = items.toMutableList()
            for (i in items.indices.reversed()) {
                val item = items[i]
                if (i == 0) {
                    val sectionItem = GameLogListItem()
                    sectionItem.isHeader = true
                    sectionItem.month = items[i].dateTime!![Calendar.MONTH]
                    sectionItem.year = items[i].dateTime!![Calendar.YEAR]
                    displayItems.add(i, sectionItem)
                } else {
                    val prevItem = items[i - 1]
                    if (item.month != prevItem.month) {
                        val sectionItem = GameLogListItem()
                        sectionItem.isHeader = true
                        sectionItem.month = items[i].dateTime!![Calendar.MONTH]
                        sectionItem.year = items[i].dateTime!![Calendar.YEAR]
                        displayItems.add(i, sectionItem)
                    }
                }
            }
            insertSeparators(displayItems)
            if (!hideStats) {
                val statsItem = GameLogListItem()
                statsItem.isStats = true
                displayItems.add(0, statsItem)
            }
            adapter.setItems(displayItems)
        }
    }

    private fun insertSeparators(items: List<GameLogListItem>) {
        for (i in items.indices) {
            val item = items[i]
            if (i == 0) {
                // First item is always month header
                item.isTimelineTopVisible = false
                item.isTimelineBottomVisible = false
            } else if (i == 1 && i == items.size - 1) {
                item.isTimelineTopVisible = false
                item.isTimelineBottomVisible = false
            } else if (i == 1) {
                item.isTimelineTopVisible = false
                item.isTimelineBottomVisible = true
            } else if (i == items.size - 1) {
                item.isTimelineTopVisible = true
                item.isTimelineBottomVisible = false
            } else {
                item.isTimelineTopVisible = true
                item.isTimelineBottomVisible = true
            }
        }
    }

    private fun setupCalendarYears(events: List<GameLogListItem>) {
        calendarYears.clear()
        var newestYear = 0
        var newestHuntingYear = -1
        for (event in events) {
            var huntingYear = DateTimeUtils.getHuntingYearForCalendar(event.dateTime!!)
            if (huntingYear > newestHuntingYear) {
                newestHuntingYear = huntingYear
            }
            val realYear = event.dateTime!![Calendar.YEAR]
            if (realYear > newestYear) {
                newestYear = realYear
            }

            // Some observation types don't have amounts, but count them as 1 anyway
            var amount = max(event.totalSpecimenAmount ?: 0, 1)
            if (GameLog.TYPE_SRVA == event.type) {
                // SRVA events use normal years for grouping and each event
                // should be counted as one
                huntingYear = realYear
                amount = 1
            }
            var calendarYear = calendarYears[huntingYear]
            if (calendarYear == null) {
                calendarYear = CalendarYear()
                calendarYear.year = huntingYear
                calendarYear.statistics = SeasonStats()
                calendarYears.put(huntingYear, calendarYear)
            }
            val category = SpeciesInformation.categoryForSpecies(event.speciesCode)
            if (category != null) {
                val currentValue = calendarYear.statistics!!.mCategoryData[category.mId]
                val resultValue = amount + currentValue
                calendarYear.statistics!!.mCategoryData.put(category.mId, resultValue)
            }
        }
        val selectedSeason = model.getSeasonSelected().value!!
        if (calendarYears[selectedSeason] == null) {
            val year = CalendarYear()
            year.year = selectedSeason
            year.statistics = SeasonStats()
            calendarYears.put(year.year, year)
        }
        filterView.setupSeasons(model.getSeasons().value, selectedSeason)
        val calendarYear = calendarYears[selectedSeason]
        adapter.setStats(calendarYear!!.statistics)
    }

    override fun onLogTypeSelected(type: String) {
        if (type != model.getTypeSelected().value) {
            model.selectLogType(type)
            updateOwnHarvestVisibility()
            updateFilters()
        }
    }

    override fun onLogSeasonSelected(season: Int) {
        if (season != model.getSeasonSelected().value) {
            model.selectLogSeason(season)
            updateFilters()
        }
    }

    override fun onLogSpeciesSelected(speciesIds: List<Int>) {
        if (speciesIds != model.getSpeciesSelected()) {
            // updating model updates filterview
            model.selectSpeciesIds(speciesIds)
            updateFilters()
        }
    }

    override fun onLogSpeciesCategorySelected(categoryId: Int) {
        if (categoryId != model.getCategorySelected().value) {
            // updating model updates filterview
            model.selectSpeciesCategory(categoryId)
            updateFilters()
        }
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

    override fun onItemClick(item: GameLogListItem) {
        when (item.type) {
            GameLog.TYPE_HARVEST -> {
                item.mHarvest?.let {
                    val intent = HarvestActivity.getLaunchIntentForViewing(requireActivity(), it)
                    harvestActivityResultLaunch.launch(intent)
                }
            }
            GameLog.TYPE_OBSERVATION -> {
                // todo: ensure observation category remains selected (same as previously)
                // item.mObservation.observationCategorySelected = true; // This selection is not stored so set it to true, as it must have been selected as observation is saved.
                item.mObservation?.let {
                    val intent = ObservationActivity.getLaunchIntentForViewing(requireActivity(), it)
                    observationActivityResultLaunch.launch(intent)
                }
            }
            GameLog.TYPE_SRVA -> {
                item.mSrva?.let {
                    val intent = SrvaActivity.getLaunchIntentForViewing(requireActivity(), it)
                    srvaActivityResultLaunch.launch(intent)
                }
            }
        }
    }

    override fun onSynchronizationEvent(synchronizationEvent: SynchronizationEvent) {
        if (synchronizationEvent is SynchronizationEvent.Completed &&
            synchronizationEvent.synchronizationLevel == SynchronizationLevel.USER_CONTENT) {
            updateFilters()
        }
    }

    override fun onSynchronizationScheduled(isImmediateUserContentSync: Boolean) {
        // nop
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

    private class CalendarYear {
        var year = 0
        var statistics: SeasonStats? = null
    }
}
