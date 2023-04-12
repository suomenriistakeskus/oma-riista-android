package fi.riista.mobile.pages

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.mobile.R
import fi.riista.mobile.RemoteConfig
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.harvest.HarvestActivity
import fi.riista.mobile.feature.observation.ObservationActivity
import fi.riista.mobile.feature.srva.SrvaActivity
import fi.riista.mobile.models.Species
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.AppSync.AppSyncListener
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.sync.SyncMode
import fi.riista.mobile.ui.HomeButtonView
import fi.riista.mobile.ui.MessageDialogFragment
import fi.riista.mobile.utils.Constants
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.toVisibility
import javax.inject.Inject

class HomeViewFragment : PageFragment(), AppSyncListener {

    @Inject
    internal lateinit var userInfoStore: UserInfoStore

    @Inject
    internal lateinit var speciesResolver: SpeciesResolver

    @Inject
    internal lateinit var appSync: AppSync

    @Inject
    internal lateinit var syncConfig: SyncConfig

    private val disposeBagStarted = DisposeBag()
    private val disposeBagResumed = DisposeBag()

    private var refreshItem: MenuItem? = null
    private var harvestQuickButton1: TextView? = null
    private var harvestQuickButton2: TextView? = null
    private var observationQuickButton1: TextView? = null
    private var observationQuickButton2: TextView? = null

