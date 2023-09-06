package fi.riista.mobile.feature.groupHunting.huntingDays.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.model.createTargetForHuntingDay
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDays
import fi.riista.common.domain.groupHunting.ui.huntingDays.ListHuntingDaysController
import fi.riista.common.domain.groupHunting.ui.huntingDays.ListHuntingDaysViewModel
import fi.riista.common.extensions.loadHuntingGroupTarget
import fi.riista.common.extensions.saveToBundle
import fi.riista.common.model.LocalDate
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.groupHunting.huntingDays.ViewOrEditGroupHuntingDayActivity
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.fromJodaLocalDate
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.registerDatePickerFragmentResultListener
import fi.riista.mobile.utils.DateTimeUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class ListGroupHuntingDaysActivity
    : BaseActivity()
    , HuntingDayItemViewHolder.Listener
    , DateTimePickerFragment.Listener
{

    private lateinit var huntingGroupTarget: HuntingGroupTarget
    private lateinit var controller: ListHuntingDaysController
    private val disposeBag = DisposeBag()

    private lateinit var adapter: ListHuntingDaysRecyclerViewAdapter

    private lateinit var layoutHuntingDaysExist: View
    private lateinit var layoutNoHuntingDays: View
    private lateinit var noHuntingDaysMessageTextView: TextView
    private lateinit var noHuntingDaysActionButton: MaterialButton
    private lateinit var btnFilterStartDate: MaterialButton
    private lateinit var btnFilterEndDate: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var canCreateHuntingDay = false
        set(value) {
            val shouldInvalidateMenu = value != canCreateHuntingDay
            field = value

            if (shouldInvalidateMenu) {
                invalidateOptionsMenu()
            }
        }

    /**
     * Should the data be reloaded upon resume?
     *
     * Allows refetching content e.g. after creating a new hunting day.
     */
    private var shouldReloadDataUponResume: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_hunting_days)

        huntingGroupTarget = getTargetFromIntent(intent)
                ?: run {
                    finish()
                    return
                }

        setCustomTitle(getString(R.string.group_hunting_hunting_days_title))

        controller = ListHuntingDaysController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                groupTarget = huntingGroupTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this)
        )

        savedInstanceState?.let {
            shouldReloadDataUponResume = it.getBoolean(KEY_SHOULD_RELOAD_DATA, false)
            controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }

        adapter = ListHuntingDaysRecyclerViewAdapter(layoutInflater, itemListener = this)
        findViewById<RecyclerView>(R.id.rv_huntingDays).also {
            it.adapter = adapter
            it.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        }

        layoutHuntingDaysExist = findViewById(R.id.layout_hunting_days_exist)
        layoutNoHuntingDays = findViewById(R.id.layout_no_hunting_days)
        noHuntingDaysMessageTextView = findViewById(R.id.tv_no_hunting_days_message)
        noHuntingDaysActionButton = findViewById(R.id.btn_no_hunting_days_action)
        btnFilterStartDate = findViewById(R.id.btn_filter_start_date)
        btnFilterEndDate = findViewById(R.id.btn_filter_end_date)
        progressBar = findViewById(R.id.progress_horizontal)

        registerDatePickerFragmentResultListener(DATE_PICKER_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menuInflater.inflate(R.menu.menu_refresh, menu)
        menu.findItem(R.id.item_add)?.apply {
            isVisible = canCreateHuntingDay
        }
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
            R.id.item_add -> {
                addHuntingDay()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
        outState.putBoolean(KEY_SHOULD_RELOAD_DATA, shouldReloadDataUponResume)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { loadStatus ->
            when (loadStatus) {
                ViewModelLoadStatus.NotLoaded -> onViewModelNotLoaded()
                ViewModelLoadStatus.Loading -> onViewModelLoading()
                ViewModelLoadStatus.LoadFailed -> onViewModelLoadFailed()
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = loadStatus.viewModel
                    val huntingDays = viewModel.huntingDays

                    canCreateHuntingDay = viewModel.canCreateHuntingDay
                    when {
                        huntingDays == null -> onViewModelLoadedButNoHuntingDays(viewModel)
                        huntingDays.filteredHuntingDays.isEmpty() -> {
                            onViewModelLoadedButNoHuntingDaysAfterFiltering(
                                    huntingDays, viewModel.canCreateHuntingDay
                            )
                        }
                        else -> onViewModelLoadedAndHuntingDaysAvailable(huntingDays)
                    }
                    progressBar.visibility = View.GONE
                }
            }
        }.disposeBy(disposeBag)

        loadViewModelIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun onViewModelNotLoaded() {
        layoutHuntingDaysExist.visibility = View.GONE
        layoutNoHuntingDays.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun onViewModelLoading() {
        layoutHuntingDaysExist.visibility = View.GONE

        layoutNoHuntingDays.visibility = View.VISIBLE
        noHuntingDaysActionButton.visibility = View.GONE
        noHuntingDaysMessageTextView.text = getString(R.string.group_hunting_loading_hunting_days)
        progressBar.visibility = View.VISIBLE
    }

    private fun onViewModelLoadFailed() {
        layoutHuntingDaysExist.visibility = View.GONE

        layoutNoHuntingDays.visibility = View.VISIBLE

        noHuntingDaysMessageTextView.text =
            getString(R.string.group_hunting_failed_to_load_hunting_days)
        with(noHuntingDaysActionButton) {
            visibility = View.VISIBLE
            text = getString(R.string.try_again)
            setOnClickListener {
                loadViewModelIfNeeded()
            }
        }
        progressBar.visibility = View.GONE
    }

    private fun onViewModelLoadedButNoHuntingDays(viewModel: ListHuntingDaysViewModel) {
        layoutHuntingDaysExist.visibility = View.GONE

        layoutNoHuntingDays.visibility = View.VISIBLE
        noHuntingDaysMessageTextView.text = viewModel.noHuntingDaysText
        if (viewModel.canCreateHuntingDay) {
            with(noHuntingDaysActionButton) {
                visibility = View.VISIBLE
                text = getString(R.string.group_hunting_add_hunting_day)
                setOnClickListener {
                    addHuntingDay()
                }
            }
        } else {
            noHuntingDaysActionButton.visibility = View.GONE
        }
    }

    private fun onViewModelLoadedButNoHuntingDaysAfterFiltering(huntingDays: HuntingDays,
                                                                canCreateHuntingDay: Boolean) {
        // hunting days exist but there's no hunting days after filtering
        // -> keep hunting days layout visible in order to update filtering
        layoutHuntingDaysExist.visibility = View.VISIBLE
        adapter.huntingDays = listOf()

        layoutNoHuntingDays.visibility = View.VISIBLE
        if (canCreateHuntingDay) {
            with(noHuntingDaysActionButton) {
                visibility = View.VISIBLE
                text = getString(R.string.group_hunting_add_hunting_day)
                setOnClickListener {
                    addHuntingDay()
                }
            }
            noHuntingDaysMessageTextView.text =
                getString(R.string.group_hunting_no_hunting_days_after_filtering_but_can_create)
        } else {
            noHuntingDaysActionButton.visibility = View.GONE
            noHuntingDaysMessageTextView.text =
                getString(R.string.group_hunting_no_hunting_days_after_filtering)
        }

        updateDateFilters(huntingDays)
    }

    private fun onViewModelLoadedAndHuntingDaysAvailable(huntingDays: HuntingDays) {
        layoutNoHuntingDays.visibility = View.GONE

        layoutHuntingDaysExist.visibility = View.VISIBLE
        adapter.huntingDays = huntingDays.filteredHuntingDays

        updateDateFilters(huntingDays)
    }

    private fun updateDateFilters(huntingDays: HuntingDays) {
        updateDateFilter(
                btnFilterStartDate,
                FIELD_ID_START_DATE,
                huntingDays.filterStartDate,
                huntingDays.minFilterDate,
                huntingDays.filterEndDate
        )
        updateDateFilter(
                btnFilterEndDate,
                FIELD_ID_END_DATE,
                huntingDays.filterEndDate,
                huntingDays.filterStartDate,
                huntingDays.maxFilterDate
        )
    }

    private fun updateDateFilter(dateFilterButton: MaterialButton,
                                 fieldId: Int,
                                 filterDate: LocalDate,
                                 minDate: LocalDate,
                                 maxDate: LocalDate
    ) {
        with (dateFilterButton) {
            text = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(filterDate.toJodaLocalDate())
            setOnClickListener {
                val pickerFragment = DateTimePickerFragment.create(
                        requestCode = DATE_PICKER_REQUEST_CODE,
                        fieldId = fieldId,
                        selectedDate = filterDate.toJodaLocalDate(),
                        minDate = minDate.toJodaLocalDate(),
                        maxDate = maxDate.toJodaLocalDate()
                )
                pickerFragment.show(supportFragmentManager, "datePicker")
            }
        }
    }

    override fun onDateTimeSelected(fieldId: Int, dateTime: DateTime) {
        val localDate = LocalDate.fromJodaLocalDate(dateTime.toLocalDate())
        when (fieldId) {
            FIELD_ID_START_DATE -> controller.eventDispatcher.dispatchFilterStartDateChanged(localDate)
            FIELD_ID_END_DATE -> controller.eventDispatcher.dispatchFilterEndDateChanged(localDate)
        }
    }

    private fun addHuntingDay() {
        startCreateHuntingDay(preferredHuntingDayDate = null)
    }

    override fun onViewHuntingDay(huntingDayId: GroupHuntingDayId) {
        // reload just in case user approves or rejects entries. Approving / rejecting
        // can affect how list items are displayed
        shouldReloadDataUponResume = true

        startActivity(ViewOrEditGroupHuntingDayActivity.getIntentForViewing(
                packageContext = this,
                huntingDayTarget = huntingGroupTarget.createTargetForHuntingDay(huntingDayId),
        ))
    }

    override fun onEditHuntingDay(huntingDayId: GroupHuntingDayId) {
        startActivity(ViewOrEditGroupHuntingDayActivity.getIntentForEditing(
                packageContext = this,
                huntingDayTarget = huntingGroupTarget.createTargetForHuntingDay(huntingDayId),
        ))
    }

    override fun onCreateHuntingDay(preferredHuntingDayDate: LocalDate?) {
        startCreateHuntingDay(preferredHuntingDayDate)
    }

    private fun startCreateHuntingDay(preferredHuntingDayDate: LocalDate?) {
        // reload just in case day was added
        shouldReloadDataUponResume = true

        startActivity(ViewOrEditGroupHuntingDayActivity.getIntentForCreating(
                packageContext = this,
                huntingGroupTarget = huntingGroupTarget,
                preferredHuntingDayDate = preferredHuntingDayDate,
        ))
    }

    private fun loadViewModelIfNeeded() {
        val viewModelStatus = controller.viewModelLoadStatus.value
        if (viewModelStatus is ViewModelLoadStatus.Loaded && !shouldReloadDataUponResume) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    companion object {
        private const val FIELD_ID_START_DATE = 1
        private const val FIELD_ID_END_DATE = 2
        private const val EXTRAS_PREFIX = "ListGroupHuntingDaysActivity"
        private const val KEY_SHOULD_RELOAD_DATA = "${EXTRAS_PREFIX}_should_reload_data"
        private const val CONTROLLER_STATE_PREFIX = "LGHDA_controller"
        private const val DATE_PICKER_REQUEST_CODE = "LGHDA_date_picker_request_code"

        fun getLaunchIntent(packageContext: Context, huntingGroupTarget: HuntingGroupTarget): Intent {
            return Intent(packageContext, ListGroupHuntingDaysActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        huntingGroupTarget.saveToBundle(it, EXTRAS_PREFIX)
                    })
                }
        }

        fun getTargetFromIntent(intent: Intent): HuntingGroupTarget? {
            return intent.extras?.loadHuntingGroupTarget(EXTRAS_PREFIX)
        }
    }
}
