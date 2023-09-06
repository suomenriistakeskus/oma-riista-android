package fi.riista.mobile.feature.groupHunting

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.ui.groupSelection.SelectHuntingGroupController
import fi.riista.common.domain.groupHunting.ui.groupSelection.SelectHuntingGroupField
import fi.riista.common.domain.groupHunting.ui.groupSelection.SelectHuntingGroupViewModel
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.isDeer
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.groupHunting.harvests.GroupHarvestActivity
import fi.riista.mobile.feature.groupHunting.huntingDays.list.ListGroupHuntingDaysActivity
import fi.riista.mobile.feature.groupHunting.observations.GroupObservationActivity
import fi.riista.mobile.riistaSdkHelpers.*
import fi.riista.mobile.ui.MessageDialogFragment
import fi.riista.mobile.ui.NotificationButton
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class GroupHuntingActivity
    : BaseActivity()
    , DataFieldViewHolderTypeResolver<SelectHuntingGroupField> {

    private lateinit var filters: RecyclerView
    private lateinit var adapter: DataFieldRecyclerViewAdapter<SelectHuntingGroupField>

    private lateinit var layoutGroupHuntingAvailable: View
    private lateinit var newHarvestBtn: NotificationButton
    private lateinit var bigNewHarvestBtn: NotificationButton
    private lateinit var newObservationBtn: NotificationButton
    private lateinit var diaryOnMapBtn: NotificationButton
    private lateinit var huntingDaysBtn: NotificationButton

    private lateinit var layoutGroupHuntingNotAvailable: View
    private lateinit var textViewGroupHuntingLoadMessage: TextView
    private lateinit var btnReloadData: MaterialButton

    private lateinit var controller: SelectHuntingGroupController
    private val disposeBag = DisposeBag()

    private var shouldRefreshGroupDataNextTime: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_hunting)
        setCustomTitle(getString(R.string.group_hunting_main_page_title))

        controller = SelectHuntingGroupController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                stringProvider = ContextStringProviderFactory.createForContext(this),
                languageProvider = AppLanguageProvider(this),
                speciesResolver = AppSpeciesResolver()
        )

        if (savedInstanceState != null) {
            controller.restoreFromBundle(savedInstanceState, PREFIX_CONTROLLER_STATE)
            shouldRefreshGroupDataNextTime = savedInstanceState.getBoolean(
                    KEY_SHOULD_REFRESH_NEXT_TIME, false)
        }

        layoutGroupHuntingAvailable = findViewById(R.id.layout_group_hunting_available)
        filters = findViewById(R.id.rv_hunting_group_filters)
        filters.adapter = DataFieldRecyclerViewAdapter(viewHolderTypeResolver = this).apply {
            registerViewHolderFactory(
                ChoiceViewHolder.Factory(
                    eventDispatcher = controller.eventDispatcher
            ))
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
        }.also {
            adapter = it
        }

        setupButtons()
        setupGroupHuntingNotAvailableViews()

        // try to show the group hunting intro text automatically.
        showIntroMessage(
                // don't allow showing it second time automatically!
                onlyShowIfNotShownPreviously = true,

                // trying to display automatically -> increment display count
                incrementAutomaticDisplayCount = true
        )

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (RiistaSDK.groupHuntingIntroMessageHandler().getMessage() != null) {
            menuInflater.inflate(R.menu.menu_info, menu)
        }
        menuInflater.inflate(R.menu.menu_refresh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_info -> {
                showIntroMessage(
                        // allow displaying multiple times
                        onlyShowIfNotShownPreviously = false,

                        // don't count "manual" displays towards display count limit
                        incrementAutomaticDisplayCount = false,
                )
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
        controller.saveToBundle(outState, PREFIX_CONTROLLER_STATE)
        outState.putBoolean(KEY_SHOULD_REFRESH_NEXT_TIME, shouldRefreshGroupDataNextTime)
        super.onSaveInstanceState(outState)
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

        loadViewModelIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun showIntroMessage(
        onlyShowIfNotShownPreviously: Boolean,
        incrementAutomaticDisplayCount: Boolean
    ) {
        RiistaSDK.groupHuntingIntroMessageHandler().getMessage()
            ?.takeIf { message ->
                if (onlyShowIfNotShownPreviously) {
                    val displayCount = RiistaSDK.groupHuntingIntroMessageHandler()
                        .getMessageAutomaticDisplayCount(message.id)

                    displayCount == 0
                } else {
                    true
                }
            }?.let { message ->
                if (incrementAutomaticDisplayCount) {
                    RiistaSDK.groupHuntingIntroMessageHandler()
                        .incrementMessageAutomaticDisplayCount(message.id)
                }

                MessageDialogFragment
                    .create(message)
                    .show(supportFragmentManager, "MessageDialogFragment")
            }
    }

    private fun onViewModelNotLoaded() {
        layoutGroupHuntingAvailable.visibility = View.GONE
        layoutGroupHuntingNotAvailable.visibility = View.GONE
    }

    private fun onViewModelLoading() {
        layoutGroupHuntingAvailable.visibility = View.GONE
        layoutGroupHuntingNotAvailable.visibility = View.VISIBLE

        textViewGroupHuntingLoadMessage.setText(R.string.group_hunting_loading_content)
        btnReloadData.visibility = View.GONE
    }

    private fun onViewModelLoadFailed() {
        layoutGroupHuntingAvailable.visibility = View.GONE
        layoutGroupHuntingNotAvailable.visibility = View.VISIBLE

        textViewGroupHuntingLoadMessage.setText(R.string.group_hunting_loading_content_failed)
        btnReloadData.visibility = View.VISIBLE

        updateButtonStates(
                huntingGroupSelected  = false,
                canCreateHarvest = false,
                canCreateObservation = false,
                proposedEventCount = 0,
                selectedSpecies = null,
        )

        adapter.setDataFields(listOf())
    }

    private fun onViewModelLoaded(viewModel: SelectHuntingGroupViewModel) {
        layoutGroupHuntingAvailable.visibility = View.VISIBLE
        layoutGroupHuntingNotAvailable.visibility = View.GONE

        updateButtonStates(
                huntingGroupSelected = viewModel.huntingGroupSelected,
                canCreateHarvest = viewModel.canCreateHarvest,
                canCreateObservation = viewModel.canCreateObservation,
                proposedEventCount = viewModel.proposedEventsCount,
                selectedSpecies = viewModel.selectedSpecies,
        )

        adapter.setDataFields(viewModel.fields)

        fetchHuntingGroupDataIfNeeded(refresh = shouldRefreshGroupDataNextTime)
        shouldRefreshGroupDataNextTime = false
    }


    private fun loadViewModelIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded &&
            !shouldRefreshGroupDataNextTime) {
            return
        }

        // don't refresh here, instead refresh _group_ data
        loadViewModel(refresh = false)
    }

    private fun loadViewModel(refresh: Boolean) {
        MainScope().launch {
            controller.loadViewModel(refresh = refresh)

            if (refresh) {
                controller.fetchHuntingGroupDataIfNeeded(refresh = true)
            }
        }
    }

    override fun resolveViewHolderType(dataField: DataField<SelectHuntingGroupField>): DataFieldViewHolderType {
        return when (dataField) {
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is LabelField -> dataField.determineViewHolderType()
            is SpecimenField,
            is InstructionsField,
            is StringField,
            is IntField,
            is DoubleField,
            is BooleanField,
            is SpeciesField,
            is DateAndTimeField,
            is LocationField,
            is GenderField,
            is AgeField,
            is SelectDurationField,
            is HuntingDayAndTimeField,
            is HarvestField,
            is ObservationField,
            is DateField,
            is TimespanField,
            is AttachmentField,
            is ButtonField,
            is ChipField,
            is CustomUserInterfaceField -> {
                throw RuntimeException("Unexpected type!")
            }
        }
    }

    private fun setupButtons() {
        newHarvestBtn = findViewById<NotificationButton>(R.id.group_hunting_new_harvest).also {
            it.findViewById<View>(R.id.button_content).setOnClickListener {
                onNewHarvestClicked()
            }
        }

        bigNewHarvestBtn = findViewById<NotificationButton>(R.id.group_hunting_new_harvest_big).also {
            it.findViewById<View>(R.id.button_content).setOnClickListener {
                onNewHarvestClicked()
            }
        }

        newObservationBtn = findViewById<NotificationButton>(R.id.group_hunting_new_observation).also {
            it.findViewById<View>(R.id.button_content).setOnClickListener {
                onNewObservationClicked()
            }
        }

        diaryOnMapBtn = findViewById<NotificationButton>(R.id.group_hunting_diary_on_map).also {
            it.findViewById<View>(R.id.button_content).setOnClickListener {
                onMapClicked()
            }
        }

        huntingDaysBtn = findViewById<NotificationButton>(R.id.group_hunting_hunting_days).also {
            it.findViewById<View>(R.id.button_content).setOnClickListener {
                onHuntingDaysClicked()
            }
        }
    }

    private fun setupGroupHuntingNotAvailableViews() {
        layoutGroupHuntingNotAvailable = findViewById(R.id.layout_group_hunting_not_available)
        textViewGroupHuntingLoadMessage = findViewById(R.id.tv_group_hunting_not_available_message)
        btnReloadData = findViewById<MaterialButton>(R.id.btn_reload_group_hunting_data).also {
            it.setOnClickListener {
                loadViewModel(refresh = true)
            }
        }
    }

    private fun updateButtonStates(huntingGroupSelected: Boolean,
                                   canCreateHarvest: Boolean,
                                   canCreateObservation: Boolean,
                                   proposedEventCount: Int,
                                   selectedSpecies: SpeciesCode?) {
        listOf(diaryOnMapBtn, huntingDaysBtn).forEach {
            it.isEnabled = huntingGroupSelected
        }
        newHarvestBtn.isEnabled = canCreateHarvest
        bigNewHarvestBtn.isEnabled = canCreateHarvest
        newObservationBtn.isEnabled = canCreateObservation

        // For deers only big version of new harvest button. For other species show both new harvest and observation buttons.
        val showOnlyBigNewHarvestBtn = selectedSpecies != null && selectedSpecies.isDeer()
        newObservationBtn.visibility = (!showOnlyBigNewHarvestBtn).toVisibility()
        newHarvestBtn.visibility = (!showOnlyBigNewHarvestBtn).toVisibility()
        bigNewHarvestBtn.visibility = showOnlyBigNewHarvestBtn.toVisibility()

        if (proposedEventCount > 0) {
            diaryOnMapBtn.notification = proposedEventCount.toString()
            diaryOnMapBtn.notificationVisibility = View.VISIBLE
        } else {
            diaryOnMapBtn.notificationVisibility = View.INVISIBLE
        }
    }

    private fun fetchHuntingGroupDataIfNeeded(refresh: Boolean) {
        MainScope().launch {
            controller.fetchHuntingGroupDataIfNeeded(refresh = refresh)
        }
    }

    private fun onNewHarvestClicked() {
        getHuntingGroupTarget()?.let { target ->
            // only the amount of proposed entries is indicated
            // -> no need to refresh group data after creating new harvest
            val intent = GroupHarvestActivity.getLaunchIntentForCreating(this, target)
            startActivity(intent)
        }
    }

    private fun onNewObservationClicked() {
        getHuntingGroupTarget()?.let { target ->
            // only the amount of proposed entries is indicated
            // -> no need to refresh group data after creating new observation
            val intent = GroupObservationActivity.getLaunchIntentForCreating(
                    packageContext = this,
                    huntingGroupTarget = target,
                    harvestTarget = null
            )
            startActivity(intent)
        }
    }

    private fun onMapClicked() {
        getHuntingGroupTarget()?.let { target ->
            // the proposed entries can be accepted on the map
            // -> refresh group data when returning
            shouldRefreshGroupDataNextTime = true
            val intent = GroupHuntingMapActivity.getLaunchIntent(this, target)
            startActivity(intent)
        }
    }

    private fun onHuntingDaysClicked() {
        getHuntingGroupTarget()?.let { target ->
            // the proposed entries can be accepted via hunting days
            // -> refresh group data when returning
            shouldRefreshGroupDataNextTime = true
            val intent = ListGroupHuntingDaysActivity.getLaunchIntent(this, target)
            startActivity(intent)
        }
    }

    private fun getHuntingGroupTarget(): HuntingGroupTarget? {
        return controller.getLoadedViewModelOrNull()?.selectedHuntingGroupTarget
    }

    companion object {
        private const val PREFIX_CONTROLLER_STATE = "GHA_controller"
        private const val KEY_SHOULD_REFRESH_NEXT_TIME = "GHA_key_should_refresh_group_data"
    }
}