    private val synchronizeAfterEventCreated = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            appSync.syncImmediatelyIfAutomaticSyncEnabled()
        }
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setupActionBar(R.layout.actionbar_home, true)

        with (view.findViewById<HomeButtonView>(R.id.home_harvest_view)) {
            findViewById<View>(R.id.home_view_main_item)
                .setOnClickListener { startHarvestEditActivity(null) }
            harvestQuickButton1 = findViewById(R.id.home_view_subitem_1_button)
            harvestQuickButton2 = findViewById(R.id.home_view_subitem_2_button)
        }

        with (view.findViewById<HomeButtonView>(R.id.home_observation_view)) {
            findViewById<View>(R.id.home_view_main_item)
                .setOnClickListener { startObservationEditActivity(null) }
            observationQuickButton1 = findViewById(R.id.home_view_subitem_1_button)
            observationQuickButton2 = findViewById(R.id.home_view_subitem_2_button)
        }

        with (view.findViewById<HomeButtonView>(R.id.home_srva_view)) {
            findViewById<View>(R.id.home_view_main_item).setOnClickListener { startSrvaEditActivity() }
            visibility = UiUtils.isSrvaVisible(userInfoStore.getUserInfo()).toVisibility()
        }

        with (view.findViewById<HomeButtonView>(R.id.home_map_view)) {
            findViewById<View>(R.id.home_view_main_item).setOnClickListener { onMapClick() }
        }

        with (view.findViewById<HomeButtonView>(R.id.home_my_details_view)) {
            findViewById<View>(R.id.home_view_main_item).setOnClickListener { onMyDetailsClick() }
            findViewById<View>(R.id.home_view_subitem_1_button).setOnClickListener { onHuntingLicenseClick() }
            findViewById<View>(R.id.home_view_subitem_2_button).setOnClickListener { onShootingTestsClick() }
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_refresh, menu)

        // Show/hide refresh button according to sync settings.
        refreshItem = menu.findItem(R.id.item_refresh)
        updateManualSyncButtonIndicator(manualSyncPossible = appSync.manualSynchronizationPossible.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_refresh) {
            appSync.synchronizeUsing(syncMode = SyncMode.SYNC_MANUAL)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        setViewTitle(getString(R.string.title_front_page))
        updateQuickButtons()
        appSync.addSyncListener(listener = this, notifyImmediately = true)

        appSync.manualSynchronizationPossible.bindAndNotify { manualSyncPossible ->
            refreshItem?.let { item ->
                item.isEnabled = manualSyncPossible
                item.icon.alpha = when (manualSyncPossible) {
                    true -> 255
                    false -> Constants.DISABLED_ALPHA
                }
            }
        }.disposeBy(disposeBagStarted)
    }

    override fun onStop() {
        super.onStop()
        appSync.removeSyncListener(this)
        disposeBagStarted.disposeAll()
    }

    override fun onResume() {
        super.onResume()
        updateManualSyncButtonIndicator(manualSyncPossible = appSync.manualSynchronizationPossible.value)

        RemoteConfig.remoteConfigFetched.bindAndNotify { remoteConfigFetched ->
            if (remoteConfigFetched) {
                // startup message delivered through remote config, only try to display if/once remote config fetched
                displayAppStartupMessageIfNeeded()
            }
        }.disposeBy(disposeBagResumed)
    }

    override fun onPause() {
        super.onPause()
        disposeBagResumed.disposeAll()
    }

    private fun displayAppStartupMessageIfNeeded() {
        if (!isResumed) {
            return
        }

        RiistaSDK.appStartupMessageHandler.getAppStartupMessageToBeDisplayed()
            ?.let { message ->
                if (message.preventFurtherAppUsage) {
                    // further app usage prevention most likely won't be able to kill the app (i.e. RiistaApplication
                    // still exists after dialog fragment is dismissed and app/activities are finished).
                    // --> Reset the 'displayed' flag so this dialog gets displayed again next
                    //     time when the app is opened
                    RiistaSDK.appStartupMessageHandler.resetStartupMessageDisplayAttempted()
                }

                MessageDialogFragment.create(message = message)
                    .show(parentFragmentManager, "startup message")
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

    private fun updateQuickButtons() {
        val view = view
        if (view != null) {
            updateHarvestQuickButtons()
            updateObservationQuickButtons()
        }
    }

    private fun updateHarvestQuickButtons() {
        setupHarvestQuickButton(harvestQuickButton1, speciesResolver.findSpecies(QUICK_BUTTON1_DEFAULT))
        setupHarvestQuickButton(harvestQuickButton2, speciesResolver.findSpecies(QUICK_BUTTON2_DEFAULT))

        val latestSpecies = RiistaSDK.harvestContext.getLatestHarvestSpecies(2)
        if (isAdded) {
            if (latestSpecies.isNotEmpty()) {
                setupHarvestQuickButton(
                    harvestQuickButton1,
                    speciesResolver.findSpecies(latestSpecies[0].knownSpeciesCodeOrNull())
                )
            }
            if (latestSpecies.size >= 2) {
                setupHarvestQuickButton(
                    harvestQuickButton2,
                    speciesResolver.findSpecies(latestSpecies[1].knownSpeciesCodeOrNull())
                )
            }
        }
    }

    private fun setupHarvestQuickButton(buttonView: TextView?, species: Species) {
        if (isAdded) {
            buttonView?.text = species.mName
            buttonView?.setOnClickListener { startHarvestEditActivity(species) }
        }
    }

    private fun updateObservationQuickButtons() {
        setupObservationQuickButton(observationQuickButton1, speciesResolver.findSpecies(QUICK_BUTTON1_DEFAULT))
        setupObservationQuickButton(observationQuickButton2, speciesResolver.findSpecies(QUICK_BUTTON2_DEFAULT))

        val latestSpecies = RiistaSDK.observationContext.getLatestObservationSpecies(2)
        if (isAdded) {
            if (latestSpecies.isNotEmpty()) {
                setupObservationQuickButton(
                    observationQuickButton1,
                    speciesResolver.findSpecies(latestSpecies[0].knownSpeciesCodeOrNull())
                )
            }
            if (latestSpecies.size >= 2) {
                setupObservationQuickButton(
                    observationQuickButton2,
                    speciesResolver.findSpecies(latestSpecies[1].knownSpeciesCodeOrNull())
                )
            }
        }
    }

    private fun setupObservationQuickButton(buttonView: TextView?, species: Species) {
        if (isAdded) {
            buttonView?.text = species.mName
            buttonView?.setOnClickListener { startObservationEditActivity(species) }
        }
    }

    private fun startHarvestEditActivity(species: Species?) {
        val speciesCode: Int? = species?.mId
        val intent = HarvestActivity.getLaunchIntentForCreating(requireActivity(), speciesCode)

        synchronizeAfterEventCreated.launch(intent)
    }

    private fun startObservationEditActivity(species: Species?) {
        val speciesCode: Int? = species?.mId
        val intent = ObservationActivity.getLaunchIntentForCreating(requireActivity(), speciesCode)

        synchronizeAfterEventCreated.launch(intent)
    }

    private fun startSrvaEditActivity() {
        val intent = SrvaActivity.getLaunchIntentForCreating(requireActivity())

        // srva gets synchronized internally, no need to synchronize here
        startActivity(intent)
    }

    private fun onMyDetailsClick() {
        val activity = activity as MainActivity?
        if (activity != null) {
            activity.selectItem(R.id.menu_more)
            activity.replacePageFragment(MyDetailsFragment.newInstance())
        }
    }

    private fun onHuntingLicenseClick() {
        MyDetailsLicenseFragment.newInstance()
            .show(requireActivity().supportFragmentManager, MyDetailsLicenseFragment.TAG)
    }

    private fun onShootingTestsClick() {
        MyDetailsShootingTestsFragment.newInstance()
            .show(requireActivity().supportFragmentManager, MyDetailsShootingTestsFragment.TAG)
    }

    private fun onMapClick() {
        (activity as? MainActivity)?.let {
            it.selectItem(R.id.menu_map)
            it.replacePageFragment(MapViewer.newInstance(true))
        }
    }

    override fun onSyncStarted() {
        // nop
    }

    override fun onSyncCompleted() {
        updateQuickButtons()
    }

    companion object {
        // Button defaults if there aren't any previous entries
        private const val QUICK_BUTTON1_DEFAULT = SpeciesInformation.MOOSE_ID
        private const val QUICK_BUTTON2_DEFAULT = SpeciesInformation.MOUNTAIN_HARE_ID // Metsäjänis
    }
}
