package fi.riista.mobile.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
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
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.ui.GameLogFilterView.GameLogFilterListener
import fi.riista.mobile.ui.GameLogListItem
import fi.riista.mobile.ui.GameLogListItem.OnClickListItemListener
import fi.riista.mobile.utils.Constants
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.UiUtils.isSrvaVisible
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.viewmodel.GameLogViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.*
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

    private var refreshItem: MenuItem? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var adapter: GameLogAdapter
    private var displayItems: MutableList<GameLogListItem> = ArrayList()
    private val calendarYears = SparseArray<CalendarYear?>()
    private lateinit var filterView: GameLogFilterView
    private lateinit var model: GameLogViewModel

    private val harvestActivityResultLaunch = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> onHarvestActivityResult(result.resultCode, result.data)
    }
    private val observationActivityResultLaunch = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> onObservationActivityResult(result.resultCode, result.data)
    }
    private val srvaActivityResultLaunch = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> onSrvaActivityResult(result.resultCode, result.data)
    }

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
                    appSync.synchronizeUsing(syncMode = SyncMode.SYNC_MANUAL)
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
        model.refreshSeasons()
        filterView.setupTypes(
            isSrvaVisible(userInfoStore.getUserInfo()),
            false,
            model.getTypeSelected().value
        )
        filterView.setupSeasons(model.getSeasons().value, model.getSeasonSelected().value)
        filterView.setupSpecies(model.getSpeciesSelected().value!!, model.getCategorySelected().value)
        refreshList()
        return view
    }

    private fun refreshList() {
        model.refreshSeasons()
        val selection = model.getTypeSelected().value
        if (GameLog.TYPE_HARVEST == selection) {
            loadHarvests()
        } else if (GameLog.TYPE_OBSERVATION == selection) {
            loadObservations()
        } else if (GameLog.TYPE_SRVA == selection) {
            loadSrvas()
        } else if (selection != null) {
            throw RuntimeException("Unsupported selection type: $selection")
        }
    }

    private fun loadHarvests() {
        val harvestProvider = RiistaSDK.harvestContext.harvestProvider
        MainScope().launch {
            harvestProvider.fetch(refresh = false)
            val harvests = harvestProvider.harvests
            val items = harvests?.map { harvest ->
                GameLogListItem.fromHarvest(harvest)
            } ?: emptyList()
            addItems(items, false)
        }
    }

    private fun loadObservations() {
        val observationProvider = RiistaSDK.observationContext.observationProvider
        MainScope().launch {
            observationProvider.fetch(refresh = false)
            val observations = observationProvider.observations
            val items = observations?.map { observation ->
                GameLogListItem.fromObservation(observation)
            } ?: emptyList()
            addItems(items, false)
        }
    }

    private fun loadSrvas() {
        val srvaEventProvider = RiistaSDK.srvaContext.srvaEventProvider
        MainScope().launch {
            srvaEventProvider.fetch(refresh = false)
            val srvaEvents = srvaEventProvider.srvaEvents
            val items = srvaEvents?.map { event ->
                GameLogListItem.fromSrva(event)
            } ?: emptyList()
            addItems(items, true)
        }
    }

    private fun clearList() {
        displayItems.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
        inflater.inflate(R.menu.menu_refresh, menu)

        // Show/hide refresh button according to sync settings.
        refreshItem = menu.findItem(R.id.item_refresh)
        updateManualSyncButtonIndicator(manualSyncPossible = appSync.manualSynchronizationPossible.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val typeSelected = model.getTypeSelected().value
        return when (item.itemId) {
            R.id.item_add -> {
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
                true
            }
            R.id.item_refresh -> {
                if (appSync.synchronizeUsing(syncMode = SyncMode.SYNC_MANUAL)) {
                    // user initiated manual synchronization using refresh button
                    // -> prevent other sync possibility (swipe-to-refresh)
                    swipeRefreshLayout?.isEnabled = false
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
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
    }

    override fun onStop() {
        super.onStop()
        disposeBag.disposeAll()
        appSync.removeSyncListener(this)
    }

    override fun onResume() {
        super.onResume()

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
        refreshItem?.let { item ->
            item.isVisible = syncConfig.syncMode == SyncMode.SYNC_MANUAL
            item.isEnabled = manualSyncPossible
            item.icon.alpha = when (manualSyncPossible) {
                true -> 255
                false -> Constants.DISABLED_ALPHA
            }
        }
    }

    private fun onHarvestActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (HarvestActivity.getHarvestCreatedOrModified(data.extras)) {
                refreshList()
            }
        }
    }

    private fun onObservationActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (ObservationActivity.getObservationCreatedOrModified(data.extras)) {
                refreshList()
            }
        }
    }

    private fun onSrvaActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (SrvaActivity.getSrvaEventCreatedOrModified(data.extras)) {
                // assume srva is already saved, no need for automatic sync
                refreshList()
            }
        }
    }

    private fun filterCurrentYearItems(items: List<GameLogListItem>): MutableList<GameLogListItem> {
        val filtered: MutableList<GameLogListItem> = ArrayList(items.size)
        val season = model.getSeasonSelected().value ?: return filtered
        val startDate = DateTimeUtils.getHuntingYearStart(season)
        val endDate = DateTimeUtils.getHuntingYearEnd(season)
        for (event in items) {
            val eventTime = DateTime(event.dateTime)
            if (GameLog.TYPE_SRVA == event.type) {
                if (eventTime.year == season) {
                    filtered.add(event)
                }
            } else {
                if (eventTime.isAfter(startDate) && eventTime.isBefore(endDate)) {
                    filtered.add(event)
                }
            }
        }
        return filterSpeciesItems(filtered)
    }

    private fun filterSpeciesItems(items: MutableList<GameLogListItem>): MutableList<GameLogListItem> {
        val speciesCodes: List<Int?>? = model.getSpeciesSelected().value
        if (speciesCodes == null || speciesCodes.isEmpty()) {
            return items
        }
        val filtered: MutableList<GameLogListItem> = ArrayList(items.size)
        for (event in items) {
            if (speciesCodes.contains(event.speciesCode)) {
                filtered.add(event)
            }
        }
        return filtered
    }

    private fun addItems(allEvents: List<GameLogListItem>, hideStats: Boolean) {
        if (activity != null) {
            clearList()

            val newItems = allEvents.sortedByDescending { it.dateTime }
                .let { events ->
                    setupCalendarYears(events)
                    filterCurrentYearItems(events)
                }

            displayItems = newItems
            for (i in newItems.indices.reversed()) {
                val item = newItems[i]
                if (i == 0) {
                    val sectionItem = GameLogListItem()
                    sectionItem.isHeader = true
                    sectionItem.month = newItems[i].dateTime!![Calendar.MONTH]
                    sectionItem.year = newItems[i].dateTime!![Calendar.YEAR]
                    displayItems.add(i, sectionItem)
                } else {
                    val prevItem = newItems[i - 1]
                    if (item.month != prevItem.month) {
                        val sectionItem = GameLogListItem()
                        sectionItem.isHeader = true
                        sectionItem.month = newItems[i].dateTime!![Calendar.MONTH]
                        sectionItem.year = newItems[i].dateTime!![Calendar.YEAR]
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
            refreshList()
        }
    }

    override fun onLogSeasonSelected(season: Int) {
        if (season != model.getSeasonSelected().value) {
            model.selectLogSeason(season)
            refreshList()
        }
    }

    override fun onLogSpeciesSelected(speciesIds: List<Int>) {
        if (speciesIds != model.getSpeciesSelected()) {
            model.selectSpeciesIds(speciesIds)
            filterView.setupSpecies(speciesIds, model.getCategorySelected().value)
            refreshList()
        }
    }

    override fun onLogSpeciesCategorySelected(categoryId: Int) {
        if (categoryId != model.getCategorySelected().value) {
            model.selectSpeciesCategory(categoryId)
            val speciesIds = model.getSpeciesSelected().value!!
            filterView.setupSpecies(speciesIds, model.getCategorySelected().value)
            refreshList()
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

    override fun onSyncStarted() {
        // no-op
    }

    override fun onSyncCompleted() {
        refreshList()
    }

    private class CalendarYear {
        var year = 0
        var statistics: SeasonStats? = null
    }
}
