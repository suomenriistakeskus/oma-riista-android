package fi.riista.mobile.feature.groupHunting.huntingDays.select

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import fi.riista.common.RiistaSDK
import fi.riista.common.extensions.*
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.ui.huntingDays.select.SelectHuntingDayController
import fi.riista.common.domain.groupHunting.ui.huntingDays.select.SelectHuntingDayViewModel
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
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SelectGroupHuntingDayActivity
    : BaseActivity(), SelectHuntingDayItemViewHolder.SelectionListener {

    private lateinit var huntingGroupTarget: HuntingGroupTarget
    private lateinit var controller: SelectHuntingDayController
    private val disposeBag = DisposeBag()

    private lateinit var adapter: SelectHuntingDayRecyclerViewAdapter

    private lateinit var layoutHuntingDaysExist: View
    private lateinit var layoutNoHuntingDays: View
    private lateinit var layoutSuggestedHuntingDay: View
    private lateinit var textViewSuggestedHuntingDay: TextView
    private lateinit var btnAddSuggestedHuntingDay: MaterialButton
    private lateinit var noHuntingDaysMessageTextView: TextView
    private lateinit var noHuntingDaysActionButton: MaterialButton

    private lateinit var cancelSelectionButton: MaterialButton
    private lateinit var selectButton: MaterialButton

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
        setContentView(R.layout.activity_select_hunting_day)

        huntingGroupTarget = getTargetFromIntent(intent)
                ?: run {
                    finish()
                    return
                }

        setCustomTitle(getString(R.string.group_hunting_hunting_days_title))

        controller = SelectHuntingDayController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                groupTarget = huntingGroupTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this)
        )

        controller.preferredHuntingDayDate = getPreferredHuntingDayDateFromIntent(intent)
        controller.initiallySelectedHuntingDayId = getHuntingDayIdFromIntent(intent)
        savedInstanceState?.let {
            controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }

        adapter = SelectHuntingDayRecyclerViewAdapter(layoutInflater, selectionListener = this)
        findViewById<RecyclerView>(R.id.rv_huntingDays).also {
            it.adapter = adapter
            it.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        }

        layoutHuntingDaysExist = findViewById(R.id.layout_hunting_days_exist)
        layoutNoHuntingDays = findViewById(R.id.layout_no_hunting_days)
        layoutSuggestedHuntingDay = findViewById<View>(R.id.layout_suggested_day_notification)
            .also {
                textViewSuggestedHuntingDay = it.findViewById(R.id.tv_suggested_day_text)
                btnAddSuggestedHuntingDay = it.findViewById(R.id.btn_add_hunting_day)
            }
        noHuntingDaysMessageTextView = findViewById(R.id.tv_no_hunting_days_message)
        noHuntingDaysActionButton = findViewById(R.id.btn_no_hunting_days_action)

        cancelSelectionButton = findViewById<MaterialButton>(R.id.btn_cancel_select).also {
            it.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        selectButton = findViewById<MaterialButton>(R.id.btn_select).also {
            it.setOnClickListener {
                setResult(
                        Activity.RESULT_OK,
                        createResultData(
                                fieldId = getFieldIdFromIntent(intent),
                                huntingDayId = controller.selectedHuntingDayId
                        )
                )
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menuInflater.inflate(R.menu.menu_refresh, menu)
        menu?.findItem(R.id.item_add)?.apply {
            isVisible = canCreateHuntingDay
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_add -> {
                addHuntingDay(preferredHuntingDayDate = null)
                true
            }
            R.id.item_refresh -> {
                loadViewModel(refresh = true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { loadStatus ->
            when (loadStatus) {
                ViewModelLoadStatus.NotLoaded -> onViewModelNotLoaded()
                ViewModelLoadStatus.Loading -> onViewModelLoading()
                ViewModelLoadStatus.LoadFailed -> onViewModelLoadFailed()
                is ViewModelLoadStatus.Loaded -> onViewModelLoaded(loadStatus.viewModel)
            }
        }.disposeBy(disposeBag)

        loadViewModelIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun addHuntingDay(preferredHuntingDayDate: LocalDate?) {
        // reload just in case day was added
        shouldReloadDataUponResume = true

        startActivity(
                ViewOrEditGroupHuntingDayActivity.getIntentForCreating(
                        packageContext = this,
                        huntingGroupTarget = huntingGroupTarget,
                        preferredHuntingDayDate = preferredHuntingDayDate,
                )
        )
    }

    override fun onHuntingDaySelected(huntingDayId: GroupHuntingDayId) {
        controller.eventDispatcher.dispatchHuntingDaySelected(huntingDayId)
    }

    private fun onViewModelNotLoaded() {
        layoutHuntingDaysExist.visibility = View.GONE
        layoutNoHuntingDays.visibility = View.GONE
        layoutSuggestedHuntingDay.visibility = View.GONE
    }

    private fun onViewModelLoading() {
        layoutHuntingDaysExist.visibility = View.GONE

        layoutNoHuntingDays.visibility = View.VISIBLE
        noHuntingDaysActionButton.visibility = View.GONE
        noHuntingDaysMessageTextView.text = getString(R.string.group_hunting_loading_hunting_days)

        layoutSuggestedHuntingDay.visibility = View.GONE
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
    }

    private fun onViewModelLoaded(viewModel: SelectHuntingDayViewModel) {
        canCreateHuntingDay = viewModel.canCreateHuntingDay
        if (viewModel.huntingDays.isEmpty()) {
            onViewModelLoadedButNoHuntingDays(viewModel)
        } else {
            onViewModelLoadedAndHuntingDaysAvailable(viewModel)
        }
    }

    private fun onViewModelLoadedButNoHuntingDays(viewModel: SelectHuntingDayViewModel) {
        layoutHuntingDaysExist.visibility = View.GONE

        layoutNoHuntingDays.visibility = View.VISIBLE
        if (viewModel.canCreateHuntingDay) {
            val suggestedHuntingDayDate = viewModel.suggestedHuntingDayDate
            noHuntingDaysMessageTextView.text =
                when (suggestedHuntingDayDate) {
                    null -> viewModel.noHuntingDaysText
                    else -> getSuggestedDayText(suggestedHuntingDayDate)
                }

            with(noHuntingDaysActionButton) {
                visibility = View.VISIBLE
                text = getString(R.string.group_hunting_add_hunting_day)
                setOnClickListener {
                    addHuntingDay(preferredHuntingDayDate = suggestedHuntingDayDate)
                }
            }
        } else {
            noHuntingDaysMessageTextView.text = viewModel.noHuntingDaysText
            noHuntingDaysActionButton.visibility = View.GONE
        }
    }

    private fun onViewModelLoadedAndHuntingDaysAvailable(viewModel: SelectHuntingDayViewModel) {
        layoutNoHuntingDays.visibility = View.GONE

        val showSuggestedDayLayout =
            when (val suggestedHuntingDayDate = viewModel.suggestedHuntingDayDate) {
                null -> false
                else -> {
                    textViewSuggestedHuntingDay.text = getSuggestedDayText(suggestedHuntingDayDate)
                    btnAddSuggestedHuntingDay.setOnClickListener {
                        addHuntingDay(suggestedHuntingDayDate)
                    }
                    true
                }
            }
        layoutHuntingDaysExist.visibility = View.VISIBLE
        layoutSuggestedHuntingDay.visibility = showSuggestedDayLayout.toVisibility()

        adapter.huntingDays = viewModel.huntingDays

        selectButton.isEnabled = viewModel.isHuntingDaySelected
    }

    private fun getSuggestedDayText(suggestedDate: LocalDate): String {
        val formattedDate = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(
                suggestedDate.toJodaLocalDate()
        )

        return getString(R.string.group_hunting_suggested_hunting_day_for_entry, formattedDate)
    }

    private fun loadViewModelIfNeeded() {
        val viewModelStatus = controller.viewModelLoadStatus.value
        if (viewModelStatus is ViewModelLoadStatus.Loaded && !shouldReloadDataUponResume) {
            return
        }

        loadViewModel(refresh = false)
    }

    private fun loadViewModel(refresh: Boolean) {
        MainScope().launch {
            controller.loadViewModel(refresh = refresh)
        }
    }

    companion object {
        private const val EXTRAS_PREFIX = "SelectGroupHuntingDayActivity"
        private const val CONTROLLER_STATE_PREFIX = "SGHDA_controller"
        private const val KEY_FIELD_ID = "${EXTRAS_PREFIX}_field_id"
        private const val KEY_PREFERRED_HUNTING_DAY_DATE = "${EXTRAS_PREFIX}_preferred_hunting_day_date"
        private const val KEY_HUNTING_DAY_ID = "${EXTRAS_PREFIX}_hunting_day_id"

        private fun createResultData(fieldId: Int, huntingDayId: GroupHuntingDayId?): Intent {
            return Intent().apply {
                putExtras(Bundle().also {
                    it.putInt(KEY_FIELD_ID, fieldId)
                    if (huntingDayId != null) {
                        it.putGroupHuntingDayId(KEY_HUNTING_DAY_ID, huntingDayId)
                    }
                })
            }
        }

        fun getFieldIdFromIntent(intent: Intent): Int {
            val fieldId = intent.getIntExtra(KEY_FIELD_ID, -1).takeIf { it >= 0 }
            requireNotNull(fieldId) {
                "FieldId is required to exist in the intent"
            }

            return fieldId
        }

        fun getPreferredHuntingDayDateFromIntent(intent: Intent): LocalDate? {
            return intent.extras?.getLocalDate(KEY_PREFERRED_HUNTING_DAY_DATE)
        }

        fun getHuntingDayIdFromIntent(intent: Intent): GroupHuntingDayId? {
            return intent.extras?.getGroupHuntingDayId(KEY_HUNTING_DAY_ID)
        }

        fun getLaunchIntent(
            packageContext: Context,
            huntingGroupTarget: HuntingGroupTarget,
            fieldId: Int,
            selectedHuntingDayId: GroupHuntingDayId?,
            preferredHuntingDayDate: LocalDate?,
        ): Intent {
            return Intent(packageContext, SelectGroupHuntingDayActivity::class.java)
                .apply {
                    putExtras(Bundle().also { bundle ->
                        huntingGroupTarget.saveToBundle(bundle, EXTRAS_PREFIX)
                        bundle.putInt(KEY_FIELD_ID, fieldId)
                        selectedHuntingDayId?.let {
                            bundle.putGroupHuntingDayId(KEY_HUNTING_DAY_ID, it)
                        }
                        preferredHuntingDayDate?.let {
                            bundle.putLocalDate(KEY_PREFERRED_HUNTING_DAY_DATE, it)
                        }
                    })
                }
        }

        fun getTargetFromIntent(intent: Intent): HuntingGroupTarget? {
            return intent.extras?.loadHuntingGroupTarget(EXTRAS_PREFIX)
        }
    }
}
