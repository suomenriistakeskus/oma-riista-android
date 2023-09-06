package fi.riista.mobile.feature.groupHunting

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestId
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservation
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationId
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.model.createTargetForHarvest
import fi.riista.common.domain.groupHunting.model.createTargetForObservation
import fi.riista.common.domain.groupHunting.ui.diary.DiaryController
import fi.riista.common.domain.groupHunting.ui.diary.DiaryEvent
import fi.riista.common.domain.groupHunting.ui.diary.DiaryFilter
import fi.riista.common.domain.groupHunting.ui.diary.ListGroupDiaryEntriesController
import fi.riista.common.domain.groupHunting.ui.diary.loadFromBundle
import fi.riista.common.domain.groupHunting.ui.diary.saveToBundle
import fi.riista.common.extensions.loadHuntingGroupTarget
import fi.riista.common.extensions.saveToBundle
import fi.riista.common.model.LocalDate
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.groupHunting.harvests.GroupHarvestActivity
import fi.riista.mobile.feature.groupHunting.map.ListGroupDiaryEntriesDialogFragment
import fi.riista.mobile.feature.groupHunting.observations.GroupObservationActivity
import fi.riista.mobile.pages.GroupHuntingDiaryListener
import fi.riista.mobile.pages.GroupHuntingMapViewer
import fi.riista.mobile.riistaSdkHelpers.fromJodaLocalDate
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.registerDatePickerFragmentResultListener
import fi.riista.mobile.utils.DateTimeUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class GroupHuntingMapActivity
    : BaseActivity()
    , GroupHuntingMapViewer.Manager
    , ListGroupDiaryEntriesDialogFragment.Manager
    , DateTimePickerFragment.Listener
{

    private var huntingGroupTarget: HuntingGroupTarget? = null
    private lateinit var controller: DiaryController
    private val disposeBag = DisposeBag()
    private var diaryListener: GroupHuntingDiaryListener? = null

    private lateinit var dateFilterLayout: View
    private lateinit var noContentTextView: TextView
    private lateinit var startDateButton: MaterialButton
    private lateinit var endDateButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var harvests: List<GroupHuntingHarvest> = listOf()
    private var observations: List<GroupHuntingObservation> = listOf()
    private var diaryFilter: DiaryFilter = DiaryFilter(DiaryFilter.EventType.ALL, DiaryFilter.AcceptStatus.ALL)
    private var huntingGroupArea: HuntingGroupArea? = null
    private var zoomLevel: Float? = null
    private var mapLocation: Location? = null

    override val listGroupDiaryEntriesController: ListGroupDiaryEntriesController
        get() {
            return ListGroupDiaryEntriesController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    huntingGroupTarget = requireNotNull(huntingGroupTarget)
            )
        }

    /**
     * Should the data be reloaded upon resume?
     *
     * Allows refetching content e.g. after approving/rejecting a harvest/observation
     */
    private var shouldReloadDataUponResume: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_hunting_map)
        setCustomTitle(getString(R.string.group_hunting_entries_on_map_title))

        val target = getHuntingGroupTargetFromIntent(intent)
        if (target == null) {
            finish()
            return
        } else {
            huntingGroupTarget = target
        }

        controller = DiaryController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                groupTarget = target
        )

        savedInstanceState?.let {
            controller.loadFromBundle(it)

        }

        if (savedInstanceState == null) {
            // only create the first fragment if not restoring from saved state
            supportFragmentManager.beginTransaction()
                    .add(R.id.layout_fragment_container, GroupHuntingMapViewer.newInstance())
                    .commit()
        }

        registerDatePickerFragmentResultListener(DATE_PICKER_REQUEST_CODE)

        dateFilterLayout = findViewById(R.id.layout_date_filter)
        noContentTextView = findViewById(R.id.tv_no_content)
        startDateButton = findViewById(R.id.btn_filter_start_date)
        endDateButton = findViewById(R.id.btn_filter_end_date)
        progressBar = findViewById(R.id.progress_horizontal)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_refresh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_refresh -> {
                MainScope().launch {
                    controller.loadViewModel(refresh = true)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState)
        zoomLevel?.let { outState.putFloat(ZOOM_LEVEL, it) }
        mapLocation?.let { outState.putParcelable(LOCATION, it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        zoomLevel = if (savedInstanceState.containsKey(ZOOM_LEVEL)) {
            savedInstanceState.getFloat(ZOOM_LEVEL)
        } else {
            null
        }
        mapLocation = if (savedInstanceState.containsKey(LOCATION)) {
            savedInstanceState.getParcelable(LOCATION)
        } else {
            null
        }
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { loadStatus ->
            when (loadStatus) {
                ViewModelLoadStatus.NotLoaded -> {
                    progressBar.visibility = View.GONE
                }
                ViewModelLoadStatus.Loading -> {
                    dateFilterLayout.visibility = View.INVISIBLE
                    noContentTextView.visibility = View.VISIBLE
                    noContentTextView.text = getString(R.string.loading_content)
                    progressBar.visibility = View.VISIBLE
                }
                ViewModelLoadStatus.LoadFailed -> {
                    dateFilterLayout.visibility = View.INVISIBLE
                    noContentTextView.visibility = View.VISIBLE
                    noContentTextView.text = getString(R.string.content_loading_failed)
                    progressBar.visibility = View.GONE
                }
                is ViewModelLoadStatus.Loaded -> with(loadStatus.viewModel) {
                    when (val diaryEvents = events) {
                        null -> {
                            dateFilterLayout.visibility = View.INVISIBLE
                            noContentTextView.visibility = View.VISIBLE
                            noContentTextView.text = getString(R.string.group_hunting_no_harvests_no_observations)

                            harvests = listOf()
                            observations = listOf()
                            huntingGroupArea = null
                        }
                        else -> {
                            dateFilterLayout.visibility = View.VISIBLE
                            noContentTextView.visibility = View.GONE

                            harvests = diaryEvents.filteredEvents.harvests
                            observations = diaryEvents.filteredEvents.observations
                            huntingGroupArea = diaryEvents.huntingGroupArea
                            diaryFilter = diaryEvents.diaryFilter

                            updateDateFilters(diaryEvents)
                        }
                    }

                    progressBar.visibility = View.GONE
                    diaryListener?.updateMarkers()
                    diaryListener?.updateFilter()
                }
            }
        }.disposeBy(disposeBag)

        loadViewModelIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun loadViewModelIfNotLoaded() {
        val viewModelStatus = controller.viewModelLoadStatus.value
        if (viewModelStatus is ViewModelLoadStatus.Loaded && !shouldReloadDataUponResume) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    private fun updateDateFilters(diaryEvents: DiaryEvent) {
        updateDateFilter(
                startDateButton,
                FIELD_ID_START_DATE,
                diaryEvents.filterStartDate,
                diaryEvents.minFilterDate,
                diaryEvents.filterEndDate
        )
        updateDateFilter(
                endDateButton,
                FIELD_ID_END_DATE,
                diaryEvents.filterEndDate,
                diaryEvents.filterStartDate,
                diaryEvents.maxFilterDate
        )
    }

    private fun updateDateFilter(
        dateFilterButton: MaterialButton,
        dialogId: Int,
        filterDate: LocalDate,
        minDate: LocalDate,
        maxDate: LocalDate
    ) {
        with (dateFilterButton) {
            text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(filterDate.toJodaLocalDate())
            setOnClickListener {
                val pickerFragment = DateTimePickerFragment.create(
                    requestCode = DATE_PICKER_REQUEST_CODE,
                    fieldId = dialogId,
                    selectedDate = filterDate.toJodaLocalDate(),
                    minDate = minDate.toJodaLocalDate(),
                    maxDate = maxDate.toJodaLocalDate()
                )
                pickerFragment.show(supportFragmentManager, "datePicker")
            }
        }
    }

    override fun setFullscreenMode(fullscreen: Boolean) {
        val attrs = window.attributes
        val actionBar = supportActionBar
        if (fullscreen) {
            attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            actionBar?.hide()
        } else {
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            actionBar?.show()
        }
        window.attributes = attrs
    }

    override fun getHarvestsToBeDisplayed(): List<GroupHuntingHarvest> {
        return harvests
    }

    @Suppress("FoldInitializerAndIfToElvis")
    override fun onHarvestClicked(harvestId: GroupHuntingHarvestId) {
        val harvestTarget = huntingGroupTarget?.createTargetForHarvest(harvestId)
        if (harvestTarget == null) {
            return
        }

        val harvest = controller.findHarvest(harvestTarget)
        if (harvest == null) {
            return
        }

        // reload just in case the harvest will be approved/rejected
        shouldReloadDataUponResume = true

        val intent = GroupHarvestActivity.getLaunchIntentForViewing(
                packageContext = this,
                groupHuntingHarvestTarget = harvestTarget,
                harvestAcceptStatus = harvest.acceptStatus,
        )
        startActivity(intent)
    }

    override fun getObservationsToBeDisplayed(): List<GroupHuntingObservation> {
        return observations
    }

    override fun onObservationClicked(observationId: GroupHuntingObservationId) {
        val observationTarget = huntingGroupTarget?.createTargetForObservation(observationId) ?: return

        val observation = controller.findObservation(observationTarget) ?: return

        // reload just in case the observation was approved/rejected
        shouldReloadDataUponResume = true

        val intent = GroupObservationActivity.getLaunchIntentForViewing(
            packageContext = this,
            groupHuntingObservationTarget = observationTarget,
            observationAcceptStatus = observation.acceptStatus,
        )
        startActivity(intent)
    }

    override fun onMultipleEntriesClicked(
        harvests: List<GroupHuntingHarvestId>,
        observations: List<GroupHuntingObservationId>
    ) {
        ListGroupDiaryEntriesDialogFragment.create(
                harvestIds = harvests,
                observationIds = observations,
        ).show(supportFragmentManager, "ListGroupDiaryEntriesDialogFragment")
    }

    override fun registerDiaryListener(listener: GroupHuntingDiaryListener) {
        diaryListener = listener
    }

    override fun getHuntingGroupArea(): HuntingGroupArea? {
        return huntingGroupArea
    }

    override fun getZoomLevel(): Float? {
        return zoomLevel
    }

    override fun setZoomLevel(zoomLevel: Float) {
        this.zoomLevel = zoomLevel
    }

    override fun getLocation(): Location? {
        return mapLocation
    }

    override fun setLocation(mapLocation: Location) {
        this.mapLocation = mapLocation
    }

    override fun getDiaryFilter(): DiaryFilter {
        return diaryFilter
    }

    override fun setDiaryFilter(diaryFilter: DiaryFilter) {
        controller.eventDispatcher.dispatchDiaryFilterChanged(diaryFilter)
    }

    override fun onDateTimeSelected(fieldId: Int, dateTime: DateTime) {
        val localDate = LocalDate.fromJodaLocalDate(dateTime.toLocalDate())
        when (fieldId) {
            FIELD_ID_START_DATE -> controller.eventDispatcher.dispatchFilterStartDateChanged(localDate)
            FIELD_ID_END_DATE -> controller.eventDispatcher.dispatchFilterEndDateChanged(localDate)
        }
    }

    override fun onViewHarvest(harvestId: GroupHuntingHarvestId,
                               harvestAcceptStatus: AcceptStatus) {
        val harvestTarget = huntingGroupTarget?.createTargetForHarvest(harvestId) ?: return

        GroupHarvestActivity.getLaunchIntentForViewing(
                packageContext = this,
                groupHuntingHarvestTarget = harvestTarget,
                harvestAcceptStatus = harvestAcceptStatus
        ).let {
            startActivity(it)
        }
    }

    override fun onViewObservation(observationId: GroupHuntingObservationId,
                                   observationAcceptStatus: AcceptStatus) {
        val observationTarget = huntingGroupTarget?.createTargetForObservation(observationId) ?: return

        GroupObservationActivity.getLaunchIntentForViewing(
                packageContext = this,
                groupHuntingObservationTarget = observationTarget,
                observationAcceptStatus = observationAcceptStatus
        ).let {
            startActivity(it)
        }
    }

    companion object {
        private const val DATE_PICKER_REQUEST_CODE = "GHMA_date_picker_request_code"
        private const val FIELD_ID_START_DATE = 1
        private const val FIELD_ID_END_DATE = 2
        private const val ZOOM_LEVEL = "zoom-level"
        private const val LOCATION = "location"
        private const val EXTRAS_PREFIX = "GroupHuntingMapActivity"


        fun getLaunchIntent(packageContext: Context, huntingGroupTarget: HuntingGroupTarget): Intent {
            return Intent(packageContext, GroupHuntingMapActivity::class.java)
                .apply {
                    putExtras(
                            Bundle().also {
                                huntingGroupTarget.saveToBundle(it, EXTRAS_PREFIX)
                            }
                    )
                }
        }

        fun getHuntingGroupTargetFromIntent(intent: Intent): HuntingGroupTarget? {
            return intent.extras?.loadHuntingGroupTarget(EXTRAS_PREFIX)
        }
    }
}
